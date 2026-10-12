package ic2.core.recipe.v2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeWeighted;
import ic2.api.recipe.RecipeOutputWeighted;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class WeightedMachineRecipeSerializer extends Ic2RecipeCodecSource<RecipeHolder<IRecipeInput, Collection<ItemStack>>> {
   private final RecipeType<?> recipeType;
   @Nullable
   private final Function<JsonObject, CompoundTag> metaProcessor;

   public WeightedMachineRecipeSerializer(RecipeType<?> recipeType, @Nullable Function<JsonObject, CompoundTag> metaProcessor) {
      this.recipeType = recipeType;
      this.metaProcessor = metaProcessor;
   }

   @Override
   protected RecipeHolder<IRecipeInput, Collection<ItemStack>> readJson(JsonObject json) {
      JsonElement resultJsonObj = json.get("result");
      boolean weighted = GsonHelper.getAsBoolean(json, "weighted", false);
      MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe;
      if (weighted) {
         machineRecipe = new LazyMachineRecipeWeighted<>(
            () -> RecipeIo.parseInput(json.get("ingredient")),
            weightedOutput -> RecipeIo.parseWeightedOutputs(resultJsonObj, "result", weightedOutput),
            () -> this.metaProcessor != null ? this.metaProcessor.apply(json) : null
         );
      } else {
         machineRecipe = new LazyMachineRecipe<>(
            () -> RecipeIo.parseInput(json.get("ingredient")),
            () -> RecipeIo.parseOutputs(resultJsonObj, "result"),
            () -> this.metaProcessor != null ? this.metaProcessor.apply(json) : null
         );
      }

      return new RecipeHolder<>(machineRecipe, placeholderId(), this.serializerRecord(), this.recipeType);
   }

   @Override
   protected RecipeHolder<IRecipeInput, Collection<ItemStack>> readNetwork(RegistryFriendlyByteBuf buf) {
      IRecipeInput input = RecipeIo.readInput(buf);
      byte type = buf.readByte();
      MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe;
      if (type == 1) {
         machineRecipe = new MachineRecipeWeighted<>(input, RecipeIo.readWeightedOutput(buf, new RecipeOutputWeighted()), buf.readNbt());
      } else {
         machineRecipe = new MachineRecipe<>(input, RecipeIo.readOutput(buf), buf.readNbt());
      }

      return new RecipeHolder<>(machineRecipe, placeholderId(), this.serializerRecord(), this.recipeType);
   }

   @Override
   protected void writeNetwork(RegistryFriendlyByteBuf buf, RecipeHolder<IRecipeInput, Collection<ItemStack>> recipe) {
      RecipeIo.writeInput(buf, recipe.recipe().getInput());
      MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe = recipe.recipe();
      if (machineRecipe instanceof MachineRecipeWeighted<?> machineRecipeWeighted) {
         RecipeIo.writeWeightedOutput(buf, machineRecipeWeighted.getOutputWeighted());
      } else {
         RecipeIo.writeOutput(buf, machineRecipe.getOutput());
      }

      buf.writeNbt(machineRecipe.getMetaData());
   }
}
