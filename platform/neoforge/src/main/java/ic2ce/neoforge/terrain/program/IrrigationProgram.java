package ic2ce.neoforge.terrain.program;

import ic2ce.neoforge.terrain.TerrainController;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rehydrates sand, grows native plants and extends exposed trees without overwriting occupied
 * space.
 */
public final class IrrigationProgram extends TerrainProgram {
  @Override
  public boolean terraform(Level world, BlockPos chosen) {
    if (!(world instanceof ServerLevel server)) return false;
    if (world.getRandom().nextInt(48000) == 0) {
      server
          .getServer()
          .setWeatherParameters(0, 12000 + world.getRandom().nextInt(12001), true, false);
      return true;
    }
    var surface = TerrainController.getFirstBlockFrom(world, chosen, 10);
    if (surface == null || !editable(world, surface)) return false;
    if (TerrainController.switchGround(
        world, surface, Blocks.SAND, Blocks.DIRT.defaultBlockState(), true)) {
      TerrainController.switchGround(
          world, surface, Blocks.SAND, Blocks.DIRT.defaultBlockState(), true);
      return true;
    }
    var state = world.getBlockState(surface);
    var block = state.getBlock();
    if (block instanceof BonemealableBlock growth
        && growth.isValidBonemealTarget(world, surface, state)) {
      growth.performBonemeal(server, world.getRandom(), surface, state);
      return true;
    }
    if (state.is(Blocks.FIRE)) return world.removeBlock(surface, false);
    if (state.is(Blocks.SHORT_GRASS)) {
      for (var side : DirectionSets.HORIZONTAL_DIRS)
        if (spreadGrass(world, surface.relative(side))) return true;
      return false;
    }
    var above = surface.above();
    if (!state.is(BlockTags.LOGS) || !world.isEmptyBlock(above) || !replace(world, above, state))
      return false;
    BlockState leaves = null;
    for (var side : DirectionSets.HORIZONTAL_DIRS) {
      var neighbor = surface.relative(side);
      if (editable(world, neighbor) && world.getBlockState(neighbor).is(BlockTags.LEAVES)) {
        leaves = world.getBlockState(neighbor);
        break;
      }
    }
    if (leaves != null) {
      if (world.isEmptyBlock(above.above())) replace(world, above.above(), leaves);
      for (var side : DirectionSets.HORIZONTAL_DIRS)
        if (world.isEmptyBlock(above.relative(side))) replace(world, above.relative(side), leaves);
    }
    return true;
  }

  private static boolean spreadGrass(Level world, BlockPos column) {
    if (world.getRandom().nextBoolean()) return false;
    var surface = TerrainController.getFirstBlockFrom(world, column, 0);
    if (surface == null) return false;
    var state = world.getBlockState(surface);
    if (state.is(Blocks.DIRT))
      return replace(world, surface, Blocks.GRASS_BLOCK.defaultBlockState());
    var above = surface.above();
    return state.is(Blocks.GRASS_BLOCK)
        && world.isEmptyBlock(above)
        && replace(world, above, Blocks.SHORT_GRASS.defaultBlockState());
  }
}
