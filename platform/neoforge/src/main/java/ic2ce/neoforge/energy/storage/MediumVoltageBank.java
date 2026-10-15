package ic2ce.neoforge.energy.storage;

import ic2ce.neoforge.energy.EnergyBlockEntities;
import ic2ce.neoforge.runtime.profile.NotClassic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class MediumVoltageBank extends EnergyBank {
   public MediumVoltageBank(BlockPos pos, BlockState state) {
      super(EnergyBlockEntities.CESU, pos, state, 2, 128, 300000);
   }
}
