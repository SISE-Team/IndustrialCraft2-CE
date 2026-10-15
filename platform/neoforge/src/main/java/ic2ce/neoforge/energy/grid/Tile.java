package ic2ce.neoforge.energy.grid;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;

/** Registered API identity; packet accounting is shared by all of its endpoints. */
public final class Tile {
  private final IEnergyTile mainTile;
  final List<IEnergyTile> subTiles;
  final Map<BlockPos, IEnergyTile> partPositions;
  final List<Node> nodes = new ArrayList<>(3);
  final List<Node> primaryNodes = new ArrayList<>(3);
  private boolean disabled;
  private double amount;
  private int packetCount;

  Tile(EnergyNetLocal network, IEnergyTile main, List<IEnergyTile> parts) {
    mainTile = Objects.requireNonNull(main);
    subTiles = List.copyOf(parts);
    var positions = new LinkedHashMap<BlockPos, IEnergyTile>();
    for (IEnergyTile part : subTiles)
      positions.put(EnergyNet.instance.getPos(part).immutable(), part);
    partPositions = Map.copyOf(positions);
    if (main instanceof IEnergySource) role(network, NodeType.Source);
    if (main instanceof IEnergySink) role(network, NodeType.Sink);
    if (main instanceof IEnergyConductor) role(network, NodeType.Conductor);
  }

  private void role(EnergyNetLocal network, NodeType type) {
    Node endpoint = new Node(network.allocateNodeId(), this, type);
    primaryNodes.add(endpoint);
    nodes.add(endpoint);
  }

  public IEnergyTile getMainTile() {
    return mainTile;
  }

  public Collection<Node> getNodes() {
    return nodes;
  }

  public Collection<IEnergyTile> getSubTiles() {
    return subTiles;
  }

  IEnergyTile getSubTileAt(BlockPos position) {
    return partPositions.get(position);
  }

  void setDisabled() {
    disabled = true;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double value) {
    amount = value;
  }

  public int getPacketCount() {
    return packetCount;
  }

  public void setSourceData(double value, int packets) {
    amount = value;
    packetCount = packets;
  }

  @Override
  public String toString() {
    return mainTile.getClass().getSimpleName();
  }
}
