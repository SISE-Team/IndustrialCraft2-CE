package ic2ce.api.entity.block;

import java.util.Objects;
import java.util.function.Consumer;

/** Platform explosion behavior supplied explicitly by the owning mod loader. */
public final class ExplosionAccess {
  private static volatile Consumer<ExplosiveEntity> service;

  private ExplosionAccess() {}

  public static synchronized void install(Consumer<ExplosiveEntity> implementation) {
    Objects.requireNonNull(implementation);
    if (service != null && service != implementation)
      throw new IllegalStateException("Explosion platform already installed");
    service = implementation;
  }

  static void detonate(ExplosiveEntity entity) {
    Consumer<ExplosiveEntity> current = service;
    if (current == null)
      throw new IllegalStateException("IndustrialCraft2-CE explosion platform is not initialized");
    current.accept(entity);
  }
}
