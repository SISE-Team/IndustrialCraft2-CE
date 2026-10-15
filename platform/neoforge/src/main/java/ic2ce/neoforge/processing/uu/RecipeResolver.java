package ic2ce.neoforge.processing.uu;

import ic2ce.api.recipe.IRecipeInput;
import ic2ce.neoforge.recipe.crafting.PositionedCrafting;
import ic2ce.neoforge.recipe.crafting.UnorderedCrafting;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/** Native and IC2 crafting adapters preserve ingredient slots and exclude UU assembly recipes. */
public final class RecipeResolver implements IRecipeResolver {
  private static List<List<LeanItemStack>> inputs(IRecipeInput[] ingredients) {
    var result = new ArrayList<List<LeanItemStack>>(ingredients.length);
    for (var ingredient : ingredients) {
      if (ingredient == null) continue;
      var choices = new ArrayList<LeanItemStack>();
      for (var stack : ingredient.getInputs())
        if (stack != null && !stack.isEmpty() && ingredient.getAmount() > 0)
          choices.add(new LeanItemStack(stack, ingredient.getAmount()));
      result.add(List.copyOf(choices));
    }
    return List.copyOf(result);
  }

  @Override
  public List<RecipeTransformation> getTransformations() {
    var manager = ProjectRuntime.sideProxy.getRecipeManager();
    if (manager == null) return List.of();
    var result = new ArrayList<RecipeTransformation>();
    for (var holder : manager.getRecipes()) {
      var recipe = holder.value();
      List<List<LeanItemStack>> ingredients;
      ItemStack output;
      if (recipe instanceof PositionedCrafting shaped && shaped.assemblyMask == 0) {
        ingredients = inputs(shaped.input);
        output = shaped.getOutput();
      } else if (recipe instanceof UnorderedCrafting shapeless) {
        ingredients = inputs(shapeless.input);
        output = shapeless.getOutput();
      } else if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe) {
        var nativeRecipe = (CraftingRecipe) recipe;
        ingredients = RecipeUtil.convertIngredients(nativeRecipe.placementInfo().ingredients());
        output = nativeRecipe.assemble(CraftingInput.EMPTY);
      } else continue;
      if (!output.isEmpty() && !ingredients.isEmpty())
        result.add(new RecipeTransformation(1, ingredients, new LeanItemStack(output)));
    }
    return List.copyOf(result);
  }
}
