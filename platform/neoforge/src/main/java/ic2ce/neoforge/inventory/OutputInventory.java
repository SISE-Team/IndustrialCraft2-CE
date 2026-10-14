package ic2ce.neoforge.inventory;

import ic2ce.core.block.IInventorySlotHolder;
import ic2ce.engine.inventory.InsertionWorkspace;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/** Output insertion uses reusable quantity simulation and one atomic notification. */
public class OutputInventory extends InventorySlot {
  private InsertionWorkspace<ItemStack> workspace;

  public OutputInventory(IInventorySlotHolder<?> holder, String name, int count) {
    this(holder, name, count, InvSide.BOTTOM);
  }

  public OutputInventory(IInventorySlotHolder<?> holder, String name, int count, InvSide side) {
    super(holder, name, Access.O, count, side);
  }

  private InsertionWorkspace<ItemStack> workspace() {
    if (workspace == null)
      workspace =
          new InsertionWorkspace<>(
              size(),
              this::get,
              stack -> stack == null ? 0 : stack.getCount(),
              stack -> stack == null ? 0 : stack.getMaxStackSize(),
              ItemStack::isSameItemSameComponents);
    return workspace;
  }

  @Override
  public boolean accepts(ItemStack stack) {
    return false;
  }

  public boolean canAdd(ItemStack stack) {
    return canAdd(List.of(Objects.requireNonNull(stack)));
  }

  public boolean canAdd(Collection<ItemStack> stacks) {
    return workspace().prepare(stacks, getStackSizeLimit()) == 0;
  }

  public int add(ItemStack stack) {
    return add(List.of(Objects.requireNonNull(stack)));
  }

  public int add(Collection<ItemStack> stacks) {
    var workspace = workspace();
    int remaining = workspace.prepare(stacks, getStackSizeLimit());
    boolean changed = false;
    for (int i = 0; i < size(); i++)
      if (workspace.changed(i)) {
        putFromNBT(i, workspace.kind(i).copyWithCount(workspace.count(i)));
        changed = true;
      }
    if (changed) onChanged();
    return remaining;
  }
}
