package ic2ce.neoforge.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

/** Transparent industrial glass never supplies a structural attachment surface. */
public class StructuralGlass extends Block {
  public StructuralGlass(Properties properties) {
    super(properties);
  }

  @Override
  public VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos position) {
    return Shapes.empty();
  }
}
