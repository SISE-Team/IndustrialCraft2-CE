package ic2ce.neoforge.energy.storage;

import ic2ce.neoforge.energy.EnergyBlockEntities;
import ic2ce.neoforge.runtime.profile.NotClassic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** CESU tier binding for the independently implemented contact-charging controller. */
@NotClassic
public class MediumVoltagePad extends WirelessChargingPad {
  public MediumVoltagePad(BlockPos position, BlockState state) {
    super(EnergyBlockEntities.CESU_CHARGEPAD, position, state, 2, 128, 300000);
  }
}
