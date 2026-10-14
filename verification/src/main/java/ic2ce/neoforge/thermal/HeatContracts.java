package ic2ce.neoforge.thermal;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/**
 * Native heat authority, prepaid batch conservation, fluid-capacity negotiation and persistence.
 */
public final class HeatContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native thermal: " + label);
  }

  public static Result run(ServerLevel world) {
    return new HeatContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var electric =
        new ElectricHeater(
            new BlockPos(8260, 100, 8260),
            BlockDefinitions.ELECTRIC_HEAT_GENERATOR.defaultBlockState());
    electric.setLevel(world);
    electric.coilSlot.put(new ItemStack(Items.COAL));
    check(electric.getMaxHeatEmittedPerTick() == 0, "forced invalid coil cannot create bandwidth");
    electric.coilSlot.put(new ItemStack(ItemDefinitions.COIL));
    electric.energy.forceAddEnergy(100);
    electric.updateEntityServer();
    check(
        electric.getHeatBuffer() == 10 && electric.energy.getEnergy() == 90,
        "one coil reserves the exact ten-EU heat bill");
    int before = electric.getHeatBuffer();
    check(
        electric.drawHeat(electric.getFacing(), -5, false) == 0
            && electric.getHeatBuffer() == before,
        "negative heat request cannot reverse a transfer");
    check(
        electric.drawHeat(electric.getFacing().getOpposite(), 10, false) == 0
            && electric.getHeatBuffer() == before,
        "wrong connector side cannot withdraw heat");
    check(
        electric.drawHeat(electric.getFacing(), 3, true) == 3 && electric.getHeatBuffer() == before,
        "heat simulation preserves stored state");
    check(
        electric.drawHeat(electric.getFacing(), 3, false) == 3 && electric.getHeatBuffer() == 7,
        "committed heat extraction conserves quantity");
    var fuel =
        new FluidBurner(
            new BlockPos(8261, 100, 8261),
            BlockDefinitions.FLUID_HEAT_GENERATOR.defaultBlockState());
    fuel.setLevel(world);
    fuel.fluidTank.fillMbUnchecked(Ic2FluidStack.create(FluidDefinitions.BIOGAS.still, 9), false);
    fuel.updateEntityServer();
    check(
        fuel.getHeatBuffer() == 0 && fuel.fluidTank.getFluidAmount() == 9,
        "an incomplete fluid batch cannot provide nineteen free heating ticks");
    fuel.fluidTank.fillMbUnchecked(Ic2FluidStack.create(FluidDefinitions.BIOGAS.still, 1), false);
    fuel.updateEntityServer();
    check(
        fuel.getHeatBuffer() == 32 && fuel.fluidTank.isEmpty(),
        "one real ten-millibucket batch prepays heat before output");
    fuel.drawHeat(fuel.getFacing(), 1, false);
    fuel.updateEntityServer();
    check(fuel.getHeatBuffer() == 32, "one-unit refill never overflows the advertised heat buffer");
    var saved = new CompoundTag();
    fuel.saveAdditional(saved);
    var restored =
        new FluidBurner(
            new BlockPos(8262, 100, 8262),
            BlockDefinitions.FLUID_HEAT_GENERATOR.defaultBlockState());
    restored.setLevel(world);
    restored.load(saved);
    int total = 1;
    for (int tick = 0; tick < 25; tick++) {
      total += restored.drawHeat(restored.getFacing(), Integer.MAX_VALUE, false);
      restored.updateEntityServer();
    }
    check(
        total == 640 && restored.getHeatBuffer() == 0 && restored.fluidTank.isEmpty(),
        "prepaid heat survives save/load and releases exactly the batch's 640 HU");
    var exchange =
        new FluidHeatExchanger(
            new BlockPos(8263, 100, 8263),
            BlockDefinitions.LIQUID_HEAT_EXCHANGER.defaultBlockState());
    exchange.setLevel(world);
    exchange.heatexchangerslots.put(new ItemStack(ItemDefinitions.HEAT_CONDUCTOR));
    exchange.inputTank.fillMbUnchecked(Ic2FluidStack.create(Fluids.LAVA, 100), false);
    exchange.outputTank.fillMbUnchecked(
        Ic2FluidStack.create(FluidDefinitions.PAHOEHOE_LAVA.still, 1999), false);
    exchange.updateEntityServer();
    check(
        exchange.inputTank.getFluidAmount() == 99
            && exchange.outputTank.getFluidAmount() == 2000
            && exchange.getHeatBuffer() == 10,
        "one free output millibucket never drains twenty input millibuckets");
    exchange.drawHeat(exchange.getFacing(), 10, false);
    exchange.updateEntityServer();
    check(
        exchange.getHeatBuffer() == 10
            && exchange.inputTank.getFluidAmount() == 99
            && exchange.outputTank.getFluidAmount() == 2000,
        "second half of indivisible fluid heat is released without extra conversion or output"
            + " overflow");
    var solid =
        new CombustionHeater(
            new BlockPos(8264, 100, 8264),
            BlockDefinitions.SOLID_HEAT_GENERATOR.defaultBlockState());
    solid.setLevel(world);
    solid.fuelSlot.put(new ItemStack(Items.COAL));
    solid.updateEntityServer();
    saved = new CompoundTag();
    solid.saveAdditional(saved);
    var reloaded =
        new CombustionHeater(
            new BlockPos(8265, 100, 8265),
            BlockDefinitions.SOLID_HEAT_GENERATOR.defaultBlockState());
    reloaded.setLevel(world);
    reloaded.load(saved);
    reloaded.updateEntityServer();
    check(
        reloaded.getHeatBuffer() == 20 && reloaded.fuel == 398,
        "combustion reservoir and remaining burn duration survive persistence");
    return new Result(checks, List.copyOf(failures));
  }
}
