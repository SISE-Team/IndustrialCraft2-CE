package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import net.minecraft.world.item.ItemStack;

/**
 * Plating extends hull capacity and scales environmental heat damage independently of fuel pulses.
 */
public class HullPlating extends ReactorPart {
  private final int maxHeatAdd;
  private final float effectModifier;

  public HullPlating(Properties properties, int capacity, float effect) {
    super(properties);
    if (capacity < 0 || !Float.isFinite(effect) || effect < 0)
      throw new IllegalArgumentException("Invalid reactor plating");
    maxHeatAdd = capacity;
    effectModifier = effect;
  }

  @Override
  public void processChamber(ItemStack part, IReactor reactor, int x, int y, boolean thermalPhase) {
    if (thermalPhase) {
      reactor.setMaxHeat(
          (int) Math.clamp((long) reactor.getMaxHeat() + maxHeatAdd, 0, Integer.MAX_VALUE));
      double effect = reactor.getHeatEffectModifier() * (double) effectModifier;
      reactor.setHeatEffectModifier((float) Math.clamp(effect, 0, Float.MAX_VALUE));
    }
  }

  @Override
  public float influenceExplosion(ItemStack part, IReactor reactor) {
    return effectModifier < 1 ? effectModifier : 0;
  }
}
