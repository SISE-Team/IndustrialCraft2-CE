package ic2ce.api.energy;

import ic2ce.api.energy.tile.IEnergyTile;

public interface IEnergyNetEventReceiver {
   void onAdd(IEnergyTile var1);

   void onRemove(IEnergyTile var1);
}
