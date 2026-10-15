package ic2ce.neoforge.personal;

import com.mojang.authlib.GameProfile;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.menu.document.DocumentMenus;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.menu.document.ScreenDocument;
import ic2ce.neoforge.runtime.SideServices;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * UUID ownership and revocable inventory views; anonymous automation never gains a privileged
 * chest.
 */
public class PrivateChest extends InventoryBlockEntity implements OwnedDevice, IHasGui {
  private GameProfile owner;
  private static final List<AABB> SHAPE = List.of(new AABB(.0625, 0, .0625, .9375, 1, .9375));
  public final InventorySlot contentSlot =
      new InventorySlot(this, "content", InventorySlot.Access.NONE, 54);
  private final Set<UUID> viewers = new HashSet<>();
  private int usingPlayerCount;
  private byte lidAngle, prevLidAngle;

  public PrivateChest(BlockPos position, BlockState state) {
    super(PersonalBlockEntities.PERSONAL_CHEST, position, state);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    owner =
        data.contains("ownerGameProfile")
            ? ProfileUtil.read(data.getCompoundOrEmpty("ownerGameProfile"))
            : null;
    viewers.clear();
    usingPlayerCount = 0;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.remove("ownerGameProfile");
    if (owner != null) {
      var profile = new CompoundTag();
      ProfileUtil.write(profile, owner);
      data.put("ownerGameProfile", profile);
    }
  }

  @Override
  protected List<AABB> getAabbs(boolean collision) {
    return SHAPE;
  }

  @Override
  protected void updateEntityClient() {
    super.updateEntityClient();
    prevLidAngle = lidAngle;
    var world = getLevel();
    if (world == null) return;
    if (usingPlayerCount > 0 && lidAngle == 0)
      world.playSound(
          null,
          worldPosition,
          SoundEvents.CHEST_OPEN,
          SoundSource.BLOCKS,
          .5f,
          .9f + world.getRandom().nextFloat() * .1f);
    lidAngle = (byte) Math.clamp(lidAngle + (usingPlayerCount > 0 ? 1 : -1), 0, 10);
    if (lidAngle < 5 && prevLidAngle >= 5)
      world.playSound(
          null,
          worldPosition,
          SoundEvents.CHEST_CLOSE,
          SoundSource.BLOCKS,
          .5f,
          .9f + world.getRandom().nextFloat() * .1f);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    var world = getLevel();
    if (world != null
        && viewers.removeIf(
            id -> {
              var player = world.getPlayerByUUID(id);
              return player == null || !player.isAlive() || !stillValid(player);
            })) publishViewers();
  }

  @Override
  public void startOpen(ContainerUser user) {
    var world = getLevel();
    if (world != null
        && !world.isClientSide()
        && user instanceof Player player
        && permitsAccess(player.getGameProfile())
        && viewers.add(player.getUUID())) publishViewers();
  }

  @Override
  public void stopOpen(ContainerUser user) {
    var world = getLevel();
    if (world != null
        && !world.isClientSide()
        && user instanceof Player player
        && viewers.remove(player.getUUID())) publishViewers();
  }

  private void publishViewers() {
    usingPlayerCount = viewers.size();
    SideServices.network(true).updateTileEntityField(this, "usingPlayerCount");
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = super.getNetworkedFields();
    fields.add("owner");
    fields.add("usingPlayerCount");
    return fields;
  }

  @Override
  public boolean wrenchCanRemove(Player player) {
    if (!permitsAccess(player.getGameProfile())) {
      SideServices.proxy().messagePlayer(
          player, "ic2.personal_chest.owner", owner == null ? "" : owner.name());
      return false;
    }
    if (!contentSlot.isEmpty()) {
      SideServices.proxy().messagePlayer(player, "ic2.personal_chest.non_empty");
      return false;
    }
    return true;
  }

  @Override
  public boolean permitsAccess(GameProfile profile) {
    return checkAccess(this, profile);
  }

  public static <T extends BlockEntity & OwnedDevice> boolean checkAccess(
      T tile, GameProfile accessor) {
    var existing = tile.getOwner();
    if (accessor == null) return existing == null;
    var world = tile.getLevel();
    if (existing == null) {
      if (world != null && !world.isClientSide()) {
        tile.setOwner(accessor);
        tile.setChanged();
        SideServices.network(true).updateTileEntityField(tile, "owner");
      }
      return true;
    }
    if (world != null
        && !world.isClientSide()
        && world.getServer() != null
        && world
            .getServer()
            .getPlayerList()
            .isOp(new net.minecraft.server.players.NameAndId(accessor.id(), accessor.name())))
      return true;
    return existing.id() != null
        ? existing.id().equals(accessor.id())
        : Objects.equals(existing.name(), accessor.name());
  }

