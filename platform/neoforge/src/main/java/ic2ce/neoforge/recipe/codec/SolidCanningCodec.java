package ic2ce.neoforge.recipe.codec;

import com.google.gson.JsonObject;
import ic2ce.api.recipe.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/** Solid canning serializes independent container and fill bills with one exact native output. */
public final class SolidCanningCodec
    extends NativeRecipeCodec<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> {
  private RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> wrap(
      MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe) {
    return new RecipeHolder<>(
        recipe,
        placeholderId(),
        serializerRecord(),
        ic2ce.neoforge.registry.RecipeKinds.CANNER_BOTTLE);
  }

  @Override
  protected RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> readJson(JsonObject source) {
    var json = source.deepCopy();
    require(json, "container_ingredient");
    require(json, "fill_ingredient");
    var result = require(json, "result").getAsJsonObject();
    return wrap(
        new DeferredMachineRecipe<>(
            () ->
                new ICannerBottleRecipeManager.Input(
                    RecipeValueCodec.parseInput(json.get("container_ingredient")),
                    RecipeValueCodec.parseInput(json.get("fill_ingredient"))),
            () -> RecipeValueCodec.parseOutput(result)));
  }

  @Override
  protected RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> readNetwork(
      RegistryFriendlyByteBuf buffer) {
    var container = RecipeValueCodec.readInput(buffer);
    var fill = RecipeValueCodec.readInput(buffer);
    var result = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    if (result.isEmpty()) throw new IllegalArgumentException("Canning recipe has no output");
    return wrap(new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), result));
  }

  @Override
  protected void writeNetwork(
      RegistryFriendlyByteBuf buffer,
      RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> holder) {
    var recipe = holder.recipe();
    RecipeValueCodec.writeInput(buffer, recipe.getInput().container);
    RecipeValueCodec.writeInput(buffer, recipe.getInput().fill);
    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.getOutput());
  }
}
