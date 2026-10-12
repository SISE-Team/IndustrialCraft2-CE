package ic2.core.uu;

import ic2.core.IC2;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeResolver implements IRecipeResolver {
   private static final double transformCost = 1.0;

   @Override
   public List<RecipeTransformation> getTransformations() {
      List<RecipeTransformation> ret = new ArrayList<>();

      for (var holder : IC2.sideProxy.getRecipeManager().getRecipes()) {
            Recipe<?> irecipe = holder.value();
         if (irecipe instanceof ic2.core.recipe.AdvRecipe recipe && recipe.assemblyMask == 0) {
            ret.add(new RecipeTransformation(1.0, recipeInputs(recipe.input), new LeanItemStack(recipe.getOutput())));
         } else if (irecipe instanceof ic2.core.recipe.AdvShapelessRecipe recipe) {
            ret.add(new RecipeTransformation(1.0, recipeInputs(recipe.input), new LeanItemStack(recipe.getOutput())));
         } else if (irecipe instanceof net.minecraft.world.item.crafting.ShapedRecipe || irecipe instanceof net.minecraft.world.item.crafting.ShapelessRecipe) {
            var recipe=(net.minecraft.world.item.crafting.CraftingRecipe)irecipe;
            var inputs=recipe.placementInfo().ingredients();
            ItemStack output=recipe.assemble(net.minecraft.world.item.crafting.CraftingInput.EMPTY);
            if(!output.isEmpty()&&!inputs.isEmpty())ret.add(new RecipeTransformation(1.0,toDoubleStackList(inputs),new LeanItemStack(output)));
         }
      }

      return ret;
   }

   private static List<List<LeanItemStack>> recipeInputs(ic2.api.recipe.IRecipeInput[] ingredients) {
      List<List<LeanItemStack>> inputs=new ArrayList<>();
      for(var ingredient:ingredients) {
         List<LeanItemStack> choices=new ArrayList<>();
         for(var item:ingredient.getInputs())choices.add(new LeanItemStack(item,ingredient.getAmount()));
         inputs.add(choices);
      }
      return inputs;
   }

   private static List<List<LeanItemStack>> toDoubleStackList(List<Ingredient> list) {
      List<List<LeanItemStack>> ret = new ArrayList<>(list.size());

      for (Ingredient ingredient : list) {
         ItemStack[] arr = ingredient.items().map(net.minecraft.world.item.ItemStack::new).toArray(net.minecraft.world.item.ItemStack[]::new);
         List<LeanItemStack> toAdd = new ArrayList<>(arr.length);

         for (ItemStack stack : arr) {
            toAdd.add(new LeanItemStack(stack));
         }

         ret.add(toAdd);
      }

      return ret;
   }
}
