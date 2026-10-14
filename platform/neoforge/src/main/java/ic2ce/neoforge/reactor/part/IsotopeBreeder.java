package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import ic2ce.core.profile.NotExperimental;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.ItemStack;

/**
 * Depleted isotope breeding gains one base unit plus the hull-temperature bonus on thermal pulses.
 */
@NotExperimental
public class IsotopeBreeder extends WearableReactorPart {
  public IsotopeBreeder(Properties properties) {
    super(properties, 10000);
  }

  @Override
  public boolean acceptUraniumPulse(
      ItemStack part,
      IReactor reactor,
      ItemStack source,
      int x,
      int y,
      int sourceX,
      int sourceY,
      boolean thermalPhase) {
    if (thermalPhase) {
      long next = (long) getUse(part) + 1 + Math.max(0, reactor.getHeat()) / 3000;
      if (next >= getMaxUse())
        reactor.setItemAt(x, y, new ItemStack(ItemDefinitions.RE_ENRICHED_URANIUM));
      else setUse(part, (int) next);
    }
    return true;
  }

  @Override
  public double getUseFraction(ItemStack part) {
    return 1 - super.getUseFraction(part);
  }
}
