package ic2ce.api.energy.event;

import ic2ce.api.energy.tile.IEnergyTile;

public class EnergyTileUnloadEvent extends EnergyTileEvent {
   public EnergyTileUnloadEvent(IEnergyTile energyTile1) {
      super(energyTile1);
   }
}
