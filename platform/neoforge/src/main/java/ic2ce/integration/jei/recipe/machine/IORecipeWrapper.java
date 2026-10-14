package ic2ce.integration.jei.recipe.machine;

import ic2ce.api.recipe.*;
import java.util.*;
import net.minecraft.world.item.ItemStack;

/** Recipe displays preserve declared item bills and own physical output prototypes. */
public final class IORecipeWrapper {
  private final IRecipeInput input;
  private final List<ItemStack> outputs;

  public IORecipeWrapper(MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe) {
    this(recipe.getInput(), recipe.getOutput());
  }

  public IORecipeWrapper(IRecipeInput input, Collection<ItemStack> output) {
    this.input = Objects.requireNonNull(input);
    var copied = new ArrayList<ItemStack>();
    for (var stack : output) if (stack != null && !stack.isEmpty()) copied.add(stack.copy());
    outputs = List.copyOf(copied);
  }

  public List<List<ItemStack>> getInputs() {
    var choices = new ArrayList<ItemStack>();
    for (var stack : input.getInputs())
      if (stack != null && !stack.isEmpty()) choices.add(stack.copyWithCount(input.getAmount()));
    return choices.isEmpty() ? List.of() : List.of(List.copyOf(choices));
  }

  public List<ItemStack> getOutputs() {
    return outputs.stream().map(ItemStack::copy).toList();
  }
}
