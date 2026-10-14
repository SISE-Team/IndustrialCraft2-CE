package ic2ce.neoforge.recipe.codec;

import com.google.gson.*;
import com.mojang.serialization.*;
import ic2ce.api.recipe.*;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;

/**
 * One native codec bridge preserves pending lookup context and implements both decoding and
 * encoding.
 */
public abstract class NativeRecipeCodec<H extends Recipe<?>> {
  private RecipeSerializer<H> serializer;
  private final MapCodec<H> mapCodec = new JsonBridge();
  private final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, H> wireCodec =
      net.minecraft.network.codec.StreamCodec.of(this::writeNetwork, this::readNetwork);

  public final void attachSerializerRecord(RecipeSerializer<H> serializer) {
    Objects.requireNonNull(serializer);
    if (this.serializer != null && this.serializer != serializer)
      throw new IllegalStateException("Recipe serializer already attached");
    this.serializer = serializer;
  }

  protected final RecipeSerializer<H> serializerRecord() {
    return Objects.requireNonNull(serializer, "Recipe serializer not attached");
  }

  protected static Identifier placeholderId() {
    return ProjectRuntime.getIdentifier("decoded");
  }

  protected abstract H readJson(JsonObject json) throws Exception;

  protected H readJson(JsonObject json, DynamicOps<JsonElement> ops) throws Exception {
    return readJson(json);
  }

  protected abstract H readNetwork(RegistryFriendlyByteBuf buffer);

  protected abstract void writeNetwork(RegistryFriendlyByteBuf buffer, H recipe);

  protected JsonObject writeJson(H recipe) {
    if (!(recipe instanceof RecipeHolder<?, ?> holder))
      throw new IllegalArgumentException(
          "Recipe does not define JSON encoding: " + recipe.getClass().getName());
    var value = holder.recipe();
    var json = new JsonObject();
    switch (value.getInput()) {
      case IRecipeInput input -> json.add("ingredient", RecipeValueCodec.inputToJson(input));
      case ICannerBottleRecipeManager.Input input -> {
        json.add("container_ingredient", RecipeValueCodec.inputToJson(input.container));
        json.add("fill_ingredient", RecipeValueCodec.inputToJson(input.fill));
      }
      case ICannerEnrichRecipeManager.Input input -> {
        json.add("input_ingredient", RecipeValueCodec.fluidStackToJson(input.fluid));
        json.add("additive_ingredient", RecipeValueCodec.inputToJson(input.additive));
      }
      default -> throw new IllegalArgumentException("Unknown machine input value");
    }
    if (value instanceof MachineRecipeWeighted<?> weighted) {
      var array = new JsonArray();
      weighted
          .getOutputWeighted()
          .forEach(
              (stack, weight) -> {
                var entry = RecipeValueCodec.resultToJson(stack);
                entry.addProperty("weight", weight);
                array.add(entry);
              });
      json.add("result", array);
      json.addProperty("weighted", true);
    } else
      switch (value.getOutput()) {
        case ItemStack stack -> json.add("result", RecipeValueCodec.resultToJson(stack));
        case ic2ce.api.fluid.Ic2FluidStack fluid ->
            json.add("result", RecipeValueCodec.fluidStackToJson(fluid));
        case Number amount -> json.addProperty("result", amount);
        case java.util.Collection<?> outputs -> {
          var array = new JsonArray();
          for (var item : outputs) {
            if (!(item instanceof ItemStack stack))
              throw new IllegalArgumentException("Unknown recipe output element");
            array.add(RecipeValueCodec.resultToJson(stack));
          }
          json.add("result", array);
        }
        default -> throw new IllegalArgumentException("Unknown recipe output value");
      }
    var metadata = value.getMetaData();
    if (metadata != null) {
      var encoded =
          net.minecraft.nbt.NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, metadata).getAsJsonObject();
      encoded
          .entrySet()
          .forEach(
              entry -> {
                if (!json.has(entry.getKey())) json.add(entry.getKey(), entry.getValue());
              });
    }
    return json;
  }

  public final MapCodec<H> codec() {
    return mapCodec;
  }

  public final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, H> streamCodec() {
    return wireCodec;
  }

  protected static JsonElement require(JsonObject json, String key) {
    var value = json.get(key);
    if (value == null || value.isJsonNull())
      throw new JsonParseException("Missing recipe field " + key);
    return value;
  }

  protected static void outputKind(RegistryFriendlyByteBuf buffer, int expected) {
    int actual = buffer.readUnsignedByte();
    if (actual != expected)
      throw new IllegalArgumentException("Invalid recipe output discriminator " + actual);
  }

  private final class JsonBridge extends MapCodec<H> {
    @Override
    public <T> DataResult<H> decode(DynamicOps<T> ops, MapLike<T> input) {
      try {
        var json = new JsonObject();
        input
            .entries()
            .forEach(
                entry -> {
                  if (json.size() >= 256)
                    throw new IllegalArgumentException("Too many recipe fields");
                  json.add(
                      ops.getStringValue(entry.getFirst()).getOrThrow(),
                      ops.convertTo(JsonOps.INSTANCE, entry.getSecond()));
                });
        DynamicOps<JsonElement> contextual =
            ops instanceof RegistryOps<?> registries
                ? registries.withParent(JsonOps.INSTANCE)
                : JsonOps.INSTANCE;
        return DataResult.success(Objects.requireNonNull(readJson(json, contextual)));
      } catch (Exception error) {
        return DataResult.error(() -> "Invalid IC2 recipe: " + error.getMessage());
      }
    }

    @Override
    public <T> RecordBuilder<T> encode(H value, DynamicOps<T> ops, RecordBuilder<T> prefix) {
      try {
        for (var entry : writeJson(value).entrySet())
          prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
        return prefix;
      } catch (Exception error) {
        return prefix.withErrorsFrom(
            DataResult.error(() -> "Cannot encode IC2 recipe: " + error.getMessage()));
      }
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
      return Stream.of(
              "ingredient",
              "result",
              "weighted",
              "container_ingredient",
              "fill_ingredient",
              "input_ingredient",
              "additive_ingredient",
              "ingredients",
              "key",
              "pattern",
              "hidden",
              "consuming",
              "assemblyMask")
          .map(ops::createString);
    }
  }
}
