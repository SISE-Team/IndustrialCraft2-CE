package ic2ce.neoforge.energy.storage;

import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** MFSU tier binding for the independently implemented contact-charging controller. */
@NotClassic
public class ExtremeVoltagePad extends WirelessChargingPad {
  public ExtremeVoltagePad(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.MFSU_CHARGEPAD, position, state, 4, 2048, 40000000);
  }
}
