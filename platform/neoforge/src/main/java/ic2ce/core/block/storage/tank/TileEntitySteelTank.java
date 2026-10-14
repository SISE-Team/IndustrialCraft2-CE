package ic2ce.core.block.storage.tank;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySteelTank extends FluidReservoir {
   public TileEntitySteelTank(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.STEEL_TANK, pos, state, 128);
   }
}
