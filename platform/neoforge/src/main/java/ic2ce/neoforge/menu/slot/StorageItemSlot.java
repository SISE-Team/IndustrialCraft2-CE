package ic2ce.neoforge.menu.slot;

import ic2ce.api.item.ItemWrapper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Toolbox insertion uses current addon/native storage contracts rather than a fixed class list. */
public final class StorageItemSlot extends Slot {
  public StorageItemSlot(Container owner, int index, int x, int y) {
    super(owner, index, x, y);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return stack != null && !stack.isEmpty() && ItemWrapper.canBeStoredInToolbox(stack);
  }
}
