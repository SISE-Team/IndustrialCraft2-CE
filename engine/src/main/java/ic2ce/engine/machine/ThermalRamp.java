package ic2ce.engine.machine;

/** One-unit heating/holding or bounded passive cooling, without state allocation. */
public final class ThermalRamp {
  private ThermalRamp() {}

  public static int normalize(int heat, int maximum) {
    if (maximum < 0) throw new IllegalArgumentException("Negative thermal capacity");
    return Math.clamp(heat, 0, maximum);
  }

  public static int advance(int heat, int target, int maximum, int cooling, boolean powered) {
    if (cooling < 0) throw new IllegalArgumentException("Negative cooling rate");
    heat = normalize(heat, maximum);
    if (powered && target >= 0) {
      target = normalize(target, maximum);
      return Math.min(target, heat) < target ? Math.min(heat, target) + 1 : target;
    }
    return heat - Math.min(heat, cooling);
  }

  public static double ratio(int heat, int target) {
    return target <= 0 ? 0 : Math.clamp((double) heat / target, 0, 1);
  }
}
