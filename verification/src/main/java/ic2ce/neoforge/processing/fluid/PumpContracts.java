package ic2ce.neoforge.processing.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.fluid.PumpUtil;
import ic2ce.neoforge.mining.PipeMiner;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluids;

/**
 * Native world removal and exact tick bills, including obstructed paid progress and incompatible
 * fluids.
 */
public final class PumpContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native pump: " + label);
  }

  public static Result run(ServerLevel world) {
    return new PumpContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8420, 100, 8420);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, BlockDefinitions.PUMP.defaultBlockState());
    var pump = (FluidCollector) world.getBlockEntity(position);
    pump.setUpgradestat();
    var source = position.relative(pump.getFacing());
    world.setBlockAndUpdate(source, Blocks.WATER.defaultBlockState());
    pump.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(20);
    for (int tick = 0; tick < 20; tick++) pump.updateEntityServer();
    check(
        pump.fluidTank.getFluidAmount() == 1000
            && pump.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getEnergy() == 0
            && world.getBlockState(source).isAir(),
        "exactly twenty EU finishes twenty paid native ticks and removes one water source");
    pump.fluidTank.drainMbUnchecked(8000, false);
    world.setBlockAndUpdate(source, Blocks.WATER.defaultBlockState());
    pump.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(20);
    for (int tick = 0; tick < 10; tick++) pump.updateEntityServer();
    pump.fluidTank.fillMbUnchecked(Ic2FluidStack.create(Fluids.WATER, 8000), false);
    for (int tick = 0; tick < 10; tick++) pump.updateEntityServer();
    check(
        pump.progress == 10
            && pump.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getEnergy() == 10
            && world.getBlockState(source).is(Blocks.WATER),
        "full outlet preserves paid partial progress and leaves source and EU unchanged");
    pump.fluidTank.drainMbUnchecked(8000, false);
    for (int tick = 0; tick < 10; tick++) pump.updateEntityServer();
    check(
        pump.fluidTank.getFluidAmount() == 1000
            && pump.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getEnergy() == 0
            && pump.progress == 0,
        "unblocking completes the original paid cycle without buying a second full bill");
    world.setBlockAndUpdate(source, Blocks.LAVA.defaultBlockState());
    check(
        pump.pump(source, false, null) == null
            && world.getBlockState(source).is(Blocks.LAVA)
            && pump.fluidTank.getFluidAmount() == 1000,
        "an incompatible water outlet prevents actual lava-source destruction");
    pump.fluidTank.drainMbUnchecked(8000, false);
    world.setBlockAndUpdate(
        source, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 1));
    world.setBlockAndUpdate(source.east(), Blocks.STONE.defaultBlockState());
    world.setBlockAndUpdate(source.east(2), Blocks.WATER.defaultBlockState());
    check(
        PumpUtil.searchFluidSource(world, source) == null,
        "nearby source behind a solid barrier is outside the connected intake region");
    world.setBlockAndUpdate(
        source.east(), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 2));
    var found = PumpUtil.searchFluidSource(world, source);
    check(source.east(2).equals(found), "connected flowing cells reach the real same-fluid source");
    world.setBlockAndUpdate(source.east(2), Blocks.LAVA.defaultBlockState());
    check(
        PumpUtil.searchFluidSource(world, source) == null,
        "fluid search never crosses from flowing water into lava");
    world.setBlockAndUpdate(source, Blocks.WATER.defaultBlockState());
    var linked = new PipeMiner(position.east(), BlockDefinitions.MINER.defaultBlockState());
    linked.setLevel(world);
    check(
        !linked.canProvideLiquid
            && pump.pump(source, true, linked) != null
            && world.getBlockState(source).is(Blocks.WATER),
        "miner can preview its source before publishing an active intake request");
    for (int x = 0; x <= 2; x++) world.removeBlock(source.east(x), false);
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
