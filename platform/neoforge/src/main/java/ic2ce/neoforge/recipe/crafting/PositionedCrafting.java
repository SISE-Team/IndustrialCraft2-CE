package ic2ce.neoforge.recipe.crafting;

import com.google.gson.*;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import ic2ce.api.item.ElectricItem;
import ic2ce.api.recipe.IRecipeInput;
import ic2ce.compat.Ic2CraftingRecipe;
import ic2ce.neoforge.recipe.codec.NativeRecipeCodec;
import ic2ce.neoforge.recipe.codec.RecipeValueCodec;
import ic2ce.neoforge.registry.RecipeCodecs;
import java.util.*;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.*;
import net.minecraft.world.level.Level;

/** Exact positioned native crafting and full-grid UU assembly retain distinct layout protocols. */
public final class PositionedCrafting extends ShapedRecipe implements Ic2CraftingRecipe {
  public final IRecipeInput[] input;
  public final int inputWidth, inputHeight;
  public final boolean hidden, consuming;
  public int assemblyMask;
  private final IRecipeInput[] cells;
  private final ItemStackTemplate output;
  private final Identifier id;
  private final int occupiedMask;

  private PositionedCrafting(
      Identifier id,
      int width,
      int height,
      IRecipeInput[] cells,
      ItemStackTemplate output,
      boolean consuming,
      boolean hidden,
      int assemblyMask) {
    super(
        new Recipe.CommonInfo(!hidden),
        new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
        nativePattern(width, height, cells),
        Objects.requireNonNull(output));
    this.id = Objects.requireNonNull(id);
    inputWidth = width;
    inputHeight = height;
    this.cells = cells.clone();
    this.output = output;
    this.consuming = consuming;
    this.hidden = hidden;
    input = Arrays.stream(this.cells).filter(Objects::nonNull).toArray(IRecipeInput[]::new);
    this.assemblyMask = validateAssembly(assemblyMask);
    int mask = 0;
    for (int y = 0; y < height; y++)
      for (int x = 0; x < width; x++)
        if (this.cells[x + y * width] != null) mask |= 1 << (8 - x - y * 3);
    occupiedMask = mask;
  }

  private static int dimension(int size) {
    if (size < 1 || size > 3) throw new IllegalArgumentException("Crafting dimension outside 1..3");
    return size;
  }

  private static int validateAssembly(int mask) {
    if (mask < 0 || mask > 511)
      throw new IllegalArgumentException("UU grid mask outside nine physical cells");
    return mask;
  }

  private static ShapedRecipePattern nativePattern(int width, int height, IRecipeInput[] cells) {
    dimension(width);
    dimension(height);
    if (cells.length != width * height)
      throw new IllegalArgumentException("Incorrect native crafting cell count");
    var keys = new LinkedHashMap<Character, Ingredient>();
    var rows = new ArrayList<String>();
    for (int y = 0; y < height; y++) {
      var row = new StringBuilder(width);
      for (int x = 0; x < width; x++) {
        int index = x + y * width;
        var input = cells[index];
        if (input == null) row.append(' ');
        else {
          char symbol = (char) ('A' + index);
          row.append(symbol);
          keys.put(symbol, input.getIngredient());
        }
      }
      rows.add(row.toString());
    }
    if (keys.isEmpty()) throw new IllegalArgumentException("Empty shaped recipe");
    return ShapedRecipePattern.of(keys, rows);
  }

  public ItemStack getOutput() {
    return output.create();
  }

  @Override
  public boolean matches(CraftingInput grid, Level world) {
    if (grid.width() != inputWidth || grid.height() != inputHeight) return false;
    boolean normal = true, mirror = true;
    for (int y = 0; y < inputHeight; y++)
      for (int x = 0; x < inputWidth; x++) {
        var stack = grid.getItem(x + y * inputWidth);
        normal &= accepts(cells[x + y * inputWidth], stack);
        mirror &= accepts(cells[inputWidth - 1 - x + y * inputWidth], stack);
        if (!normal && !mirror) return false;
      }
    return normal || mirror;
  }

  private static boolean accepts(IRecipeInput input, ItemStack stack) {
    return input == null
        ? stack.isEmpty()
        : input.matches(stack) && stack.getCount() >= input.getAmount();
  }

