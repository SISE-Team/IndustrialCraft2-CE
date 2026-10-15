package ic2ce.neoforge.agriculture;

import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.neoforge.agriculture.menu.FluidCareMenu;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Tank-fed nine-square care uses separate prepaid fertilizer, water and herbicide operations. */
public class FluidCropCaretaker extends PoweredBlockEntity implements IHasGui, IUpgradableBlock {
  public int scanX = -4, scanY = -1, scanZ = -4;
  protected final FluidPorts fluids = addComponent(new FluidPorts(this));
  protected final Ic2FluidTank waterTank =
      fluids.addTankInsert(
          "waterTank",
          2000,
          FluidPorts.fluidPredicate(net.minecraft.world.level.material.Fluids.WATER));
  protected final Ic2FluidTank exTank =
      fluids.addTankInsert(
          "exTank", 2000, FluidPorts.fluidPredicate(FluidDefinitions.WEED_EX.still));
  public final ConsumableInventory fertilizerSlot =
      new StackWhitelistInput(this, "fertilizer", 7, new ItemStack(ItemDefinitions.FERTILIZER));
  public final TankContainerInput wasserinputSlot =
      new TankContainerInput(
          this,
          "wasserinputSlot",
          InventorySlot.Access.I,
          1,
          InventorySlot.InvSide.TOP,
          FluidContainerInventory.OpType.Drain,
          waterTank);
  public final TankContainerInput exInputSlot =
      new TankContainerInput(
          this,
          "exInputSlot",
          InventorySlot.Access.I,
          1,
          InventorySlot.InvSide.TOP,
          FluidContainerInventory.OpType.Drain,
          exTank);
  public final OutputInventory wasseroutputSlot = new OutputInventory(this, "wasseroutputSlot", 1),
      exOutputSlot = new OutputInventory(this, "exOutputSlot", 1);
  public final UpgradeInventory upgradeSlot = new UpgradeInventory(this, "upgrade", 4);

  public FluidCropCaretaker(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.CROPMATRON, position, state, 10000, 1);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    scanX = Math.clamp(data.getIntOr("scanX", -4), -4, 4);
    scanY = Math.clamp(data.getIntOr("scanY", -1), -1, 1);
    scanZ = Math.clamp(data.getIntOr("scanZ", -4), -4, 4);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("scanX", scanX);
    data.putInt("scanY", scanY);
    data.putInt("scanZ", scanZ);
  }

  private void upgrades() {
    upgradeSlot.onChanged();
    int tier = upgradeSlot.getTier(1);
    energy.setSinkTier(tier);
    dischargeSlot.setTier(tier);
    energy.setCapacity(upgradeSlot.getEnergyStorage(10000, 0, 0));
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    upgrades();
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (getLevel() != null && !getLevel().isClientSide()) upgrades();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    upgradeSlot.tick();
    boolean changed =
        wasserinputSlot.processIntoTank(waterTank, wasseroutputSlot)
            | exInputSlot.processIntoTank(exTank, exOutputSlot);
    fertilizerSlot.organize();
    var world = getLevel();
    if (world != null && world.getGameTime() % 10 == 0 && energy.getEnergy() >= 31) scan();
    if (changed) super.setChanged();
  }

  private void advance() {
    int cell =
        Math.floorMod(
            (Math.clamp(scanY, -1, 1) + 1) * 81
                + (Math.clamp(scanZ, -4, 4) + 4) * 9
                + Math.clamp(scanX, -4, 4)
                + 5,
            243);
    scanX = cell % 9 - 4;
    scanZ = cell / 9 % 9 - 4;
    scanY = cell / 81 - 1;
  }

  public void scan() {
    var world = getLevel();
    if (world == null || world.isClientSide() || energy.getEnergy() < 1) return;
    advance();
    var position = worldPosition.offset(scanX, scanY, scanZ);
    if (!world.isLoaded(position) || world.isOutsideBuildHeight(position) || !energy.useEnergy(1))
      return;
    if (world.getBlockEntity(position) instanceof CropPlot crop) {
      if (crop.getStorageNutrients() < 100
          && fertilizerSlot.consume(1, true, false) != null
          && energy.useEnergy(10)) {
        if (crop.applyFertilizer(false)) fertilizerSlot.consume(1);
        else energy.forceAddEnergy(10);
      }
      int water = crop.applyHydration(waterTank.getFluidAmount(), true);
      if (water > 0 && energy.useEnergy(10)) {
        int applied = crop.applyHydration(water, false);
        if (applied > 0) waterTank.drainMbUnchecked(applied, false);
        else energy.forceAddEnergy(10);
      }
      int pesticide = crop.applyWeedEx(exTank.getFluidAmount(), false, false, true);
      if (pesticide > 0 && energy.useEnergy(10)) {
        int applied = crop.applyWeedEx(pesticide, false, false, false);
        if (applied > 0) exTank.drainMbUnchecked(applied, false);
        else energy.forceAddEnergy(10);
      }
    } else {
      var state = world.getBlockState(position);
      if (state.is(Blocks.FARMLAND)
          && state.getValue(FarmlandBlock.MOISTURE) < 7
          && !waterTank.isEmpty()
          && energy.useEnergy(10)) {
        int units =
            Math.min(waterTank.getFluidAmount(), 7 - state.getValue(FarmlandBlock.MOISTURE));
        if (world.setBlock(
            position,
            state.setValue(FarmlandBlock.MOISTURE, state.getValue(FarmlandBlock.MOISTURE) + units),
            2)) waterTank.drainMbUnchecked(units, false);
        else energy.forceAddEnergy(10);
      }
    }
    super.setChanged();
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
    return Set.of(
        UpgradableProperty.Transformer,
        UpgradableProperty.EnergyStorage,
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.FluidConsuming);
  }

  @Override
  public OwnedMenu<FluidCropCaretaker> createServerScreenHandler(int id, Player player) {
    return new FluidCareMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new FluidCareMenu(id, inventory, this);
  }

  public Ic2FluidTank getWaterTank() {
    return waterTank;
  }

  public Ic2FluidTank getExTank() {
    return exTank;
  }
}
