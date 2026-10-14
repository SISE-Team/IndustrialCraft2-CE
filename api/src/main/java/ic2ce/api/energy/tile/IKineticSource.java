package ic2ce.api.energy.tile;

import net.minecraft.core.Direction;

/**
 * Legacy kinetic bridges advertise and draw only bounded nonnegative requests; simulation never
 * pays a native bill.
 */
public interface IKineticSource {
  @Deprecated
  int maxrequestkineticenergyTick(Direction side);

  @Deprecated
  int requestkineticenergy(Direction side, int requested);

  default int getConnectionBandwidth(Direction side) {
    return side == null ? 0 : Math.max(0, maxrequestkineticenergyTick(side));
  }

  default int drawKineticEnergy(Direction side, int requested, boolean simulate) {
    if (side == null || requested <= 0) return 0;
    int available = Math.min(requested, getConnectionBandwidth(side));
    return simulate ? available : Math.clamp(requestkineticenergy(side, available), 0, available);
  }
}
