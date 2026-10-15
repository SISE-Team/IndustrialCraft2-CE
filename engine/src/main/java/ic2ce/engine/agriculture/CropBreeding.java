package ic2ce.engine.agriculture;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

/**
 * Cross-breeding and weed-spread rules. Species are described by their property values, attribute
 * names and tier; the adapter owns world access and supplies every random draw in its native order.
 */
public final class CropBreeding {
  /** Weight of a parent's own species when it is a crossing candidate. */
  public static final long SAME_SPECIES_WEIGHT = 500;

  private CropBreeding() {}

  /** One crossing participant: ordered property values, attribute names and tier. */
  public record Species(int[] properties, String[] attributes, int tier) {
    public Species {
      Objects.requireNonNull(properties);
    }
  }

  /**
   * Crossing weight of a candidate against one parent. Each shared property within two points adds
   * to the score, each case-insensitive shared attribute adds five, and a candidate much stronger or
   * much weaker than the parent is penalized. The result is never negative.
   */
  public static long ratio(Species candidate, Species parent, boolean sameSpecies) {
    if (sameSpecies) return SAME_SPECIES_WEIGHT;
    var a = candidate.properties();
    var b = parent.properties();
    long score = 0;
    for (int index = 0; index < Math.min(a.length, b.length); index++)
      score += 2 - Math.abs((long) a[index] - b[index]);
    var ca = candidate.attributes();
    var pa = parent.attributes();
    if (ca != null && pa != null)
      for (var left : ca)
        for (var right : pa)
          if (left != null && right != null && left.equalsIgnoreCase(right)) score += 5;
    long difference = (long) candidate.tier() - parent.tier();
    if (difference > 1) score -= difference * 2;
    else if (difference < -3) score += difference;
    return Math.max(0, score);
  }

  /**
   * Uniform ticket in [0, bound) for totals beyond the int range, rejecting the biased tail of the
   * 63-bit draws taken from {@code bits}.
   */
  public static long ticket(long bound, LongSupplier bits) {
    if (bound <= 0) throw new IllegalArgumentException("Ticket bound must be positive");
    long drawn, selected;
    do {
      drawn = bits.getAsLong() >>> 1;
      selected = drawn % bound;
    } while (drawn - selected + bound - 1 < 0);
    return selected;
  }

  /**
   * Growth trait of a weed that overtakes a neighbor: the stronger of both, raised once on a coin.
   * The coin is drawn only when the trait can still rise.
   */
  public static int weedGrowth(int weedGrowth, int neighborGrowth, BooleanSupplier coin) {
    int growth = Math.max(weedGrowth, neighborGrowth);
    return growth < 31 && coin.getAsBoolean() ? growth + 1 : growth;
  }
}
