package ic2ce.engine.recipe;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Once-only value publication. Failed resolution remains retryable and null can be a resolved
 * value.
 */
public final class DeferredValue<T> implements Supplier<T> {
  private record Resolved<T>(T value) {}

  private volatile Resolved<T> result;
  private Supplier<? extends T> supplier;
  private boolean resolving;

  public DeferredValue(Supplier<? extends T> supplier) {
    this.supplier = Objects.requireNonNull(supplier);
  }

  @Override
  public T get() {
    var current = result;
    if (current != null) return current.value;
    synchronized (this) {
      current = result;
      if (current != null) return current.value;
      if (resolving) throw new IllegalStateException("Recursive deferred value resolution");
      resolving = true;
      try {
        var value = supplier.get();
        result = new Resolved<>(value);
        supplier = null;
        return value;
      } finally {
        resolving = false;
      }
    }
  }
}
