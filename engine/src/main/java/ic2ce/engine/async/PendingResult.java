package ic2ce.engine.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Single completion/cancellation state with standard Future visibility semantics. */
public class PendingResult<V> implements Future<V> {
  private final CompletableFuture<V> completion = new CompletableFuture<>();

  public CompletableFuture<V> completion() {
    return completion;
  }

  public boolean complete(V value) {
    return completion.complete(value);
  }

  public boolean fail(Throwable error) {
    return completion.completeExceptionally(error);
  }

  @Override
  public boolean cancel(boolean interrupt) {
    return completion.cancel(interrupt);
  }

  @Override
  public boolean isCancelled() {
    return completion.isCancelled();
  }

  @Override
  public boolean isDone() {
    return completion.isDone();
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    return completion.get();
  }

  @Override
  public V get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return timeout < 0 ? get() : completion.get(timeout, unit);
  }
}
