package ic2ce.neoforge.energy.storage;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class HighVoltageBank extends EnergyBank {
   public HighVoltageBank(BlockPos pos, BlockState state) {
      super(BlockEntityDefinitions.MFE, pos, state, 3, 512, 4000000);
   }

   public static class TileEntityElectricClassicMFE extends EnergyBank {
      public TileEntityElectricClassicMFE(BlockPos pos, BlockState state) {
         super(BlockEntityDefinitions.CLASSIC_MFE, pos, state, 2, 128, 600000);
         this.chargeSlot.setTier(3);
         this.dischargeSlot.setTier(3);
      }
   }
}
