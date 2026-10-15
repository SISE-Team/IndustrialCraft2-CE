package ic2ce.neoforge.energy.storage;

import ic2ce.neoforge.energy.EnergyBlockEntities;
import ic2ce.neoforge.runtime.profile.NotClassic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** BATBOX tier binding for the independently implemented contact-charging controller. */
@NotClassic
public class LowVoltagePad extends WirelessChargingPad {
  public LowVoltagePad(BlockPos position, BlockState state) {
    super(EnergyBlockEntities.BATBOX_CHARGEPAD, position, state, 1, 32, 40000);
  }
}
