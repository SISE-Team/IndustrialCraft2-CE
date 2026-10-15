package ic2ce.neoforge.processing.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.fluid.LiquidUtil;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.engine.machine.BoilerThermodynamics;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.explosion.NativeBlast;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.processing.menu.BoilerMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.thermal.HeatSupply;
import ic2ce.neoforge.world.BiomeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Metered pressure boiler; blocked steam remains paid fluid rather than refunded feed water. */
@NotClassic
public class PressureBoiler extends InventoryBlockEntity
    implements IHasGui, ScreenValues, INetworkClientTileEntityEventListener {
  public final Ic2FluidTank waterTank;
  protected final FluidPorts fluids;
  private final Ic2FluidTank steamBuffer;
  private int heatInput, inputMB, calcification, outputMB, pressure;
  private float systemHeat;
  private OutputType outputFluid = OutputType.NONE;

  private enum OutputType {
    NONE(""),
    WATER("ic2.SteamGenerator.output.water"),
    DISTILLEDWATER("ic2.SteamGenerator.output.destiwater"),
    STEAM("ic2.SteamGenerator.output.steam"),
    SUPERHEATEDSTEAM("ic2.SteamGenerator.output.hotsteam");
    final String key;

    OutputType(String key) {
      this.key = key;
    }
  }

  public PressureBoiler(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.STEAM_GENERATOR, position, state);
    fluids = addComponent(new FluidPorts(this));
    waterTank =
        fluids.addTankInsert(
            "waterTank",
            10000,
            FluidPorts.fluidPredicate(
                net.minecraft.world.level.material.Fluids.WATER,
                FluidDefinitions.DISTILLED_WATER.still));
    steamBuffer = fluids.addTank("steamBuffer", 100000, InventorySlot.Access.NONE);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    inputMB = Math.clamp(data.getIntOr("inputmb", 0), 0, 1000);
    pressure = Math.clamp(data.getIntOr("pressurevalve", 0), 0, 300);
    float temperature = data.getFloatOr("systemheat", 0);
    systemHeat = Float.isFinite(temperature) ? Math.max(0, temperature) : 0;
    calcification = Math.clamp(data.getIntOr("calcification", 0), 0, 100000);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("inputmb", inputMB);
    data.putInt("pressurevalve", pressure);
    data.putFloat("systemheat", systemHeat);
    data.putInt("calcification", calcification);
  }

  private void distributeSteam() {
    var contents = steamBuffer.getFluidStack();
    if (contents == null || contents.isEmpty()) return;
    int transferred =
        Math.clamp(LiquidUtil.distribute(this, contents.copy(), false), 0, contents.getAmountMb());
    if (transferred > 0) steamBuffer.drainMbUnchecked(transferred, false);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    outputMB = 0;
    outputFluid = OutputType.NONE;
    heatInput = 0;
    distributeSteam();
    float ambient = (float) BiomeUtil.getBiomeTemperature(getLevel(), worldPosition);
    systemHeat = Math.max(Float.isFinite(ambient) ? ambient : 0, systemHeat);
    if (!isCalcified()) heatInput = HeatSupply.collect(getLevel(), worldPosition, 1200);
    if (heatInput > 0) {
      int feed = Math.min(inputMB, waterTank.getFluidAmount());
      var step = BoilerThermodynamics.advance(systemHeat, heatInput, pressure, feed);
      if (step.boiledMb() > 0) {
        var product =
            step.temperature() >= 373.9999
                ? FluidDefinitions.SUPERHEATED_STEAM.still
                : FluidDefinitions.STEAM.still;
        int capacity =
            steamBuffer.fillMbUnchecked(Ic2FluidStack.create(product, step.boiledMb() * 100), true)
                / 100;
        if (capacity < step.boiledMb())
          step =
              BoilerThermodynamics.advance(
                  systemHeat, heatInput, pressure, Math.min(feed, capacity));
        if (step.boiledMb() > 0) {
          product =
              step.temperature() >= 373.9999
                  ? FluidDefinitions.SUPERHEATED_STEAM.still
                  : FluidDefinitions.STEAM.still;
          var steam = Ic2FluidStack.create(product, step.boiledMb() * 100);
          if (steamBuffer.fillMbUnchecked(steam, true) == steam.getAmountMb()) {
            steamBuffer.fillMbUnchecked(steam, false);
            outputMB = steam.getAmountMb();
            outputFluid =
                product == FluidDefinitions.SUPERHEATED_STEAM.still
                    ? OutputType.SUPERHEATEDSTEAM
                    : OutputType.STEAM;
          } else step = BoilerThermodynamics.advance(systemHeat, heatInput, pressure, 0);
        }
      }
      systemHeat = (float) step.temperature();
      if (step.consumedMb() > 0) {
        boolean distilled = waterTank.hasExactFluid(FluidDefinitions.DISTILLED_WATER.still);
        waterTank.drainMbUnchecked(step.consumedMb(), false);
        if (!distilled)
          calcification = (int) Math.min(100000, (long) calcification + step.consumedMb());
      }
      if (step.bypassWater() && feed > 0) {
        var water = waterTank.getFluidStack().copyWithAmountMb(feed);
        outputMB = Math.clamp(LiquidUtil.distribute(this, water, false), 0, feed);
        outputFluid =
            water.hasExactFluid(FluidDefinitions.DISTILLED_WATER.still)
                ? OutputType.DISTILLEDWATER
                : OutputType.WATER;
        waterTank.drainMbUnchecked(outputMB, false);
      }
      distributeSteam();
      if (systemHeat > 500) {
        var world = getLevel();
        world.removeBlock(worldPosition, false);
        new NativeBlast(world, null, worldPosition, 10, 0.01F, NativeBlast.Type.Heat).doExplosion();
        return;
      }
      setChanged();
    } else systemHeat = Math.max(ambient, systemHeat - 0.01F);
    setActive(heatInput > 0 && !isCalcified());
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event >= -2000 && event <= 2000)
      inputMB = (int) Math.clamp((long) inputMB + event, 0, 1000);
    else {
      long change = event > 2000 ? (long) event - 2000 : (long) event + 2000;
      pressure = (int) Math.clamp(pressure + change, 0, 300);
    }
    setChanged();
  }

  public int gaugeLiquidScaled(int width, int tank) {
    return tank == 0
        ? (int) ((long) Math.max(0, width) * waterTank.getFluidAmount() / waterTank.getCapacity())
        : 0;
  }

  public int getOutputMB() {
    return outputMB;
  }

  public int getInputMB() {
    return inputMB;
  }

  public int getHeatInput() {
    return heatInput;
  }

  public int getPressure() {
    return pressure;
  }

  public float getSystemHeat() {
    return Math.round(systemHeat * 10) / 10.0F;
  }

  public float getCalcification() {
    return Math.round(calcification / 10.0F) / 100.0F;
  }

  public boolean isCalcified() {
    return calcification >= 100000;
  }

  public String getOutputFluidName() {
    return outputFluid.key;
  }

  @Override
  public double getGuiValue(String key) {
    return switch (key) {
      case "heat" -> systemHeat / 500.0;
      case "calcification" -> calcification / 100000.0;
      default -> throw new IllegalArgumentException("Unknown boiler gauge: " + key);
    };
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new BoilerMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new BoilerMenu(syncId, inventory, this);
  }
}
