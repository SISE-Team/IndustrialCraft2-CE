package ic2ce.neoforge.energy.storage;

import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class MediumVoltageBank extends EnergyBank {
   public MediumVoltageBank(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.CESU, pos, state, 2, 128, 300000);
   }
}
