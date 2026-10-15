package ic2ce.neoforge.storage.tank;

import ic2ce.neoforge.storage.StorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIridiumTank extends FluidReservoir {
   public TileEntityIridiumTank(BlockPos pos, BlockState state) {
      super(StorageBlockEntities.IRIDIUM_TANK, pos, state, 1024);
   }
}
