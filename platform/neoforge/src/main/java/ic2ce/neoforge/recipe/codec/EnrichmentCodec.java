package ic2ce.neoforge.recipe.codec;

import com.google.gson.JsonObject;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.*;
import net.minecraft.network.RegistryFriendlyByteBuf;

/** Fluid enrichment retains one fluid bill and one additive bill, distinct from solid canning. */
public final class EnrichmentCodec
    extends NativeRecipeCodec<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> {
  private RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> wrap(
      MachineRecipe<ICannerEnrichRecipeManager.Input, Ic2FluidStack> recipe) {
    return new RecipeHolder<>(
        recipe,
        placeholderId(),
        serializerRecord(),
        ic2ce.neoforge.registry.RecipeKinds.CANNER_ENRICH);
  }

  @Override
  protected RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> readJson(
      JsonObject source) {
    var json = source.deepCopy();
    var fluid = require(json, "input_ingredient").getAsJsonObject();
    require(json, "additive_ingredient");
    var result = require(json, "result").getAsJsonObject();
    return wrap(
        new DeferredMachineRecipe<>(
            () ->
                new ICannerEnrichRecipeManager.Input(
                    RecipeValueCodec.parseFluidStack(fluid),
                    RecipeValueCodec.parseInput(json.get("additive_ingredient"))),
            () -> RecipeValueCodec.parseFluidStack(result)));
  }

  @Override
  protected RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> readNetwork(
      RegistryFriendlyByteBuf buffer) {
    var fluid = RecipeValueCodec.readFluidStack(buffer);
    var additive = RecipeValueCodec.readInput(buffer);
    var output = RecipeValueCodec.readFluidStack(buffer);
    if (fluid.isEmpty() || output.isEmpty())
      throw new IllegalArgumentException("Enrichment fluid quantities must be positive");
    return wrap(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(fluid, additive), output));
  }

  @Override
  protected void writeNetwork(
      RegistryFriendlyByteBuf buffer,
      RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> holder) {
    var recipe = holder.recipe();
    RecipeValueCodec.writeFluidStack(buffer, recipe.getInput().fluid);
    RecipeValueCodec.writeInput(buffer, recipe.getInput().additive);
    RecipeValueCodec.writeFluidStack(buffer, recipe.getOutput());
  }
}
