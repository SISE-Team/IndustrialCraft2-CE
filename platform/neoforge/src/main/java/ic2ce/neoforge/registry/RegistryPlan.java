package ic2ce.neoforge.registry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegisterEvent;

/** A typed, ordered registration journal which seals before native registry writes. */
public final class RegistryPlan<T> {
  private final ResourceKey<? extends Registry<T>> registry;
  private final LinkedHashMap<Identifier, T> entries = new LinkedHashMap<>();
  private boolean sealed;

  public RegistryPlan(ResourceKey<? extends Registry<T>> registry) {
    this.registry = Objects.requireNonNull(registry);
  }

  public synchronized <V extends T> V stage(Identifier identifier, V value) {
    Objects.requireNonNull(identifier);
    Objects.requireNonNull(value);
    if (!identifier.getNamespace().equals("ic2")) {
      throw new IllegalArgumentException("Foreign registration: " + identifier);
    }
    if (sealed)
      throw new IllegalStateException("Registry already sealed: " + registry.identifier());
    if (entries.putIfAbsent(identifier, value) != null) {
      throw new IllegalStateException("Duplicate registration: " + identifier);
    }
    return value;
  }

  public synchronized List<T> values() {
    return List.copyOf(entries.values());
  }

  public void replay(RegisterEvent event) {
    if (!registry.equals(event.getRegistryKey())) return;
    final List<Registration<T>> snapshot;
    synchronized (this) {
      if (sealed)
        throw new IllegalStateException("Repeated registry replay: " + registry.identifier());
      sealed = true;
      snapshot =
          entries.entrySet().stream()
              .map(e -> new Registration<>(e.getKey(), e.getValue()))
              .toList();
    }
    event.register(
        registry,
        helper -> snapshot.forEach(entry -> helper.register(entry.identifier(), entry.value())));
  }

  private record Registration<T>(Identifier identifier, T value) {}
}
