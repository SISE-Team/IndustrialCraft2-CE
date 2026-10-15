package ic2ce.neoforge.component;

import ic2ce.neoforge.transport.cover.Covers;
import ic2ce.neoforge.reactor.FluidCoreLookup;
import ic2ce.neoforge.world.surface.SurfaceMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Bidirectional component registration publishes one immutable generation after all conflicts are
 * checked.
 */
public final class ComponentTypes {
  private record Registry(
      Map<String, Class<? extends NativeComponent>> names,
      Map<Class<? extends NativeComponent>, String> types) {}

  private static volatile Registry current = new Registry(Map.of(), Map.of());

  private ComponentTypes() {}

  public static void init() {
    register(ElectricalBuffer.class, "energy");
    register(FluidPorts.class, "fluid");
    register(FluidCoreLookup.class, "fluidReactorLookup");
    register(SurfaceMap.class, "obscuration");
    register(AddonProcessor.class, "process");
    register(SignalInput.class, "redstone");
    register(DirectSignalOutput.class, "redstoneEmitter");
    register(AnalogSignalOutput.class, "comparatorEmitter");
    register(Covers.class, "covers");
  }

  public static synchronized void register(Class<? extends NativeComponent> type, String id) {
    Objects.requireNonNull(type);
    Objects.requireNonNull(id);
    if (id.isBlank() || id.length() > 128)
      throw new IllegalArgumentException("Invalid component identifier");
    var previous = current;
    if (previous.names.containsKey(id))
      throw new IllegalStateException("Duplicate component identifier: " + id);
    if (previous.types.containsKey(type))
      throw new IllegalStateException("Duplicate component class: " + type.getName());
    var names = new HashMap<>(previous.names);
    var types = new HashMap<>(previous.types);
    names.put(id, type);
    types.put(type, id);
    current = new Registry(Map.copyOf(names), Map.copyOf(types));
  }

  @SuppressWarnings("unchecked")
  public static <T extends NativeComponent> Class<T> getClass(String id) {
    return id == null ? null : (Class<T>) current.names.get(id);
  }

  public static String getId(Class<? extends NativeComponent> type) {
    return type == null ? null : current.types.get(type);
  }
}
