package ic2ce.neoforge.processing.fabrication;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.recipe.IPatternStorage;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.core.util.StackTag;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.processing.uu.UuIndex;
import ic2ce.engine.fluid.FractionalFluidLedger;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.FluidContainerInventory;
import ic2ce.neoforge.inventory.FluidWhitelistInput;
import ic2ce.neoforge.inventory.OutputInventory;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.processing.menu.ReplicationMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Replication commits the paid-for pattern before refreshing a continuous selection. */
@NotClassic
public class PatternReplicator extends PoweredBlockEntity
    implements IHasGui, IUpgradableBlock, INetworkClientTileEntityEventListener {
  private double uuPerTick = 1e-4, euPerTick = 512;
  private final FractionalFluidLedger prepaid = new FractionalFluidLedger();
  public double uuProcessed;
  public ItemStack pattern = ItemStack.EMPTY;
  private Mode mode = Mode.STOPPED;
  public int index, maxIndex;
  public double patternUu, patternEu;
  public final FluidContainerInventory fluidSlot =
      new FluidWhitelistInput(this, "fluid", 1, FluidDefinitions.UU_MATTER.still);
  public final OutputInventory cellSlot = new OutputInventory(this, "cell", 1);
  public final OutputInventory outputSlot = new OutputInventory(this, "output", 1);
  public final UpgradeInventory upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
  @GuiSynced public final Ic2FluidTank fluidTank;
  protected final FluidPorts fluids = addComponent(new FluidPorts(this));
  private static final Direction[] SIDES = Direction.values();

  public PatternReplicator(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.REPLICATOR, position, state, 2000000, 4);
    fluidTank =
        fluids.addTank(
            "fluidTank", 16000, FluidPorts.fluidPredicate(FluidDefinitions.UU_MATTER.still));
  }

  private boolean usablePattern() {
    return pattern != null && !pattern.isEmpty() && Double.isFinite(patternUu) && patternUu >= 0;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean changed = fluidTank.getFluidAmount() < fluidTank.getCapacity() && gainFluid();
    boolean running = false;
    if (mode != Mode.STOPPED
        && usablePattern()
        && Double.isFinite(euPerTick)
        && euPerTick >= 0
        && Double.isFinite(uuPerTick)
        && uuPerTick > 0
        && energy.getEnergy() >= euPerTick
        && outputSlot.canAdd(pattern)) {
      double remaining = Math.max(0, patternUu - uuProcessed);
      double advance = Math.min(remaining, uuPerTick);
      if (consumeUu(advance)) {
        energy.useEnergy(euPerTick);
        running = true;
        if (remaining <= uuPerTick) {
          var produced = pattern.copyWithCount(1);
          outputSlot.add(produced);
          changed = true;
          uuProcessed = 0;
          if (mode == Mode.SINGLE) mode = Mode.STOPPED;
          else refreshInfo();
        } else uuProcessed += advance;
      }
    }
    setActive(running);
    changed |= upgradeSlot.tickNoMark();
    if (changed) setChanged();
  }

  private boolean consumeUu(double buckets) {
    int required = prepaid.requiredMillibuckets(buckets);
    if (required < 0 || fluidTank.getFluidAmount() < required) return false;
    if (required > 0) {
      var fluid = fluidTank.getFluidStack();
      if (fluid == null || fluid.getFluid() != FluidDefinitions.UU_MATTER.still) return false;
      fluidTank.drainMbUnchecked(required, false);
    }
    prepaid.commit(buckets, required);
    return true;
  }

  public void refreshInfo() {
    var old = pattern;
    var storage = getPatternStorage();
    var entries = storage == null ? java.util.List.<ItemStack>of() : storage.getPatterns();
    maxIndex = entries.size();
    index = maxIndex == 0 ? 0 : Math.floorMod(index, maxIndex);
    var selected = maxIndex == 0 ? ItemStack.EMPTY : entries.get(index);
    pattern = selected == null || selected.isEmpty() ? ItemStack.EMPTY : selected.copyWithCount(1);
    patternUu = pattern.isEmpty() ? 0 : UuIndex.instance.getInBuckets(pattern);
    patternEu = usablePattern() && uuPerTick > 0 ? Math.ceil(patternUu / uuPerTick) * euPerTick : 0;
    if (!usablePattern() || !StackUtil.checkItemEqualityStrict(pattern, old)) {
      uuProcessed = 0;
      mode = Mode.STOPPED;
    } else uuProcessed = Double.isFinite(uuProcessed) ? Math.clamp(uuProcessed, 0, patternUu) : 0;
  }

  public IPatternStorage getPatternStorage() {
    var world = getLevel();
    if (world == null) return null;
    for (var side : SIDES)
      if (world.getBlockEntity(worldPosition.relative(side)) instanceof IPatternStorage storage)
        return storage;
    return null;
  }

  public void setOverclockRates() {
    upgradeSlot.onChanged();
    double multiplier = upgradeSlot.processTimeMultiplier;
    uuPerTick =
        multiplier > 0 && Double.isFinite(multiplier)
            ? Math.min(Double.MAX_VALUE, 1e-4 / multiplier)
            : 0;
    euPerTick = (512.0 + upgradeSlot.extraEnergyDemand) * upgradeSlot.energyDemandMultiplier;
    energy.setSinkTier(modifier(4, upgradeSlot.extraTier, 1));
    energy.setCapacity(
        modifier(2000000, upgradeSlot.extraEnergyStorage, upgradeSlot.energyStorageMultiplier));
  }

  private static int modifier(int base, int extra, double scale) {
    double value = ((double) base + extra) * scale;
    return Double.isNaN(value) ? 0 : (int) Math.clamp(Math.round(value), 0, Integer.MAX_VALUE);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (ProjectRuntime.sideProxy.isSimulating()) {
      setOverclockRates();
      refreshInfo();
    }
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (ProjectRuntime.sideProxy.isSimulating()) setOverclockRates();
  }

  public boolean gainFluid() {
    return fluidSlot.processIntoTank(fluidTank, cellSlot);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    prepaid.restore(data.getDoubleOr("extraUuStored", 0));
    double stored = data.getDoubleOr("uuProcessed", 0);
    uuProcessed = Double.isFinite(stored) && stored >= 0 ? stored : 0;
    index = Math.max(0, data.getIntOr("index", 0));
    int saved = data.getIntOr("mode", 0);
    mode = saved >= 0 && saved < Mode.values().length ? Mode.values()[saved] : Mode.STOPPED;
    var recorded = StackTag.parse(getSerializationLookup(), data.getCompoundOrEmpty("pattern"));
    pattern = recorded.isEmpty() ? ItemStack.EMPTY : recorded.copyWithCount(1);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putDouble("extraUuStored", prepaid.creditBuckets());
    data.putDouble("uuProcessed", uuProcessed);
    data.putInt("index", index);
    data.putInt("mode", mode.ordinal());
    if (pattern == null || pattern.isEmpty()) data.remove("pattern");
    else data.put("pattern", StackTag.save(getSerializationLookup(), pattern, new CompoundTag()));
  }

  @Override
  public void onNetworkEvent(Player actor, int event) {
    switch (event) {
      case 0, 1 -> {
        if (mode == Mode.STOPPED) {
          index += event == 0 ? -1 : 1;
          refreshInfo();
          setChanged();
        }
      }
      case 3 -> {
        mode = Mode.STOPPED;
        uuProcessed = 0;
        setChanged();
      }
      case 4, 5 -> {
        if (usablePattern()) {
          mode = event == 4 ? Mode.SINGLE : Mode.CONTINUOUS;
          setChanged();
          if (actor != null) ProjectRuntime.achievements.issueAchievement(actor, "replicateObject");
        }
      }
      default -> {}
    }
  }

  public Mode getMode() {
    return mode;
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
    return EnumSet.of(
        UpgradableProperty.Processing,
        UpgradableProperty.RedstoneSensitive,
        UpgradableProperty.Transformer,
        UpgradableProperty.EnergyStorage,
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing,
        UpgradableProperty.FluidConsuming);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return new ReplicationMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new ReplicationMenu(id, inventory, this);
  }

  public enum Mode {
    STOPPED,
    SINGLE,
    CONTINUOUS
  }
}
