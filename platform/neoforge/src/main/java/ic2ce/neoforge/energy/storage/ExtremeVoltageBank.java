package ic2ce.neoforge.energy.storage;

import ic2ce.neoforge.energy.EnergyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtremeVoltageBank extends EnergyBank {
   public ExtremeVoltageBank(BlockPos pos, BlockState state) {
      super(EnergyBlockEntities.MFSU, pos, state, 4, 2048, 40000000);
   }

   public static class TileEntityElectricClassicMFSU extends EnergyBank {
      public TileEntityElectricClassicMFSU(BlockPos pos, BlockState state) {
         super(EnergyBlockEntities.CLASSIC_MFSU, pos, state, 3, 512, 10000000);
         this.chargeSlot.setTier(4);
         this.dischargeSlot.setTier(4);
      }
   }
}
