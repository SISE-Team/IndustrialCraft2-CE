package ic2ce.neoforge.cable;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.IColoredEnergyTile;
import ic2ce.api.energy.tile.IEnergyAcceptor;
import ic2ce.api.energy.tile.IEnergyEmitter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

/** Native water/foam retention, live colors, ownership-safe removal and splitter restoration. */
public final class CableContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native cable: " + label);
  }

  public static Result run(ServerLevel world) {
    return new CableContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8380, 100, 8380);
    world.getChunkAt(position);
    var bare = (ConductorBlock) CableBlocks.COPPER_CABLE;
    world.setBlockAndUpdate(
        position, bare.defaultBlockState().setValue(ConductorBlock.WATERLOGGED, true));
    check(
        bare.tryAddInsulation(world.getBlockState(position), world, position),
        "add native copper insulation");
    var state = world.getBlockState(position);
    var insulated = (ConductorBlock) state.getBlock();
    check(
        !insulated.isFoam() && state.getValue(ConductorBlock.WATERLOGGED),
        "ordinary cable retains water without changing to foam");
    check(
        insulated.tryRemoveInsulation(state, world, position, false)
            && world.getBlockState(position).is(bare)
            && world.getBlockState(position).getValue(ConductorBlock.WATERLOGGED),
        "removing insulation retains native water");
    var foam = CableBlocks.COPPER_FOAM_CABLE;
    world.setBlockAndUpdate(position, foam.defaultBlockState());
    check(
        foam.tryAddInsulation(world.getBlockState(position), world, position)
            && ((ConductorBlock) world.getBlockState(position).getBlock()).isFoam(),
        "soft foam upgrades preserve their foam family");
    state = world.getBlockState(position);
    var foamed = (ConductorBlock) state.getBlock();
    world.setBlockAndUpdate(
        position, state.setValue(ConductorBlock.foamProperty, FoamFinish.HARD_RED));
    check(
        !foamed.tryRemoveInsulation(world.getBlockState(position), world, position, false),
        "hard foam prevents insulation surgery");
    world.setBlockAndUpdate(position, CableBlocks.INSULATED_COPPER_CABLE.defaultBlockState());
    insulated = (ConductorBlock) world.getBlockState(position).getBlock();
    var other = position.east();
    world.setBlockAndUpdate(other, insulated.defaultBlockState());
    check(
        insulated.recolor(world.getBlockState(position), world, position, DyeColor.RED),
        "actual recoloring queues a new conductor");
    var red = EnergyNet.instance.getTile(world, position);
    var black = EnergyNet.instance.getTile(world, other);
    check(
        ((IColoredEnergyTile) black).getColor(Direction.WEST) == null
            && ((IEnergyEmitter) red).emitsEnergyTo((IEnergyAcceptor) black, Direction.EAST),
        "uncolored native conductor is a wildcard for a red conductor");
    insulated.recolor(world.getBlockState(other), world, other, DyeColor.BLUE);
    var blue = EnergyNet.instance.getTile(world, other);
    check(
        !((IEnergyEmitter) red).emitsEnergyTo((IEnergyAcceptor) blue, Direction.EAST),
        "different painted colors reject actual electrical edges");
    world.removeBlock(position, false);
    world.removeBlock(other, false);
    check(
        EnergyNet.instance.getTile(world, position) == null
            && EnergyNet.instance.getTile(world, other) == null,
        "real removal hides both conductor identities immediately");
    var splitter = (RedstoneGateBlock) CableBlocks.SPLITTER_CABLE;
    world.setBlockAndUpdate(
        position, splitter.defaultBlockState().setValue(RedstoneGateBlock.active, true));
    splitter.onLoad(world.getBlockState(position), world, position);
    check(
        !world.getBlockState(position).getValue(RedstoneGateBlock.active)
            && EnergyNet.instance.getTile(world, position) == null,
        "chunk restoration reconciles a stale enabled splitter with absent redstone");
    world.setBlockAndUpdate(position.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
    check(
        world.getBlockState(position).getValue(RedstoneGateBlock.active)
            && EnergyNet.instance.getTile(world, position) != null,
        "real neighbor signal connects splitter");
    world.removeBlock(position.below(), false);
    check(
        !world.getBlockState(position).getValue(RedstoneGateBlock.active)
            && EnergyNet.instance.getTile(world, position) == null,
        "real neighbor signal removal disconnects splitter");
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
