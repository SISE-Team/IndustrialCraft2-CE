package ic2ce.neoforge.world.block;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Native casing families and actual directional fire properties define separate reactor material
 * protocols.
 */
public final class WorldMaterials {
  private static final Direction[] FACES = Direction.values();
  private static final Set<Block> CASING =
      Set.of(
          Blocks.STONE,
          Blocks.GRANITE,
          Blocks.DIORITE,
          Blocks.ANDESITE,
          Blocks.POLISHED_GRANITE,
          Blocks.POLISHED_DIORITE,
          Blocks.POLISHED_ANDESITE,
          Blocks.DEEPSLATE,
          Blocks.TUFF,
          Blocks.OBSIDIAN,
          Blocks.IRON_BLOCK,
          Blocks.COPPER_BLOCK,
          Blocks.GOLD_BLOCK,
          Blocks.DIAMOND_BLOCK,
          Blocks.EMERALD_BLOCK,
          Blocks.LAVA,
          Blocks.CLAY,
          Blocks.DIRT,
          Blocks.COARSE_DIRT,
          Blocks.GRASS_BLOCK,
          Blocks.PODZOL,
          Blocks.BRICKS,
          Blocks.STONE_BRICKS,
          Blocks.NETHER_BRICKS,
          Blocks.REINFORCED_DEEPSLATE);

  private WorldMaterials() {}

  public static boolean isReactorCasingLike(BlockState state) {
    if (state == null || state.isAir()) return false;
    if (CASING.contains(state.getBlock())) return true;
    return state.is(BlockTags.BASE_STONE_OVERWORLD)
        || state.is(BlockTags.BASE_STONE_NETHER)
        || state.is(BlockTags.STONE_BRICKS)
        || state.is(BlockTags.DIRT)
        || state.is(BlockTags.SAND)
        || !state.is(BlockTags.LEAVES)
            && (state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || state.is(BlockTags.NEEDS_IRON_TOOL)
                || state.is(BlockTags.NEEDS_DIAMOND_TOOL));
  }

  public static boolean isFlammableLike(BlockState state, BlockGetter world, BlockPos position) {
    if (state == null || state.isAir() || !state.getFluidState().isEmpty()) return false;
    for (var face : FACES)
      if (state.getFlammability(world, position, face) > 0
          || state.getFireSpreadSpeed(world, position, face) > 0) return true;
    return false;
  }
}
