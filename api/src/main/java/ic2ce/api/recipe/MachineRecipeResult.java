package ic2ce.api.recipe;

import java.util.Objects;

/** A successful immutable association between a recipe and its adjusted physical input. */
public record MachineRecipeResult<RI, RO, I>(MachineRecipe<RI, RO> recipe, I adjustedInput) {
  public MachineRecipeResult {
    Objects.requireNonNull(recipe);
  }

  public MachineRecipe<RI, RO> getRecipe() {
    return recipe;
  }

  public RO getOutput() {
    return recipe.getOutput();
  }

  public I getAdjustedInput() {
    return adjustedInput;
  }
}
