package ic2ce.engine.weather;

/**
 * Normalized closed-form cubic preserves the altitude peak and upper zero without matrix
 * construction or inversion.
 */
public record WindProfile(double peak, double linear, double quadratic, double cubic) {
  public static WindProfile fromWorld(int top, int seaLevel) {
    double height = Math.max(1, top),
        sea = Math.max(0, seaLevel),
        base = sea < height ? sea : height * .5;
    double peak = base + (height - base) * .5, ratio = height * 1.125 / peak;
    double cubic = (ratio - 2) / ((ratio - 1) * (ratio - 1));
    return new WindProfile(peak, 2 + cubic, -1 - 2 * cubic, cubic);
  }

  public double at(double height) {
    if (!Double.isFinite(height)) return 0;
    double t = height / peak;
    double value = ((cubic * t + quadratic) * t + linear) * t;
    return Double.isFinite(value) ? Math.max(0, value) : 0;
  }
}
