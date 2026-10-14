package ic2ce.neoforge.energy.transformer;

import ic2ce.core.profile.NotClassic;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class ExtremeVoltageTransformer extends VoltageTransformer {
   public ExtremeVoltageTransformer(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.EV_TRANSFORMER, pos, state, 4);
   }
}
