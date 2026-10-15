package ic2ce.neoforge.energy.grid;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.IEnergySink;
import ic2ce.api.energy.tile.IEnergySource;
import ic2ce.api.energy.tile.IEnergyTile;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.diagnostics.ProjectLog;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/** Fixed component membership; a changed component is replaced as a unit. */
public final class Grid {
  private final int uid;
  private final EnergyNetLocal network;
  private final List<Node> nodes;
  private Object data;
  private boolean suspended;

  Grid(EnergyNetLocal network, List<Node> nodes) {
    this.network = network;
    uid = network.allocateGridId();
    this.nodes = List.copyOf(nodes);
    for (Node node : nodes) node.setGrid(this);
  }

  public EnergyNetLocal getEnergyNet() {
    return network;
  }

  public Node getNode(int id) {
    for (Node node : nodes) if (node.uid == id) return node;
    return null;
  }

  public Collection<Node> getNodes() {
    return nodes;
  }

  @SuppressWarnings("unchecked")
  public <T> T getData() {
    return (T) data;
  }

  public void setData(Object value) {
    data = value;
  }

  void suspend() {
    suspended = true;
  }

  boolean isSuspended() {
    return suspended;
  }

  void release() {
    for (Node node : nodes) {
      node.links.clear();
      node.clearGrid();
      if (node.isExtraNode()) node.tile.nodes.remove(node);
    }
    data = null;
  }

  GridInfo getInfo() {
    int branches = 0, minX = Integer.MAX_VALUE, minY = minX, minZ = minX;
    int maxX = Integer.MIN_VALUE, maxY = maxX, maxZ = maxX;
    for (Node node : nodes) {
      if (node.links.size() > 2) branches++;
      for (IEnergyTile part : node.tile.subTiles) {
        var p = EnergyNet.instance.getPos(part);
        minX = Math.min(minX, p.getX());
        minY = Math.min(minY, p.getY());
        minZ = Math.min(minZ, p.getZ());
        maxX = Math.max(maxX, p.getX());
        maxY = Math.max(maxY, p.getY());
        maxZ = Math.max(maxZ, p.getZ());
      }
    }
    return new GridInfo(uid, nodes.size(), branches, minX, minY, minZ, maxX, maxY, maxZ);
  }

  void dumpInfo(String prefix, PrintStream console, PrintStream chat) {
    chat.printf("%sGrid %d: %d endpoints%n", prefix, uid, nodes.size());
  }

  void dumpNodeInfo(Node node, String prefix, PrintStream console, PrintStream chat) {
    chat.printf("%s%s at %s%n", prefix, node, EnergyNet.instance.getPos(node.tile.getMainTile()));
    switch (node.nodeType) {
      case Source ->
          chat.printf(
              "%soffered: %.2f, tier: %d%n",
              prefix,
              ((IEnergySource) node.tile.getMainTile()).getOfferedEnergy(),
              ((IEnergySource) node.tile.getMainTile()).getSourceTier());
      case Sink ->
          chat.printf(
              "%sdemanded: %.2f, tier: %d%n",
              prefix,
              ((IEnergySink) node.tile.getMainTile()).getDemandedEnergy(),
              ((IEnergySink) node.tile.getMainTile()).getSinkTier());
      case Conductor -> {}
    }
    for (NodeLink link : node.links) chat.printf("%s%s%n", prefix, link);
    EnergyNetGlobal.getCalculator().dumpNodeInfo(node, prefix, console, chat);
  }

  void dumpGraph() {
    try (var writer = Files.newBufferedWriter(Path.of("graph_" + uid + "_raw.txt"))) {
      writer.write("graph nodes {\n");
      for (Node node : nodes) {
        writer.write("  n" + node.uid + ";\n");
        for (NodeLink link : node.links)
          if (link.nodeA == node)
            writer.write(
                "  n" + node.uid + " -- n" + link.nodeB.uid + " [label=\"" + link.loss + "\"];\n");
      }
      writer.write("}\n");
    } catch (IOException error) {
      ProjectLog.LOG.debug(DiagnosticArea.EnergyNet, error, "Cannot write energy graph");
    }
  }

  @Override
  public String toString() {
    return "Grid " + uid;
  }
}
