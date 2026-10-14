package ic2ce.core.block.storage.tank;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIronTank extends FluidReservoir {
   public TileEntityIronTank(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.IRON_TANK, pos, state, 32);
   }
}
