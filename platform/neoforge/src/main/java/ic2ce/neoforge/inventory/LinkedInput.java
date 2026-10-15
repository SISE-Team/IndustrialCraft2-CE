package ic2ce.neoforge.inventory;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;

public class LinkedInput extends ConsumableInventory {
  public final InventorySlot linkedSlot;

  public LinkedInput(IInventorySlotHolder<?> holder, String name, int count, InventorySlot linked) {
    super(holder, name, count);
    linkedSlot = Objects.requireNonNull(linked);
  }

  @Override
  public boolean accepts(ItemStack stack) {
    ItemStack required = linkedSlot.get();
    return stack != null
        && !stack.isEmpty()
        && !required.isEmpty()
        && ItemStack.isSameItemSameComponents(required, stack);
  }

  public ItemStack consumeLinked(boolean simulate) {
    ItemStack required = linkedSlot.get();
    if (required.isEmpty()) return null;
    ItemStack available = consume(required.getCount(), true, true);
    if (available == null || available.getCount() != required.getCount()) return null;
    return simulate ? available : consume(required.getCount(), false, true);
  }
}
