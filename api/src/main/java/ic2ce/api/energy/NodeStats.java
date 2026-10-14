package ic2ce.api.energy;

/**
 * Immutable native electrical observation; generated record equality is useful to consumers and
 * probes.
 */
public record NodeStats(double energyIn, double energyOut, double voltage) {
  public double getEnergyIn() {
    return energyIn;
  }

  public double getEnergyOut() {
    return energyOut;
  }

  public double getVoltage() {
    return voltage;
  }
}
