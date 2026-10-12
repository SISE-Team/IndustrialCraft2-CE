package ic2.core.recipe.v2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeOutputWeighted;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.recipe.input.RecipeInputFluidContainer;
import ic2.core.recipe.input.RecipeInputIngredient;
import ic2.core.recipe.input.RecipeInputItemStack;
import ic2.core.recipe.input.RecipeInputMultiple;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;

public class RecipeIo {
   /** Parses an IC2 recipe input lazily (26.1.2: datapack recipes load before components/tags bind). */
   public static IRecipeInput parseInput(JsonElement json) {
      return new LazyRecipeInput(() -> parseInputEager(json));
   }

   /** Vanilla book metadata uses holders/templates, which are valid before components bind. */
   public static IRecipeInput parseCraftingInput(JsonElement json, com.mojang.serialization.DynamicOps<JsonElement> ops) {
      if (json.isJsonObject() && json.getAsJsonObject().has("fluid")) {
         JsonObject obj = json.getAsJsonObject();
         Ingredient ingredient = new ic2.core.recipe.input.FluidContainerIngredient(
            Identifier.parse(GsonHelper.getAsString(obj, "fluid")), GsonHelper.getAsInt(obj, "amount")
         ).toVanilla();
         return new LazyRecipeInput(() -> parseInputEager(json), ingredient);
      }
      Ingredient ingredient = parseCraftingIngredient(json, ops);
      int count = json.isJsonObject() ? GsonHelper.getAsInt(json.getAsJsonObject(), "count", 1) : 1;
      return new LazyRecipeInput(() -> new RecipeInputIngredient(ingredient, count), ingredient);
   }

   private static Ingredient parseCraftingIngredient(JsonElement json, com.mojang.serialization.DynamicOps<JsonElement> ops) {
      JsonElement value = json;
      if (json.isJsonObject()) {
         JsonObject obj = json.getAsJsonObject();
         if (obj.has("any")) {
            value = obj.get("any");
         } else if (obj.has("item")) {
            value = new com.google.gson.JsonPrimitive(GsonHelper.getAsString(obj, "item"));
         } else if (obj.has("tag")) {
            value = new com.google.gson.JsonPrimitive("#" + GsonHelper.getAsString(obj, "tag"));
         }
      }
      if (value.isJsonArray()) {
         List<Ingredient> alternatives = new ArrayList<>();
         for (JsonElement alternative : value.getAsJsonArray()) {
            alternatives.add(parseCraftingIngredient(alternative, ops));
         }
         return net.neoforged.neoforge.common.crafting.CompoundIngredient.of(alternatives.toArray(Ingredient[]::new));
      }
      return Ingredient.CODEC.parse(ops, value).getOrThrow(JsonSyntaxException::new);
   }

