package ic2ce.neoforge.menu;

import java.util.*;

/**
 * Immutable, ordered native field names; subclasses cannot mutate another menu's synchronization
 * plan.
 */
public final class MenuFields {
  private MenuFields() {}

  public static List<String> extend(List<String> parent, String... added) {
    var fields = new LinkedHashSet<String>(Objects.requireNonNull(parent));
    for (var name : added) {
      if (name == null || name.isBlank() || name.length() > 256)
        throw new IllegalArgumentException("Invalid native menu field name");
      fields.add(name);
    }
    return List.copyOf(fields);
  }
}
