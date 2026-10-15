package ic2ce.neoforge.storage.tank;

import ic2ce.neoforge.storage.StorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityBronzeTank extends FluidReservoir {
   public TileEntityBronzeTank(BlockPos pos, BlockState state) {
      super(StorageBlockEntities.BRONZE_TANK, pos, state, 32);
   }
}