   public static net.minecraft.world.item.ItemStackTemplate parseOutputTemplate(JsonObject json) {
      Item item = GsonHelper.getAsItem(json, "item").value();
      int count = GsonHelper.getAsInt(json, "count", 1);
      net.minecraft.core.component.DataComponentPatch.Builder patch = net.minecraft.core.component.DataComponentPatch.builder();
      CompoundTag nbt = getNbt(json, "nbt", null);
      if (nbt != null) {
         patch.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(nbt));
      }
      return new net.minecraft.world.item.ItemStackTemplate(item, count, patch.build());
   }

   private static IRecipeInput parseInputEager(JsonElement json) {
      if (json.isJsonArray()) {
         return parseMultiple(json.getAsJsonArray(), 1);
      } else {
         int count = 1;
         if (json.isJsonObject()) {
            count = GsonHelper.getAsInt(json.getAsJsonObject(), "count", 1);
         }

         if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (object.has("fluid")) {
               return new RecipeInputFluidContainer(asFluid(object.get("fluid"), "fluid"), GsonHelper.getAsInt(object, "amount"));
            }

            if (object.has("nbt")) {
               Item item = GsonHelper.getAsItem(object, "item").value();
               ItemStack stack = new ItemStack(item, count);
               ic2.core.util.StackTag.set(stack, asNbt(object.get("data"), "data"));
               return new RecipeInputItemStack(stack);
            }

            if (object.has("any")) {
               JsonElement entry = object.get("any");
               if (!entry.isJsonArray()) {
                  throw new JsonSyntaxException("\"any\" IC2 ingredient entry must be an array, was " + GsonHelper.getType(entry));
               }

               return parseMultiple(entry.getAsJsonArray(), count);
            }
         }

         return new RecipeInputIngredient(parseIngredient(json), count);
      }
   }

   private static RecipeInputMultiple parseMultiple(JsonArray array, int count) {
      IRecipeInput[] inputs = new IRecipeInput[array.size()];

      for (int i = 0; i < array.size(); i++) {
         inputs[i] = parseInput(array.get(i));
      }

      return new RecipeInputMultiple(count, inputs);
   }

   public static Ic2FluidStack parseFluidStack(JsonObject json) {
      Fluid fluid = asFluid(json.get("fluid"), "fluid");
      int amountMb = GsonHelper.getAsInt(json, "amount");
      return FluidHandler.createFluidStackMb(fluid, amountMb, null);
   }

   public static Collection<ItemStack> parseOutputs(JsonElement json, String name) {
      if (!json.isJsonArray()) {
         if (json.isJsonObject()) {
            return List.of(parseOutput(json.getAsJsonObject()));
         } else {
            throw new JsonSyntaxException("Expected " + name + " to be an array or object for output parsing.");
         }
      } else {
         JsonArray array = json.getAsJsonArray();
         ArrayList<ItemStack> output = new ArrayList<>(array.size());

         for (int i = 0; i < array.size(); i++) {
            output.addAll(parseOutputs(array.get(i), "array element"));
         }

         return output;
      }
   }

   public static void parseWeightedOutputs(JsonElement json, String name, RecipeOutputWeighted randomOutput) {
      if (json.isJsonArray()) {
         JsonArray array = json.getAsJsonArray();

         for (int i = 0; i < array.size(); i++) {
            parseWeightedOutputs(array.get(i), "array element", randomOutput);
         }
      } else {
         if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected " + name + " to be an array or object for output parsing.");
         }

         parseWeightedOutput(json.getAsJsonObject(), randomOutput);
      }
   }

   public static ItemStack parseOutput(JsonObject json) {
      Item item = GsonHelper.getAsItem(json, "item").value();
      int count = GsonHelper.getAsInt(json, "count", 1);
      CompoundTag nbt = getNbt(json, "nbt", null);
      ItemStack stack = new ItemStack(item, count);
      ic2.core.util.StackTag.set(stack, nbt);
      return stack;
   }

   public static void parseWeightedOutput(JsonObject json, RecipeOutputWeighted randomOutput) {
      Item item = GsonHelper.getAsItem(json, "item").value();
      int count = GsonHelper.getAsInt(json, "count", 1);
      int weight = GsonHelper.getAsInt(json, "weight", 1);
      CompoundTag nbt = getNbt(json, "nbt", null);
      ItemStack stack = new ItemStack(item, count);
      ic2.core.util.StackTag.set(stack, nbt);
      randomOutput.addOutput(stack, weight);
   }

   public static JsonObject resultToJson(ItemStack stack) {
      JsonObject json = new JsonObject();
      json.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
      if (stack.getCount() != 1) {
         json.addProperty("count", stack.getCount());
      }

      return json;
   }

   public static JsonObject fluidStackToJson(Ic2FluidStack stack) {
      JsonObject json = new JsonObject();
      json.addProperty("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
      json.addProperty("amount", stack.getAmountMb());
      return json;
   }

   public static void writeInput(FriendlyByteBuf buf, IRecipeInput input) {
      if (input instanceof LazyRecipeInput lazy) {
         writeInput(buf, lazy.unwrap());
      } else if (input instanceof RecipeInputFluidContainer fluidContainer) {
         buf.writeByte(0);
         buf.writeVarInt(BuiltInRegistries.FLUID.getId(fluidContainer.fluid));
         buf.writeVarInt(fluidContainer.amount);
      } else if (input instanceof RecipeInputIngredient ingredient) {
         buf.writeByte(1);
         Ingredient.CONTENTS_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf)buf, ingredient.getIngredient());
         buf.writeVarInt(ingredient.getAmount());
      } else if (input instanceof RecipeInputItemStack stack) {
         buf.writeByte(2);
         net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf)buf, stack.input);
      } else {
         if (!(input instanceof RecipeInputMultiple mult)) {
            throw new IllegalArgumentException("Unkown RecipeInput type: " + input.getClass().getName());
         }

         buf.writeByte(3);
         buf.writeVarInt(mult.inputs.length);

         for (IRecipeInput i : mult.inputs) {
            writeInput(buf, i);
         }

         buf.writeVarInt(mult.getAmount());
      }
   }

   public static IRecipeInput readInput(FriendlyByteBuf buf) {
      return (IRecipeInput)(switch (buf.readByte()) {
         case 0 -> new RecipeInputFluidContainer((Fluid)BuiltInRegistries.FLUID.byId(buf.readVarInt()), buf.readVarInt());
         case 1 -> new RecipeInputIngredient(Ingredient.CONTENTS_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf)buf), buf.readVarInt());
         case 2 -> new RecipeInputItemStack(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf)buf));
         case 3 -> {
            IRecipeInput[] inputs = new IRecipeInput[buf.readVarInt()];

            for (int i = 0; i < inputs.length; i++) {
               inputs[i] = readInput(buf);
            }

            yield new RecipeInputMultiple(buf.readVarInt(), inputs);
         }
         default -> throw new IllegalArgumentException("Unkown RecipeInput type.");
      });
   }

   public static void writeOutput(FriendlyByteBuf buf, Collection<ItemStack> output) {
      buf.writeByte(0);
      buf.writeVarInt(output.size());

      for (ItemStack stack : output) {
         net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf)buf, stack);
      }
   }

   public static void writeIntegerOutput(FriendlyByteBuf buf, int output) {
      buf.writeByte(0);
      buf.writeInt(output);
   }

   public static void writeWeightedOutput(FriendlyByteBuf buf, RecipeOutputWeighted outputs) {
      buf.writeByte(1);
      buf.writeVarInt(outputs.getOutputs().size());
      outputs.forEach((stack, weight) -> {
         net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf)buf, stack);
         buf.writeInt(weight);
      });
   }

   public static Collection<ItemStack> readOutput(FriendlyByteBuf buf) {
      int amount = buf.readVarInt();
      List<ItemStack> stacks = new ArrayList<>(amount);

      for (int i = 0; i < amount; i++) {
         stacks.add(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf)buf));
      }

      return stacks;
   }

   public static Integer readIntegerOutput(FriendlyByteBuf buf) {
      return buf.readInt();
   }

   public static RecipeOutputWeighted readWeightedOutput(FriendlyByteBuf buf, RecipeOutputWeighted outputs) {
      int amount = buf.readVarInt();

      for (int i = 0; i < amount; i++) {
         outputs.addOutput(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf)buf), buf.readInt());
      }

      return outputs;
   }

   public static void writeFluidStack(FriendlyByteBuf buf, Ic2FluidStack stack) {
      buf.writeVarInt(BuiltInRegistries.FLUID.getId(stack.getFluid()));
      buf.writeVarInt(stack.getAmountMb());
   }

   public static Ic2FluidStack readFluidStack(FriendlyByteBuf buf) {
      return FluidHandler.createFluidStackMb((Fluid)BuiltInRegistries.FLUID.byId(buf.readVarInt()), buf.readVarInt(), null);
   }

   private static Fluid asFluid(JsonElement element, String name) {
      if (element.isJsonPrimitive()) {
         String string = element.getAsString();
         return (Fluid)BuiltInRegistries.FLUID
            .getOptional(Identifier.parse(string))
            .orElseThrow(() -> new JsonSyntaxException("Expected " + name + " to be an fluid, was unknown string '" + string + "'"));
      } else {
         throw new JsonSyntaxException("Expected " + name + " to be an fluid, was " + GsonHelper.getType(element));
      }
   }

   private static CompoundTag asNbt(JsonElement element, String name) {
      Tag nbtElement = (Tag)JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, element);
      if (nbtElement instanceof CompoundTag nbt) {
         return nbt;
      } else {
         throw new JsonSyntaxException("Expected " + name + " to be an NBT compound (an object), was " + GsonHelper.getType(element));
      }
   }

   @Nullable
   @Contract("_,_,!null->!null;_,_,null->_")
   private static CompoundTag getNbt(JsonObject object, String key, @Nullable CompoundTag defaultNbt) {
      return object.has(key) ? asNbt(object.get(key), key) : defaultNbt;
   }
   /** Parses a vanilla-style ingredient (item/tag/array) without the removed Ingredient.fromJson. */
   private static Ingredient parseIngredient(JsonElement json) {
      if (json.isJsonObject()) {
         JsonObject object = json.getAsJsonObject();
         if (object.has("tag")) {
            net.minecraft.tags.TagKey<Item> tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse(GsonHelper.getAsString(object, "tag")));
            // 26.1.2: an unknown/empty tag must not kill recipe sync (it used to
            // disconnect the client with an encoder exception); degrade to empty.
            return BuiltInRegistries.ITEM.get(tag)
               .map(Ingredient::of)
               .orElseGet(() -> {
                  ic2.core.IC2.log.warn(ic2.core.util.LogCategory.General, "Unknown item tag %s in recipe ingredient.", tag.location());
                  return Ingredient.of(java.util.stream.Stream.<net.minecraft.world.level.ItemLike>empty());
               });
         }
         if (object.has("item")) {
            return Ingredient.of(GsonHelper.getAsItem(object, "item").value());
         }
         throw new JsonSyntaxException("Expected 'item' or 'tag' in ingredient, got " + object);
      }
      if (json.isJsonArray()) {
         java.util.List<Item> items = new ArrayList<>();
         for (JsonElement element : json.getAsJsonArray()) {
            Ingredient ingredient = parseIngredient(element);
            ingredient.getValues().forEach(holder -> items.add(holder.value()));
         }
         return Ingredient.of(items.stream());
      }
      throw new JsonSyntaxException("Expected object or array for ingredient, got " + GsonHelper.getType(json));
   }
}
