package ic2ce.neoforge.agriculture;

import ic2ce.api.crops.*;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.fluid.FluidHandler;
import ic2ce.neoforge.runtime.SharedRandom;
import ic2ce.neoforge.runtime.SideServices;
import ic2ce.neoforge.world.BiomeUtil;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.engine.agriculture.CropMath;
import ic2ce.engine.recipe.WeightedChoice;
import ic2ce.neoforge.agriculture.item.EncodedSeed;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.item.container.FixedCell;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * Server-owned crop phenotype, paid harvests and loaded-neighbor breeding; material IDs remain
 * stable.
 */
public class CropPlot extends NativeBlockEntity implements ICropTile {
  public boolean dirty = true;
  public static int tickRate = 256;
  public static final boolean debug = Boolean.getBoolean("ic2.crops.debug");
  private CropCard crop;
  private int statGrowth, statGain, statResistance, storageNutrients, storageWater, storageWeedEX;
  private int terrainAirQuality,
      terrainHumidity,
      terrainNutrients,
      currentAge,
      growthPoints,
      scanLevel;
  private int scheduledRate, scheduledPeriod, scheduledPhase, scheduledMask;
  private long lastGrowthTime = Long.MIN_VALUE;
  private boolean scheduledTicked;
  private int pendingFields = (1 << 14) - 1;
  private boolean persistenceMarked;
  private final BlockPos.MutableBlockPos terrainCursor = new BlockPos.MutableBlockPos();
  private CompoundTag customData = new CompoundTag();
  private static final List<AABB> OUTLINE = List.of(new AABB(.2, 0, .2, .8, .85, .8)),
      COLLISION = List.of(new AABB(0, 0, 0, 0, 0, 0));
  private static final Direction[] NEIGHBORS = {
    Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
  };
  private static final String[] FIELDS = {
    "crop",
    "currentAge",
    "statGrowth",
    "statGain",
    "statResistance",
    "storageNutrients",
    "storageWater",
    "storageWeedEX",
    "terrainHumidity",
    "terrainNutrients",
    "terrainAirQuality",
    "growthPoints",
    "scanLevel",
    "customData"
  };

  public CropPlot(BlockPos position, BlockState state) {
    super(
        BlockEntityDefinitions.get(BuiltInRegistries.BLOCK.getKey(state.getBlock())),
        position,
        state);
    crop = Crops.instance.getCropCard(getBlockType());
  }

  private boolean server() {
    return getLevel() != null && !getLevel().isClientSide() && !isRemoved();
  }

  private CropMath.Genome genome() {
    return CropMath.Genome.bounded(statGrowth, statGain, statResistance);
  }

  private void changed() {
    changed((1 << FIELDS.length) - 1);
  }

  private void changed(int fields) {
    pendingFields |= fields;
    dirty = true;
    if (!persistenceMarked) {
      persistenceMarked = true;
      setChanged();
    }
    var world = getLevel();
    if (world != null && !world.isClientSide()) {
      var data = ic2ce.neoforge.world.WorldData.get(world, false);
      if (data != null) data.cropTicks.queueDirty(this);
    }
  }

  @Override
  public void setChanged() {
    var world = getLevel();
    if (world != null) world.blockEntityChanged(worldPosition);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    statGrowth = Math.clamp(data.getIntOr("statGrowth", 0), 0, 31);
    statGain = Math.clamp(data.getIntOr("statGain", 0), 0, 31);
    statResistance = Math.clamp(data.getIntOr("statResistance", 0), 0, 31);
    storageNutrients = Math.clamp(data.getIntOr("storageNutrients", 0), 0, 200);
    storageWater = Math.clamp(data.getIntOr("storageWater", 0), 0, 200);
    storageWeedEX = Math.clamp(data.getIntOr("storageWeedEX", 0), 0, 150);
    terrainHumidity = Math.clamp(data.getIntOr("terrainHumidity", 0), 0, 127);
    terrainNutrients = Math.clamp(data.getIntOr("terrainNutrients", 0), 0, 127);
    terrainAirQuality = Math.clamp(data.getIntOr("terrainAirQuality", 0), 0, 127);
    currentAge = Math.clamp(data.getIntOr("currentAge", 0), 0, crop == null ? 0 : crop.getMaxAge());
    growthPoints = Math.max(0, data.getIntOr("growthPoints", 0));
    scanLevel = Math.clamp(data.getIntOr("scanLevel", 0), 0, 4);
    customData = data.getCompoundOrEmpty("customData").copy();
    dirty = true;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("statGrowth", statGrowth);
    data.putInt("statGain", statGain);
    data.putInt("statResistance", statResistance);
    data.putInt("storageNutrients", storageNutrients);
    data.putInt("storageWater", storageWater);
    data.putInt("storageWeedEX", storageWeedEX);
    data.putInt("terrainHumidity", terrainHumidity);
    data.putInt("terrainNutrients", terrainNutrients);
    data.putInt("terrainAirQuality", terrainAirQuality);
    data.putInt("currentAge", getCurrentAge());
    data.putInt("growthPoints", growthPoints);
    data.putInt("scanLevel", scanLevel);
    data.put("customData", customData.copy());
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = super.getNetworkedFields();
    fields.addAll(List.of(FIELDS));
    return fields;
  }

