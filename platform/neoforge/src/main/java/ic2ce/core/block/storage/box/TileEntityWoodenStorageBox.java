package ic2ce.core.block.storage.box;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityWoodenStorageBox extends TileEntityStorageBox {
   public TileEntityWoodenStorageBox(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.WOODEN_STORAGE_BOX, pos, state, 27);
   }
}
