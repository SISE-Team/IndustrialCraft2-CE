package ic2ce.neoforge.processing;

import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;

/**
 * Fabrication energy quantities must remain finite and positive before becoming native capacities.
 */
public final class FabricationSettings {
  private FabricationSettings() {}

  public static int energyCost() {
    double cost =
        1000000.0
            * NativeConfigValues.getFloat(ProjectConfiguration.get(), "balance/uuEnergyFactor");
    if (!Double.isFinite(cost) || cost <= 0)
      throw new IllegalArgumentException("UU energy factor must be finite and positive");
    return (int) Math.clamp(Math.round(cost), 1, Integer.MAX_VALUE);
  }
}
