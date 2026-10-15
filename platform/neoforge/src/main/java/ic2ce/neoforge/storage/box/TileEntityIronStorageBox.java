package ic2ce.neoforge.storage.box;

import ic2ce.neoforge.storage.StorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIronStorageBox extends TileEntityStorageBox {
   public TileEntityIronStorageBox(BlockPos pos, BlockState state) {
      super(StorageBlockEntities.IRON_STORAGE_BOX, pos, state, 45);
   }
}
