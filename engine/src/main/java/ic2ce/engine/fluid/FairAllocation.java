package ic2ce.engine.fluid;

import java.util.Arrays;

/** Integer max-min allocation with rotating remainders and reusable caller-owned arrays. */
public final class FairAllocation {
  private FairAllocation() {}

  public static void plan(int budget, int[] demand, int[] allocated, int rotation) {
    if (budget < 0 || demand == allocated || demand.length != allocated.length)
      throw new IllegalArgumentException("Invalid distribution workspace");
    Arrays.fill(allocated, 0);
    long total = 0;
    for (int value : demand) {
      if (value < 0) throw new IllegalArgumentException("Negative outlet demand");
      total += value;
    }
    int remaining = (int) Math.min(budget, total);
    while (remaining > 0) {
      int count = 0;
      for (int index = 0; index < demand.length; index++)
        if (allocated[index] < demand[index]) count++;
      if (count == 0) return;
      int share = remaining / count;
      if (share == 0) {
        int skip = Math.floorMod(rotation, count), start = 0;
        for (int index = 0; index < demand.length; index++)
          if (allocated[index] < demand[index] && skip-- == 0) {
            start = index;
            break;
          }
        for (int offset = 0; offset < demand.length && remaining > 0; offset++) {
          int index = (start + offset) % demand.length;
          if (allocated[index] < demand[index]) {
            allocated[index]++;
            remaining--;
          }
        }
        continue;
      }
      boolean saturated = false;
      for (int index = 0; index < demand.length; index++) {
        int free = demand[index] - allocated[index];
        if (free > 0 && free <= share) {
          allocated[index] += free;
          remaining -= free;
          saturated = true;
        }
      }
      if (saturated) continue;
      for (int index = 0; index < demand.length; index++)
        if (allocated[index] < demand[index]) {
          allocated[index] += share;
          remaining -= share;
        }
    }
  }
}
