package ic2ce.neoforge.mining;

import ic2ce.api.item.ElectricItem;
import ic2ce.api.item.IMiningDrill;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.neoforge.fluid.FluidHandler;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.fluid.LiquidUtil;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.item.tool.OreScanner;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.mining.menu.PipeMinerMenu;
import ic2ce.neoforge.processing.fluid.FluidCollector;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.BlockGroups;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.AutomationActors;
import ic2ce.neoforge.terrain.MaterialValuation;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** Pipe descent, lateral drill work, pump handoff and paid retraction use distinct work phases. */
public class PipeMiner extends PoweredBlockEntity implements IHasGui, IUpgradableBlock {
  private static final Direction[] DIRECTIONS = Direction.values();
  private static final Set<UpgradableProperty> UPGRADES =
      Set.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
  private Mode lastMode = Mode.None;
  private int scannedLevel = Integer.MIN_VALUE, scanRange, lastX, lastZ;
  private BlockPos pendingTarget;
  private BlockState pendingState;
  public int progress;
  public boolean pumpMode, canProvideLiquid;
  public BlockPos liquidPos;
  public final InventorySlot buffer;
  public final UpgradeInventory upgradeSlot;
  public final ConsumableInventory drillSlot, pipeSlot, scannerSlot;
  boolean tickingUpgrades;

  public PipeMiner(BlockPos position, BlockState state) {
    super(
        BlockEntityDefinitions.MINER,
        position,
        state,
        1000,
        Math.max(
            0, NativeConfigValues.getInt(ProjectConfiguration.get(), "balance/minerDischargeTier")),
        false);
    drillSlot =
        new TypeFilteredInput(
            this,
            "drill",
            InventorySlot.Access.IO,
            1,
            InventorySlot.InvSide.TOP,
            IMiningDrill.class) {
          @Override
          public boolean canOutput() {
            return !tickingUpgrades && super.canOutput();
          }
        };
    pipeSlot =
        new BlockInput(this, "pipe", InventorySlot.Access.IO, 1, InventorySlot.InvSide.TOP) {
          @Override
          public boolean canOutput() {
            return !tickingUpgrades && super.canOutput();
          }
        };
    scannerSlot =
        new ItemWhitelistInput(
            this,
            "scanner",
            InventorySlot.Access.IO,
            1,
            InventorySlot.InvSide.BOTTOM,
            ItemDefinitions.SCANNER,
            ItemDefinitions.ADVANCED_SCANNER) {
          @Override
          public boolean canOutput() {
            return !tickingUpgrades && super.canOutput();
          }
        };
    upgradeSlot = new UpgradeInventory(this, "upgrade", 1);
    buffer =
        new InventorySlot(this, "buffer", InventorySlot.Access.IO, 15, InventorySlot.InvSide.SIDE);
    lastX = position.getX();
    lastZ = position.getZ();
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    scannedLevel = Integer.MIN_VALUE;
    lastX = worldPosition.getX();
    lastZ = worldPosition.getZ();
    canProvideLiquid = false;
    liquidPos = null;
  }

  @Override
  protected void onUnloaded() {
    canProvideLiquid = false;
    liquidPos = null;
    pendingTarget = null;
    pendingState = null;
    super.onUnloaded();
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    int id = data.getIntOr("lastMode", 0);
    lastMode = id >= 0 && id < Mode.VALUES.length ? Mode.VALUES[id] : Mode.None;
    progress = Math.max(0, data.getIntOr("progress", 0));
    pendingTarget = null;
    pendingState = null;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("lastMode", lastMode.ordinal());
    data.putInt("progress", progress);
  }

  private int tickReserve() {
    var item = drillSlot.get().getItem();
    return item == ItemDefinitions.IRIDIUM_DRILL
        ? 200
        : item == ItemDefinitions.DIAMOND_DRILL ? 20 : drillSlot.isEmpty() ? 3 : 6;
  }

  private void charge(ItemStack stack, int tier, int reserve) {
    if (stack.isEmpty()) return;
    double available = Math.max(0, energy.getEnergy() - reserve);
    if (available == 0) return;
    double offered = ElectricItem.manager.charge(stack, available, tier, false, true);
    if (!Double.isFinite(offered) || offered < 0 || offered > available)
      throw new IllegalStateException("Mining tool exceeded its charge preview");
    if (offered == 0 || !energy.useEnergy(offered)) return;
    double accepted = ElectricItem.manager.charge(stack, offered, tier, false, false);
    if (!Double.isFinite(accepted) || accepted < 0 || accepted > offered)
      throw new IllegalStateException("Mining tool exceeded reserved charging work");
    if (accepted < offered) energy.forceAddEnergy(offered - accepted);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    int reserve = tickReserve();
    charge(scannerSlot.get(), 2, reserve);
    charge(drillSlot.get(), 3, reserve);
    tickingUpgrades = true;
    try {
      upgradeSlot.tick();
    } finally {
      tickingUpgrades = false;
    }
    boolean working = work();
    setActive(working);
    if (working) super.setChanged();
  }

