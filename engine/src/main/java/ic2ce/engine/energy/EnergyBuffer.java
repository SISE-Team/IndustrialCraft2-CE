package ic2ce.engine.energy;

/** Primitive energy ledger. Capacity changes retain stored work and discrete overflow. */
public final class EnergyBuffer {
  private double capacity, stored;

  public EnergyBuffer(double capacity) {
    setCapacity(capacity);
  }

  public double capacity() {
    return capacity;
  }

  public double stored() {
    return stored;
  }

  public double free() {
    return Math.max(0, capacity - stored);
  }

  public void setCapacity(double value) {
    nonnegativeFinite(value);
    capacity = value;
  }

  public void restoreStored(double value) {
    stored = Double.isFinite(value) ? Math.max(0, value) : 0;
  }

  public double add(double delta) {
    if (!Double.isFinite(delta)) throw new IllegalArgumentException("Non-finite energy delta");
    double accepted = delta >= 0 ? Math.min(delta, free()) : -Math.min(-delta, stored);
    stored += accepted;
    return accepted;
  }

  public void forceDelta(double delta) {
    if (!Double.isFinite(delta)) throw new IllegalArgumentException("Non-finite energy delta");
    stored = Math.clamp(stored + delta, 0, Double.MAX_VALUE);
  }

  public boolean canSpend(double amount) {
    nonnegativeFinite(amount);
    return stored >= amount;
  }

  public boolean spendExact(double amount) {
    if (!canSpend(amount)) return false;
    stored -= amount;
    return true;
  }

  public double withdraw(double amount, boolean simulate) {
    nonnegativeFinite(amount);
    double accepted = Math.min(amount, stored);
    if (!simulate) stored -= accepted;
    return accepted;
  }

  private static void nonnegativeFinite(double value) {
    if (!Double.isFinite(value) || value < 0)
      throw new IllegalArgumentException("Invalid energy quantity: " + value);
  }
}
