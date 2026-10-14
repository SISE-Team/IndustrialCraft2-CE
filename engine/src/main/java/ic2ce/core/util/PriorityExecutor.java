package ic2ce.core.util;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * CPU-bound fixed platform workers with stable three-lane FIFO priorities and race-safe executor
 * admission.
 */
public class PriorityExecutor extends ThreadPoolExecutor {
  public PriorityExecutor(int workers) {
    super(
        workers,
        workers,
        0,
        TimeUnit.MILLISECONDS,
        new Lanes<>(),
        Thread.ofPlatform().daemon().name("ic2ce-worker-", 0).factory());
  }

  public enum Priority {
    High,
    Default,
    Low
  }

  public interface CustomPriority {
    Priority getPriority();
  }

  private static Priority priority(Object task) {
    return task instanceof CustomPriority custom
        ? Objects.requireNonNull(custom.getPriority())
        : Priority.Default;
  }

  private static final class Task<T> extends FutureTask<T> implements CustomPriority {
    private final Priority priority;

    Task(Callable<T> callable) {
      super(callable);
      priority = priority(callable);
    }

    Task(Runnable runnable, T result) {
      super(runnable, result);
      priority = priority(runnable);
    }

    @Override
    public Priority getPriority() {
      return priority;
    }
  }

  @Override
  protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
    return new Task<>(Objects.requireNonNull(callable));
  }

  @Override
  protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T result) {
    return new Task<>(Objects.requireNonNull(runnable), result);
  }

  public <T> RunnableFuture<T> makeTask(Callable<T> callable) {
    return newTaskFor(callable);
  }

  public <T> List<? extends Future<T>> submitAll(List<Callable<T>> callables) {
    var tasks = new ArrayList<RunnableFuture<T>>(callables.size());
    for (var callable : List.copyOf(callables)) tasks.add(newTaskFor(callable));
    try {
      executeAll(tasks);
    } catch (RejectedExecutionException error) {
      for (var task : tasks) task.cancel(false);
      throw error;
    }
    return List.copyOf(tasks);
  }

  public void executeAll(List<? extends Runnable> tasks) {
    for (var task : List.copyOf(tasks)) execute(task);
  }

  /**
   * Iterator is a detached snapshot; removal delegates to the queue so cancellation remains
   * effective.
   */
  private static final class Lanes<E> extends AbstractQueue<E> implements BlockingQueue<E> {
    private final ArrayDeque<E> high = new ArrayDeque<>(),
        normal = new ArrayDeque<>(),
        low = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition available = lock.newCondition();
    private int count;

    private ArrayDeque<E> lane(E task) {
      return switch (priority(task)) {
        case High -> high;
        case Default -> normal;
        case Low -> low;
      };
    }

    private E next() {
      var value = high.pollFirst();
      if (value == null) value = normal.pollFirst();
      if (value == null) value = low.pollFirst();
      if (value != null) count--;
      return value;
    }

    @Override
    public boolean offer(E task) {
      Objects.requireNonNull(task);
      var queue = lane(task);
      lock.lock();
      try {
        queue.addLast(task);
        count++;
        available.signal();
        return true;
      } finally {
        lock.unlock();
      }
    }

    @Override
    public void put(E task) {
      offer(task);
    }

    @Override
    public boolean offer(E task, long timeout, TimeUnit unit) {
      Objects.requireNonNull(unit);
      return offer(task);
    }

    @Override
    public E poll() {
      lock.lock();
      try {
        return next();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public E peek() {
      lock.lock();
      try {
        return !high.isEmpty()
            ? high.peekFirst()
            : !normal.isEmpty() ? normal.peekFirst() : low.peekFirst();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public E take() throws InterruptedException {
      lock.lockInterruptibly();
      try {
        while (count == 0) available.await();
        return next();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
      long remaining = Objects.requireNonNull(unit).toNanos(timeout);
      lock.lockInterruptibly();
      try {
        while (count == 0) {
          if (remaining <= 0) return null;
          remaining = available.awaitNanos(remaining);
        }
        return next();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public int size() {
      lock.lock();
      try {
        return count;
      } finally {
        lock.unlock();
      }
    }

    @Override
    public int remainingCapacity() {
      return Integer.MAX_VALUE;
    }

    @Override
    public boolean remove(Object task) {
      lock.lock();
      try {
        boolean removed =
            high.removeFirstOccurrence(task)
                || normal.removeFirstOccurrence(task)
                || low.removeFirstOccurrence(task);
        if (removed) count--;
        return removed;
      } finally {
        lock.unlock();
      }
    }

    @Override
    public boolean contains(Object task) {
      lock.lock();
      try {
        return high.contains(task) || normal.contains(task) || low.contains(task);
      } finally {
        lock.unlock();
      }
    }

    @Override
    public void clear() {
      lock.lock();
      try {
        high.clear();
        normal.clear();
        low.clear();
        count = 0;
      } finally {
        lock.unlock();
      }
    }

    @Override
    public int drainTo(Collection<? super E> destination) {
      return drainTo(destination, Integer.MAX_VALUE);
    }

    @Override
    public int drainTo(Collection<? super E> destination, int maximum) {
      Objects.requireNonNull(destination);
      if (destination == this) throw new IllegalArgumentException("Cannot drain a queue to itself");
      lock.lock();
      try {
        int moved = 0;
        while (moved < maximum && count > 0) {
          var value =
              !high.isEmpty()
                  ? high.peekFirst()
                  : !normal.isEmpty() ? normal.peekFirst() : low.peekFirst();
          destination.add(value);
          next();
          moved++;
        }
        return moved;
      } finally {
        lock.unlock();
      }
    }

    @Override
    public Iterator<E> iterator() {
      var snapshot = new ArrayList<E>();
      lock.lock();
      try {
        snapshot.addAll(high);
        snapshot.addAll(normal);
        snapshot.addAll(low);
      } finally {
        lock.unlock();
      }
      return new Iterator<>() {
        int index;
        E current;
        boolean removable;

        public boolean hasNext() {
          return index < snapshot.size();
        }

        public E next() {
          if (!hasNext()) throw new NoSuchElementException();
          current = snapshot.get(index++);
          removable = true;
          return current;
        }

        public void remove() {
          if (!removable) throw new IllegalStateException();
          Lanes.this.remove(current);
          removable = false;
        }
      };
    }
  }
}
