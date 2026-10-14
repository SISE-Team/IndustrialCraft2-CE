package ic2ce.engine.fluid;

/** Bounded integer quantity arithmetic shared by native fluid storage adapters. */
public final class FluidAmounts {
  private FluidAmounts() {}

  public static int fill(int capacity, int stored, int requested) {
    return requested <= 0 ? 0 : Math.min(requested, Math.max(0, capacity - stored));
  }

  public static int drain(int stored, int requested) {
    return requested <= 0 ? 0 : Math.min(requested, Math.max(0, stored));
  }
}
