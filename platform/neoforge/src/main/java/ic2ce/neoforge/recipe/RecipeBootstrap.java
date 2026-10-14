package ic2ce.neoforge.recipe;

import ic2ce.api.recipe.*;
import ic2ce.neoforge.recipe.codec.*;
import ic2ce.neoforge.recipe.processing.*;
import ic2ce.neoforge.registry.RecipeKinds;
import java.util.Collection;
import java.util.function.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Native recipe generations rebuild the appropriate distinct family manager when its datapack
 * manager changes.
 */
public final class RecipeBootstrap {
  private RecipeBootstrap() {}

  public static void registerRecipes() {
    Recipes.furnace = new SmeltingRecipes();
    Recipes.emptyFluidContainer = new ContainerDrainingRecipes();
    Recipes.fillFluidContainer = new ContainerFillingRecipes();
    Recipes.macerator = basic(RecipeKinds.MACERATOR);
    Recipes.extractor = basic(RecipeKinds.EXTRACTOR);
    Recipes.compressor = basic(RecipeKinds.COMPRESSOR);
    Recipes.centrifuge = basic(RecipeKinds.CENTRIFUGE);
    Recipes.blockcutter = basic(RecipeKinds.BLOCK_CUTTER);
    Recipes.blastfurnace = basic(RecipeKinds.BLAST_FURNACE);
    Recipes.metalformerExtruding = basic(RecipeKinds.METAL_FORMER_EXTRUDING);
    Recipes.metalformerCutting = basic(RecipeKinds.METAL_FORMER_CUTTING);
    Recipes.metalformerRolling = basic(RecipeKinds.METAL_FORMER_ROLLING);
    Recipes.oreWashing = basic(RecipeKinds.ORE_WASHER);
    Recipes.matterFabricator =
        family(
            RecipeKinds.MATTER_FABRICATOR,
            AmplifierRecipes::new,
            (manager, recipe) ->
                manager.addRecipe(recipe.getInput(), recipe.getOutput(), null, false));
    Recipes.cannerBottle =
        family(
            RecipeKinds.CANNER_BOTTLE,
            SolidCanningRecipes::new,
            (manager, recipe) ->
                manager.addRecipe(recipe.getInput(), recipe.getOutput(), null, false));
    Recipes.cannerEnrich =
        family(
            RecipeKinds.CANNER_ENRICH,
            EnrichmentRecipes::new,
            (manager, recipe) ->
                manager.addRecipe(recipe.getInput(), recipe.getOutput(), null, false));
  }

  private static RecipeGeneration<IBasicMachineRecipeManager> basic(
      RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> type) {
    return new RecipeGeneration<>(
        generation -> {
          var result = new BasicMachineRecipes();
          for (var row : generation.byType(type)) result.addRecipe(row.value().recipe(), false);
          return result;
        });
  }

  private static <I, O, R, M extends R> RecipeGeneration<R> family(
      RecipeType<RecipeHolder<I, O>> type,
      Supplier<M> factory,
      BiConsumer<M, MachineRecipe<I, O>> admit) {
    return new RecipeGeneration<>(
        generation -> {
          var result = factory.get();
          for (var row : generation.byType(type)) admit.accept(result, row.value().recipe());
          return result;
        });
  }
}
