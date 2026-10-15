package ic2ce.engine.text;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Exact decimal SI presentation with a bounded significant-digit count. */
public final class SiFormat {
  private static final String[] PREFIXES = {
    "y", "z", "a", "f", "p", "n", "µ", "m", "", "k", "M", "G", "T", "P", "E", "Z", "Y"
  };

  private SiFormat() {}

  /** Formats a value as "mantissa prefix"; the trailing space separates a caller-supplied unit. */
  public static String format(double value, int digits) {
    if (digits < 1 || digits > 18)
      throw new IllegalArgumentException("Significant digits outside [1,18]");
    if (value == 0) return "0 ";
    if (Double.isNaN(value)) return "NaN ";
    if (Double.isInfinite(value)) return value < 0 ? "-∞ " : "∞ ";
    var exact = BigDecimal.valueOf(value);
    int group = Math.floorDiv(exact.precision() - exact.scale() - 1, 3);
    var scaled =
        exact.scaleByPowerOfTen(-3 * group).round(new MathContext(digits, RoundingMode.HALF_UP));
    if (scaled.abs().compareTo(BigDecimal.valueOf(1000)) >= 0) {
      group++;
      scaled = scaled.scaleByPowerOfTen(-3);
    }
    String prefix = group >= -8 && group <= 8 ? PREFIXES[group + 8] : "E" + (3 * group);
    return scaled.stripTrailingZeros().toPlainString() + " " + prefix;
  }
}
