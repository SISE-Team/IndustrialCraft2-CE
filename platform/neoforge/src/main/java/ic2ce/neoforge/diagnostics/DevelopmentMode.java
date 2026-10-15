package ic2ce.neoforge.diagnostics;

/** Development-only content and controls are enabled by the INDEV system property. */
public final class DevelopmentMode {
  private static final boolean ACTIVE = System.getProperty("INDEV") != null;

  private DevelopmentMode() {}

  public static boolean active() {
    return ACTIVE;
  }
}
