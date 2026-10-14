package ic2ce.neoforge.weather;

import ic2ce.core.*;
import ic2ce.engine.weather.WindProfile;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.Objects;
import net.minecraft.world.level.Level;

/**
 * Per-world wind uses that world's random stream, a constant-size altitude curve and bounded degree
 * wrapping.
 */
public class WorldWind {
  private final Level world;
  private final WindProfile altitude;
  private int windStrength, windDirection;
  public int windTicker;

  public WorldWind(Level world) {
    this.world = Objects.requireNonNull(world);
    var random = world.getRandom();
    windStrength = 5 + random.nextInt(20);
    windDirection = random.nextInt(360);
    altitude =
        WindProfile.fromWorld(
            ProjectRuntime.getWorldMaxHeight(world), ProjectRuntime.getSeaLevel(world));
  }

  public void updateWind() {
    if ((windTicker++ & 127) != 0) return;
    var random = world.getRandom();
    int up = windStrength > 20 ? 30 - windStrength : 10,
        down = windStrength < 10 ? windStrength : 10;
    if (random.nextInt(100) < up) windStrength++;
    else if (random.nextInt(100) < down) windStrength--;
    windDirection =
        Math.floorMod(
            windDirection
                + switch (random.nextInt(3)) {
                  case 0 -> -18;
                  case 2 -> 18;
                  default -> 0;
                },
            360);
  }

  public double getWindAt(double height) {
    double weather = world.isThundering() ? 1.5 : world.isRaining() ? 1.25 : 1;
    return windStrength * altitude.at(height) * weather * 2.4;
  }

  public double getMaxWind() {
    return 108;
  }

  public int getWindDirection() {
    return windDirection;
  }
}
