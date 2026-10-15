package ic2ce.neoforge.processing.uu;

import ic2ce.neoforge.runtime.SideServices;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/** Smelting contributes one real input choice and its per-operation processing cost. */
public final class VanillaSmeltingResolver implements IRecipeResolver {
  @Override
  public List<RecipeTransformation> getTransformations() {
    var result = new ArrayList<RecipeTransformation>();
    var manager = SideServices.proxy().getRecipeManager();
    if (manager == null) return List.of();
    for (var holder : manager.getRecipes())
      if (holder.value() instanceof SmeltingRecipe recipe) {
        var outputs = recipe.assemble(new SingleRecipeInput(ItemStack.EMPTY));
        if (!outputs.isEmpty())
          result.add(
              new RecipeTransformation(
                  14,
                  RecipeUtil.convertIngredients(List.of(recipe.input())),
                  new LeanItemStack(outputs)));
      }
    return List.copyOf(result);
  }
}
