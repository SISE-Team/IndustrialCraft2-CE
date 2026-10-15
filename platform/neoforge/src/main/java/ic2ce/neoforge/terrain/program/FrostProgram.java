package ic2ce.neoforge.terrain.program;

import ic2ce.neoforge.terrain.TerrainController;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;

/** Freezes exposed water and deepens snow after its surrounding columns acquire snow. */
public final class FrostProgram extends TerrainProgram {
  @Override
  public boolean terraform(Level world, BlockPos chosen) {
    var surface = TerrainController.getFirstBlockFrom(world, chosen, 10);
    if (surface == null || !editable(world, surface)) return false;
    var state = world.getBlockState(surface);
    if (state.is(Blocks.WATER)) return replace(world, surface, Blocks.ICE.defaultBlockState());
    if (state.is(Blocks.ICE) && world.getBlockState(surface.below()).is(Blocks.WATER))
      return replace(world, surface.below(), Blocks.ICE.defaultBlockState());
    if (state.is(Blocks.SNOW)) {
      boolean surrounded = true;
      for (var side : DirectionSets.HORIZONTAL_DIRS)
        surrounded &= snowyColumn(world, surface.relative(side), surface.getY());
      if (surrounded) return replace(world, surface, Blocks.SNOW_BLOCK.defaultBlockState());
      int layers = state.getValue(SnowLayerBlock.LAYERS);
      return layers < 8
          && replace(world, surface, state.setValue(SnowLayerBlock.LAYERS, layers + 1));
    }
    var above = surface.above();
    var snow = Blocks.SNOW.defaultBlockState();
    return world.isEmptyBlock(above)
        && (state.is(Blocks.ICE) || snow.canSurvive(world, above))
        && replace(world, above, snow);
  }

  private static boolean snowyColumn(Level world, BlockPos column, int minimumHeight) {
    var surface = TerrainController.getFirstBlockFrom(world, column, 16);
    if (surface == null || surface.getY() < minimumHeight) return false;
    var state = world.getBlockState(surface);
    if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) return true;
    var above = surface.above();
    var snow = Blocks.SNOW.defaultBlockState();
    if (world.isEmptyBlock(above) && (state.is(Blocks.ICE) || snow.canSurvive(world, above)))
      replace(world, above, snow);
    return false;
  }
}
