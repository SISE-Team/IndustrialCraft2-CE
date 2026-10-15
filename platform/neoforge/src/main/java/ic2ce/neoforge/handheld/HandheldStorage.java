package ic2ce.neoforge.handheld;

import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.menu.slot.TemplateSlot;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.nbt.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Owned native handheld slots persist into the actual identified physical stack without changing
 * its count.
 */
public abstract class HandheldStorage implements IHasGui {
  protected ItemStack containerStack;
  protected final ItemStack[] inventory;
  public final Player player;
  protected final InteractionHand hand;
  private boolean cleared;
  private static final AtomicInteger IDENTIFIERS = new AtomicInteger((int) System.nanoTime());
  private static final Set<Player> MAINTAINED = Collections.newSetFromMap(new WeakHashMap<>());

  public HandheldStorage(Player player, InteractionHand hand, ItemStack stack, int slots) {
    if (player == null
        || hand == null
        || stack == null
        || stack.isEmpty()
        || slots < 0
        || slots > 256) throw new IllegalArgumentException("Invalid handheld inventory");
    this.player = player;
    this.hand = hand;
    containerStack = stack;
    inventory = new ItemStack[slots];
    Arrays.fill(inventory, ItemStack.EMPTY);
    if (!player.level().isClientSide()) {
      var data = StackTag.get(stack);
      int id = data == null ? 0 : data.getIntOr("uid", 0);
      if (id == 0) {
        int allocated = IDENTIFIERS.updateAndGet(value -> value == -1 ? 1 : value + 1);
        StackTag.update(
            stack,
            tag -> {
              tag.putInt("uid", allocated);
              return tag;
            });
        data = StackTag.get(stack);
      }
      var entries = data.getListOrEmpty("Items");
      for (int index = 0; index < entries.size(); index++) {
        var encoded = entries.getCompound(index).orElseGet(CompoundTag::new);
        int slot = Byte.toUnsignedInt(encoded.getByteOr("Slot", (byte) -1));
        if (slot < slots)
          inventory[slot] = StackTag.parse(player.level().registryAccess(), encoded);
      }
    }
  }

  public int getContainerSize() {
    return inventory.length;
  }

  public boolean isEmpty() {
    for (var stack : inventory) if (stack != null && !stack.isEmpty()) return false;
    return true;
  }

  public ItemStack getItem(int slot) {
    if (slot < 0 || slot >= inventory.length) return ItemStack.EMPTY;
    var stack = inventory[slot];
    return stack == null ? ItemStack.EMPTY : stack;
  }

  public ItemStack removeItem(int slot, int amount) {
    if (amount <= 0) return ItemStack.EMPTY;
    var stack = getItem(slot);
    if (stack.isEmpty()) return ItemStack.EMPTY;
    var output = stack.split(amount);
    if (stack.isEmpty()) inventory[slot] = ItemStack.EMPTY;
    save();
    return output;
  }

  public void setItem(int slot, ItemStack stack) {
    if (slot < 0 || slot >= inventory.length) return;
    inventory[slot] =
        stack == null || stack.isEmpty()
            ? ItemStack.EMPTY
            : stack.copyWithCount(
                Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), getMaxStackSize())));
    save();
  }

  public int getMaxStackSize() {
    return 64;
  }

  public boolean canPlaceItem(int slot, ItemStack stack) {
    return false;
  }

  public void setChanged() {
    save();
  }

  public boolean stillValid(Player actor) {
    return !cleared && actor == player && getPlayerInventoryIndex() >= -1;
  }

  public ItemStack removeItemNoUpdate(int slot) {
    var stack = getItem(slot);
    if (slot >= 0 && slot < inventory.length) inventory[slot] = ItemStack.EMPTY;
    return stack;
  }

  @Override
  public void onScreenClosed(Player actor) {
    save();
    if (player.level().isClientSide()) return;
    if (!MAINTAINED.remove(actor)) {
      var actual = physical();
      if (actual != null)
        StackTag.update(
            actual,
            data -> {
              data.remove("uid");
              return data;
            });
    }
  }

  public ItemStack getContainerStack() {
    return containerStack;
  }

  public boolean isThisContainer(ItemStack stack) {
    if (stack == null || stack.isEmpty() || !stack.is(containerStack.getItem())) return false;
    if (stack == containerStack) return true;
    var data = StackTag.get(stack);
    int uid = getUid();
    return uid != 0 && data != null && data.getIntOr("uid", 0) == uid;
  }

  protected int getUid() {
    var data = StackTag.get(containerStack);
    return data == null ? 0 : data.getIntOr("uid", 0);
  }

  protected int getPlayerInventoryIndex() {
    if (isThisContainer(player.containerMenu.getCarried())) return -1;
    for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
      if (isThisContainer(player.getInventory().getItem(slot))) return slot;
    return Integer.MIN_VALUE;
  }

  private ItemStack physical() {
    int index = getPlayerInventoryIndex();
    return index == -1
        ? player.containerMenu.getCarried()
        : index >= 0 ? player.getInventory().getItem(index) : null;
  }

  protected void save() {
    if (cleared || player.level().isClientSide()) return;
    var actual = physical();
    if (actual == null) return;
    var entries = encode();
    StackTag.update(
        actual,
        data -> {
          if (entries.isEmpty()) data.remove("Items");
          else data.put("Items", entries);
          return data;
        });
    containerStack = actual;
    player.getInventory().setChanged();
  }

  private ListTag encode() {
    var result = new ListTag();
    for (int slot = 0; slot < inventory.length; slot++) {
      var stack = getItem(slot);
      if (stack.isEmpty()) continue;
      if (isThisContainer(stack)) {
        inventory[slot] = ItemStack.EMPTY;
        continue;
      }
      var data = new CompoundTag();
      StackTag.save(player.level().registryAccess(), stack, data);
      data.putByte("Slot", (byte) slot);
      result.add(data);
    }
    return result;
  }

  public void saveAsThrown(ItemStack stack) {
    if (cleared || player.level().isClientSide() || stack == null || stack.isEmpty()) return;
    var entries = encode();
    StackTag.update(
        stack,
        data -> {
          if (entries.isEmpty()) data.remove("Items");
          else data.put("Items", entries);
          data.remove("uid");
          return data;
        });
    clearContent();
  }

  public void clearContent() {
    Arrays.fill(inventory, ItemStack.EMPTY);
    cleared = true;
  }

  public TemplateSlot.ChangeCallback makeSaveCallback() {
    return ignored -> save();
  }

  public void onEvent(String event) {}

  public static void addMaintainedPlayer(Player actor) {
    if (actor != null) MAINTAINED.add(actor);
  }
}
