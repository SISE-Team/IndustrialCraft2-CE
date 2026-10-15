package ic2ce.neoforge.reactor;

import ic2ce.neoforge.runtime.profile.NotClassic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class LapisCondensatorService extends CondensatorService {
   public LapisCondensatorService(BlockPos pos, BlockState state) {
      super(ReactorBlockEntities.RCI_LZH, pos, state, new ItemStack(ReactorItems.LZH_CONDENSATOR), new ItemStack(Blocks.LAPIS_BLOCK));
   }
}
