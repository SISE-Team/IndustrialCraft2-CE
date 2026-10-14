package ic2ce.engine.machine;

/** Fractional experience conservation with bounded payouts and a representable saved balance. */
public final class ExperienceAccount {
  private ExperienceAccount() {}

  public static double normalize(double balance) {
    return Double.isFinite(balance) ? Math.clamp(balance, 0, Integer.MAX_VALUE) : 0;
  }

  public static double earn(double balance, double earned) {
    return normalize(balance)
        + (Double.isFinite(earned) && earned > 0
            ? Math.min(earned, Integer.MAX_VALUE - normalize(balance))
            : 0);
  }

  public static int claimable(double balance, int maximum) {
    if (maximum < 0) throw new IllegalArgumentException("Negative experience payout limit");
    return (int) Math.min(Math.floor(normalize(balance)), maximum);
  }

  public static double afterClaim(double balance, int paid) {
    double normalized = normalize(balance);
    if (paid < 0 || paid > normalized)
      throw new IllegalArgumentException("Experience payout exceeds balance");
    return normalized - paid;
  }
}
