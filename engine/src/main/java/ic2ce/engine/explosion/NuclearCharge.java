package ic2ce.engine.explosion;

import java.util.Objects;

/**
 * Distinct isotope payload policies preserve physical detonator thresholds and bounded radiation
 * arithmetic.
 */
public final class NuclearCharge {
  private NuclearCharge() {}

  public enum Payload {
    NONE,
    URANIUM_238,
    URANIUM_BLOCK,
    SMALL_URANIUM_235,
    URANIUM_235,
    SMALL_PLUTONIUM,
    PLUTONIUM
  }

  public record Profile(float power, int radiationRange) {
    public boolean armed() {
      return power >= 0;
    }
  }

  public static Profile evaluate(int detonators, Payload payload, int quantity, float limit) {
    Objects.requireNonNull(payload);
    if (detonators <= 0 || quantity < 0 || !Float.isFinite(limit) || limit < 0)
      return new Profile(-1, 0);
    double power = 5 * Math.cbrt(detonators);
    int multiplier = 0;
    if (quantity > 0)
      switch (payload) {
        case NONE -> {}
        case URANIUM_238 -> multiplier = 1;
        case URANIUM_BLOCK -> multiplier = 6;
        case SMALL_URANIUM_235 -> {
          multiplier = 2;
          if (detonators >= 64) power += Math.pow(quantity, 1.6) / 18;
        }
        case URANIUM_235 -> {
          multiplier = 2;
          if (detonators >= 32) power += Math.pow(quantity, 1.4) / 2;
        }
        case SMALL_PLUTONIUM -> {
          multiplier = 3;
          if (detonators >= 32) power += (double) quantity * quantity / 18;
        }
        case PLUTONIUM -> {
          multiplier = 4;
          if (detonators >= 16) power += Math.pow(quantity, 1.8) / 2;
        }
      }
    return new Profile(
        (float) Math.min(power, limit),
        (int) Math.min(Integer.MAX_VALUE, (long) detonators * multiplier));
  }
}
