package ic2ce.compat;

import net.minecraft.world.item.crafting.CraftingRecipe;

public interface Ic2CraftingRecipe extends CraftingRecipe {
   int getIc2RecipeWidth();

   int getIc2RecipeHeight();

   default int getRecipeHeight() {
      return this.getIc2RecipeHeight();
   }

   default int getRecipeWidth() {
      return this.getIc2RecipeWidth();
   }
}
