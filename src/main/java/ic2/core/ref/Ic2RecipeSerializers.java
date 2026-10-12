package ic2.core.ref;

import com.google.gson.JsonObject;
import ic2.core.IC2;
import ic2.core.recipe.AdvRecipe;
import ic2.core.recipe.v2.BasicMachineRecipeSerializer;
import ic2.core.recipe.v2.CannerBottleRecipeSerializer;
import ic2.core.recipe.v2.CannerEnrichRecipeSerializer;
import ic2.core.recipe.v2.IntegerOutputRecipeSerializer;
import ic2.core.recipe.v2.WeightedMachineRecipeSerializer;
import ic2.core.recipe.AdvShapelessRecipe;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class Ic2RecipeSerializers {
   public static final RecipeSerializer<AdvRecipe> SHAPED = register("shaped", new AdvRecipe.Serializer());
   public static final RecipeSerializer<AdvShapelessRecipe> SHAPELESS = register("shapeless", new AdvShapelessRecipe.Serializer());
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.IRecipeInput, java.util.Collection<net.minecraft.world.item.ItemStack>>> MACERATOR = register("macerator", new WeightedMachineRecipeSerializer(Ic2RecipeTypes.MACERATOR, null));
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.IRecipeInput, java.util.Collection<net.minecraft.world.item.ItemStack>>> EXTRACTOR = register("extractor", new BasicMachineRecipeSerializer(Ic2RecipeTypes.EXTRACTOR, null));
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.IRecipeInput, java.util.Collection<net.minecraft.world.item.ItemStack>>> COMPRESSOR = register("compressor", new BasicMachineRecipeSerializer(Ic2RecipeTypes.COMPRESSOR, null));
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.IRecipeInput, java.util.Collection<net.minecraft.world.item.ItemStack>>> CENTRIFUGE = register(
      "centrifuge", new BasicMachineRecipeSerializer(Ic2RecipeTypes.CENTRIFUGE, intMeta("minHeat"))
   );
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.IRecipeInput, java.util.Collection<net.minecraft.world.item.ItemStack>>> BLOCK_CUTTER = register(
      "block_cutter", new BasicMachineRecipeSerializer(Ic2RecipeTypes.BLOCK_CUTTER, intMeta("hardness"))
   );
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.IRecipeInput, java.util.Collection<net.minecraft.world.item.ItemStack>>> BLAST_FURNACE = register(
      "blast_furnace", new BasicMachineRecipeSerializer(Ic2RecipeTypes.BLAST_FURNACE, twoIntsMeta("fluid", "duration"))
   );
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.IRecipeInput, java.util.Collection<net.minecraft.world.item.ItemStack>>> METAL_FORMER_EXTRUDING = register(
      "metal_former_extruding", new BasicMachineRecipeSerializer(Ic2RecipeTypes.METAL_FORMER_EXTRUDING, null)
   );
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.IRecipeInput, java.util.Collection<net.minecraft.world.item.ItemStack>>> METAL_FORMER_CUTTING = register(
      "metal_former_cutting", new BasicMachineRecipeSerializer(Ic2RecipeTypes.METAL_FORMER_CUTTING, null)
   );
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.IRecipeInput, java.util.Collection<net.minecraft.world.item.ItemStack>>> METAL_FORMER_ROLLING = register(
      "metal_former_rolling", new BasicMachineRecipeSerializer(Ic2RecipeTypes.METAL_FORMER_ROLLING, null)
   );
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.IRecipeInput, java.util.Collection<net.minecraft.world.item.ItemStack>>> ORE_WASHER = register(
      "ore_washer", new BasicMachineRecipeSerializer(Ic2RecipeTypes.ORE_WASHER, intMeta("amount"))
   );
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.IRecipeInput, Integer>> MATTER_FABRICATOR = register(
      "matter_fabricator", new IntegerOutputRecipeSerializer(Ic2RecipeTypes.MATTER_FABRICATOR, null)
   );
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.ICannerBottleRecipeManager.Input, net.minecraft.world.item.ItemStack>> CANNER_BOTTLE = register("canner_bottle", new CannerBottleRecipeSerializer());
   public static final RecipeSerializer<ic2.core.recipe.v2.RecipeHolder<ic2.api.recipe.ICannerEnrichRecipeManager.Input, ic2.core.fluid.Ic2FluidStack>> CANNER_ENRICH = register("canner_enrich", new CannerEnrichRecipeSerializer());

   public static void init() {
   }

   private static <H extends net.minecraft.world.item.crafting.Recipe<?>> RecipeSerializer<H> register(String name, ic2.core.recipe.v2.Ic2RecipeCodecSource<H> source) {
      RecipeSerializer<H> serializer = new RecipeSerializer<>(source.codec(), source.streamCodec());
      source.attachSerializerRecord(serializer);
      IC2.envProxy.registerRecipeSerializer(IC2.getIdentifier(name), serializer);
      return serializer;
   }

   private static <T extends net.minecraft.world.item.crafting.Recipe<?>> RecipeSerializer<T> register(String name, RecipeSerializer<T> serializer) {
      IC2.envProxy.registerRecipeSerializer(IC2.getIdentifier(name), serializer);
      return serializer;
   }

   private static Function<JsonObject, CompoundTag> intMeta(String name) {
      return json -> {
         int metaValue = GsonHelper.getAsInt(json, name);
         CompoundTag nbt = new CompoundTag();
         nbt.putInt(name, metaValue);
         return nbt;
      };
   }

   private static Function<JsonObject, CompoundTag> twoIntsMeta(String name1, String name2) {
      return json -> {
         int metaValue1 = GsonHelper.getAsInt(json, name1);
         int metaValue2 = GsonHelper.getAsInt(json, name2);
         CompoundTag nbt = new CompoundTag();
         nbt.putInt(name1, metaValue1);
         nbt.putInt(name2, metaValue2);
         return nbt;
      };
   }
}
