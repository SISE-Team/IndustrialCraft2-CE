package ic2ce.neoforge.recipe.codec;

import com.google.gson.JsonObject;
import ic2ce.api.recipe.*;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Amplifier/material recipe protocol keeps a positive integer bill instead of manufacturing item
 * outputs.
 */
public final class QuantityRecipeCodec
    extends NativeRecipeCodec<RecipeHolder<IRecipeInput, Integer>> {
  private final RecipeType<?> type;
  private final Function<JsonObject, CompoundTag> metadata;

  public QuantityRecipeCodec(RecipeType<?> type, Function<JsonObject, CompoundTag> metadata) {
    this.type = Objects.requireNonNull(type);
    this.metadata = metadata;
  }

  private static int positive(int amount) {
    return ic2ce.neoforge.recipe.input.InputDescriptor.positive(amount);
  }

  @Override
  protected RecipeHolder<IRecipeInput, Integer> readJson(JsonObject source) {
    var json = source.deepCopy();
    require(json, "ingredient");
    int result = positive(GsonHelper.getAsInt(json, "result"));
    return new RecipeHolder<>(
        new DeferredMachineRecipe<>(
            () -> RecipeValueCodec.parseInput(json.get("ingredient")),
            () -> result,
            () -> metadata == null ? null : metadata.apply(json.deepCopy())),
        placeholderId(),
        serializerRecord(),
        type);
  }

  @Override
  protected RecipeHolder<IRecipeInput, Integer> readNetwork(RegistryFriendlyByteBuf buffer) {
    var input = RecipeValueCodec.readInput(buffer);
    outputKind(buffer, 0);
    int output = positive(RecipeValueCodec.readIntegerOutput(buffer));
    return new RecipeHolder<>(
        new MachineRecipe<>(input, output, buffer.readNbt()),
        placeholderId(),
        serializerRecord(),
        type);
  }

  @Override
  protected void writeNetwork(
      RegistryFriendlyByteBuf buffer, RecipeHolder<IRecipeInput, Integer> holder) {
    var recipe = holder.recipe();
    RecipeValueCodec.writeInput(buffer, recipe.getInput());
    RecipeValueCodec.writeIntegerOutput(buffer, positive(recipe.getOutput()));
    buffer.writeNbt(recipe.getMetaData());
  }
}
