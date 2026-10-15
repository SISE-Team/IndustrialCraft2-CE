package ic2ce.neoforge.item.paint;

import net.minecraft.world.item.DyeColor;

/** Stable pigment IDs and RGB data use native ordinal indexing and a primitive RGB dispatch. */
public enum PigmentPalette {
  BLACK(DyeColor.BLACK, 1908001),
  BLUE(DyeColor.BLUE, 3949738),
  BROWN(DyeColor.BROWN, 8606770),
  CYAN(DyeColor.CYAN, 1481884),
  GRAY(DyeColor.GRAY, 4673362),
  GREEN(DyeColor.GREEN, 6192150),
  LIGHT_BLUE(DyeColor.LIGHT_BLUE, 3847130),
  LIGHT_GRAY(DyeColor.LIGHT_GRAY, 10329495),
  LIME(DyeColor.LIME, 8439583),
  MAGENTA(DyeColor.MAGENTA, 13061821),
  ORANGE(DyeColor.ORANGE, 16351261),
  PINK(DyeColor.PINK, 15961002),
  PURPLE(DyeColor.PURPLE, 8991416),
  RED(DyeColor.RED, 11546150),
  WHITE(DyeColor.WHITE, 16383998),
  YELLOW(DyeColor.YELLOW, 16701501);
  public static final PigmentPalette[] values = values();
  private static final PigmentPalette[] BY_DYE = indexDyes();
  public final DyeColor dyeColor;
  public final int color;

  PigmentPalette(DyeColor dye, int rgb) {
    dyeColor = dye;
    color = rgb;
  }

  private static PigmentPalette[] indexDyes() {
    var index = new PigmentPalette[DyeColor.values().length];
    for (var pigment : values()) {
      int slot = pigment.dyeColor.ordinal();
      if (index[slot] != null) throw new IllegalStateException("Duplicate physical dye");
      index[slot] = pigment;
    }
    return index;
  }

  public int getId() {
    return ordinal();
  }

  public int getColor() {
    return color;
  }

  public static PigmentPalette get(DyeColor dye) {
    return dye == null ? null : BY_DYE[dye.ordinal()];
  }

  public static PigmentPalette byColor(int rgb) {
    return switch (rgb) {
      case 1908001 -> BLACK;
      case 3949738 -> BLUE;
      case 8606770 -> BROWN;
      case 1481884 -> CYAN;
      case 4673362 -> GRAY;
      case 6192150 -> GREEN;
      case 3847130 -> LIGHT_BLUE;
      case 10329495 -> LIGHT_GRAY;
      case 8439583 -> LIME;
      case 13061821 -> MAGENTA;
      case 16351261 -> ORANGE;
      case 15961002 -> PINK;
      case 8991416 -> PURPLE;
      case 11546150 -> RED;
      case 16383998 -> WHITE;
      case 16701501 -> YELLOW;
      default -> null;
    };
  }
}
