package ic2ce.neoforge.storage.tank;

import ic2ce.neoforge.storage.StorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySteelTank extends FluidReservoir {
   public TileEntitySteelTank(BlockPos pos, BlockState state) {
      super(StorageBlockEntities.STEEL_TANK, pos, state, 128);
   }
}
