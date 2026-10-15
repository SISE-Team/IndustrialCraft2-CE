package ic2ce.neoforge.reactor;

import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class RedstoneCondensatorService extends CondensatorService {
   public RedstoneCondensatorService(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.RCI_RSH, pos, state, new ItemStack(ItemDefinitions.RSH_CONDENSATOR), new ItemStack(Blocks.REDSTONE_BLOCK));
   }
}
