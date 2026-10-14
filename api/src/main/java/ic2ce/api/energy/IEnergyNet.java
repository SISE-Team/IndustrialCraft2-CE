package ic2ce.api.energy;

import ic2ce.api.energy.tile.IEnergyTile;
import ic2ce.api.info.ILocatable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IEnergyNet {
  IEnergyTile getTile(Level var1, BlockPos var2);

  IEnergyTile getSubTile(Level var1, BlockPos var2);

  <T extends BlockEntity & IEnergyTile> void addBlockEntityTile(T var1);

  <T extends ILocatable & IEnergyTile> void addLocatableTile(T var1);

  default void addTileUnchecked(IEnergyTile tile) {
    java.util.Objects.requireNonNull(tile, "Energy tile");
    switch (tile) {
      case BlockEntity entity -> addBlockEntityTile((BlockEntity & IEnergyTile) entity);
      case ILocatable location -> addLocatableTile((ILocatable & IEnergyTile) location);
      default ->
          throw new IllegalArgumentException(
              "Energy tile requires a native block entity or an explicit location: "
                  + tile.getClass().getName());
    }
  }

  void removeTile(IEnergyTile var1);

  Level getWorld(IEnergyTile var1);

  BlockPos getPos(IEnergyTile var1);

  NodeStats getNodeStats(IEnergyTile var1);

  int getAdjacentConnections(IEnergyTile var1);

  double getPowerFromTier(int var1);

  int getTierFromPower(double var1);

  void registerEventReceiver(IEnergyNetEventReceiver var1);

  void unregisterEventReceiver(IEnergyNetEventReceiver var1);
}
