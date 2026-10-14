package ic2ce.engine.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Finish callbacks publish an immutable invocation snapshot before allowing reentrant mutation. */
public final class CompletionCallbacks {
  private volatile List<Runnable> listeners = List.of();

  public synchronized void add(Runnable action) {
    Objects.requireNonNull(action);
    var next = new ArrayList<>(listeners);
    next.add(action);
    listeners = List.copyOf(next);
  }

  public synchronized void replace(Runnable action) {
    listeners = action == null ? List.of() : List.of(action);
  }

  public synchronized void clear() {
    listeners = List.of();
  }

  public void fire() {
    var snapshot = listeners;
    RuntimeException failure = null;
    for (var action : snapshot)
      try {
        action.run();
      } catch (RuntimeException error) {
        if (failure == null) failure = error;
        else if (error != failure) failure.addSuppressed(error);
      }
    if (failure != null) throw failure;
  }
}
