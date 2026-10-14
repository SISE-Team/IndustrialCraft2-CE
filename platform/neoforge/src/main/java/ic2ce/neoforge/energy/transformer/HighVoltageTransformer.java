package ic2ce.neoforge.energy.transformer;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class HighVoltageTransformer extends VoltageTransformer {
   public HighVoltageTransformer(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.HV_TRANSFORMER, pos, state, 3);
   }
}
