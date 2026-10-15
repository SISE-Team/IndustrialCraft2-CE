package ic2ce.neoforge.mining;

import ic2ce.api.item.ElectricItem;
import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.*;
import ic2ce.neoforge.fluid.FluidHandler;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.core.util.*;
import ic2ce.engine.machine.LayerSweep;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.item.tool.AdvancedOreScanner;
import ic2ce.neoforge.item.tool.OreScanner;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.mining.menu.AreaMinerMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.AutomationActors;
import ic2ce.neoforge.terrain.MaterialValuation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * Scanner-paid raster layers and separately reserved mining work over current loaded native blocks.
 */
@NotClassic
public class AreaMiner extends PoweredBlockEntity
    implements IHasGui, INetworkClientTileEntityEventListener, IUpgradableBlock {
  private static final Set<UpgradableProperty> UPGRADES =
      Set.of(
          UpgradableProperty.Augmentable,
          UpgradableProperty.RedstoneSensitive,
          UpgradableProperty.Transformer);
  private static final int SCAN_EU = 64, MINE_EU = 512;
  public final int defaultTier, workTick = 20;
  public boolean blacklist = true, silkTouch, redstonePowered;
  private int maxBlockScanCount = 5, ticker;
  private BlockPos mineTarget;
  public final ItemWhitelistInput scannerSlot =
      new ItemWhitelistInput(
          this,
          "scanner",
          InventorySlot.Access.IO,
          1,
          InventorySlot.InvSide.BOTTOM,
          ItemDefinitions.SCANNER,
          ItemDefinitions.ADVANCED_SCANNER);
  public final UpgradeInventory upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
  public final InventorySlot filterSlot =
      new InventorySlot(this, "list", InventorySlot.Access.NONE, 15);
  protected final SignalInput redstone;

  private record MiningPlan(BlockPos position, BlockState state, List<ItemStack> drops) {}

  public AreaMiner(BlockPos position, BlockState state) {
    this(
        position,
        state,
        (int)
            Math.clamp(
                2L
                    + NativeConfigValues.getInt(
                        ProjectConfiguration.get(), "balance/minerDischargeTier"),
                0,
                5));
  }

  public AreaMiner(BlockPos position, BlockState state, int tier) {
    super(BlockEntityDefinitions.ADVANCED_MINER, position, state, 4000000, tier);
    defaultTier = tier;
    redstone = addComponent(new SignalInput(this));
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (!getLevel().isClientSide()) setUpgradeStat();
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    mineTarget =
        data.contains("mineTargetX")
            ? new BlockPos(
                data.getIntOr("mineTargetX", 0),
                data.getIntOr("mineTargetY", 0),
                data.getIntOr("mineTargetZ", 0))
            : null;
    blacklist = data.getBooleanOr("blacklist", true);
    silkTouch = data.getBooleanOr("silkTouch", false);
    ticker = 0;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.remove("mineTargetX");
    data.remove("mineTargetY");
    data.remove("mineTargetZ");
    if (mineTarget != null) {
      data.putInt("mineTargetX", mineTarget.getX());
      data.putInt("mineTargetY", mineTarget.getY());
      data.putInt("mineTargetZ", mineTarget.getZ());
    }
    data.putBoolean("blacklist", blacklist);
    data.putBoolean("silkTouch", silkTouch);
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (getLevel() != null && !getLevel().isClientSide()) setUpgradeStat();
  }

  public void setUpgradeStat() {
    upgradeSlot.onChanged();
    int tier = upgradeSlot.getTier(defaultTier);
    energy.setSinkTier(tier);
    dischargeSlot.setTier(tier);
    maxBlockScanCount =
        (int) Math.clamp(5L * (1L + upgradeSlot.augmentation), 1, Integer.MAX_VALUE);
  }

  private void chargeTool() {
    if (scannerSlot.isEmpty()) return;
    double available = Math.max(0, energy.getEnergy() - MINE_EU);
    if (available == 0) return;
    var scanner = scannerSlot.get();
    double offered =
        ElectricItem.manager.charge(scanner, available, energy.getSinkTier(), false, true);
    if (!Double.isFinite(offered) || offered < 0 || offered > available)
      throw new IllegalStateException("Scanner charge preview exceeded its budget");
    if (offered == 0 || !energy.useEnergy(offered)) return;
    double accepted =
        ElectricItem.manager.charge(scanner, offered, energy.getSinkTier(), false, false);
    if (!Double.isFinite(accepted) || accepted < 0 || accepted > offered)
      throw new IllegalStateException("Scanner exceeded reserved charging work");
    if (accepted < offered) energy.forceAddEnergy(offered - accepted);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    chargeTool();
    if (work()) {
      super.setChanged();
      setActive(true);
    } else setActive(false);
  }

  private boolean work() {
    var world = getLevel();
    if (world == null || redstone.hasRedstoneInput() || !energy.canUseEnergy(MINE_EU)) return false;
    var scanner = scannerSlot.get();
    int radius =
        scanner.getItem() instanceof AdvancedOreScanner
            ? 32
            : scanner.getItem() instanceof OreScanner ? 16 : -1;
    if (radius < 0 || !ElectricItem.manager.canUse(scanner, SCAN_EU)) return false;
    if (mineTarget != null) {
      long x = (long) mineTarget.getX() - worldPosition.getX(),
          z = (long) mineTarget.getZ() - worldPosition.getZ();
      if (Math.abs(x) > radius
          || Math.abs(z) > radius
          || mineTarget.getY() >= worldPosition.getY()
          || (long) mineTarget.getY() < (long) world.getMinY() - 1) mineTarget = null;
      else if (mineTarget.getY() == world.getMinY() - 1) return false;
    }
    if (++ticker < workTick) return true;
    ticker = 0;
    long cursor = -1;
    int top = worldPosition.getY() - 1;
    if (mineTarget != null) {
      try {
        cursor =
            LayerSweep.index(
                Math.toIntExact((long) mineTarget.getX() - worldPosition.getX()),
                Math.toIntExact((long) top - mineTarget.getY()),
                Math.toIntExact((long) mineTarget.getZ() - worldPosition.getZ()),
                radius);
      } catch (IllegalArgumentException | ArithmeticException invalid) {
        mineTarget = null;
      }
    }
    boolean scanned = false;
    for (int count = 0;
        count < maxBlockScanCount && ElectricItem.manager.canUse(scanner, SCAN_EU);
        count++) {
      var cell = LayerSweep.cell(cursor + 1, radius);
      long y = (long) top - cell.depth();
      if (y < world.getMinY()) {
        mineTarget =
            new BlockPos(
                worldPosition.getX() - radius, world.getMinY() - 1, worldPosition.getZ() - radius);
        return scanned;
      }
      var target =
          new BlockPos(worldPosition.getX() + cell.x(), (int) y, worldPosition.getZ() + cell.z());
      if (!world.hasChunkAt(target)) return scanned;
      double paid =
          ElectricItem.manager.discharge(scanner, SCAN_EU, Integer.MAX_VALUE, true, false, false);
      if (paid != SCAN_EU)
        throw new IllegalStateException("Scanner failed its exact scan-work contract");
      cursor++;
      mineTarget = target;
      scanned = true;
      var state = world.getBlockState(target);
      var plan = plan(target, state.getBlock(), state);
      if (plan != null && commit(plan)) break;
    }
    return scanned;
  }

  private MiningPlan plan(BlockPos position, Block block, BlockState state) {
    var world = getLevel();
    if (world == null
        || world.isClientSide()
        || !world.hasChunkAt(position)
        || world.getBlockState(position) != state
        || state.getBlock() != block
        || state.isAir()
        || block instanceof BucketPickup
        || FluidHandler.getWorldFluid(state) != null
        || state.getDestroySpeed(world, position) < 0) return null;
    var drops = StackUtil.getDrops(world, position, state, null, 0, silkTouch);
    if (drops.isEmpty() || block instanceof EntityBlock && MaterialValuation.get(drops) <= 0)
      return null;
    boolean matched = false;
    for (var drop : drops)
      for (int index = 0; index < filterSlot.size(); index++) {
        var filter = filterSlot.get(index);
        if (!filter.isEmpty() && StackUtil.checkItemEquality(drop, filter)) {
          matched = true;
          break;
        }
      }
    if (blacklist == matched) return null;
    var owned = new ArrayList<ItemStack>(drops.size());
    for (var drop : drops) if (!drop.isEmpty()) owned.add(drop.copy());
    return owned.isEmpty() ? null : new MiningPlan(position.immutable(), state, owned);
  }

  private boolean commit(MiningPlan plan) {
    var world = getLevel();
    if (world.getBlockState(plan.position) != plan.state || !energy.canUseEnergy(MINE_EU))
      return false;
    var actor = AutomationActors.get(world);
    if (actor == null
        || NeoForge.EVENT_BUS
            .post(new BreakBlockEvent(world, plan.position, plan.state, actor))
            .isCanceled()) return false;
    if (world.getBlockState(plan.position) != plan.state || !energy.useEnergy(MINE_EU))
      return false;
    if (!world.removeBlock(plan.position, false)) {
      energy.forceAddEnergy(MINE_EU);
      return false;
    }
    StackUtil.distributeDrops(this, plan.drops);
    super.setChanged();
    return true;
  }

  public boolean canMine(BlockPos position, Block block, BlockState state) {
    return plan(position, block, state) != null;
  }

  public void doMine(BlockPos position, Block block, BlockState state) {
    var selected = plan(position, block, state);
    if (selected != null) commit(selected);
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (getLevel() == null || getLevel().isClientSide()) return;
    switch (event) {
      case 0 -> {
        mineTarget = null;
        ticker = 0;
      }
      case 1 -> {
        if (getActive()) return;
        blacklist = !blacklist;
      }
      case 2 -> {
        if (getActive()) return;
        silkTouch = !silkTouch;
      }
      default -> {
        return;
      }
    }
    super.setChanged();
  }

  public BlockPos getMineTarget() {
    return mineTarget;
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
  public void onPlaced(ItemStack blockItem, LivingEntity placer, Direction facing) {
    super.onPlaced(blockItem, placer, facing);
    if (getLevel() == null || getLevel().isClientSide()) return;
    var data = StackTag.get(blockItem);
    if (data == null) return;
    double stored = data.getDoubleOr("energy", 0);
    stored = Double.isFinite(stored) ? Math.clamp(stored, 0, energy.getCapacity()) : 0;
    energy.forceAddEnergy(stored - energy.getEnergy());
    super.setChanged();
  }

  @Override
  public ItemStack adjustDrop(ItemStack original, boolean wrench) {
    var drop = super.adjustDrop(original, wrench);
    if (drop == null || drop.isEmpty()) return drop;
    var owned = drop.copy();
    var data = StackTag.getOrCreate(owned);
    data.remove("energy");
    if (wrench || teBlock.getDefaultDrop() == MachineBlockType.DefaultDrop.Self) {
      double ratio =
          NativeConfigValues.getDouble(
              ProjectConfiguration.get(), "balance/energyRetainedInStorageBlockDrops");
      ratio = Double.isFinite(ratio) ? Math.clamp(ratio, 0, 1) : 0;
      if (ratio > 0 && energy.getEnergy() > 0)
        data.putDouble("energy", Math.min(energy.getCapacity(), energy.getEnergy()) * ratio);
    }
    StackTag.set(owned, data);
    return owned;
  }

  @Override
  public OwnedMenu<AreaMiner> createServerScreenHandler(int syncId, Player player) {
    return new AreaMinerMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new AreaMinerMenu(syncId, inventory, this);
  }
}