  @Override
  public Container getPrivilegedInventory(GameProfile accessor) {
    return new Container() {
      private boolean allowed() {
        return accessor != null && !isRemoved() && permitsAccess(accessor);
      }

      private boolean index(int slot) {
        return slot >= 0 && slot < contentSlot.size();
      }

      @Override
      public int getContainerSize() {
        return allowed() ? contentSlot.size() : 0;
      }

      @Override
      public boolean isEmpty() {
        return !allowed() || contentSlot.isEmpty();
      }

      @Override
      public ItemStack getItem(int slot) {
        return allowed() && index(slot) ? contentSlot.get(slot).copy() : ItemStack.EMPTY;
      }

      @Override
      public ItemStack removeItem(int slot, int amount) {
        if (!allowed() || !index(slot) || amount <= 0) return ItemStack.EMPTY;
        var current = contentSlot.get(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        int removed = Math.min(amount, current.getCount());
        var result = current.copyWithCount(removed);
        contentSlot.put(slot, current.copyWithCount(current.getCount() - removed));
        setChanged();
        return result;
      }

      @Override
      public ItemStack removeItemNoUpdate(int slot) {
        return removeItem(slot, Integer.MAX_VALUE);
      }

      @Override
      public void setItem(int slot, ItemStack stack) {
        setItem(slot, stack, false);
      }

      @Override
      public void setItem(int slot, ItemStack stack, boolean insideTransaction) {
        if (!allowed() || !index(slot)) return;
        var owned =
            stack == null || stack.isEmpty()
                ? ItemStack.EMPTY
                : stack.copyWithCount(
                    Math.min(
                        stack.getCount(), Math.min(stack.getMaxStackSize(), getMaxStackSize())));
        if (insideTransaction) contentSlot.putInTransaction(slot, owned);
        else {
          contentSlot.put(slot, owned);
          setChanged();
        }
      }

      @Override
      public int getMaxStackSize() {
        return contentSlot.getStackSizeLimit();
      }

      @Override
      public void setChanged() {
        if (allowed()) PrivateChest.this.setChanged();
      }

      @Override
      public boolean stillValid(Player player) {
        return allowed()
            && permitsAccess(player.getGameProfile())
            && PrivateChest.this.stillValid(player);
      }

      @Override
      public boolean canPlaceItem(int slot, ItemStack stack) {
        return allowed() && index(slot) && stack != null && contentSlot.accepts(stack);
      }

      @Override
      public void clearContent() {
        if (allowed()) {
          contentSlot.clear();
          setChanged();
        }
      }

      @Override
      public void startOpen(ContainerUser user) {
        if (allowed()) PrivateChest.this.startOpen(user);
      }

      @Override
      public void stopOpen(ContainerUser user) {
        PrivateChest.this.stopOpen(user);
      }
    };
  }

  @Override
  public GameProfile getOwner() {
    return owner;
  }

  @Override
  public void setOwner(GameProfile next) {
    if (!Objects.equals(owner, next)) {
      owner = next;
      viewers.clear();
      usingPlayerCount = 0;
      setChanged();
    }
  }

  @Override
  protected boolean canEntityDestroy(Entity entity) {
    return false;
  }

  @Override
  protected InteractionResult onActivated(
      Player player, InteractionHand hand, Direction side, Vec3 hit) {
    if (!permitsAccess(player.getGameProfile())) {
      SideServices.proxy().messagePlayer(
          player, "ic2.personal_chest.owner", owner == null ? "" : owner.name());
      return InteractionResult.FAIL;
    }
    return super.onActivated(player, hand, side, hit);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    if (!permitsAccess(player.getGameProfile()))
      throw new IllegalStateException("Personal chest access denied");
    startOpen(player);
    return new DocumentMenu<InventoryBlockEntity>(
        DocumentMenus.DYNAMIC_BE,
        id,
        player.getInventory(),
        this,
        ScreenDocument.parse(BuiltInRegistries.BLOCK.getKey(getBlockType()), getClass())) {
      @Override
      public void removed(Player user) {
        base.stopOpen(user);
        super.removed(user);
      }
    };
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(id, inventory, this);
  }

  @Override
  public void onScreenClosed(Player player) {
    stopOpen(player);
  }

  public float getLidAngle(float partialTicks) {
    return (prevLidAngle + (lidAngle - prevLidAngle) * Math.clamp(partialTicks, 0, 1)) / 10f;
  }
}
