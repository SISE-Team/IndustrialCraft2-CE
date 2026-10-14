package ic2ce.neoforge.component;

import ic2ce.api.recipe.*;
import ic2ce.api.upgrade.*;
import ic2ce.engine.machine.UpgradeMath;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.recipe.processing.SmeltingRecipes;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Addon processing component owns progress, real input/output bills and optional native upgrade
 * rates.
 */
public class AddonProcessor extends NativeComponent {
  protected int progress;
  public int defaultEnergyConsume,
      operationDuration,
      defaultTier = 1,
      defaultEnergyStorage,
      energyConsume,
      operationLength,
      operationsPerTick = 1;
  private final int baseDuration;
  private final RecipeInputInventory<IRecipeInput, Collection<ItemStack>, ItemStack> inputSlot;
  private final OutputInventory outputSlot;
  private final UpgradeInventory upgradeSlot;

  public static AddonProcessor asFurnace(InventoryBlockEntity owner) {
    return asFurnace(owner, 3, 100, 1, 4);
  }

  public static AddonProcessor asFurnace(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(
        owner, w -> SmeltingRecipes.SmeltingBridge.INSTANCE, cost, duration, outputs, upgrades);
  }

  public static AddonProcessor asMacerator(InventoryBlockEntity owner) {
    return asMacerator(owner, 2, 300, 1, 4);
  }

  public static AddonProcessor asMacerator(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(owner, Recipes.macerator, cost, duration, outputs, upgrades);
  }

  public static AddonProcessor asExtractor(InventoryBlockEntity owner) {
    return asExtractor(owner, 2, 300, 1, 4);
  }

  public static AddonProcessor asExtractor(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(owner, Recipes.extractor, cost, duration, outputs, upgrades);
  }

  public static AddonProcessor asCompressor(InventoryBlockEntity owner) {
    return asCompressor(owner, 2, 300, 1, 4);
  }

  public static AddonProcessor asCompressor(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(owner, Recipes.compressor, cost, duration, outputs, upgrades);
  }

  public static AddonProcessor asCentrifuge(InventoryBlockEntity owner) {
    return asCentrifuge(owner, 48, 500, 3, 4);
  }

  public static AddonProcessor asCentrifuge(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(owner, Recipes.centrifuge, cost, duration, outputs, upgrades);
  }

  public static AddonProcessor asRecycler(InventoryBlockEntity owner) {
    return asRecycler(owner, 1, 45, 1, 4);
  }

  public static AddonProcessor asRecycler(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(owner, w -> Recipes.recycler, cost, duration, outputs, upgrades);
  }

  public static AddonProcessor asOreWasher(InventoryBlockEntity owner) {
    return asOreWasher(owner, 16, 500, 3, 4);
  }

  public static AddonProcessor asOreWasher(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(owner, Recipes.oreWashing, cost, duration, outputs, upgrades);
  }

  public static AddonProcessor asBlockCutter(InventoryBlockEntity owner) {
    return asBlockCutter(owner, 48, 900, 1, 4);
  }

  public static AddonProcessor asBlockCutter(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(owner, Recipes.blockcutter, cost, duration, outputs, upgrades);
  }

  public static AddonProcessor asBlastFurnace(InventoryBlockEntity owner) {
    return asBlastFurnace(owner, 2, 300, 1, 4);
  }

  public static AddonProcessor asBlastFurnace(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(owner, Recipes.blastfurnace, cost, duration, outputs, upgrades);
  }

  public static AddonProcessor asExtruder(InventoryBlockEntity owner) {
    return asExtruder(owner, 10, 200, 1, 4);
  }

  public static AddonProcessor asExtruder(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(
        owner, Recipes.metalformerExtruding, cost, duration, outputs, upgrades);
  }

  public static AddonProcessor asCutter(InventoryBlockEntity owner) {
    return asCutter(owner, 10, 200, 1, 4);
  }

  public static AddonProcessor asCutter(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(owner, Recipes.metalformerCutting, cost, duration, outputs, upgrades);
  }

  public static AddonProcessor asRollingMachine(InventoryBlockEntity owner) {
    return asRollingMachine(owner, 10, 200, 1, 4);
  }

  public static AddonProcessor asRollingMachine(
      InventoryBlockEntity owner, int cost, int duration, int outputs, int upgrades) {
    return new AddonProcessor(owner, Recipes.metalformerRolling, cost, duration, outputs, upgrades);
  }

  public AddonProcessor(
      InventoryBlockEntity owner,
      Recipes.IGetter<
              ? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>>
          manager) {
    this(owner, manager, 2, 100, 1, 0);
  }

  public AddonProcessor(
      InventoryBlockEntity owner,
      Recipes.IGetter<
              ? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>>
          manager,
      int cost,
      int duration,
      int outputs,
      int upgrades) {
    this(
        owner,
        new ItemRecipeInput(owner, "input", 1, Objects.requireNonNull(manager)),
        cost,
        duration,
        outputs,
        upgrades);
  }

