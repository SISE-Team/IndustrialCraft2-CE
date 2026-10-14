package ic2ce.neoforge.recipe.processing;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.IFermenterRecipeManager;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.level.material.Fluid;

/** Validated fermentation recipes published as immutable identity snapshots. */
public class FermentationRecipes implements IFermenterRecipeManager {
  private volatile Map<Fluid, FermentationProperty> recipes = Map.of();

  @Override
  public synchronized void addRecipe(
      Fluid input, int consumed, int hu, Fluid output, int produced) {
    Objects.requireNonNull(input);
    Objects.requireNonNull(output);
    if (consumed <= 0 || hu <= 0 || produced <= 0)
      throw new IllegalArgumentException("Fermentation requires positive input, heat and output");
    if (recipes.containsKey(input))
      throw new IllegalArgumentException("Fermentation input already registered: " + input);
    var next = new IdentityHashMap<Fluid, FermentationProperty>(recipes);
    next.put(input, new FermentationProperty(consumed, hu, output, produced));
    recipes = Collections.unmodifiableMap(next);
  }

  @Override
  public FermentationProperty getFermentationInformation(Fluid fluid) {
    return fluid == null ? null : recipes.get(fluid);
  }

  @Override
  public Ic2FluidStack getOutput(Fluid input) {
    var recipe = getFermentationInformation(input);
    return recipe == null ? null : recipe.getOutput();
  }

  @Override
  public boolean acceptsFluid(Fluid fluid) {
    return fluid != null && recipes.containsKey(fluid);
  }

  @Override
  public Set<Fluid> getAcceptedFluids() {
    return recipes.keySet();
  }

  @Override
  public Map<Fluid, FermentationProperty> getRecipeMap() {
    return recipes;
  }
}
