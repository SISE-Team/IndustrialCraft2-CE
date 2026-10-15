package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import ic2ce.neoforge.runtime.profile.NotClassic;
import net.minecraft.world.item.ItemStack;

/**
 * Lithium breeding has no base pulse bonus and saturates at full exposure instead of dropping an
 * unrelated item.
 */
@NotClassic
public class LithiumBreeder extends WearableReactorPart {
  public LithiumBreeder(Properties properties) {
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
    if (thermalPhase)
      setUse(
          part,
          (int) Math.min(getMaxUse(), (long) getUse(part) + Math.max(0, reactor.getHeat()) / 3000));
    return true;
  }

  @Override
  public double getUseFraction(ItemStack part) {
    return 1 - super.getUseFraction(part);
  }
}
