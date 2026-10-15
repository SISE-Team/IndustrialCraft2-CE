package ic2ce.engine.reactor;

/**
 * Hull heat, electrical output and gauge arithmetic of a reactor core. Values are saturating and
 * never negative; nonfinite component contributions are ignored.
 */
public final class ReactorCore {
  /** Hull heat at which the gauge shows full scale: eighty-five percent of the maximum. */
  private static final int GAUGE_PERCENT = 85;

  private ReactorCore() {}

  /** Nonfinite or negative factors and outputs count as zero. */
  public static float bounded(float value) {
    return Float.isFinite(value) ? Math.max(0, value) : 0;
  }

  public static int addHeat(int heat, int amount) {
    return (int) Math.clamp((long) Math.max(0, heat) + amount, 0, Integer.MAX_VALUE);
  }

  public static float addOutput(float output, float amount) {
    if (!Float.isFinite(amount)) return output;
    return (float) Math.clamp((double) bounded(output) + amount, 0, Float.MAX_VALUE);
  }

  /** Coolant-mode heat waiting for exchange; only positive pulses accumulate. */
  public static int addEmittedHeat(int pending, int amount) {
    return amount > 0 ? (int) Math.min(Integer.MAX_VALUE, (long) pending + amount) : pending;
  }

  /** EU offered per tick for a core output, or zero when the configured factor is unusable. */
  public static double electricalOutput(float output, double factor) {
    return Double.isFinite(factor) && factor > 0 ? bounded(output) * 5.0 * factor : 0;
  }

  public static int heatGauge(int width, int heat, int maxHeat) {
    if (maxHeat <= 0) return 0;
    return (int)
        Math.clamp(
            (long) Math.max(0, width) * Math.max(0, heat) * 100 / ((long) maxHeat * GAUGE_PERCENT),
            0,
            Integer.MAX_VALUE);
  }
}
