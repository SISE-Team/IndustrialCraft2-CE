package ic2ce.core.block.storage.box;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIridiumStorageBox extends TileEntityStorageBox {
   public TileEntityIridiumStorageBox(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.IRIDIUM_STORAGE_BOX, pos, state, 126);
   }
}
