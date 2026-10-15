package ic2ce.neoforge.storage.box;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySteelStorageBox extends TileEntityStorageBox {
   public TileEntitySteelStorageBox(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.STEEL_STORAGE_BOX, pos, state, 63);
   }
}
