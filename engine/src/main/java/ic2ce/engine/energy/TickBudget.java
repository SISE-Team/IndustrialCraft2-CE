package ic2ce.engine.energy;

/** Non-storable per-tick production; repeated consumers share one conserved budget. */
public final class TickBudget {
  private int remaining;

  public void reset(int produced) {
    remaining = Math.max(0, produced);
  }

  public int available() {
    return remaining;
  }

  public int draw(int requested, boolean simulate) {
    int accepted = Math.min(remaining, Math.max(0, requested));
    if (!simulate) remaining -= accepted;
    return accepted;
  }
}
