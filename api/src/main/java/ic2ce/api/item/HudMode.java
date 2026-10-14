package ic2ce.api.item;

/** Stable saved ordinals; corrupt negative values resolve safely to a valid native display mode. */
public enum HudMode {
  DISABLED("ic2.hud.disabled"),
  BASIC("ic2.hud.basic"),
  EXTENDED("ic2.hud.extended"),
  ADVANCED("ic2.hud.advanced");
  private static final HudMode[] MODES = values();
  private final String key;

  HudMode(String key) {
    this.key = key;
  }

  public boolean shouldDisplay() {
    return this != DISABLED;
  }

  public boolean hasTooltip() {
    return ordinal() >= EXTENDED.ordinal();
  }

  public String getTranslationKey() {
    return key;
  }

  public int getID() {
    return ordinal();
  }

  public static HudMode getFromID(int id) {
    return MODES[Math.floorMod(id, MODES.length)];
  }

  public static int getMaxMode() {
    return MODES.length - 1;
  }
}
