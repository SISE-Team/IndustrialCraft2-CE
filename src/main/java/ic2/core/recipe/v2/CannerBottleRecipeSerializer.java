package ic2.core.recipe.v2;

import com.google.gson.JsonObject;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class CannerBottleRecipeSerializer extends Ic2RecipeCodecSource<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> {
   @Override
   protected RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> readJson(JsonObject json) {
      return new RecipeHolder<>(
         new LazyMachineRecipe<>(
            () -> new ICannerBottleRecipeManager.Input(
               RecipeIo.parseInput(json.get("container_ingredient")),
               RecipeIo.parseInput(json.get("fill_ingredient"))
            ),
            () -> RecipeIo.parseOutput(GsonHelper.getAsJsonObject(json, "result"))
         ),
         placeholderId(), this.serializerRecord(), ic2.core.ref.Ic2RecipeTypes.CANNER_BOTTLE
      );
   }

   @Override
   protected RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> readNetwork(RegistryFriendlyByteBuf buf) {
      IRecipeInput container = RecipeIo.readInput(buf);
      IRecipeInput fill = RecipeIo.readInput(buf);
      ItemStack output = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
      return new RecipeHolder<>(new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), output), placeholderId(), this.serializerRecord(), ic2.core.ref.Ic2RecipeTypes.CANNER_BOTTLE);
   }

   @Override
   protected void writeNetwork(RegistryFriendlyByteBuf buf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> recipe) {
      RecipeIo.writeInput(buf, recipe.recipe().getInput().container);
      RecipeIo.writeInput(buf, recipe.recipe().getInput().fill);
      ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.recipe().getOutput());
   }
}
