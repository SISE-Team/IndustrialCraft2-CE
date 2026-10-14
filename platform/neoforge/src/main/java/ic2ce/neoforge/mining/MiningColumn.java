package ic2ce.neoforge.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

/**
 * The mining column projects one precompiled central quarter-width shape into the native collision
 * protocol.
 */
public class MiningColumn extends Block {
  private static final VoxelShape COLUMN = Shapes.box(.375, 0, .375, .625, 1, .625);

  public MiningColumn(Properties properties) {
    super(properties);
  }

  @Override
  public boolean useShapeForLightOcclusion(BlockState state) {
    return true;
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter world, BlockPos position, CollisionContext context) {
    return COLUMN;
  }
}
