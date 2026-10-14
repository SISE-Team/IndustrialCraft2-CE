package ic2ce.neoforge.terrain.program;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Native terrain program boundary; changes stay in loaded, editable columns. */
public abstract class TerrainProgram {
  public abstract boolean terraform(Level world, BlockPos position);

  public void init() {}

  public static boolean editable(Level world, BlockPos position) {
    return !world.isClientSide()
        && world.isLoaded(position)
        && !world.isOutsideBuildHeight(position);
  }

  protected static boolean replace(Level world, BlockPos position, BlockState state) {
    return editable(world, position)
        && !world.getBlockState(position).equals(state)
        && world.setBlockAndUpdate(position, state);
  }
}
