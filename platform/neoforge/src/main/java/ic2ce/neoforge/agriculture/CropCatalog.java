package ic2ce.neoforge.agriculture;

import ic2ce.api.crops.*;
import ic2ce.core.util.StackTag;
import ic2ce.engine.agriculture.CropMath;
import ic2ce.neoforge.agriculture.species.BeetSpecies;
import ic2ce.neoforge.agriculture.species.CarnivorousSpecies;
import ic2ce.neoforge.agriculture.species.CarrotSpecies;
import ic2ce.neoforge.agriculture.species.CocoaSpecies;
import ic2ce.neoforge.agriculture.species.CoffeeSpecies;
import ic2ce.neoforge.agriculture.species.FlaxSpecies;
import ic2ce.neoforge.agriculture.species.FrostWartSpecies;
import ic2ce.neoforge.agriculture.species.HopSpecies;
import ic2ce.neoforge.agriculture.species.MelonSpecies;
import ic2ce.neoforge.agriculture.species.MetalSpecies;
import ic2ce.neoforge.agriculture.species.MushroomSpecies;
import ic2ce.neoforge.agriculture.species.PigmentSpecies;
import ic2ce.neoforge.agriculture.species.PotatoSpecies;
import ic2ce.neoforge.agriculture.species.PreciousMetalSpecies;
import ic2ce.neoforge.agriculture.species.PumpkinSpecies;
import ic2ce.neoforge.agriculture.species.RedstoneGrainSpecies;
import ic2ce.neoforge.agriculture.species.ReedSpecies;
import ic2ce.neoforge.agriculture.species.ResinReedSpecies;
import ic2ce.neoforge.agriculture.species.SoulWartSpecies;
import ic2ce.neoforge.agriculture.species.TreeSpecies;
import ic2ce.neoforge.agriculture.species.VenomSpecies;
import ic2ce.neoforge.agriculture.species.WeedSpecies;
import ic2ce.neoforge.agriculture.species.WheatSpecies;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockGroups;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;

/**
 * Immutable published crop generations; direct native block and seed keys; duplicate-safe
 * registration.
 */
