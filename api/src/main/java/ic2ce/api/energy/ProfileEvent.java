package ic2ce.api.energy;

import java.util.*;
import net.neoforged.bus.api.Event;

/** Profile events own immutable membership and bounded nonempty selections. */
public abstract class ProfileEvent extends Event {
  private static String name(String name) {
    if (name == null || name.isBlank() || name.length() > 256)
      throw new IllegalArgumentException("Invalid profile name");
    return name;
  }

  public static final class Load extends ProfileEvent {
    public final Set<String> loaded;
    public final String active;

    public Load(Set<String> profiles, String active) {
      var names = new LinkedHashSet<String>();
      for (var profile : Objects.requireNonNull(profiles)) names.add(name(profile));
      loaded = Collections.unmodifiableSet(names);
      this.active = name(active);
      if (!loaded.contains(active))
        throw new IllegalArgumentException("Active profile is not loaded");
    }
  }

  public static final class Switch extends ProfileEvent {
    public final String from, to;

    public Switch(String previous, String next) {
      from = name(previous);
      to = name(next);
    }
  }
}
