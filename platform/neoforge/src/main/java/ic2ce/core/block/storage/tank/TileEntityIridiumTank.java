package ic2ce.core.block.storage.tank;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIridiumTank extends FluidReservoir {
   public TileEntityIridiumTank(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.IRIDIUM_TANK, pos, state, 1024);
   }
}
