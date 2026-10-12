package ic2.core.uu;

import ic2.core.IC2;
import ic2.core.util.LogCategory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class VanillaSmeltingResolver implements IRecipeResolver {
   private static final double transformCost = 14.0;

   @Override
   public List<RecipeTransformation> getTransformations() {
      List<RecipeTransformation> ret = new ArrayList<>();

      for (var holder : IC2.sideProxy.getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof SmeltingRecipe recipe)) {
               continue;
            }
         try {
            List<List<LeanItemStack>> inputs = RecipeUtil.convertIngredients(java.util.List.of(recipe.input()));
            LeanItemStack output = new LeanItemStack(recipe.assemble(new net.minecraft.world.item.crafting.SingleRecipeInput(net.minecraft.world.item.ItemStack.EMPTY)));
            ret.add(new RecipeTransformation(14.0, inputs, output));
         } catch (IllegalArgumentException var6) {
            IC2.log.warn(LogCategory.Uu, var6, "invalid recipe");
         }
      }

      return ret;
   }
}
