package ic2ce.neoforge.weather;

import ic2ce.neoforge.runtime.AutomationActors;
import java.util.*;
import net.minecraft.server.level.ServerLevel;

/** Actual native wind drift remains within degrees even when repeatedly crossing zero. */
public final class WeatherContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run(ServerLevel world) {
    var failures = new ArrayList<String>();
    var wind = new WorldWind(world);
    boolean valid = true;
    for (int step = 0; step < 20000; step++) {
      wind.updateWind();
      valid &= wind.getWindDirection() >= 0 && wind.getWindDirection() < 360;
    }
    if (!valid)
      failures.add("native wind: degree wrapping escaped [0,360) during actual world-random drift");
    if (!Double.isFinite(wind.getWindAt(128))
        || wind.getWindAt(Double.NaN) != 0
        || wind.getWindAt(Double.POSITIVE_INFINITY) != 0)
      failures.add("native wind: malformed altitude produced nonfinite generator input");
    var first = AutomationActors.get(world);
    var second = AutomationActors.get(world);
    if (first != second || first.level() != world)
      failures.add(
          "native automation actor: repeated dimension identity does not resolve the actual native"
              + " world's cached player");
    return new Result(3, List.copyOf(failures));
  }
}
