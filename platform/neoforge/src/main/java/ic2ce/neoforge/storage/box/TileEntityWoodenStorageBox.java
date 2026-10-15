package ic2ce.neoforge.storage.box;

import ic2ce.neoforge.storage.StorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityWoodenStorageBox extends TileEntityStorageBox {
   public TileEntityWoodenStorageBox(BlockPos pos, BlockState state) {
      super(StorageBlockEntities.WOODEN_STORAGE_BOX, pos, state, 27);
   }
}
