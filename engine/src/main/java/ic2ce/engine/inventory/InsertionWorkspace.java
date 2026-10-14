package ic2ce.engine.inventory;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/** Reusable quantity-only insertion simulation; it never edits caller-owned stacks. */
public final class InsertionWorkspace<S> {
  private final Object[] kinds;
  private final int[] counts, initial;
  private final IntFunction<S> slots;
  private final ToIntFunction<S> quantity, maximum;
  private final BiPredicate<S, S> same;

  public InsertionWorkspace(
      int size,
      IntFunction<S> slots,
      ToIntFunction<S> quantity,
      ToIntFunction<S> maximum,
      BiPredicate<S, S> same) {
    if (size < 0) throw new IllegalArgumentException("Negative slot count");
    kinds = new Object[size];
    counts = new int[size];
    initial = new int[size];
    this.slots = Objects.requireNonNull(slots);
    this.quantity = Objects.requireNonNull(quantity);
    this.maximum = Objects.requireNonNull(maximum);
    this.same = Objects.requireNonNull(same);
  }

  public int prepare(Collection<S> incoming, int slotLimit) {
    if (slotLimit < 1) throw new IllegalArgumentException("Invalid slot limit");
    for (int i = 0; i < kinds.length; i++) {
      S value = slots.apply(i);
      kinds[i] = value;
      initial[i] = counts[i] = Math.max(0, quantity.applyAsInt(value));
    }
    long remaining = 0;
    if (incoming != null)
      for (S value : incoming) {
        int amount = Math.max(0, quantity.applyAsInt(value));
        int limit = Math.min(slotLimit, Math.max(0, maximum.applyAsInt(value)));
        for (int pass = 0; pass < 2 && amount > 0; pass++) {
          for (int i = 0; i < counts.length && amount > 0; i++) {
            if (pass == 0 ? counts[i] == 0 || !same.test(value, kind(i)) : counts[i] != 0) continue;
            int room = Math.max(0, limit - counts[i]);
            int accepted = Math.min(room, amount);
            if (accepted == 0) continue;
            if (counts[i] == 0) kinds[i] = value;
            counts[i] += accepted;
            amount -= accepted;
          }
        }
        remaining += amount;
      }
    return (int) Math.min(Integer.MAX_VALUE, remaining);
  }

  public int count(int slot) {
    return counts[slot];
  }

  public boolean changed(int slot) {
    return initial[slot] != counts[slot];
  }

  @SuppressWarnings("unchecked")
  public S kind(int slot) {
    return (S) kinds[slot];
  }
}
