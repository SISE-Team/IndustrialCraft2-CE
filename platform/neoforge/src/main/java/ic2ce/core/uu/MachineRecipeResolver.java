package ic2ce.core.uu;

import ic2ce.api.recipe.IMachineRecipeManager;
import ic2ce.api.recipe.IRecipeInput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Machine inputs use the recipe's declared quantity, including tag-based alternatives. */
public final class MachineRecipeResolver implements IRecipeResolver {
  private final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> manager;

  public MachineRecipeResolver(
      IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> manager) {
    this.manager = manager;
  }

  @Override
  public List<RecipeTransformation> getTransformations() {
    if (manager == null || !manager.isIterable()) return List.of();
    var result = new ArrayList<RecipeTransformation>();
    for (var recipe : manager.getRecipes()) {
      var input = recipe.getInput();
      if (input.getAmount() <= 0) continue;
      var choices = new ArrayList<LeanItemStack>();
      for (var stack : input.getInputs())
        if (stack != null && !stack.isEmpty())
          choices.add(new LeanItemStack(stack, input.getAmount()));
      var outputs = RecipeUtil.convertOutputs(recipe.getOutput());
      if (!outputs.isEmpty())
        result.add(new RecipeTransformation(14, List.of(List.copyOf(choices)), outputs));
    }
    return List.copyOf(result);
  }
}
