package ic2ce.engine.fluid;

/** Carries prepaid sub-millibucket fluid between ticks without rounding every tick up. */
public final class FractionalFluidLedger {
  private double credit;

  public double creditBuckets() {
    return credit;
  }

  public void restore(double buckets) {
    credit = Double.isFinite(buckets) && buckets >= 0 && buckets < 0.001 ? buckets : 0;
  }

  public int requiredMillibuckets(double buckets) {
    if (!Double.isFinite(buckets) || buckets < 0) return -1;
    double consumed = buckets * 1000, prepaid = credit * 1000;
    double demand = consumed - prepaid;
    double nearest = Math.rint(demand);
    double tolerance = 8 * (Math.ulp(consumed) + Math.ulp(prepaid));
    if (Math.abs(demand - nearest) <= tolerance) demand = nearest;
    if (demand <= 0) return 0;
    if (demand > Integer.MAX_VALUE) return -1;
    return (int) Math.ceil(demand);
  }

  public void commit(double buckets, int drained) {
    int required = requiredMillibuckets(buckets);
    if (required < 0 || drained != required)
      throw new IllegalArgumentException("Unbalanced fractional fluid transaction");
    credit = Math.max(0, credit + drained / 1000.0 - buckets);
  }
}
