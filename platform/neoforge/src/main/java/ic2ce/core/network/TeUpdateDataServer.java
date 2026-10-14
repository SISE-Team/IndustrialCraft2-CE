package ic2ce.core.network;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;

/** Stable field deltas keyed by logical name, with global values superseding private ones. */
public final class TeUpdateDataServer {
  private final Map<String, String> global = new LinkedHashMap<>();
  private final Map<ServerPlayer, Map<String, String>> privateFields = new IdentityHashMap<>();

  TeUpdateDataServer() {}

  private static String key(String name) {
    int separator = name.indexOf('=');
    String key = separator < 0 ? name : name.substring(0, separator);
    if (key.isEmpty() || key.length() > 64)
      throw new IllegalArgumentException("Invalid synchronized field name");
    return key;
  }

  void addGlobalField(String name) {
    String key = key(name);
    global.put(key, name);
    for (var fields : privateFields.values()) fields.remove(key);
  }

  void addPlayerField(String name, ServerPlayer player) {
    String key = key(name);
    if (!global.containsKey(key))
      privateFields.computeIfAbsent(player, _ -> new LinkedHashMap<>()).put(key, name);
  }

  Collection<String> getGlobalFields() {
    return Collections.unmodifiableCollection(global.values());
  }

  Collection<String> getPlayerFields(ServerPlayer player) {
    var fields = privateFields.get(player);
    return fields == null
        ? java.util.List.of()
        : Collections.unmodifiableCollection(fields.values());
  }
}
