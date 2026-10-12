package ic2.core.recipe.v2;

import ic2.api.recipe.MachineRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * IC2 machine recipe wrapper exposed to the vanilla recipe system.
 *
 * <p>26.1.2 note: the vanilla RecipeManager decodes recipes through codecs
 * without passing the recipe id, so {@link #id} carries a placeholder and the
 * authoritative id is the vanilla {@code net.minecraft.world.item.crafting.RecipeHolder}
 * key. Consumers read {@link #recipe()} for the actual machine recipe.</p>
 */
public record RecipeHolder<I, O>(MachineRecipe<I, O> recipe, Identifier id, RecipeSerializer<?> serializer, RecipeType<?> type)
   implements Recipe<RecipeInput> {
   @Override
   public boolean matches(RecipeInput input, Level level) {
      return false;
   }

   @Override
   public ItemStack assemble(RecipeInput input) {
      return ItemStack.EMPTY;
   }

   @Override
   public boolean isSpecial() {
      // These wrappers expose IC2 machine recipes to data packs and JEI. They
      // have no vanilla workbench placement and must bypass its ingredient check.
      return true;
   }

   @Override
   public boolean showNotification() {
      return false;
   }

   @Override
   public String group() {
      return "";
   }

   @SuppressWarnings("unchecked")
   @Override
   public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
      return (RecipeSerializer<? extends Recipe<RecipeInput>>)this.serializer;
   }

   @Override
   @SuppressWarnings("unchecked")
   public RecipeType<? extends Recipe<RecipeInput>> getType() {
      return (RecipeType<? extends Recipe<RecipeInput>>)this.type;
   }

   @Override
   public PlacementInfo placementInfo() {
      return PlacementInfo.NOT_PLACEABLE;
   }

   @Override
   public RecipeBookCategory recipeBookCategory() {
      return RecipeBookCategories.CRAFTING_MISC;
   }

   public Identifier getId() {
      return this.id;
   }
}
