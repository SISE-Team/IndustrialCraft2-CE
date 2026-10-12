package ic2.core.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ic2.api.item.ElectricItem;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import ic2.core.item.tool.ItemToolCrafting;
import ic2.core.recipe.v2.RecipeIo;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AdvShapelessRecipe implements CraftingRecipe {
   /** 26.1.2: parsed lazily; recipes decode before item components bind. */
   private final java.util.function.Supplier<net.minecraft.world.item.ItemStackTemplate> outputSupplier;
   private ItemStack output;
   public final IRecipeInput[] input;
   public final boolean hidden;
   public final boolean consuming;
   private final Identifier id;
   private PlacementInfo placementInfo;

   public AdvShapelessRecipe(Identifier id, IRecipeInput[] input, java.util.function.Supplier<net.minecraft.world.item.ItemStackTemplate> outputSupplier, boolean hidden, boolean consuming) {
      this.id = id;
      this.input = input;
      this.outputSupplier = outputSupplier;
      this.hidden = hidden;
      this.consuming = consuming;
   }

   public ItemStack getOutput() {
      if (this.output == null) {
         this.output = this.outputSupplier.get().create();
      }
      return this.output;
   }

   public boolean matches(CraftingInput inventorycrafting, Level world) {
      List<ItemStack> offered = inventorycrafting.items().stream().filter(stack -> !stack.isEmpty()).toList();
      if (offered.size() != this.input.length) {
         return false;
      }
      boolean[][] accepts = new boolean[this.input.length][offered.size()];
      for (int i = 0; i < this.input.length; i++) {
         for (int j = 0; j < offered.size(); j++) {
            accepts[i][j] = this.input[i].matches(offered.get(j));
         }
      }
      return matchAssignment(accepts, 0, 0);
   }

   private static boolean matchAssignment(boolean[][] accepts, int ingredient, int used) {
      if (ingredient == accepts.length) {
         return true;
      }
      for (int slot = 0; slot < accepts.length; slot++) {
         if ((used & 1 << slot) == 0 && accepts[ingredient][slot] && matchAssignment(accepts, ingredient + 1, used | 1 << slot)) {
            return true;
         }
      }
      return false;
   }

   public ItemStack craft(CraftingInput inventorycrafting) {
      if (!this.matches(inventorycrafting, null)) {
         return ItemStack.EMPTY;
      }
      double charge = 0;
      for (ItemStack stack : inventorycrafting.items()) {
         if (!stack.isEmpty()) {
            charge += ElectricItem.manager.getCharge(StackUtil.copyWithSize(stack, 1));
         }
      }
      ItemStack result = this.getOutput().copy();
      ElectricItem.manager.charge(result, charge, Integer.MAX_VALUE, true, false);
      return result;
   }

   @Override
   public ItemStack assemble(CraftingInput input) {
      return this.craft(input);
   }

   public boolean canShow() {
      return AdvRecipe.canShow(this.input, this.getOutput(), this.hidden);
   }

   @Override
   public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
      return CraftingRemainders.get(inv, this.consuming);
   }

   @Override
   public PlacementInfo placementInfo() {
      if (this.placementInfo == null) {
         this.placementInfo = PlacementInfo.create(Arrays.stream(this.input).map(IRecipeInput::getIngredient).toList());
      }
      return this.placementInfo;
   }

   @Override
   public List<net.minecraft.world.item.crafting.display.RecipeDisplay> display() {
      if (this.hidden && ic2.core.util.ConfigUtil.getBool(ic2.core.init.MainConfig.get(), "misc/hideSecretRecipes")) {
         return List.of();
      }
      return List.of(new net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay(
         Arrays.stream(this.input).map(input -> input.getIngredient().display()).toList(),
         new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(this.outputSupplier.get()),
         new net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay(net.minecraft.world.item.Items.CRAFTING_TABLE)
      ));
   }

   @Override
   public boolean showNotification() {
      return !this.hidden;
   }

   @Override
   public CraftingBookCategory category() {
      return CraftingBookCategory.MISC;
   }

   @Override
   public String group() {
      return "";
   }

   public Identifier getId() {
      return this.id;
   }

   public RecipeSerializer<? extends net.minecraft.world.item.crafting.CraftingRecipe> getSerializer() {
      return Ic2RecipeSerializers.SHAPELESS;
   }

   public static class Serializer extends ic2.core.recipe.v2.Ic2RecipeCodecSource<AdvShapelessRecipe> {
      @Override
      protected AdvShapelessRecipe readJson(JsonObject json) {
         return this.readJson(json, net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE,
            net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)));
      }

      @Override
      protected AdvShapelessRecipe readJson(JsonObject json, com.mojang.serialization.DynamicOps<com.google.gson.JsonElement> ops) {
         IRecipeInput[] ingredients = getIngredients(GsonHelper.getAsJsonArray(json, "ingredients"), ops);
         if (ingredients.length == 0) {
            throw new JsonParseException("No ingredients for IC2 shapeless recipe");
         } else if (ingredients.length > 9) {
            throw new JsonParseException("Too many ingredients for IC2 shapeless recipe");
         } else {
            boolean consuming = GsonHelper.getAsBoolean(json, "consuming", false);
            boolean hidden = GsonHelper.getAsBoolean(json, "hidden", false);
            return new AdvShapelessRecipe(
               ic2.core.IC2.getIdentifier("decoded"), ingredients,
               () -> RecipeIo.parseOutputTemplate(GsonHelper.getAsJsonObject(json, "result")), hidden, consuming
            );
         }
      }

      private static IRecipeInput[] getIngredients(JsonArray json, com.mojang.serialization.DynamicOps<com.google.gson.JsonElement> ops) {
         IRecipeInput[] inputs = new IRecipeInput[json.size()];

         for (int i = 0; i < json.size(); i++) {
            inputs[i] = RecipeIo.parseCraftingInput(json.get(i), ops);
         }

         return inputs;
      }

      @Override
      protected AdvShapelessRecipe readNetwork(net.minecraft.network.RegistryFriendlyByteBuf buf) {
         Identifier id = ic2.core.IC2.getIdentifier("decoded");
         IRecipeInput[] inputs = new IRecipeInput[buf.readVarInt()];

         for (int i = 0; i < inputs.length; i++) {
            inputs[i] = RecipeIo.readInput(buf);
         }

         // Network decode happens post-bind (recipe sync during play), so the
         // stack can be decoded eagerly and merely memoized behind the supplier.
         net.minecraft.world.item.ItemStack output = net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf)buf);
         boolean hidden = buf.readBoolean();
         boolean consuming = buf.readBoolean();
         return new AdvShapelessRecipe(id, inputs, () -> net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(output), hidden, consuming);
      }

      @Override
      protected void writeNetwork(net.minecraft.network.RegistryFriendlyByteBuf buf, AdvShapelessRecipe recipe) {
         buf.writeVarInt(recipe.input.length);

         for (IRecipeInput input : recipe.input) {
            RecipeIo.writeInput(buf, input);
         }

         net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf)buf, recipe.getOutput());
         buf.writeBoolean(recipe.hidden);
         buf.writeBoolean(recipe.consuming);
      }
   }
}
