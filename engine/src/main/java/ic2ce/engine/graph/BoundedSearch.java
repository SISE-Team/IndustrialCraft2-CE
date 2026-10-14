package ic2ce.engine.graph;

import java.util.Arrays;
import java.util.OptionalLong;
import java.util.function.LongToIntFunction;

/**
 * Reusable breadth-first workspace for bounded six-neighbor regions; identifiers may use all long
 * bits.
 */
public final class BoundedSearch {
  @FunctionalInterface
  public interface Neighbors {
    int write(long node, long[] destination);
  }

  private final long[] queue, keys, adjacent = new long[6];
  private final int[] stamps;
  private int generation;

  public BoundedSearch(int maximumNodes) {
    if (maximumNodes < 1 || maximumNodes > 4096)
      throw new IllegalArgumentException("Invalid bounded region size");
    queue = new long[maximumNodes];
    int table = 16;
    while (table < maximumNodes * 16) table <<= 1;
    keys = new long[table];
    stamps = new int[table];
  }

  private boolean firstVisit(long key) {
    long hash = key;
    hash = (hash ^ hash >>> 33) * 0xff51afd7ed558ccdl;
    hash ^= hash >>> 33;
    int index = (int) hash & keys.length - 1;
    while (stamps[index] == generation) {
      if (keys[index] == key) return false;
      index = index + 1 & keys.length - 1;
    }
    stamps[index] = generation;
    keys[index] = key;
    return true;
  }

  /** Classifier: negative rejects the node, zero traverses it, positive accepts a target. */
  public OptionalLong find(long start, LongToIntFunction classifier, Neighbors neighbors) {
    if (++generation <= 0) {
      Arrays.fill(stamps, 0);
      generation = 1;
    }
    firstVisit(start);
    int kind = classifier.applyAsInt(start);
    if (kind < 0) return OptionalLong.empty();
    if (kind > 0) return OptionalLong.of(start);
    int head = 0, tail = 1;
    queue[0] = start;
    while (head < tail) {
      int count = neighbors.write(queue[head++], adjacent);
      if (count < 0 || count > adjacent.length)
        throw new IllegalArgumentException("Region has more than six neighbors");
      for (int index = 0; index < count; index++) {
        long node = adjacent[index];
        if (!firstVisit(node)) continue;
        kind = classifier.applyAsInt(node);
        if (kind > 0) return OptionalLong.of(node);
        if (kind == 0 && tail < queue.length) queue[tail++] = node;
      }
    }
    return OptionalLong.empty();
  }
}
