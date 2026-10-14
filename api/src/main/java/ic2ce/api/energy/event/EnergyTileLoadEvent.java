package ic2ce.api.energy.event;

import ic2ce.api.energy.tile.IEnergyTile;

public class EnergyTileLoadEvent extends EnergyTileEvent {
   public EnergyTileLoadEvent(IEnergyTile energyTile1) {
      super(energyTile1);
   }
}
