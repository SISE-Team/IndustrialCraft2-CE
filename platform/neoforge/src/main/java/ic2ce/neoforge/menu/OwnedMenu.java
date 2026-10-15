package ic2ce.neoforge.menu;

import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.menu.slot.DisplaySlot;
import ic2ce.neoforge.menu.slot.MachineSlot;
import ic2ce.neoforge.menu.slot.TemplateSlot;
import ic2ce.neoforge.runtime.SideServices;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Native menu ownership, component-aware physical shift transfers and explicit listener lifecycle.
 */
public abstract class OwnedMenu<T extends Container> extends AbstractContainerMenu {
  protected static final int windowBorder = 8,
      slotSize = 16,
      slotDistance = 2,
      slotSeparator = 4,
      hotbarYOffset = -24,
      inventoryYOffset = -82;
  protected final Player player;
  public final T base;
  private final List<ContainerListener> listeners = new ArrayList<>();

  protected OwnedMenu(MenuType<?> type, int id, Inventory inventory, T base) {
    super(type, id);
    player = Objects.requireNonNull(inventory).player;
    this.base = Objects.requireNonNull(base);
  }

  protected void addPlayerInventorySlots(Inventory inventory, int height) {
    addPlayerInventorySlots(inventory, 178, height);
  }

  protected void addPlayerInventorySlots(Inventory inventory, int width, int height) {
    int left = (width - 162) / 2;
    for (int row = 0; row < 3; row++)
      for (int col = 0; col < 9; col++)
        addSlot(new Slot(inventory, 9 + row * 9 + col, left + col * 18, height - 82 + row * 18));
    for (int col = 0; col < 9; col++)
      addSlot(new Slot(inventory, col, left + col * 18, height - 24));
  }

  @Override
  public void clicked(int index, int button, ContainerInput action, Player actor) {
    if (actor != player
        || action == null
        || index < 0 && index != AbstractContainerMenu.SLOT_CLICKED_OUTSIDE
        || index >= slots.size()) return;
    if (index >= 0 && slots.get(index) instanceof TemplateSlot ghost) {
      ghost.slotClick(button, action, actor, this);
      return;
    }
    super.clicked(index, button, action, actor);
  }

  @Override
  public final ItemStack quickMoveStack(Player actor, int index) {
    if (actor != player || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
    var source = slots.get(index);
    if (!source.hasItem()
        || !source.mayPickup(actor)
        || source instanceof TemplateSlot
        || source instanceof DisplaySlot) return ItemStack.EMPTY;
    var physical = source.getItem();
    var before = physical.copy();
    var remainder =
        source.container == actor.getInventory()
            ? handlePlayerSlotShiftClick(actor, physical)
            : handleGUISlotShiftClick(actor, physical);
    if (remainder == null) throw new IllegalStateException("Menu transfer returned null");
    int remaining = remainder.isEmpty() ? 0 : remainder.getCount();
    if (remaining == before.getCount()) return ItemStack.EMPTY;
    if (remaining < 0 || remaining > before.getCount())
      throw new IllegalStateException("Menu transfer violated physical quantity");
    if (!(source instanceof ResultSlot)) {
      source.setByPlayer(remainder.isEmpty() ? ItemStack.EMPTY : remainder, before);
      source.setChanged();
      var taken = before.copyWithCount(before.getCount() - remaining);
      source.onTake(actor, taken);
    }
    if (!actor.level().isClientSide()) broadcastChanges();
    return before;
  }

  protected ItemStack handlePlayerSlotShiftClick(Player actor, ItemStack remaining) {
    for (int pass = 0; pass < 4 && !remaining.isEmpty(); pass++)
      for (var target : slots)
        if (target.container != actor.getInventory()
            && isValidTargetSlot(target, remaining, (pass & 1) == 1, pass < 2)) {
          remaining = transfer(remaining, target);
          if (remaining.isEmpty()) break;
        }
    return remaining;
  }

  protected ItemStack handleGUISlotShiftClick(Player actor, ItemStack remaining) {
    for (int pass = 0; pass < 2 && !remaining.isEmpty(); pass++)
      for (int index = slots.size() - 1; index >= 0; index--) {
        var target = slots.get(index);
        if (target.container == actor.getInventory()
            && isValidTargetSlot(target, remaining, pass == 1, false)) {
          remaining = transfer(remaining, target);
          if (remaining.isEmpty()) break;
        }
      }
    return remaining;
  }

  protected static boolean isValidTargetSlot(
      Slot target, ItemStack stack, boolean allowEmpty, boolean inputOnly) {
    return !(target instanceof DisplaySlot)
        && !(target instanceof TemplateSlot)
        && target.mayPlace(stack)
        && (allowEmpty || target.hasItem())
        && (!inputOnly
            || target instanceof MachineSlot nativeSlot && nativeSlot.invSlot.canInput());
  }

  protected final ItemStack transfer(ItemStack offered, Slot target) {
    if (offered == null
        || offered.isEmpty()
        || !target.mayPlace(offered)
        || target instanceof TemplateSlot
        || target instanceof DisplaySlot) return offered;
    var present = target.getItem();
    if (!present.isEmpty() && !ItemStack.isSameItemSameComponents(offered, present)) return offered;
    int capacity =
        Math.min(target.container.getMaxStackSize(offered), target.getMaxStackSize(offered));
    int free = Math.max(0, capacity - (present.isEmpty() ? 0 : present.getCount()));
    int moved = Math.min(free, offered.getCount());
    if (moved == 0) return offered;
    var replacement =
        present.isEmpty()
            ? offered.copyWithCount(moved)
            : present.copyWithCount(present.getCount() + moved);
    target.setByPlayer(replacement, present.copy());
    target.setChanged();
    return offered.copyWithCount(offered.getCount() - moved);
  }

  @Override
  public boolean stillValid(Player actor) {
    return actor == player && base.stillValid(actor);
  }

  @Override
  public void broadcastChanges() {
    super.broadcastChanges();
    if (!(player instanceof ServerPlayer actor)
        || actor.level().isClientSide()
        || !(base instanceof BlockEntity tile)) return;
    for (var field : getNetworkedFields())
      SideServices.network(true).updateTileEntityFieldTo(tile, field, actor);
    if (base instanceof NativeBlockEntity nativeTile)
      for (var component : nativeTile.getComponents()) component.onContainerUpdate(actor);
  }

  public List<String> getNetworkedFields() {
    return List.of();
  }

  @Override
  public void addSlotListener(ContainerListener listener) {
    Objects.requireNonNull(listener);
    if (!listeners.contains(listener)) listeners.add(listener);
    super.addSlotListener(listener);
  }

  @Override
  public void removeSlotListener(ContainerListener listener) {
    listeners.remove(listener);
    super.removeSlotListener(listener);
  }

  public final List<ContainerListener> getListeners() {
    return List.copyOf(listeners);
  }

  public Player getPlayer() {
    return player;
  }

  public void onContainerEvent(String event) {}
}
