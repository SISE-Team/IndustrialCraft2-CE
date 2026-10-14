package ic2ce.api.energy.tile;

import java.util.List;

public interface IMetaDelegate extends IEnergyTile {
   List<IEnergyTile> getSubTiles();
}
