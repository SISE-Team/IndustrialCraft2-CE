package ic2ce.neoforge.energy.storage;

import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** MFE tier binding for the independently implemented contact-charging controller. */
@NotClassic
public class HighVoltagePad extends WirelessChargingPad {
  public HighVoltagePad(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.MFE_CHARGEPAD, position, state, 3, 512, 4000000);
  }
}
