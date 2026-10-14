package ic2ce.api.crops;

import java.util.Objects;

/**
 * A seed registration is a complete immutable genotype, including the valid germination age zero.
 */
public final class BaseSeed {
  public final CropCard crop;
  public final int size, statGrowth, statGain, statResistance;

  public BaseSeed(CropCard crop, int age, int growth, int gain, int resistance) {
    this.crop = Objects.requireNonNull(crop);
    if (age < 0 || age > crop.getMaxAge())
      throw new IllegalArgumentException("Seed age outside the crop lifecycle");
    size = age;
    statGrowth = trait(growth);
    statGain = trait(gain);
    statResistance = trait(resistance);
  }

  private static int trait(int value) {
    if (value < 0 || value > 31) throw new IllegalArgumentException("Seed trait outside 0..31");
    return value;
  }

  @Deprecated
  public BaseSeed(
      CropCard crop, int age, int growth, int gain, int resistance, int ignoredStackSize) {
    this(crop, age, growth, gain, resistance);
  }
}
