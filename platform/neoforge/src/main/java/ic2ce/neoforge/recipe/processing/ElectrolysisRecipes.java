package ic2ce.neoforge.recipe.processing;

import ic2ce.api.recipe.IElectrolyzerRecipeManager;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.level.material.Fluid;

/** Validated immutable recipe records are published atomically without exposing mutable arrays. */
public class ElectrolysisRecipes implements IElectrolyzerRecipeManager {
  private volatile Map<Fluid, ElectrolyzerRecipe> recipes = Map.of();

  @Override
  public void addRecipe(Fluid input, int amount, int eu, ElectrolyzerOutput... outputs) {
    addRecipe(input, amount, eu, 200, outputs);
  }

  @Override
  public synchronized void addRecipe(
      Fluid input, int amount, int eu, int duration, ElectrolyzerOutput... outputs) {
    Objects.requireNonNull(input);
    var recipe = new ElectrolyzerRecipe(amount, eu, duration, outputs);
    if (recipes.containsKey(input))
      throw new IllegalArgumentException("Electrolysis input already registered: " + input);
    var next = new IdentityHashMap<Fluid, ElectrolyzerRecipe>(recipes);
    next.put(input, recipe);
    recipes = Collections.unmodifiableMap(next);
  }

  @Override
  public ElectrolyzerRecipe getElectrolysisInformation(Fluid input) {
    return input == null ? null : recipes.get(input);
  }

  @Override
  public ElectrolyzerOutput[] getOutput(Fluid input) {
    var recipe = getElectrolysisInformation(input);
    return recipe == null ? null : recipe.outputs().toArray(ElectrolyzerOutput[]::new);
  }

  @Override
  public boolean acceptsFluid(Fluid input) {
    return input != null && recipes.containsKey(input);
  }

  @Override
  public Set<Fluid> getAcceptedFluids() {
    return recipes.keySet();
  }

  @Override
  public Map<Fluid, ElectrolyzerRecipe> getRecipeMap() {
    return recipes;
  }
}