  private BlockPos operationPosition() {
    var world = getLevel();
    var position = worldPosition.mutable().move(Direction.DOWN);
    while (position.getY() >= world.getMinY()
        && world.getBlockState(position).is(BlockDefinitions.MINING_PIPE))
      position.move(Direction.DOWN);
    return position.immutable();
  }

  private boolean work() {
    var world = getLevel();
    if (world == null) return false;
    var position = operationPosition();
    if (drillSlot.isEmpty()) return retract(position);
    if (position.getY() < world.getMinY()) return false;
    var state = world.getBlockState(position);
    if (!state.is(BlockDefinitions.MINING_PIPE_TIP)) return descend(position, state, false);
    var layer = mineLevel(position.getY());
    if (layer != MineResult.Done) return layer == MineResult.Working;
    var below = position.below();
    return descend(below, world.getBlockState(below), true);
  }

  private void phase(Mode mode, BlockPos position, BlockState state) {
    if (lastMode != mode
        || pendingTarget != null && (!pendingTarget.equals(position) || pendingState != state))
      progress = 0;
    lastMode = mode;
    pendingTarget = position.immutable();
    pendingState = state;
  }

  private boolean retract(BlockPos bottom) {
    var world = getLevel();
    var position =
        bottom.getY() >= world.getMinY()
                && world.getBlockState(bottom).is(BlockDefinitions.MINING_PIPE_TIP)
            ? bottom
            : bottom.above();
    if (position.getY() >= worldPosition.getY()) return false;
    var state = world.getBlockState(position);
    if (!state.is(BlockDefinitions.MINING_PIPE) && !state.is(BlockDefinitions.MINING_PIPE_TIP))
      return false;
    phase(Mode.Withdraw, position, state);
    if (progress < 20) {
      if (!energy.useEnergy(3)) return false;
      progress++;
    }
    if (progress < 20) return true;
    if (!world.removeBlock(position, false)) return false;
    progress = 0;
    pendingTarget = null;
    storeDrop(new ItemStack(ItemDefinitions.MINING_PIPE));
    var filler = pipeSlot.consume(1, true, false);
    if (filler != null
        && !filler.is(ItemDefinitions.MINING_PIPE)
        && filler.getItem() instanceof BlockItem block) {
      if (block.place(
              new DirectionalPlaceContext(
                  world, position.above(), Direction.DOWN, filler.copyWithCount(1), Direction.UP))
          instanceof net.minecraft.world.InteractionResult.Success) pipeSlot.consume(1);
    }
    return true;
  }

  private boolean descend(BlockPos position, BlockState state, boolean tipAbove) {
    var pipe = pipeSlot.consume(1, true, false);
    if (pipe == null || !pipe.is(ItemDefinitions.MINING_PIPE)) return false;
    var world = getLevel();
    if (position.getY() < world.getMinY()) {
      if (tipAbove)
        world.setBlockAndUpdate(position.above(), BlockDefinitions.MINING_PIPE.defaultBlockState());
      return false;
    }
    var result = mineBlock(position, state);
    if (result == MineResult.Done) {
      if (!world.setBlockAndUpdate(position, BlockDefinitions.MINING_PIPE_TIP.defaultBlockState()))
        return false;
      pipeSlot.consume(1);
      if (tipAbove)
        world.setBlockAndUpdate(position.above(), BlockDefinitions.MINING_PIPE.defaultBlockState());
      return true;
    }
    if (result == MineResult.Failed_Perm && tipAbove)
      world.setBlockAndUpdate(position.above(), BlockDefinitions.MINING_PIPE.defaultBlockState());
    return result == MineResult.Working;
  }

