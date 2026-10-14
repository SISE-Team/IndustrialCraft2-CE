package ic2ce.neoforge.registry;

import com.google.gson.JsonObject;
import ic2ce.neoforge.recipe.codec.CollectionRecipeCodec;
import ic2ce.neoforge.recipe.codec.EnrichmentCodec;
import ic2ce.neoforge.recipe.codec.QuantityRecipeCodec;
import ic2ce.neoforge.recipe.codec.SolidCanningCodec;
import ic2ce.neoforge.recipe.codec.WeightedRecipeCodec;
import ic2ce.neoforge.recipe.crafting.PositionedCrafting;
import ic2ce.neoforge.recipe.crafting.UnorderedCrafting;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class RecipeCodecs {
  public static final RecipeSerializer<PositionedCrafting> SHAPED =
      register("shaped", new PositionedCrafting.Serializer());
  public static final RecipeSerializer<UnorderedCrafting> SHAPELESS =
      register("shapeless", new UnorderedCrafting.Serializer());
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.IRecipeInput,
              java.util.Collection<net.minecraft.world.item.ItemStack>>>
      MACERATOR = register("macerator", new WeightedRecipeCodec(RecipeKinds.MACERATOR, null));
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.IRecipeInput,
              java.util.Collection<net.minecraft.world.item.ItemStack>>>
      EXTRACTOR = register("extractor", new CollectionRecipeCodec(RecipeKinds.EXTRACTOR, null));
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.IRecipeInput,
              java.util.Collection<net.minecraft.world.item.ItemStack>>>
      COMPRESSOR = register("compressor", new CollectionRecipeCodec(RecipeKinds.COMPRESSOR, null));
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.IRecipeInput,
              java.util.Collection<net.minecraft.world.item.ItemStack>>>
      CENTRIFUGE =
          register(
              "centrifuge", new CollectionRecipeCodec(RecipeKinds.CENTRIFUGE, intMeta("minHeat")));
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.IRecipeInput,
              java.util.Collection<net.minecraft.world.item.ItemStack>>>
      BLOCK_CUTTER =
          register(
              "block_cutter",
              new CollectionRecipeCodec(RecipeKinds.BLOCK_CUTTER, intMeta("hardness")));
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.IRecipeInput,
              java.util.Collection<net.minecraft.world.item.ItemStack>>>
      BLAST_FURNACE =
          register(
              "blast_furnace",
              new CollectionRecipeCodec(
                  RecipeKinds.BLAST_FURNACE, twoIntsMeta("fluid", "duration")));
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.IRecipeInput,
              java.util.Collection<net.minecraft.world.item.ItemStack>>>
      METAL_FORMER_EXTRUDING =
          register(
              "metal_former_extruding",
              new CollectionRecipeCodec(RecipeKinds.METAL_FORMER_EXTRUDING, null));
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.IRecipeInput,
              java.util.Collection<net.minecraft.world.item.ItemStack>>>
      METAL_FORMER_CUTTING =
          register(
              "metal_former_cutting",
              new CollectionRecipeCodec(RecipeKinds.METAL_FORMER_CUTTING, null));
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.IRecipeInput,
              java.util.Collection<net.minecraft.world.item.ItemStack>>>
      METAL_FORMER_ROLLING =
          register(
              "metal_former_rolling",
              new CollectionRecipeCodec(RecipeKinds.METAL_FORMER_ROLLING, null));
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.IRecipeInput,
              java.util.Collection<net.minecraft.world.item.ItemStack>>>
      ORE_WASHER =
          register(
              "ore_washer", new CollectionRecipeCodec(RecipeKinds.ORE_WASHER, intMeta("amount")));
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<ic2ce.api.recipe.IRecipeInput, Integer>>
      MATTER_FABRICATOR =
          register(
              "matter_fabricator", new QuantityRecipeCodec(RecipeKinds.MATTER_FABRICATOR, null));
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.ICannerBottleRecipeManager.Input,
              net.minecraft.world.item.ItemStack>>
      CANNER_BOTTLE = register("canner_bottle", new SolidCanningCodec());
  public static final RecipeSerializer<
          ic2ce.neoforge.recipe.codec.RecipeHolder<
              ic2ce.api.recipe.ICannerEnrichRecipeManager.Input, ic2ce.api.fluid.Ic2FluidStack>>
      CANNER_ENRICH = register("canner_enrich", new EnrichmentCodec());

  public static void init() {}

  private static <H extends net.minecraft.world.item.crafting.Recipe<?>>
      RecipeSerializer<H> register(
          String name, ic2ce.neoforge.recipe.codec.NativeRecipeCodec<H> source) {
    RecipeSerializer<H> serializer = new RecipeSerializer<>(source.codec(), source.streamCodec());
    source.attachSerializerRecord(serializer);
    ProjectRuntime.envProxy.registerRecipeSerializer(
        ProjectRuntime.getIdentifier(name), serializer);
    return serializer;
  }

  private static <T extends net.minecraft.world.item.crafting.Recipe<?>>
      RecipeSerializer<T> register(String name, RecipeSerializer<T> serializer) {
    ProjectRuntime.envProxy.registerRecipeSerializer(
        ProjectRuntime.getIdentifier(name), serializer);
    return serializer;
  }

  private static Function<JsonObject, CompoundTag> intMeta(String name) {
    return integerFields(name);
  }

  private static Function<JsonObject, CompoundTag> twoIntsMeta(String first, String second) {
    return integerFields(first, second);
  }

  private static Function<JsonObject, CompoundTag> integerFields(String... fields) {
    var names = java.util.List.of(fields);
    return json -> {
      var values = new CompoundTag();
      for (var name : names) {
        var element = json.get(name);
        if (element == null
            || !element.isJsonPrimitive()
            || !element.getAsJsonPrimitive().isNumber())
          throw new com.google.gson.JsonParseException("Missing integer recipe field: " + name);
        try {
          int value = element.getAsBigDecimal().intValueExact();
          if (value < 0) throw new ArithmeticException("negative");
          values.putInt(name, value);
        } catch (ArithmeticException | NumberFormatException invalid) {
          throw new com.google.gson.JsonParseException(
              "Recipe field must be a nonnegative exact integer: " + name, invalid);
        }
      }
      return values;
    };
  }
}
