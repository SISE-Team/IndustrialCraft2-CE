package ic2ce.api.energy.event;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.IEnergyTile;
import java.util.Objects;
import net.neoforged.neoforge.event.level.LevelEvent;

/** Native energy events validate their resolved world before publishing the event. */
public class EnergyTileEvent extends LevelEvent {
  public final IEnergyTile tile;

  public EnergyTileEvent(IEnergyTile tile) {
    Objects.requireNonNull(tile, "Energy tile");
    var network = Objects.requireNonNull(EnergyNet.instance, "Energy network is not initialized");
    var world = Objects.requireNonNull(network.getWorld(tile), "Energy tile has no native world");
    super(world);
    this.tile = tile;
  }
}
