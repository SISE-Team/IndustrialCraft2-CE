package ic2ce.neoforge.processing.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.agriculture.AgricultureItems;
import ic2ce.neoforge.processing.ProcessingBlocks;
import ic2ce.neoforge.reactor.ReactorItems;
import ic2ce.neoforge.registry.FluidDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Real native phase conversion, outlet obstruction, payment and corrupt-control boundaries. */
public final class FluidMachineContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native fluid machine: " + label);
  }

  public static Result run(ServerLevel world) {
    return new FluidMachineContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8270, 100, 8270);
    var condenser = new SteamCondenser(position, ProcessingBlocks.CONDENSER.defaultBlockState());
    condenser.setLevel(world);
    condenser
        .getInputTank()
        .fillMbUnchecked(Ic2FluidStack.create(FluidDefinitions.STEAM.still, 1), false);
    condenser.progress = 9999;
    for (int slot = 0; slot < 4; slot++)
      condenser.ventSlots.put(slot, new ItemStack(ReactorItems.HEAT_VENT));
    condenser.updateEntityServer();
    check(
        condenser.getOutputTank().getFluidAmount() == 100
            && condenser.getInputTank().isEmpty()
            && condenser.progress == 0,
        "one final steam millibucket completes a passive batch even when installed fans have no"
            + " EU");
    condenser
        .getInputTank()
        .fillMbUnchecked(
            Ic2FluidStack.create(FluidDefinitions.SUPERHEATED_STEAM.still, 1000), false);
    condenser.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(10);
    condenser.updateEntityServer();
    check(
        condenser.getInputTank().getFluidAmount() == 500
            && condenser.progress == 500
            && condenser.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getEnergy()
                == 2,
        "four useful fans consume exactly eight EU and 500 steam millibuckets");
    condenser
        .getOutputTank()
        .fillMbUnchecked(Ic2FluidStack.create(FluidDefinitions.DISTILLED_WATER.still, 900), false);
    condenser.updateEntityServer();
    check(
        condenser.getInputTank().getFluidAmount() == 500
            && condenser.progress == 500
            && condenser.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getEnergy()
                == 2,
        "a full water outlet consumes neither steam nor fan energy");
    var fermenter = new BiomassFermenter(position, ProcessingBlocks.FERMENTER.defaultBlockState());
    fermenter.setLevel(world);
    var saved = new CompoundTag();
    saved.putInt("progress", 100000);
    fermenter.load(saved);
    fermenter.fertiliserSlot.put(new ItemStack(AgricultureItems.FERTILIZER, 64));
    int credit = fermenter.progress;
    fermenter.updateEntityServer();
    check(
        fermenter.progress == credit && fermenter.fertiliserSlot.get().getCount() == 64,
        "a full fertilizer slot cannot erase saved production credit");
    var boiler = new PressureBoiler(position, ProcessingBlocks.STEAM_GENERATOR.defaultBlockState());
    boiler.setLevel(world);
    saved = new CompoundTag();
    saved.putInt("inputmb", Integer.MAX_VALUE);
    saved.putInt("pressurevalve", Integer.MIN_VALUE);
    saved.putFloat("systemheat", Float.NaN);
    saved.putInt("calcification", -1);
    boiler.load(saved);
    check(
        boiler.getInputMB() == 1000
            && boiler.getPressure() == 0
            && boiler.getSystemHeat() == 0
            && boiler.getCalcification() == 0,
        "boiler controls and nonfinite saved heat are sanitized");
    boiler.onNetworkEvent(null, Integer.MAX_VALUE);
    boiler.onNetworkEvent(null, Integer.MIN_VALUE);
    check(
        boiler.getPressure() == 0 && boiler.getInputMB() == 1000,
        "extreme signed control events cannot overflow boiler limits");
    var distiller =
        new SolarDistiller(position, ProcessingBlocks.SOLAR_DISTILLER.defaultBlockState());
    distiller.setLevel(world);
    distiller.inputTank.fillMbUnchecked(
        Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1), false);
    distiller.outputTank.fillMbUnchecked(
        Ic2FluidStack.create(FluidDefinitions.HOT_COOLANT.still, 1), false);
    distiller.updateSunVisibility();
    check(
        !distiller.canWork() && distiller.inputTank.getFluidAmount() == 1,
        "an incompatible distiller outlet never accepts a lossy conversion");
    return new Result(checks, List.copyOf(failures));
  }
}
