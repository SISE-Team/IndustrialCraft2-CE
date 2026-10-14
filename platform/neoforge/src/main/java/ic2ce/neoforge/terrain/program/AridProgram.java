package ic2ce.neoforge.terrain.program;

import ic2ce.neoforge.terrain.TerrainController;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Removes moisture and vegetation, replaces soil and occasionally ignites exposed wood. */
public final class AridProgram extends TerrainProgram {
  @Override
  public boolean terraform(Level world, BlockPos chosen) {
    var surface = TerrainController.getFirstBlockFrom(world, chosen, 10);
    if (surface == null || !editable(world, surface)) return false;
    var sand = Blocks.SAND.defaultBlockState();
    if (TerrainController.switchGround(world, surface, Blocks.DIRT, sand, false)
        || TerrainController.switchGround(world, surface, Blocks.GRASS_BLOCK, sand, false)
        || TerrainController.switchGround(world, surface, Blocks.FARMLAND, sand, false)) {
      TerrainController.switchGround(world, surface, Blocks.DIRT, sand, false);
      return true;
    }
    var state = world.getBlockState(surface);
    if (state.is(Blocks.ICE) || state.is(Blocks.SNOW_BLOCK))
      return replace(world, surface, Blocks.WATER.defaultBlockState());
    if (state.is(Blocks.WATER)
        || state.is(Blocks.SNOW)
        || state.is(BlockTags.LEAVES)
        || plant(state)) {
      boolean changed = world.removeBlock(surface, false);
      var above = surface.above();
      if (editable(world, above) && plant(world.getBlockState(above)))
        changed |= world.removeBlock(above, false);
      return changed;
    }
    if ((state.is(BlockTags.PLANKS) || state.is(BlockTags.LOGS_THAT_BURN))
        && world.getRandom().nextInt(15) == 0)
      return replace(world, surface, Blocks.FIRE.defaultBlockState());
    return false;
  }

  private static boolean plant(BlockState state) {
    return state.is(BlockTags.SAPLINGS)
        || state.is(BlockTags.CROPS)
        || state.is(BlockTags.FLOWERS)
        || CultivationProgram.containsPlant(state.getBlock());
  }
}
