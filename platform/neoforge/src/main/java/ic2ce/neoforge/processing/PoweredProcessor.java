package ic2ce.neoforge.processing;

import ic2ce.api.network.INetworkTileEntityEventListener;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.IUpgradeItem;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.engine.machine.ProcessingCycle;
import ic2ce.engine.machine.UpgradeMath;
import ic2ce.neoforge.audio.PlaybackHandle;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.runtime.SideServices;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Native inventory/energy/menu adapter; processing transitions live in the engine. */
public abstract class PoweredProcessor<RI, RO, I> extends PoweredBlockEntity
    implements IHasGui, ScreenValues, INetworkTileEntityEventListener, IUpgradableBlock {
  protected Collection<ItemStack> processResult;
  protected MachineRecipeResult<RI, RO, I> recipeResult;
  protected short progress;
  public final int defaultEnergyConsume, defaultOperationLength, defaultTier, defaultEnergyStorage;
  public int energyConsume, operationLength, operationsPerTick;
  @GuiSynced protected float guiProgress;
  public PlaybackHandle sound;
  protected static final int EventStart = 0, EventInterrupt = 1, EventFinish = 2, EventStop = 3;
  public RecipeInputInventory<RI, RO, I> inputSlot;
  public final OutputInventory outputSlot;
  public final UpgradeInventory upgradeSlot;
  private final ProcessingCycle cycle = new ProcessingCycle(0, false);

  public PoweredProcessor(
      BlockEntityType<? extends PoweredProcessor<RI, RO, I>> type,
      BlockPos position,
      BlockState state,
      int demand,
      int ticks,
      int outputCount) {
    this(type, position, state, demand, ticks, outputCount, 1);
  }

  public PoweredProcessor(
      BlockEntityType<? extends PoweredProcessor<RI, RO, I>> type,
      BlockPos position,
      BlockState state,
      int demand,
      int ticks,
      int outputCount,
      int tier) {
    if (demand < 0 || ticks < 1 || tier < 0)
      throw new IllegalArgumentException("Invalid processing machine defaults");
    int initialStorage = Math.multiplyExact(demand, ticks);
    super(type, position, state, initialStorage, tier);
    defaultEnergyConsume = energyConsume = demand;
    defaultOperationLength = operationLength = ticks;
    defaultTier = tier;
    defaultEnergyStorage = initialStorage;
    operationsPerTick = 1;
    outputSlot = new OutputInventory(this, "output", outputCount);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
    comparator.setUpdate(() -> progress * 15 / operationLength);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    progress = data.getShortOr("progress", (short) 0);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putShort("progress", progress);
  }

  public float getProgress() {
    return guiProgress;
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (SideServices.proxy().isSimulating()) setOverclockRates();
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (SideServices.proxy().isSimulating()) setOverclockRates();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    var current = getRecipeResult();
    if (recipeResult == null
        || current == null
        || !Objects.equals(current.getRecipe().getInput(), recipeResult.getRecipe().getInput()))
      progress = 0;
    recipeResult = current;
    boolean runnable = consumeTickEnergy();
    boolean previouslyActive = getActive();
    if (runnable && !previouslyActive) activate(false);
    cycle.restore(progress, previouslyActive);
    ProcessingCycle.Transition transition =
        cycle.advance(recipeResult != null, runnable, operationLength);
    boolean inventoryChanged = transition == ProcessingCycle.Transition.COMPLETE;
    if (inventoryChanged) {
      // Hooks observe the completing progress before it is reset, as in existing saves/API.
      progress++;
      completeOperations();
      progress = 0;
      cycle.reset();
      if (recipeResult == null || energy.getEnergy() < energyConsume) {
        shutdown(false);
        cycle.stopAfterCompletion();
      }
    } else if (transition == ProcessingCycle.Transition.PAUSE
        || transition == ProcessingCycle.Transition.STOP)
      shutdown(transition == ProcessingCycle.Transition.PAUSE);
    progress = (short) cycle.progress();
    inventoryChanged |= upgradeSlot.tickNoMark();
    guiProgress = (float) progress / operationLength;
    if (inventoryChanged) super.setChanged();
  }

  private boolean consumeTickEnergy() {
    return recipeResult != null && energy.useEnergy(energyConsume);
  }

  public void setOverclockRates() {
    upgradeSlot.onChanged();
    cycle.restore(progress, getActive());
    UpgradeMath.Rates rates =
        UpgradeMath.rates(
            defaultOperationLength,
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
    cycle.rescale(operationLength, rates.ticks());
    operationsPerTick = rates.operations();
    operationLength = rates.ticks();
    energyConsume = rates.demand();
    energy.setSinkTier(rates.tier());
    dischargeSlot.setTier(rates.tier());
    energy.setCapacity(rates.capacity());
    progress = (short) cycle.progress();
  }

  private void completeOperations() {
    var result = recipeResult;
    for (int count = 0; result != null && count < operationsPerTick; count++) {
      Collection<ItemStack> output =
          processResult == null ? getOutput(result.getOutput()) : processResult;
      for (int slot = 0; slot < upgradeSlot.size(); slot++) {
        ItemStack stack = upgradeSlot.get(slot);
        if (!stack.isEmpty() && stack.getItem() instanceof IUpgradeItem upgrade)
          output = upgrade.onProcessEnd(stack, this, output);
      }
      if (!outputSlot.canAdd(output)) {
        recipeResult = null;
        break;
      }
      operateOnce(result, output);
      recipeResult = result = getRecipeResult();
      if (result == null) break;
    }
  }

  protected boolean directRecipeOutputPreview() {
    return false;
  }

  protected Collection<ItemStack> getOutput(RO output) {
    return StackUtil.copy((Collection<ItemStack>) output);
  }

  protected void operateOnce(MachineRecipeResult<RI, RO, I> result, Collection<ItemStack> output) {
    inputSlot.consume(result);
    if (outputSlot.add(output) != 0)
      throw new IllegalStateException("Machine output changed before recipe commit");
  }

  protected MachineRecipeResult<RI, RO, I> getRecipeResult() {
    if (inputSlot.isEmpty()) return null;
    var result = inputSlot.process();
    if (result == null) return null;
    boolean direct = directRecipeOutputPreview();
    Collection<ItemStack> output =
        direct ? (Collection<ItemStack>) result.getOutput() : getOutput(result.getOutput());
    if (!outputSlot.canAdd(output)) return null;
    processResult = direct ? null : output;
    return result;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return DocumentMenu.create(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(id, inventory, this);
  }

  @Override
  public void onNetworkEvent(int event) {}

  @Override
  public double getEnergy() {
    return energy.getEnergy();
  }

  @Override
  public boolean useEnergy(double amount) {
    return energy.useEnergy(amount);
  }

  @Override
  public double getGuiValue(String name) {
    if (name.equals("progress")) return guiProgress;
    throw new IllegalArgumentException(
        getClass().getSimpleName() + " Cannot get value for " + name);
  }
}
