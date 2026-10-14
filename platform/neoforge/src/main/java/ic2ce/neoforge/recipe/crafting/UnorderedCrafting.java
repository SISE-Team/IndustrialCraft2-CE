package ic2ce.neoforge.recipe.crafting;

import com.google.gson.*;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import ic2ce.api.recipe.IRecipeInput;
import ic2ce.engine.recipe.IngredientAssignment;
import ic2ce.neoforge.recipe.codec.NativeRecipeCodec;
import ic2ce.neoforge.recipe.codec.RecipeValueCodec;
import ic2ce.neoforge.registry.RecipeCodecs;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.*;
import net.minecraft.world.level.Level;

/**
 * Unordered native crafting uses bipartite assignment and independently positioned tool remainders.
 */
public final class UnorderedCrafting implements CraftingRecipe {
  public final IRecipeInput[] input;
  public final boolean hidden, consuming;
  private final IRecipeInput[] predicates;
  private final ItemStackTemplate output;
  private final Identifier id;
  private volatile PlacementInfo placement;

  public UnorderedCrafting(
      Identifier id,
      IRecipeInput[] inputs,
      Supplier<ItemStackTemplate> output,
      boolean hidden,
      boolean consuming) {
    this.id = Objects.requireNonNull(id);
    validateSize(inputs.length);
    predicates = inputs.clone();
    for (var predicate : predicates) Objects.requireNonNull(predicate);
    input = predicates.clone();
    this.output = Objects.requireNonNull(output.get());
    this.hidden = hidden;
    this.consuming = consuming;
  }

  private static int validateSize(int size) {
    if (size < 1 || size > 9)
      throw new IllegalArgumentException("Shapeless input count outside 1..9");
    return size;
  }

  public ItemStack getOutput() {
    return output.create();
  }

  @Override
  public boolean matches(CraftingInput grid, Level world) {
    var offered = grid.items().stream().filter(stack -> !stack.isEmpty()).toList();
    if (offered.size() != predicates.length) return false;
    int[] candidates = new int[predicates.length];
    for (int ingredient = 0; ingredient < predicates.length; ingredient++) {
      var predicate = predicates[ingredient];
      for (int slot = 0; slot < offered.size(); slot++) {
        var stack = offered.get(slot);
        if (stack.getCount() >= predicate.getAmount() && predicate.matches(stack))
          candidates[ingredient] |= 1 << slot;
      }
      if (candidates[ingredient] == 0) return false;
    }
    return IngredientAssignment.matches(candidates, predicates.length);
  }

  public ItemStack craft(CraftingInput grid) {
    return matches(grid, null)
        ? PositionedCrafting.transferCharge(output.create(), grid)
        : ItemStack.EMPTY;
  }

  @Override
  public ItemStack assemble(CraftingInput grid) {
    return craft(grid);
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput grid) {
    return CraftingRemainders.get(grid, consuming);
  }

  public boolean canShow() {
    return PositionedCrafting.canShow(null, null, hidden);
  }

  @Override
  public PlacementInfo placementInfo() {
    var current = placement;
    if (current == null) {
      synchronized (this) {
        current = placement;
        if (current == null)
          placement =
              current =
                  PlacementInfo.create(
                      Arrays.stream(predicates).map(IRecipeInput::getIngredient).toList());
      }
    }
    return current;
  }

  @Override
  public List<RecipeDisplay> display() {
    return canShow()
        ? List.of(
            new ShapelessCraftingRecipeDisplay(
                Arrays.stream(predicates).map(input -> input.getIngredient().display()).toList(),
                new SlotDisplay.ItemStackSlotDisplay(output),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)))
        : List.of();
  }

  @Override
  public boolean showNotification() {
    return !hidden;
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
    return id;
  }

  @Override
  public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
    return RecipeCodecs.SHAPELESS;
  }

  public static final class Serializer extends NativeRecipeCodec<UnorderedCrafting> {
    @Override
    protected UnorderedCrafting readJson(JsonObject json) {
      return readJson(
          json,
          net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(
                  net.minecraft.core.registries.BuiltInRegistries.REGISTRY)
              .createSerializationContext(JsonOps.INSTANCE));
    }

    @Override
    protected UnorderedCrafting readJson(JsonObject source, DynamicOps<JsonElement> ops) {
      var json = source.deepCopy();
      var array = GsonHelper.getAsJsonArray(json, "ingredients");
      var inputs = new IRecipeInput[validateSize(array.size())];
      for (int index = 0; index < inputs.length; index++)
        inputs[index] = RecipeValueCodec.parseCraftingInput(array.get(index), ops);
      var output =
          RecipeValueCodec.parseOutputTemplate(GsonHelper.getAsJsonObject(json, "result"), ops);
      return new UnorderedCrafting(
          placeholderId(),
          inputs,
          () -> output,
          GsonHelper.getAsBoolean(json, "hidden", false),
          GsonHelper.getAsBoolean(json, "consuming", false));
    }

    @Override
    protected UnorderedCrafting readNetwork(RegistryFriendlyByteBuf buffer) {
      var inputs = new IRecipeInput[validateSize(buffer.readVarInt())];
      for (int index = 0; index < inputs.length; index++)
        inputs[index] = RecipeValueCodec.readInput(buffer);
      var stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
      if (stack.isEmpty()) throw new IllegalArgumentException("Empty shapeless output");
      boolean hidden = buffer.readBoolean(), consuming = buffer.readBoolean();
      var output = ItemStackTemplate.fromNonEmptyStack(stack);
      return new UnorderedCrafting(placeholderId(), inputs, () -> output, hidden, consuming);
    }

    @Override
    protected void writeNetwork(RegistryFriendlyByteBuf buffer, UnorderedCrafting recipe) {
      buffer.writeVarInt(recipe.predicates.length);
      for (var input : recipe.predicates) RecipeValueCodec.writeInput(buffer, input);
      ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.getOutput());
      buffer.writeBoolean(recipe.hidden);
      buffer.writeBoolean(recipe.consuming);
    }

    @Override
    protected JsonObject writeJson(UnorderedCrafting recipe) {
      var json = new JsonObject();
      var inputs = new JsonArray();
      for (var input : recipe.predicates) inputs.add(RecipeValueCodec.inputToJson(input));
      json.add("ingredients", inputs);
      json.add("result", RecipeValueCodec.resultToJson(recipe.getOutput()));
      json.addProperty("hidden", recipe.hidden);
      json.addProperty("consuming", recipe.consuming);
      return json;
    }
  }
}
