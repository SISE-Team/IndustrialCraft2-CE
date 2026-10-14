package ic2ce.neoforge.recipe.codec;

import com.google.gson.*;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.*;
import ic2ce.neoforge.recipe.input.AlternativeInputs;
import ic2ce.neoforge.recipe.input.ExactStackInput;
import ic2ce.neoforge.recipe.input.FluidContentsIngredient;
import ic2ce.neoforge.recipe.input.FluidContentsInput;
import ic2ce.neoforge.recipe.input.InputDescriptor;
import ic2ce.neoforge.recipe.input.MaterialTagIngredient;
import ic2ce.neoforge.recipe.input.NativeIngredientInput;
import java.util.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

/** Bounded native JSON and wire protocols keep exact components, quantities and alternatives. */
public final class RecipeValueCodec {
  private static final int MAX_ENTRIES = 4096, MAX_DEPTH = 64;

  private RecipeValueCodec() {}

  public static DynamicOps<JsonElement> nativeOps() {
    var lookup =
        ic2ce.neoforge.runtime.ProjectRuntime.envProxy == null
            ? null
            : ic2ce.neoforge.runtime.ProjectRuntime.envProxy.getRegistryLookup();
    if (lookup == null)
      lookup = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    return lookup.createSerializationContext(JsonOps.INSTANCE);
  }

  public static JsonElement inputToJson(IRecipeInput input) {
    if (input instanceof DeferredInput lazy) return inputToJson(lazy.unwrap());
    if (input instanceof InputDescriptor descriptor) return descriptor.toJson();
    var object = new JsonObject();
    object.add(
        "ingredient",
        Ingredient.CODEC
            .encodeStart(nativeOps(), input.getIngredient())
            .getOrThrow(IllegalStateException::new));
    object.addProperty("count", input.getAmount());
    return object;
  }

  public static IRecipeInput parseInput(JsonElement json) {
    JsonElement owned = Objects.requireNonNull(json).deepCopy();
    return new DeferredInput(() -> parseInputEager(owned, 0));
  }

  public static IRecipeInput parseCraftingInput(JsonElement json, DynamicOps<JsonElement> ops) {
    JsonElement owned = Objects.requireNonNull(json).deepCopy();
    Ingredient placement = parseCraftingIngredient(owned, ops, 0);
    return new DeferredInput(() -> parseInputEager(owned, 0), placement);
  }

  private static void depth(int depth) {
    if (depth > MAX_DEPTH)
      throw new IllegalArgumentException("Recipe input nesting exceeds " + MAX_DEPTH);
  }

  private static int count(JsonObject json) {
    return InputDescriptor.positive(GsonHelper.getAsInt(json, "count", 1));
  }

