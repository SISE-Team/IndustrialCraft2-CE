package ic2ce.neoforge.recipe.processing;

import ic2ce.api.recipe.IMachineRecipeManager;
import ic2ce.api.recipe.IRecipeInput;
import ic2ce.api.recipe.MachineRecipe;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.recipe.Recipes;
import java.util.Collection;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** Authoritative vanilla smelting; native recipes assemble their own component-sensitive output. */
public final class SmeltingRecipes
    implements IMachineRecipeManager<ItemStack, ItemStack, ItemStack> {
  @Override
  public MachineRecipeResult<ItemStack, ItemStack, ItemStack> apply(
      ItemStack input, boolean acceptanceOnly) {
    if (input == null || input.isEmpty()) return null;
    var server = ServerLifecycleHooks.getCurrentServer();
    if (server == null) return null;
    var query = new SingleRecipeInput(input);
    var found =
        server.getRecipeManager().getRecipeFor(RecipeType.SMELTING, query, server.overworld());
    if (found.isEmpty()) return null;
    var recipe = found.get().value();
    var output = recipe.assemble(query);
    if (output.isEmpty()) return null;
    var metadata = new CompoundTag();
    metadata.putFloat("experience", recipe.experience() * output.getCount());
    return new MachineRecipe<>(input, output, metadata)
        .getResult(input.copyWithCount(input.getCount() - 1));
  }

  @Override
  public boolean isIterable() {
    return false;
  }

  @Override
  public Iterable<? extends MachineRecipe<ItemStack, ItemStack>> getRecipes() {
    throw new UnsupportedOperationException("Smelting follows the native recipe manager");
  }

  public enum SmeltingBridge
      implements IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> {
    INSTANCE;

    @Override
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(
        ItemStack input, boolean acceptanceOnly) {
      var result = Recipes.furnace.apply(input, acceptanceOnly);
      if (result == null) return null;
      var recipe = result.getRecipe();
      var ingredient = Recipes.inputFactory.forStack(recipe.getInput());
      return new MachineRecipe<IRecipeInput, Collection<ItemStack>>(
              ingredient, List.of(recipe.getOutput()), recipe.getMetaData())
          .getResult(result.getAdjustedInput());
    }

    @Override
    public boolean isIterable() {
      return false;
    }

    @Override
    public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
      throw new UnsupportedOperationException("Smelting follows the native recipe manager");
    }
  }
}
