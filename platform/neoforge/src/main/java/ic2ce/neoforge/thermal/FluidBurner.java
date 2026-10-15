package ic2ce.neoforge.thermal;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.Recipes;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.FluidContainerInventory;
import ic2ce.neoforge.inventory.FluidRecipeInput;
import ic2ce.neoforge.inventory.OutputInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.thermal.menu.FluidHeaterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/**
 * A real fluid batch prepays twenty full-output ticks; partial buffer refills retain unused heat.
 */
@NotClassic
public class FluidBurner extends BufferedHeatSource implements IHasGui {
  public final FluidContainerInventory fluidSlot;
  public final OutputInventory outputSlot;
  @GuiSynced protected final Ic2FluidTank fluidTank;
  protected int burnAmount, production;
  private long prepaidHeat;
  private boolean producing;
  protected final FluidPorts fluids = addComponent(new FluidPorts(this));

  public FluidBurner(BlockPos position, BlockState block) {
    super(ThermalBlockEntities.FLUID_HEAT_GENERATOR, position, block);
    fluidSlot = new FluidRecipeInput(this, "fluidSlot", 1, Recipes.fluidHeatGenerator);
    outputSlot = new OutputInventory(this, "output", 1);
    fluidTank =
        fluids.addTankInsert(
            "fluidTank", 10000, FluidPorts.fluidPredicate(Recipes.fluidHeatGenerator));
  }

  public static void init() {
    Recipes.fluidHeatGenerator = new FluidHeatManager();
    double factor =
        NativeConfigValues.getDouble(
            ProjectConfiguration.get(), "balance/energy/heatgenerator/semiFluidBiogas");
    if (Double.isFinite(factor) && factor > 0)
      addFuel(
          FluidDefinitions.BIOGAS.still,
          10,
          (int) Math.clamp(Math.round(32 * factor), 1, Integer.MAX_VALUE));
  }

  public static void addFuel(Fluid fluid, int amount, int heat) {
    Recipes.fluidHeatGenerator.addFluid(fluid, amount, heat);
  }

  @Override
  protected void updateEntityServer() {
    producing = false;
    super.updateEntityServer();
    if (needsFluid() && gainFuel()) setChanged();
    setActive(producing);
  }

  @Override
  protected int fillHeatBuffer(int maximum) {
    if (maximum <= 0) return 0;
    if (prepaidHeat == 0) {
      calcBurnAmount();
      calcHeatProduction();
      if (burnAmount <= 0 || production <= 0 || getTankAmount() < burnAmount) return 0;
      fluidTank.drainMbUnchecked(burnAmount, false);
      prepaidHeat = (long) production * 20;
    }
    int supplied = (int) Math.min(maximum, prepaidHeat);
    prepaidHeat -= supplied;
    producing = supplied > 0;
    return supplied;
  }

  public boolean isConverting() {
    return prepaidHeat > 0 || getTankAmount() >= burnAmount && burnAmount > 0 && production > 0;
  }

  protected int calcHeatProduction() {
    if (prepaidHeat > 0 || HeatBuffer > 0)
      return production = Math.max(1, production > 0 ? production : HeatBuffer);
    var fluid = getFluidfromTank();
    var burn = fluid == null ? null : Recipes.fluidHeatGenerator.getBurnProperty(fluid);
    return production =
        burn == null ? prepaidHeat > 0 ? Math.max(1, production) : 0 : Math.max(0, burn.heat);
  }

  protected void calcBurnAmount() {
    var fluid = getFluidfromTank();
    var burn = fluid == null ? null : Recipes.fluidHeatGenerator.getBurnProperty(fluid);
    burnAmount = burn == null ? 0 : Math.max(0, burn.amount);
  }

  @Override
  public int getMaxHeatEmittedPerTick() {
    return calcHeatProduction();
  }

  public Ic2FluidTank getFluidTank() {
    return fluidTank;
  }

  public Ic2FluidStack getFluidStackfromTank() {
    return fluidTank.getFluidStack();
  }

  public Fluid getFluidfromTank() {
    var stored = fluidTank.getFluidStack();
    return stored == null || stored.isEmpty() ? null : stored.getFluid();
  }

  public int getTankAmount() {
    return fluidTank.getFluidAmount();
  }

  public int gaugeLiquidScaled(int width) {
    return (int)
        ((long) Math.max(0, width) * getTankAmount() / Math.max(1, fluidTank.getCapacity()));
  }

  public boolean needsFluid() {
    return getTankAmount() < fluidTank.getCapacity();
  }

  protected boolean gainFuel() {
    return fluidSlot.processIntoTank(fluidTank, outputSlot);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    if (data.contains("fluidTank")) fluidTank.fromNbt(data.getCompoundOrEmpty("fluidTank"));
    prepaidHeat = Math.clamp(data.getLongOr("prepaidHeat", 0), 0, (long) Integer.MAX_VALUE * 20);
    production = Math.max(0, data.getIntOr("heatProduction", 0));
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.put("fluidTank", fluidTank.toNbt(new CompoundTag()));
    data.putLong("prepaidHeat", prepaidHeat);
    data.putInt("heatProduction", production);
  }

  @Override
  public OwnedMenu<FluidBurner> createServerScreenHandler(int id, Player player) {
    return new FluidHeaterMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new FluidHeaterMenu(id, inventory, this);
  }
}
