package ic2ce.neoforge.kinetic;

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

/** Actual kinetic buffers, electric motor requirements, turbine extraction and cooling budgets. */
public final class KineticContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native kinetic: " + label);
  }

  public static Result run(ServerLevel world) {
    return new KineticContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var manual =
        new ManualCrank(
            new BlockPos(8280, 100, 8280),
            BlockDefinitions.MANUAL_KINETIC_GENERATOR.defaultBlockState());
    manual.setLevel(world);
    manual.currentKU = 100;
    check(
        manual.drawKineticEnergy(manual.getFacing(), -1, false) == 0 && manual.currentKU == 100,
        "negative manual extraction cannot generate KU");
    check(
        manual.drawKineticEnergy(manual.getFacing(), 40, true) == 40 && manual.currentKU == 100,
        "manual simulation preserves the kinetic balance");
    manual.drawKineticEnergy(manual.getFacing(), 40, false);
    var saved = new CompoundTag();
    manual.saveAdditional(saved);
    manual.currentKU = 0;
    manual.load(saved);
    check(manual.currentKU == 60, "hand-created kinetic energy survives save/load");
    var motor =
        new ElectricMotor(
            new BlockPos(8281, 100, 8281),
            BlockDefinitions.ELECTRIC_KINETIC_GENERATOR.defaultBlockState());
    motor.setLevel(world);
    motor.energy.forceAddEnergy(100);
    motor.updateEntityServer();
    check(
        motor.ku == 0 && motor.energy.getEnergy() == 100,
        "an absent electric motor cannot silently spend stored EU");
    motor.slotMotor.put(new ItemStack(Items.COAL));
    check(motor.getMaxKU() == 0, "a forced non-motor item cannot create shaft bandwidth");
    motor.slotMotor.put(new ItemStack(ItemDefinitions.ELECTRIC_MOTOR));
    motor.updateEntityServer();
    check(
        motor.ku == 400 && motor.energy.getEnergy() == 0 && motor.getMaxKU() == 100,
        "one motor converts its real EU bill and sets its connector bandwidth");
    check(
        !motor.energy.getSinkDirs().contains(motor.getFacing()),
        "electric shaft face is never also an EU input");
    check(
        motor.drawKineticEnergy(motor.getFacing(), -40, false) == 0 && motor.ku == 400,
        "negative motor draw cannot create stored KU");
    check(
        motor.drawKineticEnergy(motor.getFacing(), 1000, false) == 100 && motor.ku == 300,
        "motor withdrawal is capped by both stored energy and installed bandwidth");
    saved = new CompoundTag();
    motor.saveAdditional(saved);
    motor.ku = 0;
    motor.load(saved);
    check(motor.ku == 300, "converted motor KU survives persistence");
    var cogeneration =
        new HeatDrivenCogenerator(
            new BlockPos(8282, 100, 8282),
            BlockDefinitions.STIRLING_KINETIC_GENERATOR.defaultBlockState());
    cogeneration.setLevel(world);
    saved = new CompoundTag();
    cogeneration.saveAdditional(saved);
    saved.putInt("heatbuffer", 1000);
    saved.putInt("kUBuffer", 1990);
    cogeneration.load(saved);
    cogeneration.inputTank.fillMbUnchecked(Ic2FluidStack.create(Fluids.WATER, 1000), false);
    cogeneration.updateEntityServer();
    check(
        cogeneration.drawKineticEnergy(cogeneration.getFacing(), Integer.MAX_VALUE, true) == 1990
            && cogeneration.outputTank.isEmpty(),
        "ten free KU cannot accept a twelve-KU cogeneration batch");
    check(
        cogeneration.drawKineticEnergy(cogeneration.getFacing(), -10, false) == 0,
        "negative cogeneration request never increases the buffer");
    var position = new BlockPos(8283, 100, 8283);
    world.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position.east(), BlockDefinitions.CONDENSER.defaultBlockState());
    var steam =
        new SteamTurbine(position, BlockDefinitions.STEAM_KINETIC_GENERATOR.defaultBlockState());
    steam.setLevel(world);
    steam.turbineSlot.put(new ItemStack(Items.COAL));
    steam.steamTank.fillMbUnchecked(
        Ic2FluidStack.create(FluidDefinitions.STEAM.still, 1000), false);
    steam.updateEntityServer();
    check(
        steam.steamTank.getFluidAmount() == 1000 && steam.getKUoutput() == 0,
        "invalid forced turbine cannot consume steam or create KU");
    steam.turbineSlot.put(new ItemStack(ItemDefinitions.STEAM_TURBINE));
    steam.updateEntityServer();
    int output = steam.getKUoutput();
    check(
        output == 2000
            && steam.steamTank.isEmpty()
            && steam.distilledWaterTank.getFluidAmount() == 1,
        "real steam batch creates its exact shaft budget and condensed water");
    check(
        steam.drawKineticEnergy(steam.getFacing(), 1, true) == 1,
        "turbine simulation honors the requested quantity");
    check(
        steam.drawKineticEnergy(steam.getFacing(), 1, false) == 1
            && steam.drawKineticEnergy(steam.getFacing(), Integer.MAX_VALUE, false) == output - 1
            && steam.drawKineticEnergy(steam.getFacing(), 1, false) == 0,
        "multiple draws cannot duplicate the same turbine tick output");
    steam.distilledWaterTank.fillMbUnchecked(
        Ic2FluidStack.create(FluidDefinitions.DISTILLED_WATER.still, 999), false);
    steam.steamTank.fillMbUnchecked(
        Ic2FluidStack.create(FluidDefinitions.STEAM.still, 1000), false);
    steam.updateEntityServer();
    check(
        steam.steamTank.getFluidAmount() == 1000 && steam.isTurbineBlockedByWater(),
        "a water-blocked turbine preserves its unprocessed steam");
    world.setBlockAndUpdate(
        position.east(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
    return new Result(checks, List.copyOf(failures));
  }
}