  private static Ingredient parseCraftingIngredient(
      JsonElement json, DynamicOps<JsonElement> ops, int level) {
    depth(level);
    if (json.isJsonArray()) {
      var array = json.getAsJsonArray();
      entries(array.size());
      var inputs = new ArrayList<Ingredient>(array.size());
      for (var child : array) inputs.add(parseCraftingIngredient(child, ops, level + 1));
      return CompoundIngredient.of(inputs.toArray(Ingredient[]::new));
    }
    if (json.isJsonObject()) {
      var object = json.getAsJsonObject();
      count(object);
      if (object.has("fluid"))
        return new FluidContentsIngredient(
                Identifier.parse(GsonHelper.getAsString(object, "fluid")),
                positiveField(object, "amount"))
            .toVanilla();
      if (object.has("any")) return parseCraftingIngredient(object.get("any"), ops, level + 1);
      if (object.has("ingredient"))
        return Ingredient.CODEC
            .parse(ops, object.get("ingredient"))
            .getOrThrow(JsonSyntaxException::new);
      if (object.has("tag"))
        return new MaterialTagIngredient(Identifier.parse(GsonHelper.getAsString(object, "tag")))
            .toVanilla();
      if (object.has("item")) {
        var item = item(object);
        if (object.has("components") || object.has("nbt") || object.has("data")) {
          var template = new ItemStackTemplate(item, 1, patch(object, ops));
          return DataComponentIngredient.of(true, template);
        }
        return Ingredient.of(item);
      }
    }
    if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
      String value = json.getAsString();
      if (value.startsWith("#"))
        return new MaterialTagIngredient(Identifier.parse(value.substring(1))).toVanilla();
    }
    return Ingredient.CODEC.parse(ops, json).getOrThrow(JsonSyntaxException::new);
  }

  private static IRecipeInput parseInputEager(JsonElement json, int level) {
    depth(level);
    if (json.isJsonArray()) return parseMultiple(json.getAsJsonArray(), 1, level + 1);
    if (json.isJsonObject()) {
      var object = json.getAsJsonObject();
      int amount = count(object);
      if (object.has("fluid"))
        return new FluidContentsInput(fluid(object.get("fluid")), positiveField(object, "amount"));
      if (object.has("any")) {
        if (!object.get("any").isJsonArray())
          throw new JsonSyntaxException("Recipe alternatives must be an array");
        return parseMultiple(object.getAsJsonArray("any"), amount, level + 1);
      }
      if (object.has("item")
          && (object.has("components") || object.has("nbt") || object.has("data"))) {
        var stack = new ItemStack(item(object), amount);
        stack.applyComponents(patch(object, nativeOps()));
        return new ExactStackInput(stack);
      }
      return new NativeIngredientInput(parseCraftingIngredient(object, nativeOps(), level), amount);
    }
    return new NativeIngredientInput(parseCraftingIngredient(json, nativeOps(), level), 1);
  }

  private static AlternativeInputs parseMultiple(JsonArray array, int count, int level) {
    depth(level);
    entries(array.size());
    var inputs = new ArrayList<IRecipeInput>(array.size());
    for (var child : array) inputs.add(parseInputEager(child, level + 1));
    return new AlternativeInputs(count, inputs);
  }

  private static Item item(JsonObject json) {
    var name = Identifier.parse(GsonHelper.getAsString(json, "item"));
    var item =
        BuiltInRegistries.ITEM
            .getOptional(name)
            .orElseThrow(() -> new JsonSyntaxException("Unknown item " + name));
    if (item == Items.AIR) throw new JsonSyntaxException("Air is not a physical recipe item");
    return item;
  }

  private static Fluid fluid(JsonElement json) {
    if (json == null || !json.isJsonPrimitive())
      throw new JsonSyntaxException("Recipe fluid must be a registry identifier");
    var name = Identifier.parse(json.getAsString());
    return BuiltInRegistries.FLUID
        .getOptional(name)
        .orElseThrow(() -> new JsonSyntaxException("Unknown fluid " + name));
  }

  private static int positiveField(JsonObject object, String key) {
    return InputDescriptor.positive(GsonHelper.getAsInt(object, key));
  }

  private static CompoundTag compound(JsonElement json) {
    if (json == null || !json.isJsonObject())
      throw new JsonSyntaxException("Recipe custom data must be an object");
    var converted = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
    if (converted instanceof CompoundTag compound) return compound;
    throw new JsonSyntaxException("Recipe custom data is not an NBT compound");
  }

  private static DataComponentPatch patch(JsonObject json, DynamicOps<JsonElement> ops) {
    var builder = DataComponentPatch.builder();
    if (json.has("components")) {
      var patch =
          DataComponentPatch.CODEC
              .parse(ops, json.get("components"))
              .getOrThrow(JsonSyntaxException::new);
      for (var entry : patch.entrySet()) copyComponent(builder, entry.getKey(), entry.getValue());
    }
    if (json.has("nbt") || json.has("data"))
      builder.set(
          DataComponents.CUSTOM_DATA,
          CustomData.of(compound(json.get(json.has("nbt") ? "nbt" : "data"))));
    return builder.build();
  }

  private static <T> void copyComponent(
      DataComponentPatch.Builder builder,
      net.minecraft.core.component.DataComponentType<T> type,
      Optional<?> value) {
    if (value.isEmpty()) builder.remove(type);
    else builder.set(type, (T) value.get());
  }

  public static ItemStackTemplate parseOutputTemplate(JsonObject json) {
    return parseOutputTemplate(json, nativeOps());
  }

  public static ItemStackTemplate parseOutputTemplate(
      JsonObject json, DynamicOps<JsonElement> ops) {
    return new ItemStackTemplate(item(json), count(json), patch(json, ops));
  }

  public static ItemStack parseOutput(JsonObject json) {
    var result = new ItemStack(item(json), count(json));
    result.applyComponents(patch(json, nativeOps()));
    return result;
  }

  public static Collection<ItemStack> parseOutputs(JsonElement json, String name) {
    var result = new ArrayList<ItemStack>();
    appendOutputs(json, result, 0);
    return List.copyOf(result);
  }

  private static void appendOutputs(JsonElement json, List<ItemStack> output, int level) {
    depth(level);
    if (json.isJsonArray()) {
      entries(json.getAsJsonArray().size());
      for (var child : json.getAsJsonArray()) appendOutputs(child, output, level + 1);
    } else if (json.isJsonObject()) {
      output.add(parseOutput(json.getAsJsonObject()));
      entries(output.size());
    } else throw new JsonSyntaxException("Recipe outputs must be objects or arrays");
  }

  public static void parseWeightedOutput(JsonObject json, RecipeOutputWeighted output) {
    output.addOutput(
        parseOutput(json), InputDescriptor.positive(GsonHelper.getAsInt(json, "weight", 1)));
  }

  public static void parseWeightedOutputs(
      JsonElement json, String name, RecipeOutputWeighted output) {
    appendWeighted(json, output, 0);
  }

  private static void appendWeighted(JsonElement json, RecipeOutputWeighted output, int level) {
    depth(level);
    if (json.isJsonArray()) {
      entries(json.getAsJsonArray().size());
      for (var child : json.getAsJsonArray()) appendWeighted(child, output, level + 1);
    } else if (json.isJsonObject()) {
      entries(output.getWeights().size() + 1);
      parseWeightedOutput(json.getAsJsonObject(), output);
    } else throw new JsonSyntaxException("Weighted recipe outputs must be objects or arrays");
  }

  public static Ic2FluidStack parseFluidStack(JsonObject json) {
    var fluid = fluid(json.get("fluid"));
    int amount = positiveField(json, "amount");
    if (!json.has("components") && !json.has("nbt")) return Ic2FluidStack.create(fluid, amount);
    var data = new CompoundTag();
    data.putString("FluidName", BuiltInRegistries.FLUID.getKey(fluid).toString());
    data.putInt("Amount", amount);
    if (json.has("components"))
      data.put("Components", JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json.get("components")));
    if (json.has("nbt")) data.put("Tag", compound(json.get("nbt")));
    var value = Ic2FluidStack.read(data);
    if (value == null) throw new JsonSyntaxException("Invalid native fluid components");
    return value;
  }

  public static JsonObject resultToJson(ItemStack stack) {
    Objects.requireNonNull(stack);
    if (stack.isEmpty()) throw new IllegalArgumentException("Cannot serialize empty recipe stack");
    var object = new JsonObject();
    object.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    if (stack.getCount() != 1) object.addProperty("count", stack.getCount());
    object.add(
        "components",
        DataComponentPatch.CODEC
            .encodeStart(nativeOps(), stack.getComponentsPatch())
            .getOrThrow(IllegalStateException::new));
    return object;
  }

  public static JsonObject fluidStackToJson(Ic2FluidStack stack) {
    var object = new JsonObject();
    object.addProperty("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
    object.addProperty("amount", stack.getAmountMb());
    var data = new CompoundTag();
    stack.toNbt(data);
    if (data.contains("Components"))
      object.add("components", NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, data.get("Components")));
    else if (data.contains("Tag"))
      object.add("nbt", NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, data.get("Tag")));
    return object;
  }

  private static RegistryFriendlyByteBuf registry(FriendlyByteBuf buffer) {
    if (buffer instanceof RegistryFriendlyByteBuf nativeBuffer) return nativeBuffer;
    throw new IllegalArgumentException("Recipe synchronization requires native registry lookup");
  }

  private static int entries(int amount) {
    if (amount < 0 || amount > MAX_ENTRIES)
      throw new IllegalArgumentException("Invalid recipe entry count " + amount);
    return amount;
  }

  public static void writeInput(FriendlyByteBuf buffer, IRecipeInput input) {
    writeInput(buffer, Objects.requireNonNull(input), 0);
  }

  private static void writeInput(FriendlyByteBuf buffer, IRecipeInput input, int level) {
    depth(level);
    if (input instanceof DeferredInput lazy) {
      writeInput(buffer, lazy.unwrap(), level + 1);
      return;
    }
    switch (input) {
      case FluidContentsInput container -> {
        buffer.writeByte(0);
        buffer.writeVarInt(
            BuiltInRegistries.FLUID.getId(
                container.fluid == null ? Fluids.EMPTY : container.fluid));
        buffer.writeVarInt(container.amount);
      }
      case NativeIngredientInput nativeInput -> {
        buffer.writeByte(1);
        Ingredient.CONTENTS_STREAM_CODEC.encode(registry(buffer), nativeInput.getIngredient());
        buffer.writeVarInt(nativeInput.getAmount());
      }
      case ExactStackInput exact -> {
        buffer.writeByte(2);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registry(buffer), exact.prototype());
      }
      case AlternativeInputs multiple -> {
        buffer.writeByte(3);
        buffer.writeVarInt(entries(multiple.alternatives().size()));
        for (var child : multiple.alternatives()) writeInput(buffer, child, level + 1);
        buffer.writeVarInt(multiple.getAmount());
      }
      default -> {
        buffer.writeByte(1);
        Ingredient.CONTENTS_STREAM_CODEC.encode(registry(buffer), input.getIngredient());
        buffer.writeVarInt(InputDescriptor.positive(input.getAmount()));
      }
    }
  }

  public static IRecipeInput readInput(FriendlyByteBuf buffer) {
    return readInput(buffer, 0);
  }

  private static IRecipeInput readInput(FriendlyByteBuf buffer, int level) {
    depth(level);
    return switch (buffer.readUnsignedByte()) {
      case 0 ->
          new FluidContentsInput(
              networkFluid(buffer), InputDescriptor.positive(buffer.readVarInt()));
      case 1 ->
          new NativeIngredientInput(
              Ingredient.CONTENTS_STREAM_CODEC.decode(registry(buffer)),
              InputDescriptor.positive(buffer.readVarInt()));
      case 2 -> new ExactStackInput(ItemStack.OPTIONAL_STREAM_CODEC.decode(registry(buffer)));
      case 3 -> {
        int size = entries(buffer.readVarInt());
        var alternatives = new ArrayList<IRecipeInput>(size);
        for (int index = 0; index < size; index++) alternatives.add(readInput(buffer, level + 1));
        yield new AlternativeInputs(InputDescriptor.positive(buffer.readVarInt()), alternatives);
      }
      default -> throw new IllegalArgumentException("Unknown recipe input discriminator");
    };
  }

  private static Fluid networkFluid(FriendlyByteBuf buffer) {
    int id = buffer.readVarInt();
    if (id < 0 || id >= BuiltInRegistries.FLUID.size())
      throw new IllegalArgumentException("Unknown fluid registry id " + id);
    return BuiltInRegistries.FLUID.byId(id);
  }

  public static void writeOutput(FriendlyByteBuf buffer, Collection<ItemStack> output) {
    buffer.writeByte(0);
    buffer.writeVarInt(entries(output.size()));
    for (var stack : output) ItemStack.OPTIONAL_STREAM_CODEC.encode(registry(buffer), stack);
  }

  // Output discriminators are consumed by their recipe serializers before these readers.
  public static Collection<ItemStack> readOutput(FriendlyByteBuf buffer) {
    int size = entries(buffer.readVarInt());
    var result = new ArrayList<ItemStack>(size);
    for (int index = 0; index < size; index++)
      result.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(registry(buffer)));
    return List.copyOf(result);
  }

  public static void writeIntegerOutput(FriendlyByteBuf buffer, int output) {
    buffer.writeByte(0);
    buffer.writeInt(output);
  }

  public static Integer readIntegerOutput(FriendlyByteBuf buffer) {
    return buffer.readInt();
  }

  public static void writeWeightedOutput(FriendlyByteBuf buffer, RecipeOutputWeighted output) {
    buffer.writeByte(1);
    buffer.writeVarInt(entries(output.getWeights().size()));
    output.forEach(
        (stack, weight) -> {
          ItemStack.OPTIONAL_STREAM_CODEC.encode(registry(buffer), stack);
          buffer.writeInt(weight);
        });
  }

  public static RecipeOutputWeighted readWeightedOutput(
      FriendlyByteBuf buffer, RecipeOutputWeighted output) {
    int size = entries(buffer.readVarInt());
    for (int index = 0; index < size; index++)
      output.addOutput(
          ItemStack.OPTIONAL_STREAM_CODEC.decode(registry(buffer)),
          InputDescriptor.positive(buffer.readInt()));
    return output;
  }

  public static void writeFluidStack(FriendlyByteBuf buffer, Ic2FluidStack stack) {
    buffer.writeVarInt(BuiltInRegistries.FLUID.getId(stack.getFluid()));
    buffer.writeVarInt(stack.getAmountMb());
    var data = new CompoundTag();
    stack.toNbt(data);
    buffer.writeNbt(data);
  }

  public static Ic2FluidStack readFluidStack(FriendlyByteBuf buffer) {
    var fluid = networkFluid(buffer);
    int amount = buffer.readVarInt();
    if (amount < 0) throw new IllegalArgumentException("Negative fluid quantity");
    var data = buffer.readNbt();
    if (data == null) throw new IllegalArgumentException("Missing native fluid value");
    var value = Ic2FluidStack.read(data);
    if (value == null || value.getFluid() != fluid || value.getAmountMb() != amount)
      throw new IllegalArgumentException("Native fluid metadata disagrees with the physical bill");
    return value;
  }
}