  protected AddonProcessor(
      InventoryBlockEntity owner,
      RecipeInputInventory<IRecipeInput, Collection<ItemStack>, ItemStack> input,
      int cost,
      int duration,
      int outputs,
      int upgrades) {
    super(owner);
    if (cost < 0
        || duration <= 0
        || outputs <= 0
        || outputs > 256
        || upgrades < 0
        || upgrades > 256)
      throw new IllegalArgumentException("Invalid addon processing dimensions");
    inputSlot = Objects.requireNonNull(input);
    outputSlot = new OutputInventory(owner, "output", outputs);
    upgradeSlot =
        owner instanceof IUpgradableBlock && upgrades > 0
            ? UpgradeInventory.createUnchecked(owner, "upgrade", upgrades)
            : null;
    baseDuration = duration;
    operationDuration = operationLength = duration;
    defaultEnergyConsume = energyConsume = cost;
    defaultEnergyStorage = (int) Math.min(Integer.MAX_VALUE, (long) cost * duration);
  }

  @Override
  public void readFromNbt(CompoundTag data) {
    readFromNBT(data);
  }

  public void readFromNBT(CompoundTag data) {
    progress = Math.clamp(data.getIntOr("progress", 0), 0, Math.max(0, operationDuration - 1));
  }

  @Override
  public CompoundTag writeToNbt() {
    var data = new CompoundTag();
    writeToNBT(data);
    return data;
  }

  public void writeToNBT(CompoundTag data) {
    data.putInt("progress", progress);
  }

  public static int applyModifier(int base, int extra, double multiplier) {
    return UpgradeMath.modified(base, extra, multiplier);
  }

  public void setOverclockRates() {
    double fraction = progress / (double) Math.max(1, operationDuration);
    if (upgradeSlot == null) {
      operationDuration = operationLength = baseDuration;
      energyConsume = defaultEnergyConsume;
      operationsPerTick = 1;
    } else {
      upgradeSlot.onChanged();
      var rates =
          UpgradeMath.rates(
              baseDuration,
              defaultEnergyConsume,
              defaultEnergyStorage,
              defaultTier,
              upgradeSlot.extraProcessTime,
              upgradeSlot.processTimeMultiplier,
              upgradeSlot.extraEnergyDemand,
              upgradeSlot.energyDemandMultiplier,
              upgradeSlot.extraEnergyStorage,
              upgradeSlot.energyStorageMultiplier,
              upgradeSlot.extraTier);
      operationDuration = operationLength = Math.max(1, rates.ticks());
      energyConsume = Math.max(0, rates.demand());
      operationsPerTick = Math.clamp(rates.operations(), 1, 1_000_000);
      if (parent.hasComponent(ElectricalBuffer.class)) {
        var energy = parent.getComponent(ElectricalBuffer.class);
        energy.setCapacity(rates.capacity());
        energy.setSinkTier(rates.tier());
      }
    }
    progress = Math.clamp((int) Math.floor(fraction * operationDuration), 0, operationDuration - 1);
  }

  @Override
  public void onLoaded() {
    if (parent.getLevel() != null && !parent.getLevel().isClientSide()) setOverclockRates();
  }

  @Override
  public boolean enableWorldTick() {
    return true;
  }

  @Override
  public void onWorldTick() {
    if (parent.getLevel() == null
        || parent.getLevel().isClientSide()
        || !parent.hasComponent(ElectricalBuffer.class)) return;
    var result = getOutput();
    if (result == null) {
      if (progress != 0) {
        progress = 0;
        parent.setChanged();
      }
      parent.setActive(false);
      return;
    }
    if (!parent.getComponent(ElectricalBuffer.class).useEnergy(energyConsume)) {
      parent.setActive(false);
      return;
    }
    parent.setActive(true);
    progress++;
    if (progress >= Math.max(1, operationDuration)) {
      operate(result);
      progress = 0;
    }
    parent.setChanged();
  }

  public void operate(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result) {
    for (int operation = 0; operation < operationsPerTick && result != null; operation++) {
      var output = new ArrayList<ItemStack>();
      for (var stack : result.getOutput())
        if (stack != null && !stack.isEmpty()) output.add(stack.copy());
      if (upgradeSlot != null && parent instanceof IUpgradableBlock block)
        for (int cell = 0; cell < upgradeSlot.size(); cell++) {
          var installed = upgradeSlot.get(cell);
          if (!installed.isEmpty() && installed.getItem() instanceof IUpgradeItem upgrade)
            upgrade.onProcessEnd(installed, block, output);
        }
      if (!outputSlot.canAdd(output)) break;
      operateOnce(result, output);
      result = getOutput();
    }
  }

  public void operateOnce(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result,
      Collection<ItemStack> output) {
    Objects.requireNonNull(result);
    Objects.requireNonNull(output);
    if (parent.getLevel() == null || parent.getLevel().isClientSide() || !outputSlot.canAdd(output))
      return;
    var current = inputSlot.process();
    if (current == null
        || !ItemStack.matches(current.getAdjustedInput(), result.getAdjustedInput())) return;
    inputSlot.consume(current);
    outputSlot.add(output);
    parent.setChanged();
  }

  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
    if (inputSlot.isEmpty()) return null;
    var result = inputSlot.process();
    return result != null && outputSlot.canAdd(result.getOutput()) ? result : null;
  }

  public int getProgress() {
    return progress;
  }

  public double getProgressRatio() {
    return Math.clamp(progress / (double) Math.max(1, operationDuration), 0, 1);
  }
}
