package ic2ce.neoforge.recipe.processing;

import ic2ce.api.recipe.*;
import java.util.*;
import net.minecraft.nbt.CompoundTag;

/** Amplifier selection uses the independent ordered recipe index and owned adjusted stacks. */
public class AmplifierRecipes extends OrderedMachineRecipes<IRecipeInput, Integer> {
  @Override
  protected IRecipeInput getForInput(IRecipeInput input) {
    return input;
  }

  public boolean addRecipe(
      IRecipeInput input, Integer amplification, CompoundTag metadata, boolean replace) {
    Objects.requireNonNull(input);
    Objects.requireNonNull(amplification);
    if (input.getAmount() <= 0 || amplification <= 0)
      throw new IllegalArgumentException("Amplifier input and value must be positive");
    var conflicts =
        Collections.newSetFromMap(
            new IdentityHashMap<MachineRecipe<IRecipeInput, Integer>, Boolean>());
    for (var example : input.getInputs()) {
      var conflict = getRecipe(example);
      if (conflict != null) conflicts.add(conflict);
    }
    if (!replace && !conflicts.isEmpty()) return false;
    for (var conflict : conflicts) removeStoredRecipe(conflict);
    storeRecipe(new MachineRecipe<>(input, amplification, metadata));
    return true;
  }
}