  @Override
  public void onNetworkUpdate(String field) {
    if (field.equals("crop") || field.equals("currentAge")) rerender();
    super.onNetworkUpdate(field);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (server()) {
      updateTerrainHumidity();
      updateTerrainNutrients();
      updateTerrainAirQuality();
      if (getClass() == CropPlot.class)
        ic2ce.neoforge.world.WorldData.get(getLevel()).cropTicks.add(this);
    }
  }

  @Override
  protected void onUnloaded() {
    var world = getLevel();
    if (world != null && !world.isClientSide()) {
      var data = ic2ce.neoforge.world.WorldData.get(world, false);
      if (data != null) data.cropTicks.remove(this);
    }
    super.onUnloaded();
  }

  @Override
  public void updateEntityServer() {
    if (!server()) return;
    if (scheduledPeriod == 0 || scheduledRate != tickRate) configureSchedule();
    long time = getLevel().getGameTime();
    if ((scheduledMask >= 0 ? time & scheduledMask : Math.floorMod(time, scheduledPeriod))
            == scheduledPhase
        && (!scheduledTicked || lastGrowthTime != time)) {
      scheduledTicked = true;
      lastGrowthTime = time;
      performTick();
    }
    if (dirty && !isRemoved()) publishChanges();
  }

  private void configureSchedule() {
    int period = Math.clamp(tickRate, 1, 65536);
    scheduledPhase = phaseForPeriod(period);
    scheduledRate = tickRate;
    scheduledPeriod = period;
    scheduledMask = (period & (period - 1)) == 0 ? period - 1 : -1;
  }

  int phaseForPeriod(int period) {
    long hash =
        worldPosition.getX() * 341873128712L
            ^ worldPosition.getZ() * 132897987541L
            ^ worldPosition.getY() * 31L;
    return Math.floorMod(-hash, period);
  }

  private void publishChanges() {
    var world = getLevel();
    if (dirty && !isRemoved()) {
      int fields = pendingFields == 0 ? (1 << FIELDS.length) - 1 : pendingFields;
      dirty = false;
      pendingFields = 0;
      persistenceMarked = false;
      if ((fields & 3) != 0 || crop != null && crop.isRedstoneSignalEmitter(this)) {
        var state = getLevel().getBlockState(worldPosition);
        getLevel().sendBlockUpdated(worldPosition, state, state, 3);
        getLevel().updateNeighbourForOutputSignal(worldPosition, getBlockType());
        getLevel().getChunkSource().getLightEngine().checkBlock(worldPosition);
      }
      // Delta transport discards changes when no player can receive them. Persistence stays live.
      if (!world.players().isEmpty())
        SideServices.network(true).updateTileEntityFields(this, FIELDS, fields);
    }
  }

  public void performTick() {
    if (!server()) return;
    int period = Math.clamp(tickRate, 1, 65536);
    int quarter = (int) (Math.floorDiv(getLevel().getGameTime(), period) & 3);
    if (quarter == 0) updateTerrainHumidity();
    else if (quarter == 1) updateTerrainNutrients();
    else if (quarter == 2) updateTerrainAirQuality();
    if (crop == null) {
      if (isCrossingBase() && (attemptCrossing() || attemptSpreading())) return;
      if (storageWeedEX > 0) {
        if (SharedRandom.INSTANCE.nextInt(10) == 0) {
          storageWeedEX--;
          changed(1 << 7);
        }
        return;
      }
      if (SharedRandom.INSTANCE.nextInt(100) == 0) transformCropBlock(Crops.weed, 0);
      return;
    }
    var species = crop;
    boolean ownedData = !customData.isEmpty();
    species.tick(this);
    if (!server() || crop != species) return;
    if (species.canGrow(this)) {
      performGrowthTick();
      if (!server() || crop != species) return;
      int duration = Math.max(1, species.getGrowthDuration(this));
      if (growthPoints >= duration) {
        growthPoints = 0;
        setCurrentAge(getCurrentAge() + 1);
      }
    }
    if (storageNutrients > 0) {
      storageNutrients--;
      changed(1 << 5);
    }
    if (storageWater > 0) {
      storageWater--;
      changed(1 << 6);
    }
    if (ownedData || !customData.isEmpty()) changed(1 << 13);
    if (species.isWeed(this) && SharedRandom.INSTANCE.nextInt(50) - statGrowth <= 2)
      performWeedWork();
  }

