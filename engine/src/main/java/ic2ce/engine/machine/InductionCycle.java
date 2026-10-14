package ic2ce.engine.machine;

/** Coupled heater and two-lane furnace progress with an explicit per-tick energy bill. */
public final class InductionCycle {
  public static final int MAX_HEAT = 10000, COMPLETION = 4000;
  private int heat, progress;
  private boolean working, heated;

  public void restore(int heat, int progress) {
    this.heat = ThermalRamp.normalize(heat, MAX_HEAT);
    this.progress = Math.clamp(progress, 0, COMPLETION + MAX_HEAT / 30);
  }

  public boolean completionDue() {
    return progress >= COMPLETION;
  }

  public void complete() {
    progress = 0;
  }

  public int heat() {
    return heat;
  }

  public int progress() {
    return progress;
  }

  public boolean working() {
    return working;
  }

  public boolean heated() {
    return heated;
  }

  public int advance(boolean recipeAvailable, boolean hold, double availableEnergy) {
    if (!Double.isFinite(availableEnergy) || availableEnergy < 0)
      throw new IllegalArgumentException("Invalid induction energy budget");
    int bill = 0;
    heated = (recipeAvailable || hold) && availableEnergy >= 1;
    if (heated) {
      bill = 1;
      heat = Math.min(MAX_HEAT, heat + 1);
    } else heat = Math.max(0, heat - 4);
    working = recipeAvailable && availableEnergy - bill >= 15;
    if (!recipeAvailable) progress = 0;
    else if (working) {
      progress += heat / 30;
      bill += 15;
    }
    return bill;
  }
}