  private MineResult mineLevel(int y) {
    if (scannerSlot.isEmpty()) return MineResult.Done;
    if (scannedLevel != y) {
      scanRange = ((OreScanner) scannerSlot.get().getItem()).startLayerScan(scannerSlot.get());
      if (scanRange <= 0) return MineResult.Failed_Temp;
      scannedLevel = y;
      lastX = worldPosition.getX();
      lastZ = worldPosition.getZ();
    }
    var world = getLevel();
    var target = new BlockPos.MutableBlockPos();
    for (int x = -scanRange; x <= scanRange; x++)
      for (int z = -scanRange; z <= scanRange; z++) {
        target.set(worldPosition.getX() + x, y, worldPosition.getZ() + z);
        if (!world.hasChunkAt(target)) return MineResult.Failed_Temp;
        var state = world.getBlockState(target);
        if (state.isAir()
            || state.is(BlockDefinitions.MINING_PIPE)
            || state.is(BlockDefinitions.MINING_PIPE_TIP)) continue;
        var liquid = LiquidUtil.getLiquid(world, target);
        boolean interesting =
            liquid != null
                ? pumpMode && canPump(target)
                : state.is(BlockGroups.ORES)
                    || MaterialValuation.get(
                            StackUtil.getPickStack(
                                world, target, state, AutomationActors.get(world)))
                        > 0
                    || MaterialValuation.get(StackUtil.getDrops(world, target, state, 0)) > 0;
        if (!interesting || liquid == null && !canMine(target, state)) continue;
        var result = mineTowards(target);
        if (result == MineResult.Done) return MineResult.Working;
        if (result != MineResult.Failed_Perm) return result;
      }
    return MineResult.Done;
  }

  private MineResult mineTowards(BlockPos destination) {
    int dx = Math.abs(destination.getX() - worldPosition.getX()),
        dz = Math.abs(destination.getZ() - worldPosition.getZ());
    int sx = Integer.compare(destination.getX(), worldPosition.getX()),
        sz = Integer.compare(destination.getZ(), worldPosition.getZ());
    int cx = worldPosition.getX(), cz = worldPosition.getZ(), doneX = 0, doneZ = 0;
    while (doneX < dx || doneZ < dz) {
      boolean advanceAir = cx == lastX && cz == lastZ;
      long xError = Math.abs((long) (doneX + 1) * dz - (long) doneZ * dx),
          zError = Math.abs((long) doneX * dz - (long) (doneZ + 1) * dx);
      if (doneX < dx && (doneZ == dz || xError <= zError)) {
        cx += sx;
        doneX++;
      } else {
        cz += sz;
        doneZ++;
      }
      var target = new BlockPos(cx, destination.getY(), cz);
      var world = getLevel();
      if (!world.hasChunkAt(target)) return MineResult.Failed_Temp;
      var state = world.getBlockState(target);
      var liquid = state.isAir() ? null : LiquidUtil.getLiquid(world, target);
      if (!advanceAir
          && (state.isAir()
              || liquid != null && !liquid.isSource && !(pumpMode && canPump(target)))) continue;
      var result = mineBlock(target, state);
      if (result == MineResult.Done) {
        lastX = cx;
        lastZ = cz;
      }
      return result;
    }
    lastX = worldPosition.getX();
    lastZ = worldPosition.getZ();
    return MineResult.Done;
  }

  private MineResult mineBlock(BlockPos position, BlockState state) {
    var world = getLevel();
    if (!state.isAir()) {
      var liquid = LiquidUtil.getLiquid(world, position);
      if (liquid != null && (liquid.isSource || pumpMode && canPump(position))) {
        liquidPos = position.immutable();
        canProvideLiquid = true;
        return isAnyPumpConnected() ? MineResult.Failed_Temp : MineResult.Failed_Perm;
      }
      if (liquid == null && !canMine(position, state)) return MineResult.Failed_Perm;
    }
    canProvideLiquid = false;
    liquidPos = null;
    var item = drillSlot.get().getItem();
    Mode mode;
    int cost, duration;
    if (state.isAir()) {
      mode = Mode.MineAir;
      cost = 3;
      duration = 20;
    } else if (item == ItemDefinitions.DRILL) {
      mode = Mode.MineDrill;
      cost = 6;
      duration = 200;
    } else if (item == ItemDefinitions.DIAMOND_DRILL) {
      mode = Mode.MineDDrill;
      cost = 20;
      duration = 50;
    } else if (item == ItemDefinitions.IRIDIUM_DRILL) {
      mode = Mode.MineIDrill;
      cost = 200;
      duration = 20;
    } else if (item instanceof IMiningDrill drill) {
      mode = Mode.MineCustomDrill;
      cost = drill.energyUse(drillSlot.get(), world, position, state);
      duration = drill.breakTime(drillSlot.get(), world, position, state);
      if (cost < 0 || duration < 1) return MineResult.Failed_Perm;
    } else return MineResult.Failed_Perm;
    phase(mode, position, state);
    if (progress < duration) {
      if (!energy.useEnergy(cost)) return MineResult.Failed_Temp;
      progress++;
    }
    if (progress < duration) return MineResult.Working;
    if (state.isAir() || harvestBlock(position, state)) {
      progress = 0;
      pendingTarget = null;
      pendingState = null;
      return MineResult.Done;
    }
    return MineResult.Failed_Temp;
  }

