package ic2ce.integration.jei;

import ic2ce.api.recipe.IRecipeInput;
import ic2ce.integration.jei.recipe.machine.*;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.processing.ProcessingBlocks;
import ic2ce.neoforge.recipe.codec.*;
import ic2ce.neoforge.registry.RecipeKinds;
import java.util.*;
import java.util.function.Supplier;
import mezz.jei.api.*;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Declarative native recipe bindings publish the actual current synchronized datapack recipes and
 * distinct machine modes.
 */
@JeiPlugin
public final class Ic2JeiPlugin implements IModPlugin {
  private record Binding(
      RecipeType<IORecipeWrapper> display,
      Supplier<MachineBlockType> device,
      Supplier<
              net.minecraft.world.item.crafting.RecipeType<
                  RecipeHolder<IRecipeInput, Collection<ItemStack>>>>
          nativeType,
      int metalMode) {}

  private static Binding binding(
      String id,
      Supplier<MachineBlockType> block,
      Supplier<
              net.minecraft.world.item.crafting.RecipeType<
                  RecipeHolder<IRecipeInput, Collection<ItemStack>>>>
          type,
      int mode) {
    return new Binding(RecipeType.create("ic2", id, IORecipeWrapper.class), block, type, mode);
  }

  private static final List<Binding> BINDINGS =
      List.of(
          binding(
              "blast_furnace",
              () -> ProcessingBlocks.BLAST_FURNACE,
              () -> RecipeKinds.BLAST_FURNACE,
              -1),
          binding(
              "block_cutter",
              () -> ProcessingBlocks.BLOCK_CUTTER,
              () -> RecipeKinds.BLOCK_CUTTER,
              -1),
          binding(
              "centrifuge", () -> ProcessingBlocks.CENTRIFUGE, () -> RecipeKinds.CENTRIFUGE, -1),
          binding(
              "compressor", () -> ProcessingBlocks.COMPRESSOR, () -> RecipeKinds.COMPRESSOR, -1),
          binding("extractor", () -> ProcessingBlocks.EXTRACTOR, () -> RecipeKinds.EXTRACTOR, -1),
          binding("macerator", () -> ProcessingBlocks.MACERATOR, () -> RecipeKinds.MACERATOR, -1),
          binding(
              "metal_former_extruding",
              () -> ProcessingBlocks.METAL_FORMER,
              () -> RecipeKinds.METAL_FORMER_EXTRUDING,
              0),
          binding(
              "metal_former_rolling",
              () -> ProcessingBlocks.METAL_FORMER,
              () -> RecipeKinds.METAL_FORMER_ROLLING,
              1),
          binding(
              "metal_former_cutting",
              () -> ProcessingBlocks.METAL_FORMER,
              () -> RecipeKinds.METAL_FORMER_CUTTING,
              2),
          binding(
              "ore_washer",
              () -> ProcessingBlocks.ORE_WASHING_PLANT,
              () -> RecipeKinds.ORE_WASHER,
              -1));
  private static volatile mezz.jei.api.runtime.IJeiRuntime runtime;

  static mezz.jei.api.runtime.IJeiRuntime runtimeSnapshot() {
    return runtime;
  }

  @Override
  public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime available) {
    runtime = Objects.requireNonNull(available);
    ic2ce.neoforge.client.widget.RecipeOverlay.jeiRecipeListOpener =
        categories -> {
          var owned = categories.clone();
          return button -> {
            var current = runtime;
            if (current == null) return;
            var types = new ArrayList<mezz.jei.api.recipe.types.IRecipeType<?>>();
            for (String category : owned) {
              String path = category.equals("ore_washing_plant") ? "ore_washer" : category;
              var id = Identifier.tryParse(path.indexOf(':') < 0 ? "ic2:" + path : path);
              if (id == null) continue;
              var type = current.getRecipeManager().getRecipeType(id);
              if (type.isEmpty() && path.indexOf(':') < 0)
                type =
                    current.getRecipeManager().getRecipeType(Identifier.withDefaultNamespace(path));
              type.ifPresent(types::add);
            }
            if (!types.isEmpty()) current.getRecipesGui().showTypes(List.copyOf(types));
          };
        };
    ic2ce.neoforge.crafting.client.WorkbenchScreen.jeiScreenRecipesGuiCheck =
        screen -> available.getRecipesGui().getClass().isInstance(screen);
  }

  @Override
  public void onRuntimeUnavailable() {
    runtime = null;
    ic2ce.neoforge.client.widget.RecipeOverlay.jeiRecipeListOpener = null;
    ic2ce.neoforge.crafting.client.WorkbenchScreen.jeiScreenRecipesGuiCheck = null;
  }

  @Override
  public Identifier getPluginUid() {
    return Identifier.fromNamespaceAndPath("ic2", "plugin");
  }

  static List<IRecipeCategory<?>> categories(IGuiHelper graphics) {
    return BINDINGS.stream()
        .<IRecipeCategory<?>>map(
            binding ->
                binding.metalMode() < 0
                    ? new DynamicCategory(binding.device().get(), binding.display(), graphics)
                    : new MetalFormerCategory(binding.display(), binding.metalMode(), graphics))
        .toList();
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    registration.addRecipeCategories(
        categories(registration.getJeiHelpers().getGuiHelper()).toArray(IRecipeCategory[]::new));
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    for (var binding : BINDINGS)
      registration.addRecipeCatalyst(new ItemStack(binding.device().get()), binding.display());
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    var generation = RecipeGeneration.clientRecipes();
    for (var binding : BINDINGS) {
      var recipes = new ArrayList<IORecipeWrapper>();
      for (var row : generation.byType(binding.nativeType().get()))
        recipes.add(new IORecipeWrapper(row.value().recipe()));
      registration.addRecipes(binding.display(), List.copyOf(recipes));
    }
  }
}