  public boolean matchesAssembly(List<ItemStack> grid, Level world) {
    if (grid.size() != 9) return false;
    int mask = 0;
    for (var stack : grid) mask = mask << 1 | (stack.isEmpty() ? 0 : 1);
    return validateAssembly(assemblyMask) == mask && matches(CraftingInput.of(3, 3, grid), world);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public RecipeType<CraftingRecipe> getType() {
    return assemblyMask == 0
        ? RecipeType.CRAFTING
        : (RecipeType) ic2ce.neoforge.registry.RecipeKinds.UU_ASSEMBLY;
  }

  public ItemStack craft(CraftingInput grid) {
    return matches(grid, null) ? transferCharge(output.create(), grid) : ItemStack.EMPTY;
  }

  static ItemStack transferCharge(ItemStack result, CraftingInput grid) {
    double charge = 0;
    if (ElectricItem.manager != null) {
      for (var stack : grid.items())
        if (!stack.isEmpty()) {
          double present =
              ElectricItem.manager.getCharge(
                  stack.getCount() == 1 ? stack : stack.copyWithCount(1));
          if (present > 0 && !Double.isNaN(present)) charge += present;
        }
      if (charge > 0) ElectricItem.manager.charge(result, charge, Integer.MAX_VALUE, true, false);
    }
    return result;
  }

  @Override
  public ItemStack assemble(CraftingInput grid) {
    return craft(grid);
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput grid) {
    return CraftingRemainders.get(grid, consuming);
  }

  public static boolean canShow(Object[] ignored, ItemStack output, boolean hidden) {
    return !hidden
        || !ic2ce.neoforge.config.NativeConfigValues.getBool(
            ic2ce.neoforge.config.ProjectConfiguration.get(), "misc/hideSecretRecipes");
  }

  public boolean canShow() {
    return canShow(null, null, hidden);
  }

  @Override
  public int getIc2RecipeWidth() {
    return inputWidth;
  }

  @Override
  public int getIc2RecipeHeight() {
    return inputHeight;
  }

  public Identifier getId() {
    return id;
  }

  public List<Optional<Ingredient>> getIngredients() {
    return Arrays.stream(cells)
        .map(
            input ->
                input == null ? Optional.<Ingredient>empty() : Optional.of(input.getIngredient()))
        .toList();
  }

  @Override
  public List<RecipeDisplay> display() {
    return canShow()
        ? List.of(
            new ShapedCraftingRecipeDisplay(
                inputWidth,
                inputHeight,
                getIngredients().stream().map(Ingredient::optionalIngredientToDisplay).toList(),
                new SlotDisplay.ItemStackSlotDisplay(output),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)))
        : List.of();
  }

