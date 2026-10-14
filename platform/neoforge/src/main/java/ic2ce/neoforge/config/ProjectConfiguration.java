package ic2ce.neoforge.config;

import ic2ce.core.util.*;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Complete configuration generations are published after successful load; disk saves replace a
 * complete temporary file.
 */
public final class ProjectConfiguration {
  public static volatile boolean ignoreInvalidRecipes;

  private record Generation(Config current, Config defaults) {}

  private static volatile Generation generation;

  private ProjectConfiguration() {}

  public static synchronized void load() {
    var current = new Config("IndustrialCraft2-CE configuration");
    var defaults = new Config("IndustrialCraft2-CE default configuration");
    try (var source =
        Objects.requireNonNull(
            ProjectRuntime.class.getResourceAsStream("/assets/ic2/config/general.ini"),
            "Missing project configuration material")) {
      byte[] material = source.readAllBytes();
      current.load(new ByteArrayInputStream(material));
      defaults.load(new ByteArrayInputStream(material));
      var file = getFile();
      if (file.isFile()) current.load(file);
    } catch (IOException error) {
      throw new UncheckedIOException("Cannot load IndustrialCraft2-CE configuration", error);
    }
    upgrade(current);
    save(current);
    boolean ignore = NativeConfigValues.getBool(current, "recipes/ignoreInvalidRecipes");
    generation = new Generation(current, defaults);
    ignoreInvalidRecipes = ignore;
  }

  private static Generation ready() {
    var current = generation;
    if (current == null)
      throw new IllegalStateException("IndustrialCraft2-CE configuration has not loaded");
    return current;
  }

  public static Config get() {
    return ready().current();
  }

  public static Config.Value getDefault(String key) {
    return ready().defaults().get(key);
  }

  public static Iterator<Config.Value> getDefaults(String section) {
    return ready().defaults().getSub(section).valueIterator();
  }

  public static synchronized void save() {
    save(ready().current());
  }

  private static void save(Config values) {
    var target = getFile().toPath().toAbsolutePath();
    Path temporary = null;
    try {
      Files.createDirectories(target.getParent());
      temporary = Files.createTempFile(target.getParent(), ".ic2ce-config-", ".tmp");
      values.save(temporary.toFile());
      try {
        Files.move(
            temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException unavailable) {
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException error) {
      throw new UncheckedIOException("Cannot save IndustrialCraft2-CE configuration", error);
    } finally {
      if (temporary != null)
        try {
          Files.deleteIfExists(temporary);
        } catch (IOException ignored) {
        }
    }
  }

  private static File getFile() {
    return new File(new File(ProjectRuntime.sideProxy.getMinecraftDir(), "config"), "IC2.ini");
  }

  private static void upgrade(Config values) {
    for (var ore : List.of("copper", "tin", "uranium", "lead")) {
      var old = values.remove("worldgen/" + ore + "Ore");
      if (old != null && !old.getBool()) {
        var enabled = values.get("worldgen/" + ore + "/enabled");
        if (enabled != null) enabled.set(false);
      }
    }
    var blacklist =
        NativeConfigValues.asList(
            NativeConfigValues.getString(values, "balance/recyclerBlacklist"));
    boolean changed = false;
    for (int index = 0; index < blacklist.size(); index++) {
      String input = blacklist.get(index);
      if (input.equals("IC2:blockScaffold") || input.equals("IC2:scaffold")) {
        blacklist.set(index, "ic2:scaffold");
        changed = true;
      }
    }
    if (changed) values.set("balance/recyclerBlacklist", String.join(", ", blacklist));
    var audio = values.remove("misc/enableIc2Audio");
    if (audio != null) values.get("audio/enabled").set(audio.getBool());
    var limit = values.remove("misc/maxAudioSourceCount");
    if (limit != null) values.get("audio/maxSourceCount").set(limit.getInt());
  }
}
