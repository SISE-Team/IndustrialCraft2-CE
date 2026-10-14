package ic2ce.core.block.storage.tank;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityBronzeTank extends FluidReservoir {
   public TileEntityBronzeTank(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.BRONZE_TANK, pos, state, 32);
   }
}
