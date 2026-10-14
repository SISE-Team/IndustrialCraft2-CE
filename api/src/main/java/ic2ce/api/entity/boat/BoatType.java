package ic2ce.api.entity.boat;

import java.util.*;
import java.util.stream.Stream;
import net.minecraft.world.item.Item;

/** Stable named native hull descriptors publish immutable registration generations. */
public final class BoatType {
  private static volatile Map<String, BoatType> types = Map.of();
  private final String name;
  private final Item base;

  private BoatType(Item base, String name) {
    this.base = base;
    this.name = name;
  }

  public static synchronized BoatType register(Item base, String name) {
    if (name == null || name.isBlank()) throw new IllegalArgumentException("Unnamed boat hull");
    var prior = types.get(name);
    if (prior != null) {
      if (prior.base != base)
        throw new IllegalArgumentException("Boat hull already registered: " + name);
      return prior;
    }
    var result = new BoatType(base, name);
    var next = new LinkedHashMap<>(types);
    next.put(name, result);
    types = Collections.unmodifiableMap(next);
    return result;
  }

  public static Stream<BoatType> stream() {
    return types.values().stream();
  }

  public String getName() {
    return name;
  }

  public Item getBaseItem() {
    return base;
  }
}
