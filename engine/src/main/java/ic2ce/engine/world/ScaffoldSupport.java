package ic2ce.engine.world;

import java.util.Arrays;

/**
 * Grounded vertical pillars seed a bounded strongest-first frontier; lateral and nongrounded upward
 * spans pay one reach unit.
 */
public final class ScaffoldSupport {
  private ScaffoldSupport() {}

  public static int[] solve(
      int[] reach, int[] above, int[] offsets, int[] lateral, boolean[] foundation) {
    int size = reach.length;
    if (above.length != size
        || foundation.length != size
        || offsets.length != size + 1
        || offsets[0] != 0
        || offsets[size] != lateral.length)
      throw new IllegalArgumentException("Invalid scaffold graph dimensions");
    int maximum = 0;
    for (int node = 0; node < size; node++) {
      if (reach[node] < 0
          || reach[node] > 32
          || above[node] < -1
          || above[node] >= size
          || above[node] == node
          || offsets[node] > offsets[node + 1])
        throw new IllegalArgumentException("Invalid scaffold graph node");
      maximum = Math.max(maximum, reach[node]);
    }
    for (int node : lateral)
      if (node < 0 || node >= size)
        throw new IllegalArgumentException("Invalid scaffold graph edge");
    int[] strength = new int[size];
    Arrays.fill(strength, -1);
    int[] pillarQueue = new int[Math.max(1, size)];
    int head = 0, tail = 0;
    boolean[] pillar = new boolean[size];
    for (int node = 0; node < size; node++)
      if (foundation[node]) {
        pillar[node] = true;
        strength[node] = reach[node];
        pillarQueue[tail++] = node;
      }
    while (head < tail) {
      int below = pillarQueue[head++], up = above[below];
      if (up < 0) continue;
      if (!pillar[up]) {
        pillar[up] = true;
        strength[up] = Math.max(reach[up], strength[below] - 1);
        pillarQueue[tail++] = up;
      } else strength[up] = Math.max(strength[up], Math.max(reach[up], strength[below] - 1));
    }
    // Lower pillar seeds can be improved by stronger nearby pillars. Immutable bucket entries
    // preserve stale queues.
    int[][] buckets = new int[maximum + 1][];
    int[] counts = new int[maximum + 1];
    for (int node = 0; node < size; node++)
      if (strength[node] >= 0) enqueue(buckets, counts, strength[node], node);
    for (int score = maximum; score > 0; score--)
      for (int cursor = 0; cursor < counts[score]; cursor++) {
        int node = buckets[score][cursor];
        if (strength[node] != score) continue;
        int candidate = score - 1;
        int up = above[node];
        if (up >= 0 && strength[up] < candidate) {
          strength[up] = candidate;
          enqueue(buckets, counts, candidate, up);
        }
        for (int edge = offsets[node]; edge < offsets[node + 1]; edge++) {
          int neighbor = lateral[edge];
          if (strength[neighbor] < candidate) {
            strength[neighbor] = candidate;
            enqueue(buckets, counts, candidate, neighbor);
          }
        }
      }
    return strength;
  }

  private static void enqueue(int[][] buckets, int[] counts, int score, int node) {
    int[] queue = buckets[score];
    int count = counts[score];
    if (queue == null) buckets[score] = queue = new int[16];
    else if (count == queue.length)
      buckets[score] = queue = Arrays.copyOf(queue, Math.multiplyExact(queue.length, 2));
    queue[count] = node;
    counts[score] = count + 1;
  }
}
