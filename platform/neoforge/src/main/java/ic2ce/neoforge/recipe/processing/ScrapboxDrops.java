package ic2ce.neoforge.recipe.processing;

import ic2ce.api.recipe.*;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.SharedRandom;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/**
 * Per-manager immutable probability generations own drop templates and avoid shared static float
 * totals.
 */
public final class ScrapboxDrops implements IScrapboxManager {
  private record Entry(ItemStack template, double weight) {}

  private record Distribution(List<Entry> entries, double[] cumulative, double total) {}

  private record Builtin(ItemLike item, float weight) {}

  private volatile Distribution distribution = new Distribution(List.of(), new double[0], 0);
  private boolean builtinsLoaded;

  public static void setup() {
    if (Recipes.scrapboxDrops != null)
      throw new IllegalStateException("Scrapbox recipes already initialized");
    Recipes.scrapboxDrops = new ScrapboxDrops();
  }

  public static void load() {
    if (Recipes.scrapboxDrops instanceof ScrapboxDrops manager) manager.loadBuiltins();
  }

  ScrapboxDrops() {}

  private static Entry entry(ItemStack stack, float weight) {
    Objects.requireNonNull(stack);
    if (stack.isEmpty() || !Float.isFinite(weight) || weight <= 0)
      throw new IllegalArgumentException(
          "Nonempty scrapbox output and finite positive weight required");
    return new Entry(stack.copy(), weight);
  }

  private void publish(List<Entry> entries) {
    var cumulative = new double[entries.size()];
    double total = 0;
    for (int index = 0; index < entries.size(); index++) {
      total += entries.get(index).weight;
      cumulative[index] = total;
    }
    if (!Double.isFinite(total))
      throw new IllegalArgumentException("Scrapbox distribution overflow");
    distribution = new Distribution(List.copyOf(entries), cumulative, total);
  }

  public synchronized void addDrop(ItemStack stack, float weight) {
    var entries = new ArrayList<>(distribution.entries);
    entries.add(entry(stack, weight));
    publish(entries);
  }

  public synchronized boolean addRecipe(
      IRecipeInput input, Collection<ItemStack> outputs, CompoundTag metadata, boolean replace) {
    Objects.requireNonNull(input);
    Objects.requireNonNull(outputs);
    if (input.getAmount() != 1 || !input.matches(new ItemStack(ItemDefinitions.SCRAP_BOX)))
      throw new IllegalArgumentException("Scrapbox operations require one physical scrap box");
    if (outputs.size() != 1 || metadata == null || !metadata.contains("weight"))
      throw new IllegalArgumentException("Scrapbox operation needs one output and weight metadata");
    var output = entry(outputs.iterator().next(), metadata.getFloatOr("weight", 0));
    var entries = new ArrayList<>(distribution.entries);
    if (replace)
      entries.removeIf(
          previous -> ItemStack.isSameItemSameComponents(previous.template, output.template));
    entries.add(output);
    publish(entries);
    return true;
  }

  public boolean addRecipe(
      IRecipeInput input, CompoundTag metadata, boolean replace, ItemStack... outputs) {
    return addRecipe(input, Arrays.asList(outputs), metadata, replace);
  }

  private static ItemStack select(Distribution data, double fraction) {
    if (data.entries.isEmpty()) return ItemStack.EMPTY;
    double sample = Math.clamp(fraction, 0, Math.nextDown(1d)) * data.total;
    int low = 0, high = data.cumulative.length - 1;
    while (low < high) {
      int middle = (low + high) >>> 1;
      if (sample < data.cumulative[middle]) high = middle;
      else low = middle + 1;
    }
    return data.entries.get(low).template.copy();
  }

  ItemStack sample(double fraction) {
    return select(distribution, fraction);
  }

  @Override
  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(
      ItemStack input, boolean test) {
    if (input == null || input.isEmpty() || !input.is(ItemDefinitions.SCRAP_BOX)) return null;
    var data = distribution;
    if (data.entries.isEmpty()) return null;
    var output = select(data, test ? 0 : SharedRandom.INSTANCE.nextDouble());
    return new MachineRecipe<IRecipeInput, Collection<ItemStack>>(
            Recipes.inputFactory.forItem(ItemDefinitions.SCRAP_BOX), List.of(output))
        .getResult(input.copyWithCount(input.getCount() - 1));
  }

