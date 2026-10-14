package ic2ce.api.energy.tile;

import net.minecraft.core.Direction;

/** Legacy heat sources expose a bounded prediction and a separate committing draw. */
public interface IHeatSource {
  @Deprecated
  int maxrequestHeatTick(Direction side);

  @Deprecated
  int requestHeat(Direction side, int amount);

  default int getConnectionBandwidth(Direction side) {
    return side == null ? 0 : Math.max(0, maxrequestHeatTick(side));
  }

  default int drawHeat(Direction side, int requested, boolean simulate) {
    if (requested <= 0 || side == null) return 0;
    int admitted = Math.min(requested, getConnectionBandwidth(side));
    return simulate || admitted == 0
        ? admitted
        : Math.clamp(requestHeat(side, admitted), 0, admitted);
  }
}
