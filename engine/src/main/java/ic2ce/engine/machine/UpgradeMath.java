package ic2ce.engine.machine;

/** Pure upgrade arithmetic, independent of stack types and loader callbacks. */
public final class UpgradeMath {
  private UpgradeMath() {}

  public record Rates(int operations, int ticks, int demand, int capacity, int tier) {}

  public static int operations(int length, int addedTime, double timeMultiplier) {
    if (length == 0) return 64;
    double scaled = ((double) length + addedTime) * 64 * timeMultiplier;
    return (int) Math.min(Math.ceil(64 / scaled), 2.147483647E9);
  }

  public static int ticks(int length, int addedTime, double timeMultiplier) {
    if (length == 0) return 1;
    double scaled = ((double) length + addedTime) * 64 * timeMultiplier;
    return Math.max(
        1, (int) Math.round(scaled * operations(length, addedTime, timeMultiplier) / 64));
  }

  public static int modified(int base, long added, double multiplier) {
    double rounded = Math.round(((double) base + added) * multiplier);
    return rounded > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(0, rounded);
  }

  public static Rates rates(
      int length,
      int demand,
      int storage,
      int tier,
      int addedTime,
      double timeMultiplier,
      int addedDemand,
      double demandMultiplier,
      int addedStorage,
      double storageMultiplier,
      int addedTier) {
    int ticks = ticks(length, addedTime, timeMultiplier);
    int energy = modified(demand, addedDemand, demandMultiplier);
    return new Rates(
        operations(length, addedTime, timeMultiplier),
        ticks,
        energy,
        modified(storage, (long) addedStorage + (long) ticks * energy, storageMultiplier),
        modified(tier, addedTier, 1));
  }
}
