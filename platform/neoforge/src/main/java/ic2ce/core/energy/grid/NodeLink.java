package ic2ce.core.energy.grid;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.IEnergyTile;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** An undirected route edge with each endpoint's direction to its neighbor. */
public final class NodeLink {
  final Node nodeA, nodeB;
  final Direction dirFromA, dirFromB;
  final double loss;
  final List<Node> skippedNodes = new ArrayList<>(0);

  NodeLink(Node a, Node b, double loss) {
    this(a, b, loss, direction(a, b));
  }

  NodeLink(Node a, Node b, double loss, Direction direction) {
    if (a == b) throw new IllegalArgumentException("Self edge");
    nodeA = a;
    nodeB = b;
    this.loss = loss;
    dirFromA = direction;
    dirFromB = direction == null ? null : direction.getOpposite();
  }

  public Node getNeighbor(Node node) {
    if (node == nodeA) return nodeB;
    if (node == nodeB) return nodeA;
    throw new IllegalArgumentException("Endpoint is not on edge");
  }

  public double getLoss() {
    return loss;
  }

  public Direction getDirFrom(Node node) {
    return node == nodeA ? dirFromA : node == nodeB ? dirFromB : null;
  }

  private static Direction direction(Node a, Node b) {
    for (IEnergyTile first : a.tile.subTiles)
      for (IEnergyTile second : b.tile.subTiles) {
        BlockPos from = EnergyNet.instance.getPos(first), to = EnergyNet.instance.getPos(second);
        int x = to.getX() - from.getX(), y = to.getY() - from.getY(), z = to.getZ() - from.getZ();
        if (Math.abs(x) + Math.abs(y) + Math.abs(z) != 1) continue;
        return x != 0
            ? (x > 0 ? Direction.EAST : Direction.WEST)
            : y != 0
                ? (y > 0 ? Direction.UP : Direction.DOWN)
                : z > 0 ? Direction.SOUTH : Direction.NORTH;
      }
    return null;
  }

  @Override
  public String toString() {
    return nodeA + " -> " + nodeB + " (" + loss + ")";
  }
}
