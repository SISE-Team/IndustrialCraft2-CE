package ic2ce.neoforge.energy.transformer;

import ic2ce.neoforge.energy.EnergyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MediumVoltageTransformer extends VoltageTransformer {
   public MediumVoltageTransformer(BlockPos pos, BlockState state) {
      super(EnergyBlockEntities.MV_TRANSFORMER, pos, state, 2);
   }
}
