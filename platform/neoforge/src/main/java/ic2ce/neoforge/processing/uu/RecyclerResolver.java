package ic2ce.neoforge.processing.uu;

import ic2ce.api.recipe.Recipes;
import ic2ce.neoforge.processing.solid.MaterialRecycler;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;

/** Resolves the recycler after obtaining the generation's actual material candidates. */
public final class RecyclerResolver implements ILateRecipeResolver {
  @Override
  public List<RecipeTransformation> getTransformations(Iterable<LeanItemStack> materials) {
    int chance = MaterialRecycler.recycleChance();
    var choices = new ArrayList<LeanItemStack>();
    for (var material : materials) {
      var stack = material.toMcStack();
      if (stack.isEmpty()) continue;
      var result = Recipes.recycler.apply(stack, false);
      if (result != null && !result.getOutput().isEmpty())
        choices.add(material.copyWithSize(chance));
    }
    return List.of(
        new RecipeTransformation(
            55.0 * chance / 4000 * 107,
            List.of(List.copyOf(choices)),
            new LeanItemStack(ItemDefinitions.SCRAP)));
  }
}
