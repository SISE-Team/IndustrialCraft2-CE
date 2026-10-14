package ic2ce.neoforge.energy.transformer;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LowVoltageTransformer extends VoltageTransformer {
   public LowVoltageTransformer(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.LV_TRANSFORMER, pos, state, 1);
   }
}
