package ic2ce.neoforge.menu.document;

/** The supported projections of the same immutable screen document. */
public enum ScreenProjection {
  GAME("game"),
  JEI("jei");

  public final String name;

  ScreenProjection(String externalName) {
    name = externalName;
  }

  public static ScreenProjection get(String externalName) {
    if (externalName == null) return null;
    return switch (externalName) {
      case "game" -> GAME;
      case "jei" -> JEI;
      default -> null;
    };
  }
}
