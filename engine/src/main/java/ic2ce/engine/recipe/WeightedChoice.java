package ic2ce.engine.recipe;

import java.util.Arrays;

/** Append-built cumulative weights with allocation-free logarithmic selection. */
public final class WeightedChoice {
  private long[] limits = new long[4];
  private int size;
  private long total;

  public void add(long weight) {
    if (weight < 0) throw new IllegalArgumentException("Negative output weight");
    long next = Math.addExact(total, weight);
    if (size == limits.length) limits = Arrays.copyOf(limits, limits.length * 2);
    limits[size++] = next;
    total = next;
  }

  public long totalWeight() {
    return total;
  }

  public int size() {
    return size;
  }

  public void clear() {
    size = 0;
    total = 0;
  }

  public int select(long ticket) {
    if (ticket < 0 || ticket >= total)
      throw new IllegalArgumentException("Ticket outside total weight");
    int low = 0, high = size;
    while (low < high) {
      int middle = (low + high) >>> 1;
      if (ticket < limits[middle]) high = middle;
      else low = middle + 1;
    }
    return low;
  }
}
