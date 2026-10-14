package ic2ce.engine.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Ordered recipe selection with immutable generations and dense item keys. Readers never lock a
 * warm candidate bucket. Predicates remain live queries; only a caller-certified item-membership
 * test may exclude a candidate.
 */
public final class OrderedRecipeIndex<Q, R> {
  private record Entry<R>(R recipe, int[] directKeys) {}

  private static final class Generation<R> {
    final List<Entry<R>> entries;
    final Object epoch;
    final AtomicReferenceArray<List<R>> buckets;

    Generation(List<Entry<R>> entries, Object epoch, int capacity) {
      this.entries = entries;
      this.epoch = epoch;
      this.buckets = new AtomicReferenceArray<>(capacity);
    }
  }

  private int capacity;
  private final ToIntFunction<Q> key;
  private final BiPredicate<R, Q> mayMatchItem;
  private final BiPredicate<R, Q> matches;
  private final boolean directFirst;
  private volatile Generation<R> generation;

  public OrderedRecipeIndex(
      int capacity,
      ToIntFunction<Q> key,
      BiPredicate<R, Q> mayMatchItem,
      BiPredicate<R, Q> matches) {
    this(capacity, key, mayMatchItem, matches, true);
  }

  public OrderedRecipeIndex(
      int capacity,
      ToIntFunction<Q> key,
      BiPredicate<R, Q> mayMatchItem,
      BiPredicate<R, Q> matches,
      boolean directFirst) {
    if (capacity < 1) throw new IllegalArgumentException("Empty key domain");
    this.capacity = capacity;
    this.key = Objects.requireNonNull(key);
    this.mayMatchItem = Objects.requireNonNull(mayMatchItem);
    this.matches = Objects.requireNonNull(matches);
    this.directFirst = directFirst;
    generation = new Generation<>(List.of(), null, capacity);
  }

  /** Direct entries have precedence over deferred entries, each in insertion order. */
  public synchronized void add(R recipe, int[] directKeys) {
    Objects.requireNonNull(recipe);
    int[] keys = directKeys == null ? null : directKeys.clone();
    if (keys != null)
      for (int value : keys) {
        if (value < 0) throw new IllegalArgumentException("Negative registry key");
        capacity = Math.max(capacity, Math.addExact(value, 1));
      }
    var old = generation;
    var entries = new ArrayList<>(old.entries);
    entries.add(new Entry<>(recipe, keys));
    generation = new Generation<>(List.copyOf(entries), old.epoch, capacity);
  }

  public synchronized void removeIf(Predicate<R> selector) {
    var old = generation;
    var entries = new ArrayList<>(old.entries);
    entries.removeIf(entry -> selector.test(entry.recipe()));
    generation = new Generation<>(List.copyOf(entries), old.epoch, capacity);
  }

  public R find(Q input, Object epoch) {
    int item = key.applyAsInt(input);
    if (item < 0) throw new IllegalArgumentException("Negative registry key");
    Generation<R> state = generation;
    if (state.epoch != epoch || item >= state.buckets.length()) state = refreshEpoch(epoch, item);
    List<R> candidates = state.buckets.get(item);
    if (candidates == null) {
      var selected = new ArrayList<R>();
      if (directFirst) {
        for (Entry<R> entry : state.entries) {
          int[] keys = entry.directKeys();
          if (keys != null && contains(keys, item)) selected.add(entry.recipe());
        }
        for (Entry<R> entry : state.entries) {
          if (entry.directKeys() == null && mayMatchItem.test(entry.recipe(), input))
            selected.add(entry.recipe());
        }
      } else {
        for (Entry<R> entry : state.entries) {
          int[] keys = entry.directKeys();
          if (keys == null ? mayMatchItem.test(entry.recipe(), input) : contains(keys, item))
            selected.add(entry.recipe());
        }
      }
      List<R> built = List.copyOf(selected);
      if (state.buckets.compareAndSet(item, null, built)) candidates = built;
      else candidates = state.buckets.get(item);
    }
    for (int i = 0; i < candidates.size(); i++) {
      R recipe = candidates.get(i);
      if (matches.test(recipe, input)) return recipe;
    }
    return null;
  }

  private synchronized Generation<R> refreshEpoch(Object epoch, int key) {
    var state = generation;
    if (key >= capacity)
      capacity = Math.max(Math.addExact(key, 1), Math.multiplyExact(capacity, 2));
    if (state.epoch != epoch || key >= state.buckets.length())
      generation = state = new Generation<>(state.entries, epoch, capacity);
    return state;
  }

  private static boolean contains(int[] keys, int value) {
    for (int key : keys) if (key == value) return true;
    return false;
  }
}
