package ic2.core.recipe.v2;

import com.google.gson.JsonObject;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class BasicMachineRecipeSerializer extends Ic2RecipeCodecSource<RecipeHolder<IRecipeInput, Collection<ItemStack>>> {
   private final RecipeType<?> recipeType;
   @Nullable
   private final Function<JsonObject, CompoundTag> metaProcessor;

   public BasicMachineRecipeSerializer(RecipeType<?> recipeType, @Nullable Function<JsonObject, CompoundTag> metaProcessor) {
      this.recipeType = recipeType;
      this.metaProcessor = metaProcessor;
   }

   @Override
   protected RecipeHolder<IRecipeInput, Collection<ItemStack>> readJson(JsonObject json) {
      // 26.1.2: recipes are decoded during resource loading, before holder
      // components bind; defer the actual parsing to first use.
      return new RecipeHolder<>(
         new LazyMachineRecipe<>(
            () -> RecipeIo.parseInput(json.get("ingredient")),
            () -> RecipeIo.parseOutputs(json.get("result"), "result"),
            () -> this.metaProcessor != null ? this.metaProcessor.apply(json) : null
         ),
         placeholderId(), this.serializerRecord(), this.recipeType
      );
   }

   @Override
   protected RecipeHolder<IRecipeInput, Collection<ItemStack>> readNetwork(RegistryFriendlyByteBuf buf) {
      IRecipeInput input = RecipeIo.readInput(buf);
      byte type = buf.readByte();
      if (type != 0) {
         throw new Error("Reading recipe error! The type of recipe is wrong!");
      } else {
         return new RecipeHolder<>(new MachineRecipe<>(input, RecipeIo.readOutput(buf), buf.readNbt()), placeholderId(), this.serializerRecord(), this.recipeType);
      }
   }

   @Override
   protected void writeNetwork(RegistryFriendlyByteBuf buf, RecipeHolder<IRecipeInput, Collection<ItemStack>> recipe) {
      RecipeIo.writeInput(buf, recipe.recipe().getInput());
      RecipeIo.writeOutput(buf, recipe.recipe().getOutput());
      buf.writeNbt(recipe.recipe().getMetaData());
   }
}
