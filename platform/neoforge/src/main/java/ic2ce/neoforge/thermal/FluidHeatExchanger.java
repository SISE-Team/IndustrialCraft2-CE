package ic2ce.neoforge.thermal;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.core.LiquidHeatExchangerManager;
import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.processing.menu.HeatExchangeMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/**
 * Each converted millibucket reserves equal cold output and keeps heat exceeding the current
 * emission budget.
 */
public class FluidHeatExchanger extends BufferedHeatSource implements IHasGui, IUpgradableBlock {
  public final Ic2FluidTank inputTank, outputTank;
  public final ConsumableInventory heatexchangerslots;
  public final OutputInventory hotoutputSlot, cooloutputSlot;
  public final FluidContainerInventory hotfluidinputSlot, coolfluidinputSlot;
  public final UpgradeInventory upgradeSlot;
  protected final FluidPorts fluids = addComponent(new FluidPorts(this));
  private long prepaidHeat;

  public FluidHeatExchanger(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.LIQUID_HEAT_EXCHANGER, position, block);
    inputTank =
        fluids.addTankInsert(
            "inputTank", 2000, FluidPorts.fluidPredicate(Recipes.liquidCooldownManager));
    outputTank = fluids.addTankExtract("outputTank", 2000);
    heatexchangerslots =
        new StackWhitelistInput(
            this, "heatExchanger", 10, new ItemStack(ItemDefinitions.HEAT_CONDUCTOR));
    heatexchangerslots.setStackSizeLimit(1);
    hotoutputSlot = new OutputInventory(this, "hotOutputSlot", 1);
    cooloutputSlot = new OutputInventory(this, "outputSlot", 1);
    hotfluidinputSlot =
        new FluidRecipeInput(
            this,
            "hotFluidInput",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.TOP,
            FluidContainerInventory.OpType.Drain,
            Recipes.liquidCooldownManager);
    coolfluidinputSlot =
        new TankContainerInput(
            this,
            "coolFluidOutput",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.BOTTOM,
            FluidContainerInventory.OpType.Fill,
            outputTank);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 3);
  }

  public static void init() {
    Recipes.liquidCooldownManager = new LiquidHeatExchangerManager(false);
    Recipes.liquidHeatupManager = new LiquidHeatExchangerManager(true);
    ProjectRuntime.envProxy.runAfterRegistryInit(
        () -> {
          int lava = cost("heatExchangerLava", 20),
              coolant = cost("heatExchangerHotCoolant", 20),
              water = cost("heatExchangerWater", 1);
          if (lava > 0)
            addCooldownRecipe(
                net.minecraft.world.level.material.Fluids.LAVA,
                FluidDefinitions.PAHOEHOE_LAVA.still,
                lava);
          if (coolant > 0)
            addBiDiRecipe(
                FluidDefinitions.HOT_COOLANT.still, FluidDefinitions.COOLANT.still, coolant);
          if (water > 0)
            addHeatupRecipe(
                FluidDefinitions.HOT_WATER.still,
                net.minecraft.world.level.material.Fluids.WATER,
                water);
        });
  }

  private static int cost(String key, int base) {
    return (int)
        Math.clamp(
            Math.round(
                base
                    * NativeConfigValues.getDouble(
                        ProjectConfiguration.get(), "balance/energy/fluidconversion/" + key)),
            0,
            Integer.MAX_VALUE);
  }

  public static void addBiDiRecipe(Fluid hot, Fluid cold, int heat) {
    addHeatupRecipe(hot, cold, heat);
    addCooldownRecipe(hot, cold, heat);
  }

  public static void addHeatupRecipe(Fluid hot, Fluid cold, int heat) {
    Recipes.liquidHeatupManager.addFluid(cold, hot, heat);
  }

  public static void addCooldownRecipe(Fluid hot, Fluid cold, int heat) {
    Recipes.liquidCooldownManager.addFluid(hot, cold, heat);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean changed = hotfluidinputSlot.processIntoTank(inputTank, hotoutputSlot);
    changed |= coolfluidinputSlot.processFromTank(outputTank, cooloutputSlot);
    setActive(HeatBuffer > 0);
    upgradeSlot.tick();
    if (changed) setChanged();
  }

  @Override
  public int getMaxHeatEmittedPerTick() {
    int count = 0;
    for (int slot = 0; slot < heatexchangerslots.size(); slot++)
      if (heatexchangerslots.get(slot).is(ItemDefinitions.HEAT_CONDUCTOR)) count++;
    return count * 10;
  }

  @Override
  protected int fillHeatBuffer(int maximum) {
    if (maximum <= 0) return 0;
    var input = inputTank.getFluidStack();
    if (prepaidHeat < maximum && input != null && !input.isEmpty()) {
      var exchange = Recipes.liquidCooldownManager.getHeatExchangeProperty(input.getFluid());
      if (exchange != null && exchange.outputFluid != null && exchange.huPerMB > 0) {
        long missing = maximum - prepaidHeat;
        int needed = (int) ((missing + exchange.huPerMB - 1) / exchange.huPerMB);
        int available =
            Math.min(
                inputTank.getFluidAmount(),
                Math.max(0, outputTank.getCapacity() - outputTank.getFluidAmount()));
        int amount = Math.min(needed, available);
        var output = Ic2FluidStack.create(exchange.outputFluid, amount);
        if (amount > 0 && outputTank.fillMbUnchecked(output, true) == amount) {
          inputTank.drainMbUnchecked(amount, false);
          outputTank.fillMbUnchecked(output, false);
          prepaidHeat += (long) amount * exchange.huPerMB;
        }
      }
    }
    int emitted = (int) Math.min(maximum, prepaidHeat);
    prepaidHeat -= emitted;
    return emitted;
  }

  public int gaugeLiquidScaled(int width, int selection) {
    var tank = selection == 0 ? inputTank : selection == 1 ? outputTank : null;
    return tank == null
        ? 0
        : (int)
            ((long) Math.max(0, width) * tank.getFluidAmount() / Math.max(1, tank.getCapacity()));
  }

  public Ic2FluidTank getInputTank() {
    return inputTank;
  }

  public Ic2FluidTank getOutputTank() {
    return outputTank;
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    prepaidHeat = Math.clamp(data.getLongOr("prepaidHeat", 0), 0, Integer.MAX_VALUE);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putLong("prepaidHeat", prepaidHeat);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing,
        UpgradableProperty.FluidConsuming,
        UpgradableProperty.FluidProducing);
  }

  @Override
  public double getEnergy() {
    return 40;
  }

  @Override
  public boolean useEnergy(double amount) {
    return Double.isFinite(amount) && amount >= 0;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return new HeatExchangeMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new HeatExchangeMenu(id, inventory, this);
  }
}
