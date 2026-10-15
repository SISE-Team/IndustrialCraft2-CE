package ic2ce.neoforge.energy.transformer;

import ic2ce.neoforge.energy.EnergyBlockEntities;
import ic2ce.neoforge.runtime.profile.NotClassic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class ExtremeVoltageTransformer extends VoltageTransformer {
   public ExtremeVoltageTransformer(BlockPos pos, BlockState state) {
      super(EnergyBlockEntities.EV_TRANSFORMER, pos, state, 4);
   }
}