public class CropCatalog extends Crops {
  public static CropCard cropWheat = new WheatSpecies(Cultivar.WHEAT);
  public static CropCard cropPumpkin = new PumpkinSpecies(Cultivar.PUMPKIN);
  public static CropCard cropMelon = new MelonSpecies(Cultivar.MELON);
  public static CropCard cropYellowFlower =
      new PigmentSpecies(
          Cultivar.DANDELION,
          BlockDefinitions.DANDELION_CROP,
          new String[] {"Yellow", "Flower"},
          DyeColor.YELLOW);
  public static CropCard cropRedFlower =
      new PigmentSpecies(
          Cultivar.POPPY,
          BlockDefinitions.POPPY_CROP,
          new String[] {"Red", "Flower", "Rose"},
          DyeColor.RED);
  public static CropCard cropBlackFlower =
      new PigmentSpecies(
          Cultivar.BLACKTHORN,
          BlockDefinitions.BLACKTHORN_CROP,
          new String[] {"Black", "Flower", "Rose"},
          DyeColor.BLACK);
  public static CropCard cropPurpleFlower =
      new PigmentSpecies(
          Cultivar.TULIP,
          BlockDefinitions.TULIP_CROP,
          new String[] {"Purple", "Flower", "Tulip"},
          DyeColor.PURPLE);
  public static CropCard cropBlueFlower =
      new PigmentSpecies(
          Cultivar.CYAZINT,
          BlockDefinitions.CYAZINT_CROP,
          new String[] {"Blue", "Flower"},
          DyeColor.CYAN);
  public static CropCard cropVenomilia = new VenomSpecies(Cultivar.VENOMILIA);
  public static CropCard cropReed = new ReedSpecies(Cultivar.REED);
  public static CropCard cropStickyReed = new ResinReedSpecies(Cultivar.STICKY_REED);
  public static CropCard cropCocoa = new CocoaSpecies(Cultivar.COCOA);
  public static CropCard cropFlax = new FlaxSpecies(Cultivar.FLAX);
  public static CropCard cropRedMushroom =
      new MushroomSpecies(
          Cultivar.RED_MUSHROOM,
          BlockDefinitions.RED_MUSHROOM_CROP,
          new String[] {"Red", "Food", "Mushroom"},
          new ItemStack(Blocks.RED_MUSHROOM));
  public static CropCard cropBrownMushroom =
      new MushroomSpecies(
          Cultivar.BROWN_MUSHROOM,
          BlockDefinitions.BROWN_MUSHROOM_CROP,
          new String[] {"Brown", "Food", "Mushroom"},
          new ItemStack(Blocks.BROWN_MUSHROOM));
  public static CropCard cropNetherWart = new SoulWartSpecies(Cultivar.NETHER_WART);
  public static CropCard cropTerraWart = new FrostWartSpecies(Cultivar.TERRA_WART);
  public static CropCard cropOakSapling =
      new TreeSpecies(
          Cultivar.OAK_SAPLING,
          BlockDefinitions.OAK_SAPLING_CROP,
          "acorns",
          new ItemStack(Blocks.OAK_LOG),
          new ItemStack(Blocks.OAK_SAPLING));
  public static CropCard cropSpruceSapling =
      new TreeSpecies(
          Cultivar.SPRUCE_SAPLING,
          BlockDefinitions.SPRUCE_SAPLING_CROP,
          "pine_cones",
          new ItemStack(Blocks.SPRUCE_LOG),
          new ItemStack(Blocks.SPRUCE_SAPLING));
  public static CropCard cropBirchSapling =
      new TreeSpecies(
          Cultivar.BIRCH_SAPLING,
          BlockDefinitions.BIRCH_SAPLING_CROP,
          "catkins",
          new ItemStack(Blocks.BIRCH_LOG),
          new ItemStack(Blocks.BIRCH_SAPLING));
  public static CropCard cropJungleSapling =
      new TreeSpecies(
          Cultivar.JUNGLE_SAPLING,
          BlockDefinitions.JUNGLE_SAPLING_CROP,
          "seedling",
          new ItemStack(Blocks.JUNGLE_LOG),
          new ItemStack(Blocks.JUNGLE_SAPLING));
  public static CropCard cropAcaciaSapling =
      new TreeSpecies(
          Cultivar.ACACIA_SAPLING,
          BlockDefinitions.ACACIA_SAPLING_CROP,
          "seedling",
          new ItemStack(Blocks.ACACIA_LOG),
          new ItemStack(Blocks.ACACIA_SAPLING));
  public static CropCard cropDarkOakSapling =
      new TreeSpecies(
          Cultivar.DARK_OAK_SAPLING,
          BlockDefinitions.DARK_OAK_SAPLING_CROP,
          "acorns",
          new ItemStack(Blocks.DARK_OAK_LOG),
          new ItemStack(Blocks.DARK_OAK_SAPLING));
  public static CropCard cropFerru =
      new MetalSpecies(
          Cultivar.FERRU,
          BlockDefinitions.FERRU_CROP,
          new String[] {"Gray", "Leaves", "Metal"},
          Arrays.asList(BlockTags.IRON_ORES, BlockGroups.IRON_BLOCKS),
          new ItemStack(ItemDefinitions.SMALL_IRON_DUST));
  public static CropCard cropCyprium =
      new MetalSpecies(
          Cultivar.CYPRIUM,
          BlockDefinitions.CYPRIUM_CROP,
          new String[] {"Orange", "Leaves", "Metal"},
          Arrays.asList(BlockTags.COPPER_ORES, BlockGroups.COPPER_BLOCKS),
          new ItemStack(ItemDefinitions.SMALL_COPPER_DUST));
  public static CropCard cropStagnium =
      new MetalSpecies(
          Cultivar.STAGNIUM,
          BlockDefinitions.STAGNIUM_CROP,
          new String[] {"Shiny", "Leaves", "Metal"},
          Arrays.asList(BlockGroups.TIN_ORES, BlockGroups.TIN_BLOCKS),
          new ItemStack(ItemDefinitions.SMALL_TIN_DUST));
  public static CropCard cropPlumbiscus =
      new MetalSpecies(
          Cultivar.PLUMBISCUS,
          BlockDefinitions.PLUMBISCUS_CROP,
          new String[] {"Dense", "Leaves", "Metal"},
          Arrays.asList(BlockGroups.LEAD_ORES, BlockGroups.LEAD_BLOCKS),
          new ItemStack(ItemDefinitions.SMALL_LEAD_DUST));
  public static CropCard cropAurelia =
      new PreciousMetalSpecies(
          Cultivar.AURELIA,
          BlockDefinitions.AURELIA_CROP,
          new String[] {"Gold", "Leaves", "Metal"},
          Arrays.asList(BlockTags.GOLD_ORES, BlockGroups.GOLD_BLOCKS),
          new ItemStack(ItemDefinitions.SMALL_GOLD_DUST));
  public static CropCard cropShining =
      new PreciousMetalSpecies(
          Cultivar.SHINING,
          BlockDefinitions.SHINING_CROP,
          new String[] {"Silver", "Leaves", "Metal"},
          Arrays.asList(BlockGroups.SILVER_ORES, BlockGroups.SILVER_BLOCKS),
          new ItemStack(ItemDefinitions.SMALL_SILVER_DUST));
  public static CropCard cropRedWheat = new RedstoneGrainSpecies(Cultivar.RED_WHEAT);
  public static CropCard cropCoffee = new CoffeeSpecies(Cultivar.COFFEE);
  public static CropCard cropHops = new HopSpecies(Cultivar.HOPS);
  public static CropCard cropCarrots = new CarrotSpecies(Cultivar.CARROTS);
  public static CropCard cropPotato = new PotatoSpecies(Cultivar.POTATO);
  public static CropCard cropEatingPlant = new CarnivorousSpecies(Cultivar.EATING_PLANT);
  public static CropCard cropBeetroots = new BeetSpecies(Cultivar.BEETROOTS);

