package ic2ce.engine.reactor;

/** Fractional heat accounting for indivisible coolant batches and interrupted cooling. */
public final class ReactorCooling {
  public record Exchange(int millibuckets, int emittedHu, int returnedHullHeat) {}

  private double pendingHu, hullFraction;

  public double pendingHu() {
    return pendingHu;
  }

  public double hullFraction() {
    return hullFraction;
  }

  public void restore(double pending, double fraction) {
    pendingHu = Double.isFinite(pending) ? Math.max(0, pending) : 0;
    hullFraction = Double.isFinite(fraction) && fraction >= 0 && fraction < 1 ? fraction : 0;
  }

  public Exchange exchange(int emittedHeat, double multiplier, int huPerMb, int capacity) {
    if (!Double.isFinite(multiplier) || multiplier <= 0) {
      pendingHu = 0;
      return new Exchange(0, 0, Math.max(0, emittedHeat));
    }
    double available = pendingHu + Math.max(0, emittedHeat) * multiplier;
    int wanted =
        huPerMb > 0 ? (int) Math.clamp(Math.floor(available / huPerMb), 0, Integer.MAX_VALUE) : 0;
    int moved = Math.min(wanted, Math.max(0, capacity));
    long delivered = (long) moved * Math.max(0, huPerMb);
    double remainder = Math.max(0, available - delivered);
    int returned = 0;
    // A sub-millibucket remainder is prepaid only while cooling has capacity.
    // Resource shortages return the unconverted heat to the hull exactly once.
    if (huPerMb > 0 && capacity > 0 && moved == wanted) pendingHu = remainder;
    else {
      pendingHu = 0;
      double hull = hullFraction + remainder / multiplier;
      returned = (int) Math.clamp(Math.floor(hull), 0, Integer.MAX_VALUE);
      hullFraction = hull < Integer.MAX_VALUE ? hull - returned : 0;
    }
    return new Exchange(moved, (int) Math.min(Integer.MAX_VALUE, delivered), returned);
  }
}