  public void performGrowthTick() {
    if (!server() || crop == null) return;
    int variation = SharedRandom.INSTANCE.nextInt(7);
    int tier = crop.getProperties().getTier(),
        weight =
            crop.getWeightInfluences(this, terrainHumidity, terrainNutrients, terrainAirQuality);
    long needed = Math.max(0, ((long) tier - 1) * 4 + statGrowth + statGain + statResistance);
    int resistanceDraw = needed - (long) weight * 5 > 25 ? SharedRandom.INSTANCE.nextInt(32) : 0;
    int earned =
        CropMath.growthPoints(
            statGrowth, statGain, statResistance, tier, weight, variation, resistanceDraw);
    if (earned < 0) {
      reset();
      return;
    }
    int next = CropMath.accumulate(growthPoints, earned);
    if (next != growthPoints) {
      growthPoints = next;
      changed(1 << 11);
    }
  }

  private CropPlot neighbor(BlockPos position) {
    return getLevel().isLoaded(position)
            && getLevel().getBlockEntity(position) instanceof CropPlot plant
            && !plant.isRemoved()
        ? plant
        : null;
  }

  public void performWeedWork() {
    if (!server() || crop == null) return;
    var target = worldPosition.relative(NEIGHBORS[SharedRandom.INSTANCE.nextInt(4)]);
    if (!getLevel().isLoaded(target)) return;
    var plant = neighbor(target);
    if (plant != null) {
      if (plant.crop != null
          && (plant.crop.isWeed(plant)
              || SharedRandom.INSTANCE.nextInt(32) < plant.statResistance
              || plant.hasWeedEX())) return;
      int growth = Math.max(statGrowth, plant.statGrowth);
      if (growth < 31 && SharedRandom.INSTANCE.nextBoolean()) growth++;
      var replacement = plant.transformCropBlock(Crops.weed, 0);
      if (replacement != null) replacement.setStatGrowth(growth);
    } else if (getLevel().isEmptyBlock(target)) {
      var below = target.below();
      var soil = getLevel().getBlockState(below);
      if (soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.FARMLAND)) {
        getLevel().setBlockAndUpdate(below, Blocks.GRASS_BLOCK.defaultBlockState());
        getLevel().setBlockAndUpdate(target, Blocks.SHORT_GRASS.defaultBlockState());
      }
    }
  }

  public boolean hasWeedEX() {
    if (storageWeedEX <= 0) return false;
    storageWeedEX = Math.max(0, storageWeedEX - 5);
    changed(1 << 7);
    return true;
  }

  private boolean eligible(CropPlot parent) {
    return parent != null
        && parent.crop != null
        && parent.crop.canGrow(this)
        && parent.crop.canCross(parent);
  }

  private boolean attemptCrossing() {
    if (SharedRandom.INSTANCE.nextInt(3) != 0) return false;
    var parents = new ArrayList<CropPlot>(4);
    for (var direction : NEIGHBORS) {
      var parent = neighbor(worldPosition.relative(direction));
      if (eligible(parent)
          && SharedRandom.INSTANCE.nextInt(16) <= CropMath.spreadingThreshold(parent.genome()))
        parents.add(parent);
    }
    if (parents.size() < 2) return false;
    var candidates = new ArrayList<CropCard>();
    var weights = new WeightedChoice();
    for (var candidate : Crops.instance.getCrops()) {
      long weight = 0;
      if (candidate != null && candidate.canGrow(this))
        for (var parent : parents) weight = Math.addExact(weight, ratio(candidate, parent.crop));
      candidates.add(candidate);
      weights.add(weight);
    }
    if (weights.totalWeight() <= 0) return false;
    long ticket =
        weights.totalWeight() <= Integer.MAX_VALUE
            ? SharedRandom.INSTANCE.nextInt((int) weights.totalWeight())
            : randomTicket(weights.totalWeight());
    var selected = candidates.get(weights.select(ticket));
    int count = parents.size();
    var genomes = new CropMath.Genome[count];
    for (int index = 0; index < count; index++) genomes[index] = parents.get(index).genome();
    var child =
        CropMath.inherit(
            genomes,
            SharedRandom.INSTANCE.nextInt(count * 2 + 1) - count,
            SharedRandom.INSTANCE.nextInt(count * 2 + 1) - count,
            SharedRandom.INSTANCE.nextInt(count * 2 + 1) - count);
    var replacement = transformCropBlock(selected, 0);
    if (replacement == null) return false;
    replacement.setStatGrowth(child.growth());
    replacement.setStatGain(child.gain());
    replacement.setStatResistance(child.resistance());
    return true;
  }

  private static long randomTicket(long bound) {
    long bits, selected;
    do {
      bits = SharedRandom.INSTANCE.nextLong() >>> 1;
      selected = bits % bound;
    } while (bits - selected + bound - 1 < 0);
    return selected;
  }

  private static long ratio(CropCard candidate, CropCard parent) {
    if (candidate == parent) return 500;
    var a = candidate.getProperties().getAllProperties();
    var b = parent.getProperties().getAllProperties();
    long score = 0;
    for (int index = 0; index < Math.min(a.length, b.length); index++)
      score += 2 - Math.abs((long) a[index] - b[index]);
    var ca = candidate.getAttributes();
    var pa = parent.getAttributes();
    if (ca != null && pa != null)
      for (var left : ca)
        for (var right : pa)
          if (left != null && right != null && left.equalsIgnoreCase(right)) score += 5;
    long difference = (long) candidate.getProperties().getTier() - parent.getProperties().getTier();
    if (difference > 1) score -= difference * 2;
    else if (difference < -3) score += difference;
    return Math.max(0, score);
  }

  private boolean attemptSpreading() {
    CropPlot only = null;
    for (var direction : NEIGHBORS) {
      var parent = neighbor(worldPosition.relative(direction));
      if (parent == null) continue;
      if (only != null) return false;
      only = parent;
    }
    if (!eligible(only)
        || SharedRandom.INSTANCE.nextInt(16) > CropMath.spreadingThreshold(only.genome()))
      return false;
    var replacement = transformCropBlock(only.crop, 0);
    if (replacement == null) return false;
    replacement.setStatGrowth(only.statGrowth);
    replacement.setStatGain(only.statGain);
    replacement.setStatResistance(only.statResistance);
    return true;
  }

  @Override
  protected InteractionResult onActivated(
      Player player, InteractionHand hand, Direction side, Vec3 hit) {
    if (!server()) return InteractionResult.SUCCESS;
    return rightClick(player, hand) ? InteractionResult.SUCCESS : InteractionResult.PASS;
  }

  @Override
  protected InteractionResult onClicked(Player player) {
    if (!server() || player.getAbilities().instabuild) return InteractionResult.PASS;
    if (crop != null)
      return crop.onLeftClick(this, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    if (!isCrossingBase()) return InteractionResult.PASS;
    setCrossingBase(false);
    StackUtil.dropAsEntity(getLevel(), worldPosition, new ItemStack(ItemDefinitions.CROP_STICK));
    return InteractionResult.SUCCESS;
  }

  @Override
  protected List<AABB> getAabbs(boolean collision) {
    return collision ? COLLISION : OUTLINE;
  }

  @Override
  protected void onBlockBreak() {
    if (server() && crop != null) {
      crop.onBlockDestroyed(this);
      for (var seed : seedDrops()) StackUtil.dropAsEntity(getLevel(), worldPosition, seed);
    }
  }

  public boolean rightClick(Player player, InteractionHand hand) {
    if (!server() || player == null) return false;
    var held = player.getItemInHand(hand);
    boolean creative = player.getAbilities().instabuild;
    if (!held.isEmpty()) {
      if (crop == null && !isCrossingBase() && held.is(ItemDefinitions.CROP_STICK)) {
        setCrossingBase(true);
        if (!creative) held.shrink(1);
        return true;
      }
      if (crop != null && held.is(ItemDefinitions.FERTILIZER)) {
        if (!applyFertilizer(true)) return false;
        if (!creative) held.shrink(1);
        return true;
      }
      if (crop != null
          && held.getItem() instanceof FixedCell cell
          && (held.is(ItemDefinitions.HYDRATION_CELL)
              || held.is(ItemDefinitions.WEED_EX_CELL)
              || held.is(ItemDefinitions.WATER_CELL))) {
        if (!cell.useOnCrop(creative ? held.copy() : held, this, true)) return false;
        if (!creative && cell == ItemDefinitions.WATER_CELL) {
          var empty = new ItemStack(ItemDefinitions.EMPTY_CELL);
          if (held.isEmpty()) player.setItemInHand(hand, empty);
          else if (!player.getInventory().add(empty)) player.drop(empty, false);
        }
        return true;
      }
      if (hydrate(player, hand, net.minecraft.world.level.material.Fluids.WATER, false)
          || hydrate(player, hand, FluidDefinitions.WEED_EX.still, true)) return true;
      var seed = crop == null && !isCrossingBase() ? Crops.instance.getBaseSeed(held) : null;
      int seedQuantity =
          Crops.instance instanceof CropCatalog catalog ? catalog.requiredSeedQuantity(held) : 1;
      if (seed != null
          && seedQuantity > 0
          && tryPlantIn(
              seed.crop, seed.size, seed.statGrowth, seed.statGain, seed.statResistance, 0)) {
        if (!creative) held.shrink(seedQuantity);
        return true;
      }
    }
    return crop != null && crop.onRightClick(this, player);
  }

  private boolean hydrate(
      Player player,
      InteractionHand hand,
      net.minecraft.world.level.material.Fluid fluid,
      boolean herbicide) {
    var held = player.getItemInHand(hand);
    var request = Ic2FluidStack.create(fluid, Integer.MAX_VALUE);
    int available = FluidHandler.drainMb(held.copyWithCount(1), request, true, null);
    int accepted =
        herbicide ? applyWeedEx(available, false, true, true) : applyHydration(available, true);
    if (accepted <= 0) return false;
    request.setAmountMb(accepted);
    var replacement = new MutableObject<ItemStack>();
    int removed = FluidHandler.drainMb(held.copyWithCount(1), request, false, replacement);
    if (removed <= 0 || removed > accepted || replacement.getValue() == null) return false;
    int applied =
        herbicide ? applyWeedEx(removed, false, true, false) : applyHydration(removed, false);
    if (applied != removed) return false;
    if (!player.getAbilities().instabuild) {
      var returned = replacement.getValue().copy();
      if (held.getCount() == 1) player.setItemInHand(hand, returned);
      else {
        held.shrink(1);
        if (!returned.isEmpty() && !player.getInventory().add(returned))
          player.drop(returned, false);
      }
    }
    return true;
  }

  public boolean tryPlantIn(
      CropCard species, int age, int growth, int gain, int resistance, int scan) {
    if (!server()
        || crop != null
        || species == null
        || species == Crops.weed
        || isCrossingBase()
        || !species.canGrow(this)) return false;
    var replacement = transformCropBlock(species, age);
    if (replacement == null) return false;
    replacement.setStatGrowth(growth);
    replacement.setStatGain(gain);
    replacement.setStatResistance(resistance);
    replacement.setScanLevel(scan);
    return true;
  }

  @Override
  public void onEntityCollision(Entity entity) {
    if (!server() || crop == null || !crop.onEntityCollision(this, entity)) return;
    if (SharedRandom.INSTANCE.nextInt(100) == 0
        && SharedRandom.INSTANCE.nextInt(40) > statResistance) {
      reset();
      getLevel().setBlockAndUpdate(worldPosition.below(), Blocks.DIRT.defaultBlockState());
    }
  }

  public void updateTerrainAirQuality() {
    var world = getLevel();
    if (world == null) return;
    int free = 0;
    LevelChunk chunk = null;
    int chunkX = Integer.MIN_VALUE, chunkZ = Integer.MIN_VALUE;
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++) {
        var cell = terrainCursor.setWithOffset(worldPosition, x, 0, z);
        int cellX = cell.getX() >> 4, cellZ = cell.getZ() >> 4;
        if (cellX != chunkX || cellZ != chunkZ) {
          chunkX = cellX;
          chunkZ = cellZ;
          chunk =
              world instanceof ServerLevel server
                  ? server.getChunkSource().getChunkNow(cellX, cellZ)
                  : world.isLoaded(cell) ? world.getChunkAt(cell) : null;
        }
        if (chunk == null || !world.isInValidBounds(cell)) continue;
        var state = chunk.getBlockState(cell);
        if (!state.isCollisionShapeFullBlock(world, cell)
            && !(chunk.getBlockEntity(cell, LevelChunk.EntityCreationType.IMMEDIATE)
                instanceof CropPlot)) free++;
      }
    setTerrainAirQuality(
        Math.clamp(Math.floorDiv(worldPosition.getY() - 40, 15), 0, 2)
            + free / 2
            + (world.canSeeSky(worldPosition.above()) ? 4 : 0));
  }

  public void updateTerrainHumidity() {
    var world = getLevel();
    if (world == null) return;
    long value = Crops.instance.getHumidityBiomeBonus(BiomeUtil.getBiome(world, worldPosition));
    var soil = world.getBlockState(terrainCursor.setWithOffset(worldPosition, 0, -1, 0));
    if (soil.hasProperty(FarmlandBlock.MOISTURE) && soil.getValue(FarmlandBlock.MOISTURE) == 7)
      value += 2;
    if (storageWater >= 5) value += 2;
    value += (storageWater + 24) / 25;
    setTerrainHumidity((int) Math.clamp(value, 0, 127));
  }

  public void updateTerrainNutrients() {
    var world = getLevel();
    if (world == null) return;
    long value = Crops.instance.getNutrientBiomeBonus(BiomeUtil.getBiome(world, worldPosition));
    for (int depth = 1; depth <= 4; depth++) {
      terrainCursor.setWithOffset(worldPosition, 0, -depth, 0);
      if (world.isOutsideBuildHeight(terrainCursor)
          || !world.getBlockState(terrainCursor).is(Blocks.DIRT)) break;
      value++;
    }
    value += (storageNutrients + 19) / 20;
    setTerrainNutrients((int) Math.clamp(value, 0, 127));
  }

  @Override
  public CropCard getCrop() {
    return crop;
  }

  @Override
  public void setCrop(CropCard species) {
    if (species != null) transformCropBlock(species, getCurrentAge());
  }

  @Override
  public int getCurrentAge() {
    if (crop == null) return 0;
    var property = getBlockType().getAgeProperty();
    return property != null && getBlockState().hasProperty(property)
        ? getBlockState().getValue(property)
        : currentAge;
  }

  @Override
  public void setCurrentAge(int age) {
    withCropAge(age);
  }

  @Override
  public int getStatGrowth() {
    return statGrowth;
  }

  @Override
  public void setStatGrowth(int value) {
    int next = Math.clamp(value, 0, 31);
    if (next != statGrowth) {
      statGrowth = next;
      changed(1 << 2);
    }
  }

  @Override
  public int getStatGain() {
    return statGain;
  }

  @Override
  public void setStatGain(int value) {
    int next = Math.clamp(value, 0, 31);
    if (next != statGain) {
      statGain = next;
      changed(1 << 3);
    }
  }

  @Override
  public int getStatResistance() {
    return statResistance;
  }

  @Override
  public void setStatResistance(int value) {
    int next = Math.clamp(value, 0, 31);
    if (next != statResistance) {
      statResistance = next;
      changed(1 << 4);
    }
  }

  @Override
  public int getStorageNutrients() {
    return storageNutrients;
  }

  @Override
  public void setStorageNutrients(int value) {
    int next = Math.clamp(value, 0, 200);
    if (next != storageNutrients) {
      storageNutrients = next;
      changed(1 << 5);
    }
  }

  @Override
  public int getStorageWater() {
    return storageWater;
  }

  @Override
  public void setStorageWater(int value) {
    int next = Math.clamp(value, 0, 200);
    if (next != storageWater) {
      storageWater = next;
      changed(1 << 6);
    }
  }

  @Override
  public int getStorageWeedEX() {
    return storageWeedEX;
  }

  @Override
  public void setStorageWeedEX(int value) {
    int next = Math.clamp(value, 0, 150);
    if (next != storageWeedEX) {
      storageWeedEX = next;
      changed(1 << 7);
    }
  }

  @Override
  public int getTerrainAirQuality() {
    return terrainAirQuality;
  }

  public void setTerrainAirQuality(int value) {
    int next = Math.clamp(value, 0, 127);
    if (next != terrainAirQuality) {
      terrainAirQuality = next;
      changed(1 << 10);
    }
  }

  @Override
  public int getTerrainHumidity() {
    return terrainHumidity;
  }

  public void setTerrainHumidity(int value) {
    int next = Math.clamp(value, 0, 127);
    if (next != terrainHumidity) {
      terrainHumidity = next;
      changed(1 << 8);
    }
  }

  @Override
  public int getTerrainNutrients() {
    return terrainNutrients;
  }

  public void setTerrainNutrients(int value) {
    int next = Math.clamp(value, 0, 127);
    if (next != terrainNutrients) {
      terrainNutrients = next;
      changed(1 << 9);
    }
  }

  @Override
  public int getScanLevel() {
    return scanLevel;
  }

  @Override
  public void setScanLevel(int value) {
    int next = Math.clamp(value, 0, 4);
    if (next != scanLevel) {
      scanLevel = next;
      changed(1 << 12);
    }
  }

  @Override
  public int getGrowthPoints() {
    return growthPoints;
  }

  @Override
  public void setGrowthPoints(int value) {
    int next = Math.max(0, value);
    if (next != growthPoints) {
      growthPoints = next;
      changed(1 << 11);
    }
  }

  @Override
  public boolean isCrossingBase() {
    return crop == null
        && getBlockState().hasProperty(MachineBlockType.CROSSING_BASE)
        && getBlockState().getValue(MachineBlockType.CROSSING_BASE);
  }

  @Override
  public void setCrossingBase(boolean value) {
    if (server() && crop == null && getBlockState().hasProperty(MachineBlockType.CROSSING_BASE)) {
      getLevel()
          .setBlockAndUpdate(
              worldPosition, getBlockState().setValue(MachineBlockType.CROSSING_BASE, value));
      changed();
    }
  }

  @Override
  public CompoundTag getCustomData() {
    return customData;
  }

  @Override
  public BlockPos getPosition() {
    return worldPosition;
  }

  @Override
  public Level getWorldObj() {
    return getLevel();
  }

  @Override
  public Level getWorld() {
    return getLevel();
  }

  @Override
  public BlockPos getLocation() {
    return worldPosition;
  }

  @Override
  public int getLightLevel() {
    return getLevel() == null ? 0 : getLevel().getMaxLocalRawBrightness(worldPosition);
  }

  @Override
  public int getLightValue() {
    return crop == null ? 0 : Math.clamp(crop.getEmittedLight(this), 0, 15);
  }

  private List<ItemStack> seedDrops() {
    var result = new ArrayList<ItemStack>(2);
    if (crop == null) return result;
    boolean mature = crop.canBeHarvested(this);
    double chance = crop.dropSeedChance(this) * Math.pow(1.1, statResistance);
    if (!Double.isFinite(chance) || chance < 0) chance = 0;
    int count = 0;
    if (mature) {
      if (getLevel().getRandom().nextFloat() <= (chance + 1) * .8) count++;
      double extra =
          (crop.dropSeedChance(this) + statGrowth / 100.0)
              * Math.pow(.95, Math.max(0, statGain - 23));
      if (getLevel().getRandom().nextFloat() <= extra) count++;
    } else if (getLevel().getRandom().nextFloat() <= chance * 1.5) count++;
    for (int index = 0; index < count; index++) {
      var seed = crop.getSeeds(this);
      if (seed != null && !seed.isEmpty()) result.add(seed.copy());
    }
    return result;
  }

  @Override
  public boolean pick() {
    if (!server() || crop == null) return false;
    var drops = seedDrops();
    reset();
    for (var drop : drops) StackUtil.dropAsEntity(getLevel(), worldPosition, drop);
    return true;
  }

  @Override
  public boolean performManualHarvest() {
    var drops = performHarvest();
    if (drops == null) return false;
    for (var drop : drops) StackUtil.dropAsEntity(getLevel(), worldPosition, drop);
    return true;
  }

  @Override
  public List<ItemStack> performHarvest() {
    return performHarvest(_ -> true);
  }

  public List<ItemStack> performHarvest(IntPredicate approve) {
    Objects.requireNonNull(approve);
    var species = crop;
    if (!server() || species == null || !species.canBeHarvested(this)) return null;
    double chance = species.dropGainChance() * Math.pow(1.03, statGain);
    if (!Double.isFinite(chance) || chance < 0) return null;
    int rolls =
        (int)
            Math.clamp(
                Math.round(SharedRandom.INSTANCE.nextGaussian() * chance * .6827 + chance), 0, 256);
    var drops = new ArrayList<ItemStack>();
    for (int roll = 0; roll < rolls; roll++) {
      var produced = species.getGains(this);
      if (produced == null) continue;
      if (produced.length > 256 || drops.size() + (long) produced.length > 4096) return null;
      for (var item : produced)
        if (item != null && !item.isEmpty())
          drops.add(
              item.copyWithCount(
                  (int)
                      Math.min(
                          Integer.MAX_VALUE,
                          item.getCount()
                              + (SharedRandom.INSTANCE.nextInt(100) <= statGain ? 1L : 0L))));
    }
    int nextAge = Math.clamp(species.getAgeAfterHarvest(this), 0, Math.max(0, species.getMaxAge()));
    if (crop != species || !species.canBeHarvested(this) || !approve.test(drops.size()))
      return null;
    setCurrentAge(nextAge);
    changed();
    return drops;
  }

  @Override
  public void reset() {
    if (server()) {
      var cleared = replace(BlockDefinitions.CROP_STICK, null, 0);
      if (cleared != null) cleared.resetData();
    }
  }

  public void resetData() {
    statGrowth = statGain = statResistance = growthPoints = scanLevel = currentAge = 0;
    terrainAirQuality = terrainHumidity = terrainNutrients = 0;
    customData = new CompoundTag();
    changed();
  }

  @Override
  public void updateState() {
    if (getLevel() != null) {
      var state = getLevel().getBlockState(worldPosition);
      getLevel().sendBlockUpdated(worldPosition, state, state, 2);
      changed();
    }
  }

  private boolean rootMatches(java.util.function.Predicate<BlockState> predicate) {
    if (crop == null || getLevel() == null) return false;
    int length = Math.clamp(crop.getRootsLength(this), 1, 64);
    for (int depth = 1; depth < length; depth++) {
      var position = worldPosition.below(depth);
      if (getLevel().isOutsideBuildHeight(position)) return false;
      var state = getLevel().getBlockState(position);
      if (state.isAir()) return false;
      if (predicate.test(state)) return true;
    }
    return false;
  }

  @Override
  public boolean isBlockBelow(Block block) {
    return rootMatches(state -> state.is(block));
  }

  @Override
  public boolean isBlockBelow(TagKey<Block> tag) {
    return rootMatches(state -> ic2ce.neoforge.material.CommonMaterialTags.matches(state, tag));
  }

  @Override
  public ItemStack generateSeeds(CropCard species, int growth, int gain, int resistance, int scan) {
    var genes = CropMath.Genome.bounded(growth, gain, resistance);
    return EncodedSeed.generateItemStackFromValues(
        species, genes.growth(), genes.gain(), genes.resistance(), Math.clamp(scan, 0, 4));
  }

  @Override
  protected int getLightOpacity() {
    return 0;
  }

  private CropPlot replace(Block block, CropCard selected, int age) {
    if (!server() || !(block instanceof MachineBlockType nativeBlock)) return null;
    var state = block.defaultBlockState();
    var property = nativeBlock.getCropMaxAge() > 0 ? nativeBlock.getAgeProperty() : null;
    if (selected == null && state.hasProperty(MachineBlockType.CROSSING_BASE))
      state = state.setValue(MachineBlockType.CROSSING_BASE, false);
    if (property != null && state.hasProperty(property)) {
      int maximum =
          property.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
      if (selected != null) maximum = Math.min(maximum, Math.max(0, selected.getMaxAge()));
      state = state.setValue(property, Math.clamp(age, 0, maximum));
    }
    var world = getLevel();
    if (!world.setBlockAndUpdate(worldPosition, state)
        && world.getBlockState(worldPosition).getBlock() != block) return null;
    if (!(world.getBlockEntity(worldPosition) instanceof CropPlot replacement)) return null;
    replacement.storageWater = storageWater;
    replacement.storageNutrients = storageNutrients;
    replacement.storageWeedEX = storageWeedEX;
    replacement.statGrowth = statGrowth;
    replacement.statGain = statGain;
    replacement.statResistance = statResistance;
    replacement.scanLevel = scanLevel;
    replacement.customData = customData.copy();
    replacement.currentAge =
        property != null && state.hasProperty(property) ? state.getValue(property) : 0;
    replacement.growthPoints = 0;
    if (selected != null) replacement.crop = selected;
    replacement.changed();
    replacement.updateTerrainHumidity();
    replacement.updateTerrainNutrients();
    replacement.updateTerrainAirQuality();
    return replacement;
  }

  public CropPlot transformCropBlock(CropCard species, int age) {
    return species == null ? null : replace(species.getCropBlock(), species, age);
  }

  public CropPlot transformCropBlock(Block block) {
    return replace(block, Crops.instance.getCropCard(block), getCurrentAge());
  }

  public boolean resetCropBlock(Block block) {
    return replace(block, Crops.instance.getCropCard(block), 0) != null;
  }

  public boolean withCropAge(Integer requested) {
    if (!server() || requested == null || crop == null) return false;
    var property = getBlockType().getAgeProperty();
    if (property == null || !getBlockState().hasProperty(property)) return false;
    int bounded = Math.clamp(requested, 0, Math.max(0, crop.getMaxAge()));
    if (!property.getPossibleValues().contains(bounded)) return false;
    currentAge = bounded;
    boolean result =
        getLevel().setBlockAndUpdate(worldPosition, getBlockState().setValue(property, bounded));
    changed(1 << 1);
    return result || getCurrentAge() == bounded;
  }

  @Override
  public boolean wrenchCanRemove(Player player) {
    return false;
  }

  @Override
  protected ItemStack getPickBlock(Player player, BlockHitResult target) {
    return crop == null
        ? new ItemStack(ItemDefinitions.CROP_STICK)
        : generateSeeds(crop, statGrowth, statGain, statResistance, scanLevel);
  }

  @Override
  protected void onNeighborChange(Block neighbor) {
    super.onNeighborChange(neighbor);
    if (!server()) return;
    if (!CropSoilType.contains(getLevel().getBlockState(worldPosition.below()).getBlock())) {
      pick();
      getLevel().removeBlock(worldPosition, false);
    } else if (crop != null) crop.onNeighbourChange(this);
  }

  public int applyHydration(int amount, boolean simulate) {
    if (amount <= 0) return 0;
    int accepted = Math.min(amount, 200 - storageWater);
    if (!simulate && accepted > 0) {
      storageWater += accepted;
      changed(1 << 6);
    }
    return accepted;
  }

  public int applyWeedEx(int amount, boolean fixed, boolean manual, boolean simulate) {
    if (amount <= 0) return 0;
    int capacity = Math.max(0, (manual ? 100 : 150) - storageWeedEX);
    if (fixed && capacity < amount) return 0;
    int accepted = Math.min(amount, capacity);
    if (!simulate && accepted > 0) {
      storageWeedEX += accepted;
      changed(1 << 7);
    }
    return accepted;
  }

  public boolean applyFertilizer(boolean manual) {
    if (storageNutrients >= 100) return false;
    storageNutrients = Math.min(200, storageNutrients + (manual ? 100 : 90));
    changed(1 << 5);
    return true;
  }
}
