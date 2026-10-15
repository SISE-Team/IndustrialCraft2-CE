package ic2ce.neoforge.processing.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.fluid.LiquidUtil;
import ic2ce.neoforge.fluid.PumpUtil;
import ic2ce.engine.machine.UpgradeMath;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.mining.PipeMiner;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Metered source collection; world buckets and capability drains reserve the real fluid outlet
 * first.
 */
public class FluidCollector extends PoweredBlockEntity
    implements IHasGui, IUpgradableBlock, ScreenValues {
  private static final Direction[] MINER_SIDES = {
    Direction.DOWN, Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH
  };
  private static final Set<UpgradableProperty> UPGRADES =
      Set.copyOf(
          EnumSet.of(
              UpgradableProperty.Processing,
              UpgradableProperty.Transformer,
              UpgradableProperty.EnergyStorage,
              UpgradableProperty.ItemConsuming,
              UpgradableProperty.ItemProducing,
              UpgradableProperty.FluidProducing));
  public final int defaultTier = 1,
      defaultEnergyStorage = 20,
      defaultEnergyConsume = 1,
      defaultOperationLength = 20;
  public int energyConsume = 1, operationLength = 20, operationsPerTick = 1, progress;
  public boolean redstonePowered;
  public final FluidContainerInventory containerSlot;
  public final OutputInventory outputSlot;
  public final UpgradeInventory upgradeSlot;
  protected final FluidPorts fluids;
  @GuiSynced protected final Ic2FluidTank fluidTank;
  @GuiSynced public float guiProgress;
  private PipeMiner miner;

  private record Intake(
      BlockPos position, Direction side, boolean capability, Ic2FluidStack preview) {}

  public FluidCollector(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.PUMP, position, state, 20, 1);
    containerSlot =
        new FluidContainerInventory(
            this,
            "input",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.TOP,
            FluidContainerInventory.OpType.Fill);
    outputSlot = new OutputInventory(this, "output", 1, InventorySlot.InvSide.SIDE);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
    fluids = addComponent(new FluidPorts(this));
    fluidTank = fluids.addTankExtract("fluid", 8000);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (!getLevel().isClientSide()) setUpgradestat();
  }

  @Override
  protected void onUnloaded() {
    miner = null;
    super.onUnloaded();
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    progress = Math.max(0, data.getIntOr("progress", 0));
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("progress", progress);
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (getLevel() != null && !getLevel().isClientSide()) setUpgradestat();
  }

  public void setUpgradestat() {
    double fraction = Math.clamp((double) progress / Math.max(1, operationLength), 0, 1);
    upgradeSlot.onChanged();
    var rates =
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
    operationsPerTick = rates.operations();
    operationLength = rates.ticks();
    energyConsume = rates.demand();
    energy.setCapacity(rates.capacity());
    energy.setSinkTier(rates.tier());
    dischargeSlot.setTier(rates.tier());
    progress = (int) Math.min(operationLength, Math.floor(fraction * operationLength + 1e-9));
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean changed = containerSlot.processFromTank(fluidTank, outputSlot);
    boolean working = false;
    if (canOperate()) {
      if (progress >= operationLength) working = completeBatch();
      else if (energy.useEnergy(energyConsume)) {
        progress++;
        changed = working = true;
        if (progress >= operationLength) completeBatch();
      }
    }
    setActive(working);
    changed |= upgradeSlot.tickNoMark();
    guiProgress = (float) progress / operationLength;
    if (changed) super.setChanged();
  }

  private boolean completeBatch() {
    boolean completed = false;
    for (int count = 0; count < operationsPerTick; count++) {
      if (!operate(false)) break;
      completed = true;
    }
    if (completed) {
      progress = 0;
      super.setChanged();
    }
    return completed;
  }

  private PipeMiner linkedMiner() {
    var world = getLevel();
    if (world == null) return null;
    if (miner != null
        && !miner.isRemoved()
        && miner.getLevel() == world
        && miner.getBlockPos().distManhattan(worldPosition) == 1) return miner;
    miner = null;
    for (var side : MINER_SIDES) {
      var neighbor = worldPosition.relative(side);
      if (world.hasChunkAt(neighbor) && world.getBlockEntity(neighbor) instanceof PipeMiner found) {
        miner = found;
        break;
      }
    }
    return miner;
  }

  public boolean canOperate() {
    return operate(true);
  }

  public boolean operate(boolean simulate) {
    var world = getLevel();
    if (world == null || !simulate && world.isClientSide()) return false;
    var linked = linkedMiner();
    if (linked != null && !linked.canProvideLiquid) return false;
    var start = linked == null ? worldPosition.relative(getFacing()) : linked.liquidPos;
    var fluid = pump(start, simulate, linked);
    if (fluid == null || fluid.isEmpty()) return false;
    if (!simulate) {
      if (fluidTank.fillMbUnchecked(fluid, false) != fluid.getAmountMb())
        throw new IllegalStateException("Reserved pump outlet changed during commit");
      super.setChanged();
    }
    return true;
  }

  private Intake plan(BlockPos start, PipeMiner linked) {
    var world = getLevel();
    if (world == null || start == null || !world.hasChunkAt(start)) return null;
    int free = fluidTank.getCapacity() - fluidTank.getFluidAmount();
    if (free <= 0) return null;
    Direction side = getFacing().getOpposite();
    var state = world.getBlockState(start);
    var blockEntity = world.getBlockEntity(start);
    if (linked == null && LiquidUtil.isFluidTile(state, blockEntity, side)) {
      var offered = LiquidUtil.drainTile(state, world, start, side, Math.min(1000, free), true);
      if (offered == null || offered.isEmpty()) return null;
      int accepted = fluidTank.fillMbUnchecked(offered, true);
      if (accepted <= 0) return null;
      if (accepted < offered.getAmountMb())
        offered = LiquidUtil.drainTile(state, world, start, side, accepted, true);
      return offered != null
              && !offered.isEmpty()
              && offered.getAmountMb() <= free
              && fluidTank.fillMbUnchecked(offered, true) == offered.getAmountMb()
          ? new Intake(start.immutable(), side, true, offered)
          : null;
    }
    if (free < 1000) return null;
    var source = linked == null ? PumpUtil.searchFluidSource(world, start) : start;
    if (source == null || !world.hasChunkAt(source)) return null;
    var offered = LiquidUtil.drainWorldFluidBlock(world, source, true);
    return offered != null
            && !offered.isEmpty()
            && offered.getAmountMb() <= free
            && fluidTank.fillMbUnchecked(offered, true) == offered.getAmountMb()
        ? new Intake(source.immutable(), side, false, offered)
        : null;
  }

  /**
   * Public miner preview/drain contract; only operate() inserts the drained quantity into this
   * pump.
   */
  public Ic2FluidStack pump(BlockPos start, boolean simulate, PipeMiner linked) {
    var world = getLevel();
    if (world == null || !simulate && world.isClientSide()) return null;
    var planned = plan(start, linked);
    if (planned == null) return null;
    if (simulate) return planned.preview.copy();
    var actual =
        planned.capability
            ? LiquidUtil.drainTile(
                world.getBlockState(planned.position),
                world,
                planned.position,
                planned.side,
                planned.preview.getAmountMb(),
                false)
            : LiquidUtil.drainWorldFluidBlock(world, planned.position, false);
    if (actual == null || actual.isEmpty()) return null;
    if (actual.getAmountMb() != planned.preview.getAmountMb()
        || !actual.hasExactFluid(planned.preview))
      throw new IllegalStateException("Pump source violated its reserved fluid quantity");
    return actual;
  }

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
    throw new IllegalArgumentException("Unknown pump GUI value: " + name);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return UPGRADES;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return DocumentMenu.create(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(syncId, inventory, this);
  }
}
