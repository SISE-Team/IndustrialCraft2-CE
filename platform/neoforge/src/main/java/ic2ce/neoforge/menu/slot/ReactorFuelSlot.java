package ic2ce.neoforge.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Nuclear insertion belongs to the actual reactor cell's live acceptance policy. */
public final class ReactorFuelSlot extends Slot {
  public ReactorFuelSlot(Container reactor, int cell, int x, int y) {
    super(reactor, cell, x, y);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return stack != null && !stack.isEmpty() && container.canPlaceItem(getContainerSlot(), stack);
  }
}
