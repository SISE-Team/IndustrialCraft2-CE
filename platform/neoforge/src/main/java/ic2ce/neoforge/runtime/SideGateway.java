package ic2ce.neoforge.runtime;

import java.util.Objects;
import java.util.function.Supplier;

/** Constructs only the side services available on this physical runtime. */
public final class SideGateway<T> {
  private final T server, client;

  public SideGateway(Supplier<? extends T> serverFactory, Supplier<? extends T> clientFactory) {
    server = Objects.requireNonNull(serverFactory.get());
    client =
        ProjectRuntime.envProxy.isClientEnv() ? Objects.requireNonNull(clientFactory.get()) : null;
  }

  public T get(boolean simulating) {
    return simulating ? server : client;
  }
}
