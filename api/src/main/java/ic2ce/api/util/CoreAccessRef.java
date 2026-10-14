package ic2ce.api.util;

import java.util.Objects;

/** Explicit loader-installed API services; publication has no reflection dependency. */
public final class CoreAccessRef {
  private static final StackWalker CALLER =
      StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
  private static volatile CoreAccess service;

  private CoreAccessRef() {}

  public static synchronized void install(CoreAccess implementation) {
    if (!CALLER.getCallerClass().getPackageName().startsWith("ic2ce."))
      throw new IllegalAccessError("Only IndustrialCraft2-CE can install core services");
    Objects.requireNonNull(implementation);
    if (service != null && service != implementation)
      throw new IllegalStateException("Core services already installed");
    service = implementation;
  }

  public static CoreAccess get() {
    CoreAccess current = service;
    if (current == null)
      throw new IllegalStateException("IndustrialCraft2-CE has not initialized its API");
    if (!CALLER.getCallerClass().getPackageName().startsWith("ic2ce."))
      throw new IllegalAccessError("Use the public IndustrialCraft2-CE API entry points");
    return current;
  }

  public static boolean exists() {
    return service != null;
  }
}
