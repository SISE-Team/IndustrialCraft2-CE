package ic2ce.core.energy.grid;

import ic2ce.api.energy.tile.IEnergyConductor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** A role endpoint owned by one immutable topology generation. */
public final class Node {
  final int uid;
  final Tile tile;
  final NodeType nodeType;
  final List<NodeLink> links = new ArrayList<>(6);
  private Grid grid;
  private boolean extra;

  Node(int uid, Tile tile, NodeType type) {
    this.uid = uid;
    this.tile = Objects.requireNonNull(tile);
    this.nodeType = Objects.requireNonNull(type);
  }

  public Tile getTile() {
    return tile;
  }

  public NodeType getType() {
    return nodeType;
  }

  public Grid getGrid() {
    return grid;
  }

  public Collection<NodeLink> getLinks() {
    return links;
  }

  boolean isExtraNode() {
    return extra;
  }

  void setExtraNode(boolean value) {
    if (value && nodeType == NodeType.Conductor)
      throw new IllegalStateException("Duplicate conductor");
    extra = value;
  }

  void setGrid(Grid value) {
    if (grid != null) throw new IllegalStateException("Endpoint already attached");
    grid = Objects.requireNonNull(value);
  }

  void clearGrid() {
    grid = null;
  }

  public NodeLink getLinkTo(Node neighbor) {
    for (int i = 0; i < links.size(); i++) {
      NodeLink link = links.get(i);
      if (link.getNeighbor(this) == neighbor) return link;
    }
    return null;
  }

  double getInnerLoss() {
    return nodeType == NodeType.Conductor
        ? ((IEnergyConductor) tile.getMainTile()).getConductionLoss()
        : 0.002;
  }

  @Override
  public String toString() {
    return tile + "|" + nodeType + "|" + uid;
  }
}