  @Override
  @SuppressWarnings("unchecked")
  public RecipeSerializer<ShapedRecipe> getSerializer() {
    return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) RecipeCodecs.SHAPED;
  }

  public static final class Serializer extends NativeRecipeCodec<PositionedCrafting> {
    @Override
    protected PositionedCrafting readJson(JsonObject json) {
      return readJson(
          json,
          net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(
                  net.minecraft.core.registries.BuiltInRegistries.REGISTRY)
              .createSerializationContext(JsonOps.INSTANCE));
    }

    @Override
    protected PositionedCrafting readJson(JsonObject source, DynamicOps<JsonElement> ops) {
      var json = source.deepCopy();
      var pattern = GsonHelper.getAsJsonArray(json, "pattern");
      int height = dimension(pattern.size());
      int width = dimension(pattern.get(0).getAsString().length());
      var symbols = GsonHelper.getAsJsonObject(json, "key");
      var inputs = new HashMap<Character, IRecipeInput>();
      for (var entry : symbols.entrySet()) {
        if (entry.getKey().length() != 1 || entry.getKey().equals(" "))
          throw new JsonSyntaxException("Invalid crafting key " + entry.getKey());
        inputs.put(
            entry.getKey().charAt(0), RecipeValueCodec.parseCraftingInput(entry.getValue(), ops));
      }
      var used = new HashSet<Character>();
      var cells = new IRecipeInput[width * height];
      for (int y = 0; y < height; y++) {
        String row = pattern.get(y).getAsString();
        if (row.length() != width)
          throw new JsonSyntaxException("Crafting rows have different widths");
        for (int x = 0; x < width; x++) {
          char symbol = row.charAt(x);
          if (symbol == ' ') continue;
          var input = inputs.get(symbol);
          if (input == null) throw new JsonSyntaxException("Undefined crafting symbol " + symbol);
          used.add(symbol);
          cells[x + y * width] = input;
        }
      }
      if (!used.equals(inputs.keySet())) throw new JsonSyntaxException("Unused crafting key");
      int minX = width, minY = height, maxX = -1, maxY = -1;
      for (int y = 0; y < height; y++)
        for (int x = 0; x < width; x++)
          if (cells[x + y * width] != null) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
          }
      if (maxX < 0) throw new JsonSyntaxException("Empty shaped recipe");
      int trimmedWidth = maxX - minX + 1, trimmedHeight = maxY - minY + 1;
      var trimmed = new IRecipeInput[trimmedWidth * trimmedHeight];
      for (int y = 0; y < trimmedHeight; y++)
        for (int x = 0; x < trimmedWidth; x++)
          trimmed[x + y * trimmedWidth] = cells[minX + x + (minY + y) * width];
      width = trimmedWidth;
      height = trimmedHeight;
      cells = trimmed;
      return new PositionedCrafting(
          placeholderId(),
          width,
          height,
          cells,
          RecipeValueCodec.parseOutputTemplate(GsonHelper.getAsJsonObject(json, "result"), ops),
          GsonHelper.getAsBoolean(json, "consuming", false),
          GsonHelper.getAsBoolean(json, "hidden", false),
          GsonHelper.getAsInt(json, "assemblyMask", 0));
    }

    @Override
    protected PositionedCrafting readNetwork(RegistryFriendlyByteBuf buffer) {
      int amount = buffer.readVarInt();
      if (amount < 1 || amount > 9)
        throw new IllegalArgumentException("Invalid shaped ingredient count");
      var compact = new IRecipeInput[amount];
      for (int index = 0; index < amount; index++)
        compact[index] = RecipeValueCodec.readInput(buffer);
      int width = dimension(buffer.readVarInt()), height = dimension(buffer.readVarInt());
      buffer.readBoolean();
      int mask = buffer.readInt();
      if (mask < 0 || mask > 511 || Integer.bitCount(mask) != amount)
        throw new IllegalArgumentException("Invalid shaped ingredient mask");
      var cells = new IRecipeInput[width * height];
      int next = 0;
      for (int y = 0; y < height; y++)
        for (int x = 0; x < width; x++)
          if ((mask & (1 << (8 - x - y * 3))) != 0) cells[x + y * width] = compact[next++];
      if (next != amount)
        throw new IllegalArgumentException("Crafting mask extends outside declared layout");
      var output = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
      if (output.isEmpty()) throw new IllegalArgumentException("Empty shaped output");
      boolean consuming = buffer.readBoolean(), hidden = buffer.readBoolean();
      return new PositionedCrafting(
          placeholderId(),
          width,
          height,
          cells,
          ItemStackTemplate.fromNonEmptyStack(output),
          consuming,
          hidden,
          buffer.readVarInt());
    }

    @Override
    protected void writeNetwork(RegistryFriendlyByteBuf buffer, PositionedCrafting recipe) {
      buffer.writeVarInt(recipe.input.length);
      for (var input : recipe.cells) if (input != null) RecipeValueCodec.writeInput(buffer, input);
      buffer.writeVarInt(recipe.inputWidth);
      buffer.writeVarInt(recipe.inputHeight);
      buffer.writeBoolean(true);
      buffer.writeInt(recipe.occupiedMask);
      ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.getOutput());
      buffer.writeBoolean(recipe.consuming);
      buffer.writeBoolean(recipe.hidden);
      buffer.writeVarInt(validateAssembly(recipe.assemblyMask));
    }

    @Override
    protected JsonObject writeJson(PositionedCrafting recipe) {
      var json = new JsonObject();
      var keys = new JsonObject();
      var rows = new JsonArray();
      for (int y = 0; y < recipe.inputHeight; y++) {
        var row = new StringBuilder();
        for (int x = 0; x < recipe.inputWidth; x++) {
          int index = x + y * recipe.inputWidth;
          var input = recipe.cells[index];
          if (input == null) row.append(' ');
          else {
            String key = String.valueOf((char) ('A' + index));
            row.append(key);
            keys.add(key, RecipeValueCodec.inputToJson(input));
          }
        }
        rows.add(row.toString());
      }
      json.add("key", keys);
      json.add("pattern", rows);
      json.add("result", RecipeValueCodec.resultToJson(recipe.getOutput()));
      json.addProperty("consuming", recipe.consuming);
      json.addProperty("hidden", recipe.hidden);
      json.addProperty("assemblyMask", recipe.assemblyMask);
      return json;
    }
  }
}
