package ic2ce.engine.machine;

/** Pressure-dependent sensible/latent heat, metered feed and emergency water quenching. */
public final class BoilerThermodynamics {
  public record Step(double temperature, int boiledMb, int consumedMb, boolean bypassWater) {}

  private static final double DEGREE_PER_HU = 0.0005;

  private BoilerThermodynamics() {}

  public static double target(int pressure) {
    return 100 + Math.clamp(pressure, 0, 300) / 220.0 * 274;
  }

  public static double huPerMb(int pressure) {
    return 100 + Math.clamp(pressure, 0, 300) / 220.0 * 100;
  }

  public static Step advance(double temperature, int inputHu, int pressure, int availableWater) {
    if (!Double.isFinite(temperature)) temperature = 0;
    temperature = Math.max(0, temperature);
    int supplied = Math.clamp(inputHu, 0, 1200), feed = Math.clamp(availableWater, 0, 1000);
    if (supplied == 0) return new Step(temperature, 0, 0, false);
    if (feed == 0) return new Step(temperature + supplied * DEGREE_PER_HU, 0, 0, false);
    double required = Math.max(0, target(pressure) - temperature);
    int warm =
        required <= 0.0001 ? 0 : (int) Math.min(supplied, Math.ceil(required / DEGREE_PER_HU));
    temperature += warm * DEGREE_PER_HU;
    if (warm == supplied)
      return new Step(temperature, 0, 0, pressure == 0 && temperature < 99.9999);
    double remaining = supplied - warm;
    double stored =
        Math.min(Math.max(0, (temperature - target(pressure)) / DEGREE_PER_HU), 1200 - supplied);
    int boiled = Math.min(feed, (int) Math.floor((remaining + stored) / huPerMb(pressure)));
    remaining -= boiled * huPerMb(pressure);
    if (remaining < 0) temperature += remaining * DEGREE_PER_HU;
    int quenched = 0;
    if (temperature - target(pressure) >= 0.1001) {
      quenched =
          Math.min(feed, Math.min(20, (int) Math.floor((temperature - target(pressure)) / 0.1)));
      temperature -= quenched * 0.1;
    }
    if (remaining > 0) temperature += remaining * DEGREE_PER_HU;
    return new Step(temperature, boiled, Math.max(boiled, quenched), false);
  }
}
