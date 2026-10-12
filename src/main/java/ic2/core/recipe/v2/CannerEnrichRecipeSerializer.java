package ic2.core.recipe.v2;

import com.google.gson.JsonObject;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.fluid.Ic2FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.GsonHelper;

public class CannerEnrichRecipeSerializer extends Ic2RecipeCodecSource<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> {
   @Override
   protected RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> readJson(JsonObject json) {
      return new RecipeHolder<>(
         new LazyMachineRecipe<>(
            () -> new ICannerEnrichRecipeManager.Input(
               RecipeIo.parseFluidStack(GsonHelper.getAsJsonObject(json, "input_ingredient")),
               RecipeIo.parseInput(json.get("additive_ingredient"))
            ),
            () -> RecipeIo.parseFluidStack(GsonHelper.getAsJsonObject(json, "result"))
         ),
         placeholderId(), this.serializerRecord(), ic2.core.ref.Ic2RecipeTypes.CANNER_ENRICH
      );
   }

   @Override
   protected RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> readNetwork(RegistryFriendlyByteBuf buf) {
      Ic2FluidStack input = RecipeIo.readFluidStack(buf);
      IRecipeInput additive = RecipeIo.readInput(buf);
      Ic2FluidStack result = RecipeIo.readFluidStack(buf);
      return new RecipeHolder<>(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(input, additive), result), placeholderId(), this.serializerRecord(), ic2.core.ref.Ic2RecipeTypes.CANNER_ENRICH);
   }

   @Override
   protected void writeNetwork(RegistryFriendlyByteBuf buf, RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> recipe) {
      RecipeIo.writeFluidStack(buf, recipe.recipe().getInput().fluid);
      RecipeIo.writeInput(buf, recipe.recipe().getInput().additive);
      RecipeIo.writeFluidStack(buf, recipe.recipe().getOutput());
   }
}
