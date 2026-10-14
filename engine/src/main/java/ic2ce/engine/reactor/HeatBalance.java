package ic2ce.engine.reactor;

/**
 * Exact capacity-weighted heat exchange, bounded by source heat, destination space and transfer
 * rate.
 */
public final class HeatBalance {
  private HeatBalance() {}

  public static int transfer(
      int sourceHeat, int sourceCapacity, int targetHeat, int targetCapacity, int rate) {
    if (sourceCapacity <= 0 || targetCapacity <= 0 || rate <= 0) return 0;
    int source = Math.clamp(sourceHeat, 0, sourceCapacity),
        target = Math.clamp(targetHeat, 0, targetCapacity);
    long difference = (long) source * targetCapacity - (long) target * sourceCapacity;
    long desired = difference / ((long) sourceCapacity + targetCapacity);
    if (desired > 0)
      return (int) Math.min(desired, Math.min(rate, Math.min(source, targetCapacity - target)));
    if (desired < 0)
      return -(int) Math.min(-desired, Math.min(rate, Math.min(target, sourceCapacity - source)));
    return 0;
  }

  public static int triangular(int pulses) {
    if (pulses <= 0) return 0;
    return (int) Math.min(Integer.MAX_VALUE, (long) pulses * (pulses + 1L) / 2);
  }
}
