package ic2ce.neoforge.menu.slot;

import java.util.Objects;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * Ghost-template edits own their component/count snapshots and never transfer a physical carried
 * item.
 */
public final class TemplateSlot extends Slot {
  private static final SimpleContainer DISPLAY_ONLY = new SimpleContainer(1);
  protected final ItemStack[] stacks;
  protected final int index, stackSizeLimit;
  protected final ChangeCallback changeCallback;

  public TemplateSlot(
      ItemStack[] templates, int index, int x, int y, int limit, ChangeCallback changed) {
    super(DISPLAY_ONLY, 0, x, y);
    Objects.requireNonNull(templates);
    if (index < 0 || index >= templates.length) throw new IndexOutOfBoundsException(index);
    if (limit < 1) throw new IllegalArgumentException("Ghost limit must be positive");
    stacks = templates;
    this.index = index;
    stackSizeLimit = limit;
    changeCallback = changed;
    if (stacks[index] == null) stacks[index] = ItemStack.EMPTY;
  }

  @Override
  public boolean mayPickup(Player player) {
    return false;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return false;
  }

  @Override
  public int getMaxStackSize() {
    return stackSizeLimit;
  }

  @Override
  public ItemStack getItem() {
    var stack = stacks[index];
    return stack == null ? ItemStack.EMPTY : stack.copy();
  }

  @Override
  public void set(ItemStack stack) {
    Objects.requireNonNull(stack);
    stacks[index] =
        stack.isEmpty()
            ? ItemStack.EMPTY
            : stack.copyWithCount(
                Math.min(stack.getCount(), Math.min(stackSizeLimit, stack.getMaxStackSize())));
  }

  @Override
  public ItemStack remove(int amount) {
    return ItemStack.EMPTY;
  }

  @Override
  public void setChanged() {
    if (changeCallback != null) changeCallback.onChanged(index);
  }

  public ItemStack slotClick(
      int button, ContainerInput action, Player player, AbstractContainerMenu menu) {
    if (menu instanceof ic2ce.neoforge.menu.OwnedMenu<?> bound && bound.getPlayer() != player
        || action != ContainerInput.PICKUP
        || button < 0
        || button > 1) return ItemStack.EMPTY;
    var carried = menu.getCarried();
    var previous = getItem();
    ItemStack next;
    if (carried.isEmpty()) {
      next =
          button == 0 || previous.getCount() < 2
              ? ItemStack.EMPTY
              : previous.copyWithCount(previous.getCount() / 2);
    } else {
      int limit = Math.min(stackSizeLimit, carried.getMaxStackSize());
      int offered = button == 0 ? carried.getCount() : 1;
      int count =
          ItemStack.isSameItemSameComponents(previous, carried)
              ? (int) Math.min(limit, (long) previous.getCount() + offered)
              : Math.min(limit, offered);
      next = carried.copyWithCount(count);
    }
    if (!ItemStack.matches(previous, next)) {
      set(next);
      setChanged();
    }
    return ItemStack.EMPTY;
  }

  @FunctionalInterface
  public interface ChangeCallback {
    void onChanged(int index);
  }
}