  public static void registerBaseSeeds() {
    Crops.instance.registerBaseSeed(new ItemStack(Items.WHEAT_SEEDS), cropWheat, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.PUMPKIN_SEEDS), cropPumpkin, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.MELON_SEEDS), cropMelon, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.NETHER_WART), cropNetherWart, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(
        new ItemStack(ItemDefinitions.TERRA_WART), cropTerraWart, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(
        new ItemStack(ItemDefinitions.COFFEE_BEANS), cropCoffee, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.SUGAR_CANE), cropReed, 0, 3, 0, 2);
    Crops.instance.registerBaseSeed(new ItemStack(Items.COCOA_BEANS), cropCocoa, 0, 0, 0, 0);
    Crops.instance.registerBaseSeed(new ItemStack(Items.POPPY, 4), cropRedFlower, 3, 1, 1, 1);
    Crops.instance.registerBaseSeed(
        new ItemStack(Items.DANDELION, 4), cropYellowFlower, 3, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.CARROT), cropCarrots, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.POTATO, 1), cropPotato, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(
        new ItemStack(Blocks.BROWN_MUSHROOM, 4), cropBrownMushroom, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(
        new ItemStack(Blocks.RED_MUSHROOM, 4), cropRedMushroom, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Blocks.CACTUS), cropEatingPlant, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.BEETROOT_SEEDS), cropBeetroots, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Blocks.OAK_SAPLING), cropOakSapling, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(
        new ItemStack(Blocks.SPRUCE_SAPLING), cropSpruceSapling, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(
        new ItemStack(Blocks.BIRCH_SAPLING), cropBirchSapling, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(
        new ItemStack(Blocks.JUNGLE_SAPLING), cropJungleSapling, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(
        new ItemStack(Blocks.ACACIA_SAPLING), cropAcaciaSapling, 0, 1, 1, 1);
    Crops.instance.registerBaseSeed(
        new ItemStack(Blocks.DARK_OAK_SAPLING), cropDarkOakSapling, 0, 1, 1, 1);
  }

  private record Name(String owner, String id) {}

  private record SeedKey(Item item, int damage) {}

  private record SeedSpec(CropCard species, int age, CropMath.Genome genes, int quantity) {
    BaseSeed copy() {
      return new BaseSeed(species, age, genes.growth(), genes.gain(), genes.resistance());
    }
  }

  private record View(
      Map<Name, CropCard> names,
      Map<Block, CropCard> blocks,
      List<CropCard> species,
      Map<SeedKey, SeedSpec> seeds,
      Map<BiomeType, Integer> humidity,
      Map<BiomeType, Integer> nutrients) {}

  private volatile View view =
      new View(Map.of(), Map.of(), List.of(), Map.of(), Map.of(), Map.of());

  private record BiomeBonus(int humidity, int nutrients) {}

  private final Map<Holder<Biome>, BiomeBonus> biomeCache = new WeakHashMap<>();

  public static synchronized void init() {
    if (Crops.instance instanceof CropCatalog) return;
    var catalog = new CropCatalog();
    Crops.instance = catalog;
    Crops.weed = new WeedSpecies(Cultivar.WEED);
    var bonuses =
        Map.ofEntries(
            Map.entry(BiomeType.JUNGLE, 10),
            Map.entry(BiomeType.SWAMP, 10),
            Map.entry(BiomeType.MUSHROOM, 5),
            Map.entry(BiomeType.FOREST, 5),
            Map.entry(BiomeType.RIVER, 2),
            Map.entry(BiomeType.PLAINS, 0),
            Map.entry(BiomeType.SAVANNA, -2),
            Map.entry(BiomeType.HILLS, -5),
            Map.entry(BiomeType.MOUNTAIN, -5),
            Map.entry(BiomeType.WASTELAND, -8),
            Map.entry(BiomeType.END, -10),
            Map.entry(BiomeType.NETHER, -10),
            Map.entry(BiomeType.DEAD, -10));
    bonuses.forEach(catalog::addBiomenutrientsBonus);
    registerCrops();
    registerBaseSeeds();
  }

  public static void ensureInit() {
    /* Class loading remains safe before native item registration. */
  }

  public static void registerCrops() {
    for (var species :
        List.of(
            weed,
            cropWheat,
            cropPumpkin,
            cropMelon,
            cropYellowFlower,
            cropRedFlower,
            cropBlackFlower,
            cropPurpleFlower,
            cropBlueFlower,
            cropVenomilia,
            cropReed,
            cropStickyReed,
            cropCocoa,
            cropFlax,
            cropFerru,
            cropAurelia,
            cropRedWheat,
            cropNetherWart,
            cropTerraWart,
            cropCoffee,
            cropHops,
            cropCarrots,
            cropPotato,
            cropRedMushroom,
            cropBrownMushroom,
            cropEatingPlant,
            cropCyprium,
            cropStagnium,
            cropPlumbiscus,
            cropShining,
            cropBeetroots,
            cropOakSapling,
            cropSpruceSapling,
            cropBirchSapling,
            cropJungleSapling,
            cropAcaciaSapling,
            cropDarkOakSapling)) Crops.instance.registerCrop(species);
  }

  private synchronized void bonus(BiomeType type, int amount, boolean humidity) {
    Objects.requireNonNull(type);
    var prior = view;
    var next = new EnumMap<BiomeType, Integer>(BiomeType.class);
    next.putAll(humidity ? prior.humidity : prior.nutrients);
    next.put(type, amount);
    view =
        new View(
            prior.names,
            prior.blocks,
            prior.species,
            prior.seeds,
            humidity ? Map.copyOf(next) : prior.humidity,
            humidity ? prior.nutrients : Map.copyOf(next));
    biomeCache.clear();
  }

  @Override
  public void addBiomenutrientsBonus(BiomeType type, int amount) {
    bonus(type, amount, false);
  }

  @Override
  public void addBiomehumidityBonus(BiomeType type, int amount) {
    bonus(type, amount, true);
  }

  public synchronized void invalidateBiomeCache() {
    biomeCache.clear();
  }

  private synchronized BiomeBonus biomeBonus(Holder<Biome> biome) {
    if (biome == null) return new BiomeBonus(0, 0);
    var cached = biomeCache.get(biome);
    if (cached != null) return cached;
    int humidity = Integer.MIN_VALUE, nutrients = Integer.MIN_VALUE;
    var generation = view;
    for (var type : ProjectRuntime.envProxy.getBiomeTypes(biome)) {
      var water = generation.humidity.get(type);
      if (water != null) humidity = Math.max(humidity, water);
      var food = generation.nutrients.get(type);
      if (food != null) nutrients = Math.max(nutrients, food);
    }
    var computed =
        new BiomeBonus(
            humidity == Integer.MIN_VALUE ? 0 : humidity,
            nutrients == Integer.MIN_VALUE ? 0 : nutrients);
    biomeCache.put(biome, computed);
    return computed;
  }

  @Override
  public int getHumidityBiomeBonus(Holder<Biome> biome) {
    return biomeBonus(biome).humidity;
  }

  @Override
  public int getNutrientBiomeBonus(Holder<Biome> biome) {
    return biomeBonus(biome).nutrients;
  }

  @Override
  public CropCard getCropCard(String owner, String id) {
    return owner == null || id == null ? null : view.names.get(new Name(owner, id));
  }

  @Override
  public CropCard getCropCard(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return null;
    var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
    if (stack.is(ItemTags.SAPLINGS) && itemId.getNamespace().equals("minecraft"))
      return getCropCard("ic2", itemId.getPath());
    var data = StackTag.get(stack);
    return data == null
        ? null
        : getCropCard(data.getStringOr("owner", ""), data.getStringOr("id", ""));
  }

  @Override
  public CropCard getCropCard(Block block) {
    return block == null ? null : view.blocks.get(block);
  }

  @Override
  public Collection<CropCard> getCrops() {
    return view.species;
  }

  @Override
  public synchronized void registerCrop(CropCard species) {
    Objects.requireNonNull(species);
    var name =
        new Name(
            Objects.requireNonNull(species.getOwner()), Objects.requireNonNull(species.getId()));
    if (name.owner.isEmpty()
        || name.id.isEmpty()
        || !name.owner.equals(name.owner.toLowerCase(Locale.ROOT)))
      throw new IllegalArgumentException("Invalid crop name: " + name);
    var block = Objects.requireNonNull(species.getCropBlock());
    var prior = view;
    var existing = prior.names.get(name);
    if (existing == species) return;
    if (existing != null || prior.blocks.containsKey(block))
      throw new IllegalArgumentException("Duplicate crop name or native block: " + name);
    var names = new LinkedHashMap<>(prior.names);
    names.put(name, species);
    var blocks = new IdentityHashMap<>(prior.blocks);
    blocks.put(block, species);
    var ordered = new ArrayList<>(prior.species);
    ordered.add(species);
    view =
        new View(
            Map.copyOf(names),
            Collections.unmodifiableMap(blocks),
            List.copyOf(ordered),
            prior.seeds,
            prior.humidity,
            prior.nutrients);
  }

  @Override
  public synchronized boolean registerBaseSeed(
      ItemStack stack, CropCard species, int age, int growth, int gain, int resistance) {
    if (stack == null || stack.isEmpty() || species == null) return false;
    var key = new SeedKey(stack.getItem(), stack.getDamageValue());
    var prior = view;
    if (prior.seeds.containsKey(key)) return false;
    var seeds = new HashMap<>(prior.seeds);
    seeds.put(
        key,
        new SeedSpec(
            species,
            Math.clamp(age, 0, Math.max(0, species.getMaxAge())),
            CropMath.Genome.bounded(growth, gain, resistance),
            stack.getCount()));
    view =
        new View(
            prior.names,
            prior.blocks,
            prior.species,
            Map.copyOf(seeds),
            prior.humidity,
            prior.nutrients);
    return true;
  }

  private SeedSpec seed(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return null;
    var result = view.seeds.get(new SeedKey(stack.getItem(), stack.getDamageValue()));
    return result != null && stack.getCount() >= result.quantity ? result : null;
  }

  @Override
  public BaseSeed getBaseSeed(ItemStack stack) {
    var result = seed(stack);
    return result == null ? null : result.copy();
  }

  public int requiredSeedQuantity(ItemStack stack) {
    var result = seed(stack);
    return result == null ? 0 : result.quantity;
  }
}
