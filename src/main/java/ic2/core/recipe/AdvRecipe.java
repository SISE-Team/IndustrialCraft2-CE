package ic2.core.recipe;

import net.minecraft.world.item.crafting.CraftingRecipe;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ic2.api.item.ElectricItem;
import ic2.api.recipe.IRecipeInput;
import ic2.compat.Ic2CraftingRecipe;
import ic2.core.init.MainConfig;
import ic2.core.recipe.v2.RecipeIo;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AdvRecipe extends net.minecraft.world.item.crafting.ShapedRecipe implements Ic2CraftingRecipe {
   private static final boolean debug = Util.hasAssertions();
   /** 26.1.2: parsed lazily; recipes decode before item components bind. */
   private final java.util.function.Supplier<net.minecraft.world.item.ItemStackTemplate> outputSupplier;
   private ItemStack output;
   public final IRecipeInput[] input;
   public final IRecipeInput[] inputMirrored;
   public final int[] masks;
   public final int[] masksMirrored;
   public final int inputWidth;
   public final int inputHeight;
   private final IRecipeInput[] patternInputs;
   public final boolean hidden;
   public final boolean consuming;
   public int assemblyMask;
   private final Identifier id;

   private static AdvRecipe create(
      Identifier id, int width, int height, IRecipeInput[] ingredients, java.util.function.Supplier<net.minecraft.world.item.ItemStackTemplate> result, boolean isConsuming, boolean isHidden
   ) {
      int mask = 0;
      List<IRecipeInput> inputs = new ArrayList<>();

      for (int y = 0; y < 3; y++) {
         for (int x = 0; x < 3; x++) {
            mask <<= 1;
            if (x < width && y < height && ingredients[x + y * width] != null) {
               mask |= 1;
               inputs.add(ingredients[x + y * width]);
            }
         }
      }

      IRecipeInput[] input = inputs.toArray(new IRecipeInput[0]);
      boolean mirror = false;
      if (width != 1) {
         for (int y = 0; y < height; y++) {
            if (ingredients[y * width] != ingredients[width - 1 + y * width]) {
               mirror = true;
               break;
            }
         }
      }

      return new AdvRecipe(id, width, height, mirror, mask, input, result, isConsuming, isHidden);
   }

   private AdvRecipe(
      Identifier id, int width, int height, boolean mirror, int mask, IRecipeInput[] input, java.util.function.Supplier<net.minecraft.world.item.ItemStackTemplate> resultSupplier, boolean isConsuming, boolean isHidden
   ) {
      super(new net.minecraft.world.item.crafting.Recipe.CommonInfo(!isHidden),
         new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
         vanillaPattern(width, height, mask, input), resultSupplier.get());
      this.id = id;
      this.inputWidth = width;
      this.inputHeight = height;
      this.outputSupplier = resultSupplier;
      this.consuming = isConsuming;
      this.hidden = isHidden;
      this.input = input;
      this.patternInputs = new IRecipeInput[width * height];
      int patternIndex = 0;
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            if ((mask & 1 << (8 - y * 3 - x)) != 0) {
               this.patternInputs[x + y * width] = input[patternIndex++];
            }
         }
      }
      if (!mirror) {
         this.inputMirrored = null;
      } else {
         IRecipeInput[] tmp = new IRecipeInput[9];
         int i = 0;

         for (int j = 0; i < 9; i++) {
            if ((mask & 1 << 8 - i) != 0) {
               tmp[i] = input[j];
               j++;
            }
         }

         IRecipeInput old = tmp[0];
         tmp[0] = tmp[2];
         tmp[2] = old;
         IRecipeInput var18 = tmp[3];
         tmp[3] = tmp[5];
         tmp[5] = var18;
         IRecipeInput var19 = tmp[6];
         tmp[6] = tmp[8];
         tmp[8] = var19;
         this.inputMirrored = new IRecipeInput[input.length];
         int ix = 0;

         for (int jx = 0; ix < 9; ix++) {
            if (tmp[ix] != null) {
               this.inputMirrored[jx] = tmp[ix];
               jx++;
            }
         }
      }

      int xMasks = -width + 4;
      int yMasks = -height + 4;
      this.masks = new int[xMasks * yMasks];
      if (!mirror) {
         this.masksMirrored = null;
      } else {
         this.masksMirrored = new int[this.masks.length];
      }

      for (int y = 0; y < yMasks; y++) {
         int yMask = mask >>> y * 3;

         for (int x = 0; x < xMasks; x++) {
            int xyMask = yMask >>> x;
            this.masks[x + y * xMasks] = xyMask;
            if (mirror) {
               this.masksMirrored[x + y * xMasks] = xyMask << 2 & 292 | xyMask & 146 | xyMask >>> 2 & 73;
            }
         }
      }
   }

   /** The vanilla recipe placer recognizes ShapedRecipe when laying out narrow patterns. */
   private static net.minecraft.world.item.crafting.ShapedRecipePattern vanillaPattern(int width, int height, int mask, IRecipeInput[] inputs) {
      Map<Character, Ingredient> key = new HashMap<>();
      List<String> rows = new ArrayList<>();
      int index = 0;
      for (int y = 0; y < height; y++) {
         StringBuilder row = new StringBuilder();
         for (int x = 0; x < width; x++) {
            if ((mask & 1 << (8 - y * 3 - x)) == 0) {
               row.append(' ');
            } else {
               char symbol = (char)('A' + index);
               row.append(symbol);
               key.put(symbol, inputs[index++].getIngredient());
            }
         }
         rows.add(row.toString());
      }
      return net.minecraft.world.item.crafting.ShapedRecipePattern.of(key, rows);
   }

   public ItemStack getOutput() {
      if (this.output == null) {
         this.output = this.outputSupplier.get().create();
      }
      return this.output;
   }

   public boolean matches(CraftingInput inventorycrafting, Level world) {
      return this.matchesPattern(inventorycrafting, false) || this.matchesPattern(inventorycrafting, true);
   }

   /** Legacy UU layouts distinguish recipes by the exact cells of the 3x3 grid. */
   public boolean matchesAssembly(java.util.List<ItemStack> items, Level world) {
      int mask = 0;
      for (ItemStack item : items) mask = mask << 1 | (item.isEmpty() ? 0 : 1);
      return this.assemblyMask == mask && this.matches(CraftingInput.of(3, 3, items), world);
   }

   @Override
   @SuppressWarnings({"unchecked", "rawtypes"})
   public net.minecraft.world.item.crafting.RecipeType<CraftingRecipe> getType() {
      return this.assemblyMask == 0 ? net.minecraft.world.item.crafting.RecipeType.CRAFTING
         : (net.minecraft.world.item.crafting.RecipeType)ic2.core.ref.Ic2RecipeTypes.UU_ASSEMBLY;
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

   private boolean matchesPattern(CraftingInput input, boolean mirror) {
      if (input.width() != this.inputWidth || input.height() != this.inputHeight) {
         return false;
      }
      for (int y = 0; y < this.inputHeight; y++) {
         for (int x = 0; x < this.inputWidth; x++) {
            IRecipeInput ingredient = this.patternInputs[(mirror ? this.inputWidth - 1 - x : x) + y * this.inputWidth];
            ItemStack offered = input.getItem(x + y * this.inputWidth);
            if (ingredient == null ? !offered.isEmpty() : !ingredient.matches(offered)) {
               return false;
            }
         }
      }
      return true;
   }

   @Override
   public ItemStack assemble(CraftingInput input) {
      return this.craft(input);
   }

   public static boolean canShow(Object[] input, ItemStack output, boolean hidden) {
      return !hidden || !ConfigUtil.getBool(MainConfig.get(), "misc/hideSecretRecipes");
   }

   public boolean canShow() {
      return canShow(this.input, this.getOutput(), this.hidden);
   }

   private static boolean checkMask(int mask, int[] request) {
      for (int cmpMask : request) {
         if (mask == cmpMask) {
            return true;
         }
      }

      return false;
   }

   private ItemStack checkItems(RecipeInput inventory, IRecipeInput[] request) {
      int size = inventory.size();
      double outputCharge = 0.0;
      int i = 0;

      for (int j = 0; i < size; i++) {
         ItemStack offer = inventory.getItem(i);
         if (!StackUtil.isEmpty(offer)) {
            if (!request[j++].matches(offer)) {
               return StackUtil.emptyStack;
            }

            outputCharge += ElectricItem.manager.getCharge(StackUtil.copyWithSize(offer, 1));
         }
      }

      ItemStack ret = this.getOutput().copy();
      ElectricItem.manager.charge(ret, outputCharge, Integer.MAX_VALUE, true, false);
      return ret;
   }

   @Override
   public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
      return CraftingRemainders.get(inv, this.consuming);
   }

   @Override
   public int getIc2RecipeWidth() {
      return this.inputWidth;
   }

   @Override
   public int getIc2RecipeHeight() {
      return this.inputHeight;
   }

   public List<java.util.Optional<Ingredient>> getIngredients() {
      return java.util.Arrays.stream(this.patternInputs)
         .map(input -> input == null ? java.util.Optional.<Ingredient>empty() : java.util.Optional.of(input.getIngredient())).toList();
   }

   @Override
   public List<net.minecraft.world.item.crafting.display.RecipeDisplay> display() {
      if (this.hidden && ConfigUtil.getBool(MainConfig.get(), "misc/hideSecretRecipes")) {
         return List.of();
      }
      return List.of(new net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay(
         this.inputWidth, this.inputHeight,
         this.getIngredients().stream().map(Ingredient::optionalIngredientToDisplay).toList(),
         new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(this.outputSupplier.get()),
         new net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay(net.minecraft.world.item.Items.CRAFTING_TABLE)
      ));
   }

   public Identifier getId() {
      return this.id;
   }

   @Override
   @SuppressWarnings("unchecked")
   public RecipeSerializer<net.minecraft.world.item.crafting.ShapedRecipe> getSerializer() {
      return (RecipeSerializer<net.minecraft.world.item.crafting.ShapedRecipe>)(RecipeSerializer<?>)Ic2RecipeSerializers.SHAPED;
   }

   public static final class Serializer extends ic2.core.recipe.v2.Ic2RecipeCodecSource<AdvRecipe> {
      @Override
      protected AdvRecipe readJson(JsonObject json) {
         return this.readJson(json, net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE,
            net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)));
      }

      @Override
      protected AdvRecipe readJson(JsonObject json, com.mojang.serialization.DynamicOps<JsonElement> ops) {
         Map<String, IRecipeInput> symbols = readSymbols(GsonHelper.getAsJsonObject(json, "key"), ops);
         String[] pattern = getPattern(GsonHelper.getAsJsonArray(json, "pattern"));
         int width = pattern[0].length();
         int height = pattern.length;
         IRecipeInput[] ingredients = createPatternMatrix(pattern, symbols, width, height);
         boolean consuming = GsonHelper.getAsBoolean(json, "consuming", false);
         boolean hidden = GsonHelper.getAsBoolean(json, "hidden", false);
         AdvRecipe recipe = AdvRecipe.create(
            ic2.core.IC2.getIdentifier("decoded"), width, height, ingredients,
            () -> RecipeIo.parseOutputTemplate(GsonHelper.getAsJsonObject(json, "result")), consuming, hidden
         );
         recipe.assemblyMask = GsonHelper.getAsInt(json, "assemblyMask", 0);
         return recipe;
      }

      @Override
      protected AdvRecipe readNetwork(net.minecraft.network.RegistryFriendlyByteBuf buf) {
         Identifier id = ic2.core.IC2.getIdentifier("decoded");
         IRecipeInput[] ingredients = new IRecipeInput[buf.readVarInt()];

         for (int i = 0; i < ingredients.length; i++) {
            ingredients[i] = RecipeIo.readInput(buf);
         }

         int width = buf.readVarInt();
         int height = buf.readVarInt();
         boolean mirrored = buf.readBoolean();
         int mask = buf.readInt();
         // Network decode happens post-bind (recipe sync during play), so the
         // stack can be decoded eagerly and merely memoized behind the supplier.
         net.minecraft.world.item.ItemStack output = net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf)buf);
         boolean consuming = buf.readBoolean();
         boolean hidden = buf.readBoolean();
         AdvRecipe recipe = new AdvRecipe(id, width, height, mirrored, mask, ingredients, () -> net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(output), consuming, hidden);
         recipe.assemblyMask = buf.readVarInt();
         return recipe;
      }

      @Override
      protected void writeNetwork(net.minecraft.network.RegistryFriendlyByteBuf buf, AdvRecipe recipe) {
         buf.writeVarInt(recipe.input.length);

         for (IRecipeInput ing : recipe.input) {
            RecipeIo.writeInput(buf, ing);
         }

         buf.writeVarInt(recipe.inputWidth);
         buf.writeVarInt(recipe.inputHeight);
         buf.writeBoolean(recipe.inputMirrored != null);
         buf.writeInt(recipe.masks[0]);
         net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf)buf, recipe.getOutput());
         buf.writeBoolean(recipe.consuming);
         buf.writeBoolean(recipe.hidden);
         buf.writeVarInt(recipe.assemblyMask);
      }

      private static Map<String, IRecipeInput> readSymbols(JsonObject json, com.mojang.serialization.DynamicOps<JsonElement> ops) {
         HashMap<String, IRecipeInput> map = Maps.newHashMap();

         for (Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getKey().length() != 1) {
               throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }

            if (" ".equals(entry.getKey())) {
               throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }

            map.put(entry.getKey(), RecipeIo.parseCraftingInput(entry.getValue(), ops));
         }

         return map;
      }

      private static String[] getPattern(JsonArray json) {
         String[] strings = new String[json.size()];
         if (strings.length > 3) {
            throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
         } else if (strings.length == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
         } else {
            for (int i = 0; i < strings.length; i++) {
               String string = GsonHelper.convertToString(json.get(i), "pattern[" + i + "]");
               if (string.length() > 3) {
                  throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
               }

               if (i > 0 && strings[0].length() != string.length()) {
                  throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
               }

               strings[i] = string;
            }

            int top = 0;
            int bottom = strings.length;
            while (top < bottom && strings[top].isBlank()) top++;
            while (bottom > top && strings[bottom - 1].isBlank()) bottom--;
            int left = strings[0].length();
            int right = 0;
            for (int y = top; y < bottom; y++) {
               for (int x = 0; x < strings[y].length(); x++) {
                  if (strings[y].charAt(x) != ' ') {
                     left = Math.min(left, x);
                     right = Math.max(right, x + 1);
                  }
               }
            }
            if (top == bottom || left >= right) {
               throw new JsonSyntaxException("Empty IC2 crafting pattern");
            }
            String[] trimmed = new String[bottom - top];
            for (int y = top; y < bottom; y++) {
               trimmed[y - top] = strings[y].substring(left, right);
            }
            return trimmed;
         }
      }

      private static IRecipeInput[] createPatternMatrix(String[] pattern, Map<String, IRecipeInput> symbols, int width, int height) {
         IRecipeInput[] ingredients = new IRecipeInput[width * height];
         HashSet<String> remainingKeys = new HashSet<>(symbols.keySet());

         for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[i].length(); j++) {
               String string = pattern[i].substring(j, j + 1);
               if (!string.equals(" ")) {
                  IRecipeInput ingredient = symbols.get(string);
                  if (ingredient == null) {
                     throw new JsonSyntaxException("Pattern references symbol '" + string + "' but it's not defined in the key");
                  }

                  remainingKeys.remove(string);
                  ingredients[j + width * i] = ingredient;
               }
            }
         }

         if (!remainingKeys.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + remainingKeys);
         } else {
            return ingredients;
         }
      }
   }
}
