package ic2ce.engine.inventory;

/**
 * Added-only max-min filling: existing quantities are immutable lower bounds, including overfull
 * addon slots.
 */
public final class FillLevels {
  private FillLevels() {}

  public static int[] plan(int[] existing, int[] limits, int offered) {
    if (existing == null
        || limits == null
        || existing.length != limits.length
        || existing.length > 4096
        || offered < 0) throw new IllegalArgumentException("Invalid physical filling plan");
    var result = existing.clone();
    long free = 0;
    int maximum = 0;
    for (int index = 0; index < existing.length; index++) {
      if (existing[index] < 0 || limits[index] < 0)
        throw new IllegalArgumentException("Negative physical fill quantity");
      free += Math.max(0, (long) limits[index] - existing[index]);
      maximum = Math.max(maximum, limits[index]);
    }
    long budget = Math.min(offered, free);
    if (budget == 0) return result;
    long low = 0, high = maximum;
    while (low < high) {
      long middle = (low + high + 1) >>> 1;
      if (cost(existing, limits, middle) <= budget) low = middle;
      else high = middle - 1;
    }
    long paid = 0;
    for (int index = 0; index < result.length; index++) {
      result[index] = (int) Math.max(existing[index], Math.min((long) limits[index], low));
      paid += result[index] - existing[index];
    }
    long remainder = budget - paid;
    for (int index = 0; index < result.length && remainder > 0; index++)
      if (result[index] == low && result[index] < limits[index]) {
        result[index]++;
        remainder--;
      }
    if (remainder != 0) throw new IllegalStateException("Incomplete physical filling plan");
    return result;
  }

  private static long cost(int[] existing, int[] limits, long level) {
    long cost = 0;
    for (int index = 0; index < existing.length; index++)
      cost += Math.max(0, Math.min((long) limits[index], level) - existing[index]);
    return cost;
  }
}
