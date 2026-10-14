package ic2ce.neoforge.terrain.program;

import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.terrain.TerrainController;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Moves an exposed terrain surface one block toward the controller height. */
public final class LevelingProgram extends TerrainProgram {
  private static Set<Block> removable = Set.of();

  @Override
  public void init() {
    removable =
        Set.of(
            Blocks.SNOW,
            Blocks.ICE,
            Blocks.GRASS_BLOCK,
            Blocks.SHORT_GRASS,
            Blocks.STONE,
            Blocks.GRAVEL,
            Blocks.SAND,
            Blocks.DIRT,
            Blocks.OAK_LEAVES,
            Blocks.SPRUCE_LEAVES,
            Blocks.BIRCH_LEAVES,
            Blocks.JUNGLE_LEAVES,
            Blocks.ACACIA_LEAVES,
            Blocks.DARK_OAK_LEAVES,
            Blocks.TALL_GRASS,
            Blocks.POPPY,
            Blocks.DANDELION,
            Blocks.WHEAT,
            Blocks.RED_MUSHROOM,
            Blocks.BROWN_MUSHROOM,
            Blocks.PUMPKIN,
            Blocks.MELON,
            BlockDefinitions.RUBBER_LEAVES,
            BlockDefinitions.RUBBER_SAPLING,
            BlockDefinitions.RUBBER_LOG);
  }

  @Override
  public boolean terraform(Level world, BlockPos chosen) {
    var surface = TerrainController.getFirstBlockFrom(world, chosen, 20);
    if (surface == null || !editable(world, surface)) return false;
    if (world.getBlockState(surface).is(Blocks.SNOW)) surface = surface.below();
    int difference = Integer.compare(surface.getY(), chosen.getY());
    if (difference < 0) return replace(world, surface.above(), Blocks.DIRT.defaultBlockState());
    if (difference == 0) return false;
    var state = world.getBlockState(surface);
    return (removable.contains(state.getBlock())
            || state.is(BlockTags.SAPLINGS)
            || state.is(BlockTags.LOGS))
        && world.removeBlock(surface, false);
  }
}
