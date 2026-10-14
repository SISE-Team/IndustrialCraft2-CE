package ic2ce.engine.energy;

/**
 * Primitive bounded item-charge arithmetic; positive infinity is an unlimited request, never valid
 * stored charge.
 */
public final class ChargeMath {
  private ChargeMath() {}

  public static double capacity(double value) {
    return Double.isFinite(value) && value > 0 ? value : 0;
  }

  public static double stored(double value, double capacity) {
    return Double.isFinite(value) ? Math.clamp(value, 0, capacity(capacity)) : 0;
  }

  public static double request(double amount, double limit, boolean unlimited) {
    if (Double.isNaN(amount) || amount < 0) return 0;
    if (unlimited) return amount;
    if (Double.isNaN(limit) || limit < 0) return 0;
    return Math.min(amount, limit);
  }

  public static double charge(
      double stored, double capacity, double requested, double limit, boolean unlimited) {
    double maximum = capacity(capacity), present = stored(stored, maximum);
    return Math.min(request(requested, limit, unlimited), maximum - present);
  }

  public static double discharge(
      double stored, double capacity, double requested, double limit, boolean unlimited) {
    return Math.min(request(requested, limit, unlimited), stored(stored, capacity));
  }

  public static int displayDamage(double charge, double capacity, int durability) {
    if (durability < 2 || capacity(capacity) <= 0) return 0;
    int last = durability - 1;
    return Math.clamp(last - (int) (stored(charge, capacity) / capacity * last), 0, last);
  }
}
