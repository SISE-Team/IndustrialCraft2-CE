package ic2ce.neoforge.reactor;

import ic2ce.neoforge.runtime.profile.NotClassic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class RedstoneCondensatorService extends CondensatorService {
   public RedstoneCondensatorService(BlockPos pos, BlockState state) {
      super(ReactorBlockEntities.RCI_RSH, pos, state, new ItemStack(ReactorItems.RSH_CONDENSATOR), new ItemStack(Blocks.REDSTONE_BLOCK));
   }
}
