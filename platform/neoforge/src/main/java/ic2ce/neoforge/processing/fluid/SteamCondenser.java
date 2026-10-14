package ic2ce.neoforge.processing.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.upgrade.*;
import ic2ce.core.*;
import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.profile.NotClassic;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.processing.menu.CondensationMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Passive condensation plus individually paid fans; the outlet reserves each 100mB water batch. */
@NotClassic
public class SteamCondenser extends PoweredBlockEntity
    implements IHasGui, ScreenValues, IUpgradableBlock {
  public final short ventEUCost = 2;
  public int progress;
  public final int maxProgress = 10000;
  private final Ic2FluidTank inputTank, outputTank;
  public final TankContainerInput waterInputSlot;
  public final OutputInventory waterOutputSlot;
  public final ItemWhitelistInput ventSlots;
  public final UpgradeInventory upgradeSlot;
  protected final FluidPorts fluids;
  private static final Set<UpgradableProperty> PROPERTIES =
      Set.of(
          UpgradableProperty.ItemConsuming,
          UpgradableProperty.ItemProducing,
          UpgradableProperty.FluidConsuming,
          UpgradableProperty.FluidProducing,
          UpgradableProperty.Transformer);

  public SteamCondenser(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.CONDENSER, position, state, 100000, 3);
    fluids = addComponent(new FluidPorts(this));
    inputTank =
        fluids.addTankInsert(
            "inputTank",
            100000,
            FluidPorts.fluidPredicate(
                FluidDefinitions.STEAM.still, FluidDefinitions.SUPERHEATED_STEAM.still));
    outputTank = fluids.addTankExtract("outputTank", 1000);
    waterInputSlot =
        new TankContainerInput(
            this,
            "waterInputSlot",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.BOTTOM,
            FluidContainerInventory.OpType.Fill,
            outputTank);
    waterOutputSlot = new OutputInventory(this, "waterOutputSlot", 1);
    ventSlots = new ItemWhitelistInput(this, "ventSlots", 4, ItemDefinitions.HEAT_VENT);
    ventSlots.setStackSizeLimit(1);
    upgradeSlot = new UpgradeInventory(this, "upgradeSlot", 1);
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
  protected void onLoaded() {
    super.onLoaded();
    if (!getLevel().isClientSide()) updateTier();
  }

  private void updateTier() {
    upgradeSlot.onChanged();
    int tier = upgradeSlot.getTier(3);
    energy.setSinkTier(tier);
    dischargeSlot.setTier(tier);
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (getLevel() != null && !getLevel().isClientSide() && upgradeSlot != null) updateTier();
  }

  public byte getVents() {
    byte count = 0;
    for (int slot = 0; slot < ventSlots.size(); slot++)
      if (ventSlots.get(slot).is(ItemDefinitions.HEAT_VENT)) count++;
    return count;
  }

  private boolean emitWater() {
    if (progress < maxProgress) return true;
    var batch = Ic2FluidStack.create(FluidDefinitions.DISTILLED_WATER.still, 100);
    if (outputTank.fillMbUnchecked(batch, true) != 100) return false;
    outputTank.fillMbUnchecked(batch, false);
    progress -= maxProgress;
    return true;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    waterInputSlot.processFromTank(outputTank, waterOutputSlot);
    int previousProgress = progress;
    boolean changed = false, working = false;
    if (emitWater()
        && !inputTank.isEmpty()
        && outputTank.fillMbUnchecked(
                Ic2FluidStack.create(FluidDefinitions.DISTILLED_WATER.still, 100), true)
            == 100) {
      int available = Math.min(inputTank.getFluidAmount(), maxProgress - progress);
      int usefulFans = Math.min(getVents(), Math.max(0, (available - 1) / 100));
      int paidFans = Math.min(usefulFans, (int) Math.min(4, energy.getEnergy() / ventEUCost));
      int drain = Math.min(available, 100 + paidFans * 100);
      if (drain > 0 && energy.useEnergy(paidFans * ventEUCost)) {
        progress += inputTank.drainMbUnchecked(drain, false).getAmountMb();
        changed = working = true;
        emitWater();
      }
    }
    setActive(working);
    changed |= upgradeSlot.tickNoMark();
    if (changed || previousProgress != progress) super.setChanged();
  }

  public Ic2FluidTank getInputTank() {
    return inputTank;
  }

  public Ic2FluidTank getOutputTank() {
    return outputTank;
  }

  @Override
  public double getGuiValue(String key) {
    if (!key.equals("progress"))
      throw new IllegalArgumentException("Unknown condenser gauge: " + key);
    return (double) progress / maxProgress;
  }

  public int gaugeLiquidScaled(int width, int index) {
    var tank = index == 0 ? inputTank : index == 1 ? outputTank : null;
    return tank == null
        ? 0
        : (int) ((long) Math.max(0, width) * tank.getFluidAmount() / tank.getCapacity());
  }

  @Override
  public double getEnergy() {
    return 0;
  }

  @Override
  public boolean useEnergy(double amount) {
    return amount == 0;
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return PROPERTIES;
  }

  @Override
  public OwnedMenu<SteamCondenser> createServerScreenHandler(int syncId, Player player) {
    return new CondensationMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new CondensationMenu(syncId, inventory, this);
  }
}
