package ic2ce.neoforge.registry;


import ic2ce.api.recipe.ICannerBottleRecipeManager;
import ic2ce.api.recipe.ICannerEnrichRecipeManager;
import ic2ce.api.recipe.IRecipeInput;
import ic2ce.neoforge.runtime.ProjectRuntime;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.recipe.codec.RecipeHolder;
import java.util.Collection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeKinds {
   public static final RecipeType<ic2ce.neoforge.recipe.crafting.PositionedCrafting> UU_ASSEMBLY = register("uu_assembly");
   public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> MACERATOR = register("macerator");
   public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> EXTRACTOR = register("extractor");
   public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> COMPRESSOR = register("compressor");
   public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> CENTRIFUGE = register("centrifuge");
   public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> BLOCK_CUTTER = register("block_cutter");
   public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> BLAST_FURNACE = register("blast_furnace");
   public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> METAL_FORMER_EXTRUDING = register("metal_former_extruding");
   public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> METAL_FORMER_CUTTING = register("metal_former_cutting");
   public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> METAL_FORMER_ROLLING = register("metal_former_rolling");
   public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> ORE_WASHER = register("ore_washing");
   public static final RecipeType<RecipeHolder<IRecipeInput, Integer>> MATTER_FABRICATOR = register("matter_fabricator");
   public static final RecipeType<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> CANNER_BOTTLE = register("canner_bottle");
   public static final RecipeType<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> CANNER_ENRICH = register("canner_enrich");

   public static void init() {
   }

   private static <T extends Recipe<?>> RecipeType<T> register(String name) {
      return ProjectRuntime.envProxy.registerRecipeType(ProjectRuntime.getIdentifier(name));
   }
}