  private boolean harvestBlock(BlockPos position, BlockState state) {
    var world = getLevel();
    double depthBill = Math.max(0, 2L * ((long) worldPosition.getY() - position.getY()));
    if (world.getBlockState(position) != state || !energy.canUseEnergy(depthBill)) return false;
    var actor = AutomationActors.get(world);
    if (actor == null
        || NeoForge.EVENT_BUS.post(new BreakBlockEvent(world, position, state, actor)).isCanceled()
        || world.getBlockState(position) != state) return false;
    if (!energy.useEnergy(depthBill)) return false;
    boolean toolPaid =
        switch (lastMode) {
          case MineDrill -> ElectricItem.manager.use(drillSlot.get(), 50, null);
          case MineDDrill -> ElectricItem.manager.use(drillSlot.get(), 80, null);
          case MineIDrill -> ElectricItem.manager.use(drillSlot.get(), 800, null);
          case MineCustomDrill ->
              ((IMiningDrill) drillSlot.get().getItem())
                  .breakBlock(drillSlot.get(), world, position, state);
          default -> false;
        };
    if (!toolPaid) {
      energy.forceAddEnergy(depthBill);
      return false;
    }
    var drops = StackUtil.getDrops(world, position, state, lastMode == Mode.MineIDrill ? 3 : 0);
    if (!world.removeBlock(position, false)) {
      energy.forceAddEnergy(depthBill);
      return false;
    }
    for (var drop : drops) storeDrop(drop);
    return true;
  }

  private void storeDrop(ItemStack drop) {
    if (drop == null || drop.isEmpty()) return;
    var remaining = drop.copy();
    for (int index = 0; index < buffer.size() && !remaining.isEmpty(); index++) {
      var stored = buffer.get(index);
      if (!stored.isEmpty() && !ItemStack.isSameItemSameComponents(stored, remaining)) continue;
      int limit = Math.min(buffer.getStackSizeLimit(), remaining.getMaxStackSize()),
          accepted = Math.min(remaining.getCount(), Math.max(0, limit - stored.getCount()));
      if (accepted == 0) continue;
      buffer.put(
          index,
          (stored.isEmpty() ? remaining : stored).copyWithCount(stored.getCount() + accepted));
      remaining.shrink(accepted);
    }
    if (!remaining.isEmpty()) StackUtil.dropAsEntity(getLevel(), worldPosition, remaining);
    super.setChanged();
  }

  public boolean canPump(BlockPos target) {
    return isPumpConnected(target);
  }

  public boolean canMine(BlockPos position, BlockState state) {
    var block = state.getBlock();
    if (state.isAir()) return true;
    if (block == BlockDefinitions.MINING_PIPE
        || block == BlockDefinitions.MINING_PIPE_TIP
        || block == Blocks.CHEST) return false;
    if (block == Blocks.WATER || block == Blocks.LAVA || FluidHandler.getWorldFluid(state) != null)
      return isPumpConnected(position);
    if (state.getDestroySpeed(getLevel(), position) < 0) return false;
    return !state.requiresCorrectToolForDrops()
        || block == Blocks.COBWEB
        || !drillSlot.isEmpty() && drillSlot.get().isCorrectToolForDrops(state);
  }

  public boolean isPumpConnected(BlockPos target) {
    var world = getLevel();
    if (world == null || target == null) return false;
    for (var side : DIRECTIONS) {
      var position = worldPosition.relative(side);
      if (world.hasChunkAt(position)
          && world.getBlockEntity(position) instanceof FluidCollector pump
          && pump.pump(target, true, this) != null) return true;
    }
    return false;
  }

  public boolean isAnyPumpConnected() {
    var world = getLevel();
    if (world == null) return false;
    for (var side : DIRECTIONS) {
      var position = worldPosition.relative(side);
      if (world.hasChunkAt(position) && world.getBlockEntity(position) instanceof FluidCollector)
        return true;
    }
    return false;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_MINER_OPERATE;
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
  public Set<UpgradableProperty> getUpgradableProperties() {
    return UPGRADES;
  }

  @Override
  public OwnedMenu<PipeMiner> createServerScreenHandler(int syncId, Player player) {
    return new PipeMinerMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new PipeMinerMenu(syncId, inventory, this);
  }

  enum MineResult {
    Working,
    Done,
    Failed_Temp,
    Failed_Perm
  }

  enum Mode {
    None,
    Withdraw,
    MineAir,
    MineDrill,
    MineDDrill,
    MineIDrill,
    MineCustomDrill;
    static final Mode[] VALUES = values();
  }
}
