package ic2ce.neoforge.personal;

import com.mojang.authlib.GameProfile;
import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.network.INetworkTileEntityEventListener;
import ic2ce.core.IHasGui;
import ic2ce.core.event.WorldData;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.core.util.ProfileUtil;
import ic2ce.core.util.StackTag;
import ic2ce.core.util.StackUtil;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.LinkedInput;
import ic2ce.neoforge.inventory.OutputInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.personal.menu.OwnerItemTradeMenu;
import ic2ce.neoforge.personal.menu.PublicItemTradeMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.io.IOException;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Stock-backed item invoices hold owned payment and supply until every stage completes. */
public class ItemTradeTerminal extends InventoryBlockEntity
    implements OwnedDevice,
        IHasGui,
        INetworkTileEntityEventListener,
        INetworkClientTileEntityEventListener {
  private GameProfile owner;
  private int ticker = ProjectRuntime.random.nextInt(64);
  private boolean marketAttached;
  public int totalTradeCount, stock;
  public boolean infinite;
  public final InventorySlot demandSlot =
      new InventorySlot(this, "demand", InventorySlot.Access.NONE, 1);
  public final InventorySlot offerSlot =
      new InventorySlot(this, "offer", InventorySlot.Access.NONE, 1);
  public final LinkedInput inputSlot = new LinkedInput(this, "input", 1, demandSlot);
  public final OutputInventory outputSlot = new OutputInventory(this, "output", 1);
  private ItemStack payment = ItemStack.EMPTY,
      promised = ItemStack.EMPTY,
      reserved = ItemStack.EMPTY;
  private boolean validInvoice = true;

  public ItemTradeTerminal(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.TRADE_O_MAT, position, state);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    owner =
        data.contains("ownerGameProfile")
            ? ProfileUtil.read(data.getCompoundOrEmpty("ownerGameProfile"))
            : null;
    totalTradeCount = Math.max(0, data.getIntOr("totalTradeCount", 0));
    infinite = data.getBooleanOr("infinite", false);
    payment = read(data, "pendingPayment");
    promised = read(data, "pendingOffer");
    reserved = read(data, "reservedOffer");
    validInvoice =
        (reserved.isEmpty()
                || !promised.isEmpty()
                    && ItemStack.isSameItemSameComponents(reserved, promised)
                    && reserved.getCount() <= promised.getCount())
            && (!promised.isEmpty() || payment.isEmpty())
            && (promised.isEmpty() || !payment.isEmpty() || !reserved.isEmpty());
  }

  private static ItemStack read(CompoundTag data, String field) {
    return data.contains(field)
        ? StackTag.parseHolder(data.getCompoundOrEmpty(field))
        : ItemStack.EMPTY;
  }

  private void write(CompoundTag data, String field, ItemStack stack) {
    data.remove(field);
    if (!stack.isEmpty()) {
      var saved = new CompoundTag();
      StackTag.save(getLevel().registryAccess(), stack, saved);
      data.put(field, saved);
    }
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
    data.putInt("totalTradeCount", totalTradeCount);
    data.putBoolean("infinite", infinite);
    write(data, "pendingPayment", payment);
    write(data, "pendingOffer", promised);
    write(data, "reservedOffer", reserved);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    trade();
    if (infinite) stock = -1;
    else if (++ticker >= 64) {
      ticker = 0;
      updateStock();
    }
  }

  private void trade() {
    if (!validInvoice) return;
    if (promised.isEmpty()) {
      var paid = inputSlot.consumeLinked(true);
      var offer = offerSlot.get();
      if (paid == null || paid.isEmpty() || offer.isEmpty() || !outputSlot.canAdd(offer)) return;
      if (infinite) {
        var consumed = inputSlot.consumeLinked(false);
        if (consumed == null || consumed.isEmpty()) return;
        outputSlot.add(offer.copy());
        completed();
        return;
      }
      if (StackUtil.fetch(this, offer.copy(), true) != offer.getCount()
          || StackUtil.distribute(this, paid.copy(), true) != paid.getCount()) return;
      var consumed = inputSlot.consumeLinked(false);
      if (consumed == null || consumed.isEmpty()) return;
      payment = consumed.copy();
      promised = offer.copy();
      reserved = ItemStack.EMPTY;
      setChanged();
    }
    int missing = promised.getCount() - reserved.getCount();
    if (missing > 0) {
      int fetched = StackUtil.fetch(this, promised.copyWithCount(missing), false);
      if (fetched < 0 || fetched > missing)
        throw new IllegalStateException("Trade supply exceeded its reserved quantity");
      if (fetched > 0) {
        reserved = promised.copyWithCount(reserved.getCount() + fetched);
        setChanged();
      }
      if (fetched < missing) return;
    }
    if (!outputSlot.canAdd(reserved)) return;
    if (!payment.isEmpty()) {
      int delivered = StackUtil.distribute(this, payment.copy(), false);
      if (delivered < 0 || delivered > payment.getCount())
        throw new IllegalStateException("Trade income exceeded reserved payment quantity");
      if (delivered > 0) {
        payment = payment.copyWithCount(payment.getCount() - delivered);
        setChanged();
      }
      if (!payment.isEmpty()) return;
    }
    if (outputSlot.add(reserved) != 0)
      throw new IllegalStateException("Trade output changed after capacity reservation");
    promised = reserved = ItemStack.EMPTY;
    stock = Math.max(0, stock - 1);
    completed();
  }

  private void completed() {
    if (totalTradeCount < Integer.MAX_VALUE) totalTradeCount++;
    setChanged();
    ProjectRuntime.network.get(true).initiateTileEntityEvent(this, 0, true);
  }

  public void updateStock() {
    var offer = offerSlot.get();
    stock =
        infinite
            ? -1
            : offer.isEmpty()
                ? 0
                : StackUtil.fetch(this, offer.copyWithCount(Integer.MAX_VALUE), true)
                    / offer.getCount();
  }

  public final boolean isWireless() {
    return getActive();
  }

  private void reconcileMarket() {
    var world = getLevel();
    if (world == null || world.isClientSide()) return;
    boolean wanted = isWireless() && !isRemoved();
    if (wanted == marketAttached) return;
    var market = WorldData.get(world).tradeMarket;
    if (wanted) market.registerTradeOMat(this);
    else market.unregisterTradeOMat(this);
    marketAttached = wanted;
  }

  public final boolean setWireless(boolean wireless) {
    var world = getLevel();
    if (world == null || world.isClientSide() || isWireless() == wireless) return false;
    setActive(wireless);
    reconcileMarket();
    setChanged();
    return true;
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (getLevel() != null && !getLevel().isClientSide()) {
      updateStock();
      reconcileMarket();
    }
  }

  @Override
  protected void onUnloaded() {
    if (marketAttached && getLevel() != null) {
      WorldData.get(getLevel()).tradeMarket.unregisterTradeOMat(this);
      marketAttached = false;
    }
    super.onUnloaded();
  }

  @Override
  protected InteractionResult onActivated(
      Player player, InteractionHand hand, Direction side, Vec3 hit) {
    var world = getLevel();
    var held = player.getItemInHand(hand);
    if (world != null
        && !isWireless()
        && held.is(ItemDefinitions.REMOTE_INTERFACE_UPGRADE)
        && permitsAccess(player.getGameProfile())) {
      if (!world.isClientSide() && setWireless(true) && !player.getAbilities().instabuild)
        held.shrink(1);
      return InteractionResult.SUCCESS;
    }
    return super.onActivated(player, hand, side, hit);
  }

  @Override
  protected List<ItemStack> getAuxDrops(int fortune) {
    var drops = super.getAuxDrops(fortune);
    if (isWireless()) drops.add(new ItemStack(ItemDefinitions.REMOTE_INTERFACE_UPGRADE));
    if (!payment.isEmpty()) drops.add(payment.copy());
    if (!reserved.isEmpty()) drops.add(reserved.copy());
    return drops;
  }

  @Override
  public boolean permitsAccess(GameProfile profile) {
    return PrivateChest.checkAccess(this, profile);
  }

  @Override
  public Container getPrivilegedInventory(GameProfile profile) {
    return this;
  }

  @Override
  public GameProfile getOwner() {
    return owner;
  }

  @Override
  public void setOwner(GameProfile profile) {
    owner = profile;
    setChanged();
  }

  @Override
  public boolean wrenchCanRemove(Player player) {
    return permitsAccess(player.getGameProfile());
  }

  @Override
  protected boolean canEntityDestroy(Entity entity) {
    return false;
  }

  private boolean canToggleInfinite(Player player) {
    if (player == null || player.level().isClientSide() || player.level().getServer() == null)
      return false;
    var profile = player.getGameProfile();
    return player
        .level()
        .getServer()
        .getPlayerList()
        .isOp(new net.minecraft.server.players.NameAndId(profile.id(), profile.name()));
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = super.getNetworkedFields();
    fields.add("owner");
    return fields;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return permitsAccess(player.getGameProfile())
        ? new OwnerItemTradeMenu(id, player.getInventory(), this, canToggleInfinite(player))
        : new PublicItemTradeMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return data.readBoolean()
        ? new OwnerItemTradeMenu(id, inventory, this, data.readBoolean())
        : new PublicItemTradeMenu(id, inventory, this);
  }

  @Override
  public void writeScreenOpenData(Player player, InteractionHand hand, GrowingBuffer data)
      throws IOException {
    boolean allowed = permitsAccess(player.getGameProfile());
    data.writeBoolean(allowed);
    if (allowed) data.writeBoolean(canToggleInfinite(player));
  }

  @Override
  public void onScreenClosed(Player player) {}

  @Override
  public void onNetworkEvent(int event) {
    var world = getLevel();
    if (event == 0 && world != null && world.isClientSide())
      world.playLocalSound(
          worldPosition.getX(),
          worldPosition.getY(),
          worldPosition.getZ(),
          SoundDefinitions.MACHINE_OMAT_OPERATE,
          SoundSource.BLOCKS,
          1,
          1,
          true);
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event == 0 && canToggleInfinite(player)) {
      infinite = !infinite;
      updateStock();
      setChanged();
    }
  }
}
