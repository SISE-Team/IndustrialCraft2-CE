package ic2ce.engine.machine;

/** A reusable processing clock. It owns no world, inventory or energy objects. */
public final class ProcessingCycle {
  public enum Transition {
    IDLE,
    START,
    CONTINUE,
    COMPLETE,
    PAUSE,
    STOP
  }

  private int progress;
  private boolean active;

  public ProcessingCycle(int savedProgress, boolean active) {
    progress = Math.max(0, savedProgress);
    this.active = active;
  }

  public int progress() {
    return progress;
  }

  public boolean active() {
    return active;
  }

  public void restore(int savedProgress, boolean currentActive) {
    progress = Math.max(0, savedProgress);
    active = currentActive;
  }

  public void reset() {
    progress = 0;
  }

  public void rescale(int oldLength, int newLength) {
    if (oldLength < 1 || newLength < 1)
      throw new IllegalArgumentException("Invalid operation length");
    progress = (int) Math.floor((double) progress / oldLength * newLength + .1);
  }

  /** Energy and output acceptance must already have been decided by the owning thread. */
  public Transition advance(boolean recipeAvailable, boolean canRun, int operationLength) {
    if (operationLength < 1) throw new IllegalArgumentException("Invalid operation length");
    if (!canRun) {
      boolean previouslyActive = active;
      active = false;
      if (!recipeAvailable) progress = 0;
      if (!previouslyActive) return Transition.IDLE;
      return progress == 0 ? Transition.STOP : Transition.PAUSE;
    }
    boolean starting = !active;
    active = true;
    if (++progress >= operationLength) {
      progress = 0;
      return Transition.COMPLETE;
    }
    return starting ? Transition.START : Transition.CONTINUE;
  }

  public void stopAfterCompletion() {
    active = false;
  }
}
