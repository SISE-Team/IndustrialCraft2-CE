package ic2ce.engine.agriculture;

import java.util.Objects;

/**
 * Checked phenotype arithmetic; native worlds, crop callbacks and randomness belong to the adapter.
 */
public final class CropMath {
  private CropMath() {}

  public record Genome(int growth, int gain, int resistance) {
    public Genome {
      if ((growth | gain | resistance) < 0 || growth > 31 || gain > 31 || resistance > 31)
        throw new IllegalArgumentException("Crop traits must be within zero and thirty-one");
    }

    public static Genome bounded(int growth, int gain, int resistance) {
      return new Genome(
          Math.clamp(growth, 0, 31), Math.clamp(gain, 0, 31), Math.clamp(resistance, 0, 31));
    }
  }

  public record Growth(int points, boolean dies) {}

  public static Growth growth(
      Genome genome, int tier, int environmentWeight, int variation, int resistanceDraw) {
    Objects.requireNonNull(genome);
    int points =
        growthPoints(
            genome.growth,
            genome.gain,
            genome.resistance,
            tier,
            environmentWeight,
            variation,
            resistanceDraw);
    return new Growth(Math.max(0, points), points < 0);
  }

  /** Primitive result for native hot paths: negative one denotes death, otherwise earned points. */
  public static int growthPoints(
      int growth,
      int gain,
      int resistance,
      int tier,
      int environmentWeight,
      int variation,
      int resistanceDraw) {
    if ((growth | gain | resistance) < 0 || growth > 31 || gain > 31 || resistance > 31)
      throw new IllegalArgumentException("Crop traits must be within zero and thirty-one");
    if (variation < 0 || variation >= 7 || resistanceDraw < 0 || resistanceDraw >= 32)
      throw new IllegalArgumentException("Invalid native growth random draw");
    long needed = Math.max(0, ((long) tier - 1) * 4 + growth + gain + resistance);
    long provided = (long) environmentWeight * 5;
    long deficit = needed - provided;
    if (deficit > 25 && resistanceDraw > resistance) return -1;
    long percent = provided >= needed ? 100 + provided - needed : Math.max(0, 100 - deficit * 4);
    long points = (3L + variation + growth) * percent / 100;
    return (int) Math.clamp(points, 0, Integer.MAX_VALUE);
  }

  public static int accumulate(int current, int points) {
    if (current < 0 || points < 0) throw new IllegalArgumentException("Negative crop progress");
    return (int) Math.min(Integer.MAX_VALUE, current + (long) points);
  }

  public static Genome inherit(
      Genome[] parents, int growthMutation, int gainMutation, int resistanceMutation) {
    if (parents.length == 0 || parents.length > 4)
      throw new IllegalArgumentException("Breeding requires one to four parents");
    long growth = 0, gain = 0, resistance = 0;
    for (var parent : parents) {
      Objects.requireNonNull(parent);
      growth += parent.growth;
      gain += parent.gain;
      resistance += parent.resistance;
    }
    int count = parents.length;
    if (Math.abs((long) growthMutation) > count
        || Math.abs((long) gainMutation) > count
        || Math.abs((long) resistanceMutation) > count)
      throw new IllegalArgumentException("Mutation exceeds parent bound");
    return Genome.bounded(
        (int) (growth / count) + growthMutation,
        (int) (gain / count) + gainMutation,
        (int) (resistance / count) + resistanceMutation);
  }

  public static int spreadingThreshold(Genome parent) {
    return Math.clamp(
        4
            + (parent.growth >= 16 ? 1 : 0)
            + (parent.growth >= 30 ? 1 : 0)
            + (parent.resistance >= 28 ? 27 - parent.resistance : 0),
        0,
        15);
  }
}
