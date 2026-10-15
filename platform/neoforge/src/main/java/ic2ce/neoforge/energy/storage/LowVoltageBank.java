package ic2ce.neoforge.energy.storage;

import ic2ce.neoforge.energy.EnergyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LowVoltageBank extends EnergyBank {
   public LowVoltageBank(BlockPos pos, BlockState state) {
      super(EnergyBlockEntities.BATBOX, pos, state, 1, 32, 40000);
   }
}
