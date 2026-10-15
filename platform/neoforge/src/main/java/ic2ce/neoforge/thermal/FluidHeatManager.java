package ic2ce.neoforge.thermal;

import ic2ce.api.recipe.IFluidHeatManager;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Heat-fuel registrations carry positive batch volume and heat rather than electrical power. */
public final class FluidHeatManager implements IFluidHeatManager {
  private volatile Map<Fluid, BurnProperty> properties = Map.of();

  @Override
  public synchronized void addFluid(Fluid fluid, int amount, int heat) {
    if (fluid == null || fluid == Fluids.EMPTY || amount <= 0 || heat <= 0)
      throw new IllegalArgumentException("Invalid fluid heat fuel");
    var before = properties;
    if (before.containsKey(fluid))
      throw new IllegalStateException("Duplicate fluid heat fuel: " + fluid);
    var next = new IdentityHashMap<Fluid, BurnProperty>(before);
    next.put(fluid, new BurnProperty(amount, heat));
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
