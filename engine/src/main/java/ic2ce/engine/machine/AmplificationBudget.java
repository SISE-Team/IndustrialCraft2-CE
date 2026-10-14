package ic2ce.engine.machine;

/** Amplifier credit is spent at full precision, including fractional incoming energy packets. */
public final class AmplificationBudget {
  private double available;

  public double available() {
    return available;
  }

  public void restore(double value) {
    available = Double.isFinite(value) ? Math.clamp(value, 0, Integer.MAX_VALUE) : 0;
  }

  public void add(int amount) {
    if (amount <= 0) throw new IllegalArgumentException("Nonpositive amplifier value");
    available = Math.min(Integer.MAX_VALUE, available + amount);
  }

  public double consume(double incoming) {
    if (!Double.isFinite(incoming) || incoming <= 0) return 0;
    double spent = Math.min(available, incoming);
    available -= spent;
    return spent;
  }
}
