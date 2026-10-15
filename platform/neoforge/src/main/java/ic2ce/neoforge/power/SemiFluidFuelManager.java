package ic2ce.neoforge.power;

import ic2ce.api.recipe.ISemiFluidFuelManager;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Immutable published semi-fluid fuel properties; invalid registrations cannot change a live recipe
 * table.
 */
public final class SemiFluidFuelManager implements ISemiFluidFuelManager {
  private volatile Map<Fluid, BurnProperty> properties = Map.of();

  @Override
  public synchronized void addFluid(Fluid fluid, int amount, double power) {
    if (fluid == null
        || fluid == Fluids.EMPTY
        || amount <= 0
        || !Double.isFinite(power)
        || power <= 0) throw new IllegalArgumentException("Invalid semi-fluid fuel");
    var before = properties;
    if (before.containsKey(fluid))
      throw new IllegalStateException("Duplicate semi-fluid fuel: " + fluid);
    var next = new IdentityHashMap<Fluid, BurnProperty>(before);
    next.put(fluid, new BurnProperty(amount, power));
    properties = java.util.Collections.unmodifiableMap(next);
  }

  @Override
  public BurnProperty getBurnProperty(Fluid fluid) {
    return fluid == null ? null : properties.get(fluid);
  }

  @Override
  public boolean acceptsFluid(Fluid fluid) {
    return fluid != null && properties.containsKey(fluid);
  }

  @Override
  public Set<Fluid> getAcceptedFluids() {
    return properties.keySet();
  }

  @Override
  public Map<Fluid, BurnProperty> getBurnProperties() {
    return properties;
  }
}
