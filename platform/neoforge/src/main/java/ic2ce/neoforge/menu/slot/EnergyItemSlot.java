package ic2ce.neoforge.menu.slot;

import ic2ce.api.info.Info;
import ic2ce.api.item.ElectricItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A battery/fuel slot uses observational energy predicates; acceptance cannot spend a candidate
 * charge.
 */
public final class EnergyItemSlot extends Slot {
  public int tier;

  public EnergyItemSlot(Container owner, int tier, int index, int x, int y) {
    super(owner, index, x, y);
    if (tier < 0) throw new IllegalArgumentException("Negative battery slot tier");
    this.tier = tier;
  }

  public EnergyItemSlot(Container owner, int index, int x, int y) {
    this(owner, Integer.MAX_VALUE, index, x, y);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    if (stack == null || stack.isEmpty() || tier < 0) return false;
    double fuel = Info.getItemInfo().getEnergyValue(stack);
    return Double.isFinite(fuel) && fuel > 0
        || ElectricItem.manager.discharge(stack, Double.POSITIVE_INFINITY, tier, true, true, true)
            > 0;
  }
}
