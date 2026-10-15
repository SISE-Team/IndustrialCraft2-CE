package ic2ce.neoforge.energy.grid;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.*;
import java.util.List;
import net.minecraft.core.Direction;

/** Native path metadata rebuilt only when topology changes. */
final class EnergyPath {
  final Node source, target;
  final List<Node> conductors;
  final double loss;
  final Direction targetDirection;
  final double minEffectEnergy,
      minInsulationEnergyAbsorption,
      minInsulationBreakdownEnergy,
      minConductorBreakdownEnergy;
  final int minX, minY, minZ, maxX, maxY, maxZ;
  int targetIndex, lastCalcId = -1;
  double energySupplied, maxPacketConducted;

  EnergyPath(Node source, Node target, List<Node> conductors, double loss) {
    this.source = source;
    this.target = target;
    this.conductors = List.copyOf(conductors);
    this.loss = loss;
    Node previous = conductors.isEmpty() ? source : conductors.getLast();
    targetDirection = target.getLinkTo(previous).getDirFrom(target);
    double absorption = Double.POSITIVE_INFINITY,
        insulation = Double.POSITIVE_INFINITY,
        breakdown = Double.POSITIVE_INFINITY;
    int lowX = Integer.MAX_VALUE, lowY = lowX, lowZ = lowX;
    int highX = Integer.MIN_VALUE, highY = highX, highZ = highX;
    for (Node node : conductors) {
      var conductor = (IEnergyConductor) node.getTile().getMainTile();
      absorption = Math.min(absorption, conductor.getInsulationEnergyAbsorption());
      insulation = Math.min(insulation, conductor.getInsulationBreakdownEnergy());
      breakdown = Math.min(breakdown, conductor.getConductorBreakdownEnergy());
      for (IEnergyTile part : node.getTile().getSubTiles()) {
        var position = EnergyNet.instance.getPos(part);
        lowX = Math.min(lowX, position.getX());
        lowY = Math.min(lowY, position.getY());
        lowZ = Math.min(lowZ, position.getZ());
        highX = Math.max(highX, position.getX());
        highY = Math.max(highY, position.getY());
        highZ = Math.max(highZ, position.getZ());
      }
    }
    minInsulationEnergyAbsorption = absorption;
    minInsulationBreakdownEnergy = insulation;
    minConductorBreakdownEnergy = breakdown;
    minEffectEnergy = Math.min(absorption, Math.min(insulation, breakdown));
    minX = lowX;
    minY = lowY;
    minZ = lowZ;
    maxX = highX;
    maxY = highY;
    maxZ = highZ;
  }
}
