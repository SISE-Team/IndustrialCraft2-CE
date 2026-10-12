package ic2.core.recipe.v2;

import ic2.core.IC2;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Base class for IC2's datapack recipe serializers.
 *
 * <p>26.1.2 turned {@link RecipeSerializer} into a record holding a
 * {@link com.mojang.serialization.MapCodec} and a network {@link net.minecraft.network.codec.StreamCodec}.
 * IC2's recipes use a custom JSON shape, so the map codec bridges the generic
 * ops back to a Gson {@link com.google.gson.JsonObject} and reuses the existing
 * JSON parsing logic. The recipe id is not available at decode time (vanilla
 * attaches it afterwards in its own holder), so implementations pass a
 * placeholder id when constructing {@link RecipeHolder}s.</p>
 */
public abstract class Ic2RecipeCodecSource<H extends Recipe<?>> {
   private RecipeSerializer<H> serializerRecord;

   /** Called once during registration with the serializer record built from this source. */
   public void attachSerializerRecord(RecipeSerializer<H> record) {
      this.serializerRecord = record;
   }

   protected RecipeSerializer<H> serializerRecord() {
      return this.serializerRecord;
   }

   /** Placeholder id used while decoding (vanilla keys recipes by its own holder id). */
   protected static Identifier placeholderId() {
      return IC2.getIdentifier("decoded");
   }

   /** Reads a recipe from datapack JSON. */
   protected abstract H readJson(com.google.gson.JsonObject json) throws Exception;

   /** Keep the world's pending tag lookup for ingredients, without creating stacks. */
   protected H readJson(com.google.gson.JsonObject json, com.mojang.serialization.DynamicOps<com.google.gson.JsonElement> ops) throws Exception {
      return this.readJson(json);
   }

   /** Reads a recipe synced through the network. */
   protected abstract H readNetwork(net.minecraft.network.RegistryFriendlyByteBuf buf);

   /** Writes a recipe to the network. */
   protected abstract void writeNetwork(net.minecraft.network.RegistryFriendlyByteBuf buf, H recipe);

   public final com.mojang.serialization.MapCodec<H> codec() {
      return new Ic2JsonMapCodec();
   }

   public final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, H> streamCodec() {
      return net.minecraft.network.codec.StreamCodec.of(
         (buf, recipe) -> this.writeNetwork(buf, recipe),
         buf -> this.readNetwork(buf)
      );
   }

   private final class Ic2JsonMapCodec extends com.mojang.serialization.MapCodec<H> {
      @Override
      public <T> com.mojang.serialization.DataResult<H> decode(com.mojang.serialization.DynamicOps<T> ops, com.mojang.serialization.MapLike<T> input) {
         com.google.gson.JsonObject json;
         try {
            json = Ic2RecipeCodecSource.toJsonObject(ops, input);
         } catch (Exception e) {
            return com.mojang.serialization.DataResult.error(() -> "Failed to bridge IC2 recipe JSON: " + e);
         }

         try {
            com.mojang.serialization.DynamicOps<com.google.gson.JsonElement> jsonOps =
               ops instanceof net.minecraft.resources.RegistryOps<?> registryOps
                  ? registryOps.withParent(com.mojang.serialization.JsonOps.INSTANCE)
                  : com.mojang.serialization.JsonOps.INSTANCE;
            return com.mojang.serialization.DataResult.success(Ic2RecipeCodecSource.this.readJson(json, jsonOps));
         } catch (Exception e) {
            return com.mojang.serialization.DataResult.error(() -> "Failed to parse IC2 recipe: " + e);
         }
      }

      @Override
      public <T> com.mojang.serialization.RecordBuilder<T> encode(
         H input, com.mojang.serialization.DynamicOps<T> ops, com.mojang.serialization.RecordBuilder<T> prefix
      ) {
         // IC2 recipes are only decoded from JSON; encoding is not supported.
         return prefix;
      }

      @Override
      public <T> java.util.stream.Stream<T> keys(com.mojang.serialization.DynamicOps<T> ops) {
         return java.util.stream.Stream.empty();
      }
   }

   /** Converts a generic ops map (e.g. RegistryOps over JsonOps) back into a Gson object. */
   private static <T> com.google.gson.JsonObject toJsonObject(com.mojang.serialization.DynamicOps<T> ops, com.mojang.serialization.MapLike<T> map) {
      com.google.gson.JsonObject json = new com.google.gson.JsonObject();
      com.mojang.serialization.JsonOps jsonOps = com.mojang.serialization.JsonOps.INSTANCE;
      map.entries().forEach(pair -> {
         com.google.gson.JsonElement key = ops.convertTo(jsonOps, pair.getFirst());
         com.google.gson.JsonElement value = ops.convertTo(jsonOps, pair.getSecond());
         json.add(key.getAsString(), value);
      });
      return json;
   }
}
