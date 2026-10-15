package ic2ce.neoforge.inventory;

import ic2ce.api.info.Info;
import net.minecraft.world.item.ItemStack;

public class FuelInput extends ConsumableInventory {
  public final boolean allowLava;

  public FuelInput(IInventorySlotHolder<?> holder, String name, int count, boolean lava) {
    super(holder, name, Access.I, count, InvSide.SIDE);
    allowLava = lava;
  }

  @Override
  public boolean accepts(ItemStack stack) {
    return stack != null
        && !stack.isEmpty()
        && Info.getItemInfo().getFuelValue(stack, allowLava) > 0;
  }

  public int consumeFuel() {
    ItemStack fuel = consume(1);
    return fuel == null ? 0 : Info.getItemInfo().getFuelValue(fuel, allowLava);
  }
}
