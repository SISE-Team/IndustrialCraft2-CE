package ic2ce.neoforge.recipe.codec;

import ic2ce.api.recipe.MachineRecipe;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * Machine-only datapack association; actual workbench recipes have their own native placement
 * implementation.
 */
public record RecipeHolder<I, O>(
    MachineRecipe<I, O> recipe, Identifier id, RecipeSerializer<?> serializer, RecipeType<?> type)
    implements Recipe<RecipeInput> {
  public RecipeHolder {
    Objects.requireNonNull(recipe);
    Objects.requireNonNull(id);
    Objects.requireNonNull(serializer);
    Objects.requireNonNull(type);
  }

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

  @Override
  public PlacementInfo placementInfo() {
    return PlacementInfo.NOT_PLACEABLE;
  }

  @Override
  public RecipeBookCategory recipeBookCategory() {
    return RecipeBookCategories.CRAFTING_MISC;
  }

  @Override
  @SuppressWarnings("unchecked")
  public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
    return (RecipeSerializer<? extends Recipe<RecipeInput>>) serializer;
  }

  @Override
  @SuppressWarnings("unchecked")
  public RecipeType<? extends Recipe<RecipeInput>> getType() {
    return (RecipeType<? extends Recipe<RecipeInput>>) type;
  }

  public Identifier getId() {
    return id;
  }
}
