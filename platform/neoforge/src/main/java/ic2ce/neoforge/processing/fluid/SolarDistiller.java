package ic2ce.neoforge.processing.fluid;

import ic2ce.api.crops.BiomeType;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.upgrade.*;
import ic2ce.core.*;
import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.profile.NotClassic;
import ic2ce.core.util.BiomeUtil;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.power.SolarCollector;
import ic2ce.neoforge.processing.menu.DistillationMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Sunlight-driven 1:1 distillation with biome-specific cadence and exact output reservation. */
@NotClassic
public class SolarDistiller extends InventoryBlockEntity implements IHasGui, IUpgradableBlock {
  public final Ic2FluidTank inputTank, outputTank;
  private int tickrate = 72, updateTicker;
  private float skyLight;
  public final OutputInventory wateroutputSlot, destiwateroutputSlott;
  public final FluidWhitelistInput waterinputSlot;
  public final TankContainerInput destiwaterinputSlot;
  public final UpgradeInventory upgradeSlot;
  protected final FluidPorts fluids;
  private static final Set<UpgradableProperty> PROPERTIES =
      Set.of(
          UpgradableProperty.ItemConsuming,
          UpgradableProperty.ItemProducing,
          UpgradableProperty.FluidProducing);

  public SolarDistiller(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.SOLAR_DISTILLER, position, state);
    fluids = addComponent(new FluidPorts(this));
    inputTank =
        fluids.addTankInsert(
            "inputTank",
            10000,
            FluidPorts.fluidPredicate(net.minecraft.world.level.material.Fluids.WATER));
    outputTank = fluids.addTankExtract("outputTank", 10000);
    waterinputSlot =
        new FluidWhitelistInput(
            this,
            "waterInput",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.TOP,
            FluidContainerInventory.OpType.Drain,
            net.minecraft.world.level.material.Fluids.WATER);
    destiwaterinputSlot =
        new TankContainerInput(
            this,
            "destilledWaterInput",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.BOTTOM,
            FluidContainerInventory.OpType.Fill,
            outputTank);
    wateroutputSlot = new OutputInventory(this, "waterOutput", 1);
    destiwateroutputSlott = new OutputInventory(this, "destilledWaterOutput", 1);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 3);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    tickrate = getTickRate();
    updateTicker = (int) Math.floorMod(worldPosition.asLong(), tickrate);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    waterinputSlot.processIntoTank(inputTank, wateroutputSlot);
    boolean worked = false;
    if (++updateTicker >= tickrate) {
      updateTicker = 0;
      updateSunVisibility();
      if (canWork()) {
        outputTank.fillMbUnchecked(
            Ic2FluidStack.create(FluidDefinitions.DISTILLED_WATER.still, 1), false);
        inputTank.drainMbUnchecked(1, false);
        worked = true;
      }
      setActive(canWork());
    }
    destiwaterinputSlot.processFromTank(outputTank, destiwateroutputSlott);
    boolean upgraded = upgradeSlot.tickNoMark();
    if (worked || upgraded) setChanged();
  }

  public void updateSunVisibility() {
    skyLight =
        getLevel() == null ? 0 : SolarCollector.getSkyLight(getLevel(), worldPosition.above());
  }

  public int getTickRate() {
    if (getLevel() == null) return 72;
    var biome = BiomeUtil.getBiome(getLevel(), worldPosition);
    return ProjectRuntime.envProxy.biomeHasType(biome, BiomeType.HOT)
        ? 36
        : ProjectRuntime.envProxy.biomeHasType(biome, BiomeType.COLD) ? 144 : 72;
  }

  public boolean canWork() {
    return inputTank.hasExactFluid(net.minecraft.world.level.material.Fluids.WATER)
        && skyLight > 0.5F
        && outputTank.fillMbUnchecked(
                Ic2FluidStack.create(FluidDefinitions.DISTILLED_WATER.still, 1), true)
            == 1;
  }

  public int gaugeLiquidScaled(int width, int index) {
    var tank = index == 0 ? inputTank : index == 1 ? outputTank : null;
    return tank == null
        ? 0
        : (int) ((long) Math.max(0, width) * tank.getFluidAmount() / tank.getCapacity());
  }

  @Override
  public double getEnergy() {
    return 40;
  }

  @Override
  public boolean useEnergy(double amount) {
    return Double.isFinite(amount) && amount >= 0 && amount <= 40;
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return PROPERTIES;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new DistillationMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new DistillationMenu(syncId, inventory, this);
  }
}
