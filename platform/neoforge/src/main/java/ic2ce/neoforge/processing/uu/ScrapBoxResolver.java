package ic2ce.neoforge.processing.uu;

import ic2ce.api.recipe.Recipes;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;

/** Probabilistic drops are priced by a bounded expected whole-box quantity. */
public final class ScrapBoxResolver implements IRecipeResolver {
  @Override
  public List<RecipeTransformation> getTransformations() {
    var result = new ArrayList<RecipeTransformation>();
    for (var drop : Recipes.scrapboxDrops.getDrops().entrySet()) {
      var stack = drop.getKey();
      double chance = drop.getValue();
      if (stack == null || stack.isEmpty() || !Double.isFinite(chance) || chance <= 0) continue;
      int boxes = (int) Math.clamp(Math.round(1 / chance), 1, Integer.MAX_VALUE);
      result.add(
          new RecipeTransformation(
              1,
              List.of(List.of(new LeanItemStack(ItemDefinitions.SCRAP_BOX, boxes))),
              new LeanItemStack(stack)));
    }
    return List.copyOf(result);
  }
}
