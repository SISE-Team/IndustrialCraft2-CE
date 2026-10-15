package ic2ce.neoforge.storage.box;

import ic2ce.neoforge.storage.StorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityBronzeStorageBox extends TileEntityStorageBox {
   public TileEntityBronzeStorageBox(BlockPos pos, BlockState state) {
      super(StorageBlockEntities.BRONZE_STORAGE_BOX, pos, state, 45);
   }
}
