package ic2ce.engine.inventory;

/** Pure nominal wear capacity; prediction never consumes randomness or items. */
public final class DurabilityBudget {
  private DurabilityBudget() {}

  public static int available(int count, int damage, int maximum) {
    if (count <= 0 || maximum <= 0) return 0;
    long capacity = Math.max(0, maximum - (long) damage) + (count - 1L) * maximum;
    return (int) Math.min(Integer.MAX_VALUE, capacity);
  }
}
