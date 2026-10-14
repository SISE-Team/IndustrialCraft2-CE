package ic2ce.neoforge.recipe.codec;

import com.google.gson.JsonObject;
import ic2ce.api.recipe.*;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Deterministic collection-output machine protocol with deferred bound stacks and owned decode
 * JSON.
 */
public final class CollectionRecipeCodec
    extends NativeRecipeCodec<RecipeHolder<IRecipeInput, Collection<ItemStack>>> {
  private final RecipeType<?> type;
  private final Function<JsonObject, CompoundTag> metadata;

  public CollectionRecipeCodec(RecipeType<?> type, Function<JsonObject, CompoundTag> metadata) {
    this.type = Objects.requireNonNull(type);
    this.metadata = metadata;
  }

  @Override
  protected RecipeHolder<IRecipeInput, Collection<ItemStack>> readJson(JsonObject source) {
    var json = source.deepCopy();
    require(json, "ingredient");
    require(json, "result");
    return new RecipeHolder<>(
        new DeferredMachineRecipe<>(
            () -> RecipeValueCodec.parseInput(json.get("ingredient")),
            () -> RecipeValueCodec.parseOutputs(json.get("result"), "result"),
            () -> metadata == null ? null : metadata.apply(json.deepCopy())),
        placeholderId(),
        serializerRecord(),
        type);
  }

  @Override
  protected RecipeHolder<IRecipeInput, Collection<ItemStack>> readNetwork(
      RegistryFriendlyByteBuf buffer) {
    var input = RecipeValueCodec.readInput(buffer);
    outputKind(buffer, 0);
    var outputs = RecipeValueCodec.readOutput(buffer);
    if (outputs.isEmpty()) throw new IllegalArgumentException("Machine recipe has no outputs");
    return new RecipeHolder<>(
        new MachineRecipe<>(input, outputs, buffer.readNbt()),
        placeholderId(),
        serializerRecord(),
        type);
  }

  @Override
  protected void writeNetwork(
      RegistryFriendlyByteBuf buffer, RecipeHolder<IRecipeInput, Collection<ItemStack>> holder) {
    var recipe = holder.recipe();
    RecipeValueCodec.writeInput(buffer, recipe.getInput());
    RecipeValueCodec.writeOutput(buffer, recipe.getOutput());
    buffer.writeNbt(recipe.getMetaData());
  }
}
