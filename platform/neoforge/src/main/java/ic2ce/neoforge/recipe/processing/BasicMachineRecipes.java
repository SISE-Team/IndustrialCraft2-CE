package ic2ce.neoforge.recipe.processing;

import ic2ce.api.recipe.*;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Public machine-recipe API backed by the rewritten ordered index. */
public class BasicMachineRecipes extends OrderedMachineRecipes<IRecipeInput, Collection<ItemStack>>
    implements IBasicMachineRecipeManager {
  @Override
  protected IRecipeInput getForInput(IRecipeInput input) {
    return input;
  }

  @Override
  protected boolean consumeContainer(
      ItemStack input,
      ItemStack remainder,
      MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe) {
    for (ItemStack output : recipe.getOutput()) {
      if (StackUtil.checkItemEqualityStrict(remainder, output)) return true;
      if (ProjectRuntime.envProxy.hasRecipeRemainder(output)
          && StackUtil.checkItemEqualityStrict(
              input, ProjectRuntime.envProxy.getRecipeRemainder(output))) return true;
    }
    return false;
  }

  public boolean addRecipe(
      MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe, boolean replace) {
    Objects.requireNonNull(recipe, "null recipe");
    Objects.requireNonNull(recipe.getInput(), "null recipe input");
    Objects.requireNonNull(recipe.getOutput(), "null recipe output");
    if (recipe.getOutput().isEmpty()) throw new IllegalArgumentException("no outputs");
    if (!resolveDuplicates(recipe.getInput(), replace)) return false;
    storeRecipe(recipe);
    return true;
  }

  public boolean addRecipe(
      IRecipeInput input, CompoundTag metadata, boolean replace, ItemStack... outputs) {
    return addRecipe(input, Arrays.asList(outputs), metadata, replace);
  }

  public boolean addRecipe(
      IRecipeInput input, Collection<ItemStack> outputs, CompoundTag metadata, boolean replace) {
    Objects.requireNonNull(input, "null recipe input");
    Objects.requireNonNull(outputs, "null recipe output");
    if (outputs.isEmpty()) throw new IllegalArgumentException("no outputs");
    var copied = new ArrayList<ItemStack>(outputs.size());
    for (ItemStack output : outputs) {
      if (output == null || output.isEmpty()) return invalid("Empty machine recipe output");
      if (input.matches(output)
          && (metadata == null || !metadata.contains("ignoreSameInputOutput")))
        return invalid("Machine recipe output also matches its input: " + output);
      copied.add(output.copy());
    }
    return addRecipe(new MachineRecipe<>(input, copied, metadata), replace);
  }

  private boolean resolveDuplicates(IRecipeInput input, boolean replace) {
    List<ItemStack> examples = input.getInputs();
    if (!replace) {
      for (ItemStack example : examples) if (getRecipe(example) != null) return false;
      return true;
    }
    for (ItemStack example : examples) {
      MachineRecipe<IRecipeInput, Collection<ItemStack>> existing;
      while ((existing = getRecipe(example)) != null) removeStoredRecipe(existing);
    }
    return true;
  }

  @Override
  public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput) {
    var recipe = getRecipe(input);
    if (recipe == null) return null;
    int amount = recipe.getInput().getAmount();
    if (input.getCount() < amount
        || ProjectRuntime.envProxy.hasRecipeRemainder(input) && input.getCount() != amount)
      return null;
    if (adjustInput) {
      if (ProjectRuntime.envProxy.hasRecipeRemainder(input))
        throw new UnsupportedOperationException("Use apply for container remainders");
      input.shrink(amount);
    }
    return new RecipeOutput(recipe.getMetaData(), new ArrayList<>(recipe.getOutput()));
  }

  public void removeRecipe(ItemStack input, Collection<ItemStack> outputs) {
    var recipe = getRecipe(input);
    if (recipe == null || recipe.getOutput().size() != outputs.size()) return;
    var remaining = new ArrayList<>(outputs);
    for (ItemStack output : recipe.getOutput()) {
      int found = -1;
      for (int i = 0; i < remaining.size(); i++)
        if (StackUtil.checkItemEqualityStrict(output, remaining.get(i))) {
          found = i;
          break;
        }
      if (found < 0) return;
      remaining.remove(found);
    }
    removeStoredRecipe(recipe);
  }

  private boolean invalid(String message) {
    if (!ProjectConfiguration.ignoreInvalidRecipes) throw new IllegalArgumentException(message);
    ProjectRuntime.log.warn(DiagnosticArea.Recipe, message);
    return false;
  }
}
