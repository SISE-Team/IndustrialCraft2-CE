package ic2ce.core.energy;

import ic2ce.api.energy.NodeStats;

class MutableNodeStats extends NodeStats {
   protected MutableNodeStats() {
      super(0.0, 0.0, 0.0);
   }

   protected void set(double energyIn, double energyOut, double voltage) {
      this.energyIn = energyIn;
      this.energyOut = energyOut;
      this.voltage = voltage;
   }
}
