package ic2ce.neoforge.recipe.codec;

import com.google.gson.JsonObject;
import ic2ce.api.recipe.*;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/** Explicit deterministic and weighted branches retain each table's native output protocol. */
public final class WeightedRecipeCodec
    extends NativeRecipeCodec<RecipeHolder<IRecipeInput, Collection<ItemStack>>> {
  private final RecipeType<?> type;
  private final Function<JsonObject, CompoundTag> metadata;

  public WeightedRecipeCodec(RecipeType<?> type, Function<JsonObject, CompoundTag> metadata) {
    this.type = Objects.requireNonNull(type);
    this.metadata = metadata;
  }

  @Override
  protected RecipeHolder<IRecipeInput, Collection<ItemStack>> readJson(JsonObject source) {
    var json = source.deepCopy();
    require(json, "ingredient");
    require(json, "result");
    java.util.function.Supplier<CompoundTag> meta =
        () -> metadata == null ? null : metadata.apply(json.deepCopy());
    MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe =
        GsonHelper.getAsBoolean(json, "weighted", false)
            ? new DeferredWeightedRecipe<>(
                () -> RecipeValueCodec.parseInput(json.get("ingredient")),
                table -> RecipeValueCodec.parseWeightedOutputs(json.get("result"), "result", table),
                meta)
            : new DeferredMachineRecipe<>(
                () -> RecipeValueCodec.parseInput(json.get("ingredient")),
                () -> RecipeValueCodec.parseOutputs(json.get("result"), "result"),
                meta);
    return new RecipeHolder<>(recipe, placeholderId(), serializerRecord(), type);
  }

  @Override
  protected RecipeHolder<IRecipeInput, Collection<ItemStack>> readNetwork(
      RegistryFriendlyByteBuf buffer) {
    var input = RecipeValueCodec.readInput(buffer);
    int kind = buffer.readUnsignedByte();
    MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe =
        switch (kind) {
          case 0 ->
              new MachineRecipe<>(input, RecipeValueCodec.readOutput(buffer), buffer.readNbt());
          case 1 ->
              new MachineRecipeWeighted<>(
                  input,
                  RecipeValueCodec.readWeightedOutput(buffer, new RecipeOutputWeighted()),
                  buffer.readNbt());
          default ->
              throw new IllegalArgumentException("Unknown weighted output discriminator " + kind);
        };
    return new RecipeHolder<>(recipe, placeholderId(), serializerRecord(), type);
  }

  @Override
  protected void writeNetwork(
      RegistryFriendlyByteBuf buffer, RecipeHolder<IRecipeInput, Collection<ItemStack>> holder) {
    var recipe = holder.recipe();
    RecipeValueCodec.writeInput(buffer, recipe.getInput());
    if (recipe instanceof MachineRecipeWeighted<?> weighted)
      RecipeValueCodec.writeWeightedOutput(buffer, weighted.getOutputWeighted());
    else RecipeValueCodec.writeOutput(buffer, recipe.getOutput());
    buffer.writeNbt(recipe.getMetaData());
  }
}
