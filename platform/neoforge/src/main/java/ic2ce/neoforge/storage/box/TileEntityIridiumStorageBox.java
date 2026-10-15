package ic2ce.neoforge.storage.box;

import ic2ce.neoforge.storage.StorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIridiumStorageBox extends TileEntityStorageBox {
   public TileEntityIridiumStorageBox(BlockPos pos, BlockState state) {
      super(StorageBlockEntities.IRIDIUM_STORAGE_BOX, pos, state, 126);
   }
}