  @Override
  public RecipeOutput getOutputFor(ItemStack input, boolean adjust) {
    var result = apply(input, false);
    if (result == null) return null;
    if (adjust) input.setCount(result.getAdjustedInput().getCount());
    return new RecipeOutput(null, new ArrayList<>(result.getOutput()));
  }

  @Override
  public ItemStack getDrop(ItemStack input, boolean adjust) {
    var result = apply(input, false);
    if (result == null) return null;
    if (adjust) input.setCount(result.getAdjustedInput().getCount());
    return result.getOutput().iterator().next().copy();
  }

  @Override
  public boolean isIterable() {
    return false;
  }

  @Override
  public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
    throw new UnsupportedOperationException("Scrapbox selection is one weighted operation");
  }

  @Override
  public Map<ItemStack, Float> getDrops() {
    var data = distribution;
    var result = new LinkedHashMap<ItemStack, Float>();
    for (var entry : data.entries)
      result.put(entry.template.copy(), (float) (entry.weight / data.total));
    return Collections.unmodifiableMap(result);
  }

  private synchronized void loadBuiltins() {
    if (builtinsLoaded) return;
    var defaults =
        List.of(
            new Builtin(Items.WOODEN_HOE, 5.01f),
            new Builtin(Blocks.DIRT, 5),
            new Builtin(Items.STICK, 4),
            new Builtin(Blocks.SHORT_GRASS, 3),
            new Builtin(Blocks.GRAVEL, 3),
            new Builtin(Blocks.NETHERRACK, 2),
            new Builtin(Items.ROTTEN_FLESH, 2),
            new Builtin(Items.APPLE, 1.5f),
            new Builtin(Items.BREAD, 1.5f),
            new Builtin(ItemDefinitions.FILLED_TIN_CAN, 1.5f),
            new Builtin(Items.WOODEN_SWORD, 1),
            new Builtin(Items.WOODEN_SHOVEL, 1),
            new Builtin(Items.WOODEN_PICKAXE, 1),
            new Builtin(Blocks.SOUL_SAND, 1),
            new Builtin(Items.OAK_SIGN, 1),
            new Builtin(Items.LEATHER, 1),
            new Builtin(Items.FEATHER, 1),
            new Builtin(Items.BONE, 1),
            new Builtin(Items.COOKED_PORKCHOP, .9f),
            new Builtin(Items.COOKED_BEEF, .9f),
            new Builtin(Blocks.PUMPKIN, .9f),
            new Builtin(Items.COOKED_CHICKEN, .9f),
            new Builtin(Items.MINECART, .01f),
            new Builtin(Items.REDSTONE, .9f),
            new Builtin(ItemDefinitions.RUBBER, .8f),
            new Builtin(Items.GLOWSTONE_DUST, .8f),
            new Builtin(ItemDefinitions.COAL_DUST, .8f),
            new Builtin(ItemDefinitions.COPPER_DUST, .8f),
            new Builtin(ItemDefinitions.TIN_DUST, .8f),
            new Builtin(ItemDefinitions.SINGLE_USE_BATTERY, .7f),
            new Builtin(ItemDefinitions.IRON_DUST, .7f),
            new Builtin(ItemDefinitions.GOLD_DUST, .7f),
            new Builtin(Items.SLIME_BALL, .6f),
            new Builtin(Blocks.IRON_ORE, .5f),
            new Builtin(Items.GOLDEN_HELMET, .01f),
            new Builtin(Blocks.GOLD_ORE, .5f),
            new Builtin(Items.CAKE, .5f),
            new Builtin(Items.DIAMOND, .1f),
            new Builtin(Items.EMERALD, .05f),
            new Builtin(Items.ENDER_PEARL, .08f),
            new Builtin(Items.BLAZE_ROD, .04f),
            new Builtin(Items.EGG, .8f),
            new Builtin(Blocks.COPPER_ORE, .7f),
            new Builtin(ItemDefinitions.TIN_ORE, .7f));
    var entries = new ArrayList<>(distribution.entries);
    for (var drop : defaults) entries.add(entry(new ItemStack(drop.item), drop.weight));
    publish(entries);
    builtinsLoaded = true;
  }
}
