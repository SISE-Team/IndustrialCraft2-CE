package ic2ce.core;

import ic2ce.api.recipe.ILiquidAcceptManager;
import ic2ce.api.recipe.ILiquidHeatExchangerManager;
import ic2ce.api.recipe.Recipes;
import ic2ce.neoforge.fluid.FluidHandler;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Directional heat-exchange recipes are validated before immutable publication. */
public final class LiquidHeatExchangerManager implements ILiquidHeatExchangerManager {
  private final boolean heatup;
  private volatile Map<Fluid, HeatExchangeProperty> properties = Map.of();
  private final ILiquidAcceptManager oneWay =
      new ILiquidAcceptManager() {
        public boolean acceptsFluid(Fluid input) {
          var exchange = getHeatExchangeProperty(input);
          var opposite = getOpposite();
          return exchange != null
              && (opposite == null || !opposite.acceptsFluid(exchange.outputFluid));
        }

        public Set<Fluid> getAcceptedFluids() {
          var accepted = java.util.Collections.newSetFromMap(new IdentityHashMap<Fluid, Boolean>());
          for (var input : properties.keySet()) if (acceptsFluid(input)) accepted.add(input);
          return java.util.Collections.unmodifiableSet(accepted);
        }
      };

  public LiquidHeatExchangerManager(boolean heatup) {
    this.heatup = heatup;
  }

  private void invalid(String message) {
    if (ProjectConfiguration.ignoreInvalidRecipes)
      ProjectRuntime.log.warn(DiagnosticArea.Recipe, message);
    else throw new IllegalArgumentException(message);
  }

  @Override
  public synchronized void addFluid(Fluid input, Fluid output, int heatPerMillibucket) {
    if (input == null
        || output == null
        || input == Fluids.EMPTY
        || output == Fluids.EMPTY
        || input == output
        || heatPerMillibucket == 0
        || heatPerMillibucket == Integer.MIN_VALUE) {
      invalid("Invalid heat-exchange fluid or energy quantity");
      return;
    }
    var before = properties;
    if (before.containsKey(input)) {
      invalid("Duplicate heat-exchange fluid: " + input);
      return;
    }
    int from = FluidHandler.getTemperature(input), to = FluidHandler.getTemperature(output);
    if (heatup ? from >= to : from <= to) {
      invalid("Heat-exchange temperature contradicts its direction: " + input + " -> " + output);
      return;
    }
    var next = new IdentityHashMap<Fluid, HeatExchangeProperty>(before);
    next.put(input, new HeatExchangeProperty(output, Math.abs(heatPerMillibucket)));
    properties = java.util.Collections.unmodifiableMap(next);
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
  public HeatExchangeProperty getHeatExchangeProperty(Fluid fluid) {
    return fluid == null ? null : properties.get(fluid);
  }

  @Override
  public Map<Fluid, HeatExchangeProperty> getHeatExchangeProperties() {
    return properties;
  }

  @Override
  public ILiquidAcceptManager getSingleDirectionLiquidManager() {
    return oneWay;
  }

  public ILiquidHeatExchangerManager getOpposite() {
    return heatup ? Recipes.liquidCooldownManager : Recipes.liquidHeatupManager;
  }
}
