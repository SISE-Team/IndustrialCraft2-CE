package ic2ce.core.block.storage.box;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityBronzeStorageBox extends TileEntityStorageBox {
   public TileEntityBronzeStorageBox(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.BRONZE_STORAGE_BOX, pos, state, 45);
   }
}
