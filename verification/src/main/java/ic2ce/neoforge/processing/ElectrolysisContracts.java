package ic2ce.neoforge.processing;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.IElectrolyzerRecipeManager.*;
import ic2ce.neoforge.processing.fluid.FluidElectrolyzer;
import ic2ce.neoforge.recipe.processing.ElectrolysisRecipes;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.storage.StationaryFluidStore;
import ic2ce.neoforge.storage.StorageBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/** Actual two-tank electrolysis must finish using exactly its final paid tick. */
public final class ElectrolysisContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native electrolysis: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ElectrolysisContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8290, 100, 8290);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, ProcessingBlocks.ELECTROLYZER.defaultBlockState());
    world.setBlockAndUpdate(position.below(), StorageBlocks.TANK.defaultBlockState());
    world.setBlockAndUpdate(position.above(), StorageBlocks.TANK.defaultBlockState());
    var machine = (FluidElectrolyzer) world.getBlockEntity(position);
    var hydrogen = ((StationaryFluidStore) world.getBlockEntity(position.below())).getFluidTank();
    var oxygen = ((StationaryFluidStore) world.getBlockEntity(position.above())).getFluidTank();
    machine
        .getInput()
        .fillMbUnchecked(
            Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 40), false);
    machine.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(32 * 200);
    oxygen.fillMbUnchecked(Ic2FluidStack.create(FluidDefinitions.HOT_COOLANT.still, 1), false);
    MachineFixtureAccess.tick(machine);
    check(
        machine.getEnergy() == 6400
            && machine.getInput().getFluidAmount() == 40
            && hydrogen.isEmpty()
            && MachineFixtureAccess.number(machine, "progress").intValue() == 0,
        "one blocked product prevents every input, EU and output mutation");
    oxygen.drainMbUnchecked(Integer.MAX_VALUE, false);
    for (int tick = 0; tick < 200; tick++) MachineFixtureAccess.tick(machine);
    check(
        machine.getEnergy() == 0
            && machine.getInput().isEmpty()
            && hydrogen.getFluidAmount() == 26
            && oxygen.getFluidAmount() == 13,
        "exact 6400 EU completes all 200 ticks and produces both products after the last 32 EU is"
            + " spent");
    check(
        hydrogen.hasExactFluid(FluidDefinitions.HYDROGEN.still)
            && oxygen.hasExactFluid(FluidDefinitions.OXYGEN.still),
        "directional outlets receive the correct actual native fluids");
    var targets =
        new ElectrolyzerOutput[] {
          new ElectrolyzerOutput(FluidDefinitions.HYDROGEN.still, 26, Direction.DOWN)
        };
    var registry = new ElectrolysisRecipes();
    registry.addRecipe(net.minecraft.world.level.material.Fluids.WATER, 40, 32, targets);
    targets[0] = new ElectrolyzerOutput(FluidDefinitions.OXYGEN.still, 13, Direction.UP);
    var immutable =
        registry.getElectrolysisInformation(net.minecraft.world.level.material.Fluids.WATER);
    check(
        immutable.outputs().getFirst().fluid() == FluidDefinitions.HYDROGEN.still,
        "caller array edits cannot replace registered products");
    var copy = registry.getOutput(net.minecraft.world.level.material.Fluids.WATER);
    copy[0] = targets[0];
    check(
        registry.getOutput(net.minecraft.world.level.material.Fluids.WATER)[0].fluid()
            == FluidDefinitions.HYDROGEN.still,
        "returned array edits cannot modify recipe records");
    boolean rejected = false;
    try {
      immutable.outputs().clear();
    } catch (UnsupportedOperationException expected) {
      rejected = true;
    }
    check(rejected, "recipe target lists are immutable");
    rejected = false;
    try {
      new ElectrolyzerRecipe(40, -1, 200, targets);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(rejected, "negative recipe power cannot publish a free or energy-producing operation");
    world.removeBlock(position.above(), false);
    world.removeBlock(position.below(), false);
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
