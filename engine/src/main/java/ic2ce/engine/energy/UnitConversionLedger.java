package ic2ce.engine.energy;

/** Prepaid output from indivisible input units is reused by later packets. */
public final class UnitConversionLedger {
  private double credit;

  public double credit() {
    return credit;
  }

  public void restore(double energy) {
    credit = Double.isFinite(energy) && energy >= 0 ? energy : 0;
  }

  public int requiredUnits(double energy, double perUnit) {
    if (!Double.isFinite(energy) || energy < 0 || !Double.isFinite(perUnit) || perUnit <= 0)
      return -1;
    double delta = energy - credit;
    if (delta <= 0 || credit > 0 && delta <= 8 * (Math.ulp(energy) + Math.ulp(credit))) return 0;
    double units = delta / perUnit;
    if (units == 0) return 1;
    double nearest = Math.rint(units);
    if (Math.abs(units - nearest) <= 8 * Math.ulp(units)) units = nearest;
    return units > Integer.MAX_VALUE ? -1 : (int) Math.ceil(units);
  }

  public void commit(double energy, double perUnit, int consumedUnits) {
    if (consumedUnits < 0 || consumedUnits != requiredUnits(energy, perUnit))
      throw new IllegalArgumentException("Unbalanced conversion transaction");
    credit = Math.max(0, credit + consumedUnits * perUnit - energy);
  }
}
