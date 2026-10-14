package ic2ce.neoforge.cable;

import java.util.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;

/**
 * Stable saved finish names are separate from immutable lookup and the physical curing protocol.
 */
public enum FoamFinish implements StringRepresentable {
  SOFT("soft", null),
  HARD_WHITE("hard_white", DyeColor.WHITE),
  HARD_ORANGE("hard_orange", DyeColor.ORANGE),
  HARD_MAGENTA("hard_magenta", DyeColor.MAGENTA),
  HARD_LIGHT_BLUE("hard_light_blue", DyeColor.LIGHT_BLUE),
  HARD_YELLOW("hard_yellow", DyeColor.YELLOW),
  HARD_LIME("hard_lime", DyeColor.LIME),
  HARD_PINK("hard_pink", DyeColor.PINK),
  HARD_GRAY("hard_gray", DyeColor.GRAY),
  HARD_LIGHT_GRAY("hard_light_gray", DyeColor.LIGHT_GRAY),
  HARD_CYAN("hard_cyan", DyeColor.CYAN),
  HARD_PURPLE("hard_purple", DyeColor.PURPLE),
  HARD_BLUE("hard_blue", DyeColor.BLUE),
  HARD_BROWN("hard_brown", DyeColor.BROWN),
  HARD_GREEN("hard_green", DyeColor.GREEN),
  HARD_RED("hard_red", DyeColor.RED),
  HARD_BLACK("hard_black", DyeColor.BLACK);
  public static final FoamFinish[] VALUES = values();
  private static final FoamFinish[] HARD = hardIndex();
  private static final Map<String, FoamFinish> NAMES = names();
  public static final FoamFinish DEFAULT_HARD =
      getHard(ic2ce.neoforge.world.block.ConstructionWall.DEFAULT_COLOR);
  private final String name;
  private final DyeColor pigment;

  FoamFinish(String name, DyeColor pigment) {
    this.name = name;
    this.pigment = pigment;
  }

  private static FoamFinish[] hardIndex() {
    var index = new FoamFinish[DyeColor.values().length];
    for (var finish : values())
      if (finish.pigment != null) index[finish.pigment.ordinal()] = finish;
    return index;
  }

  private static Map<String, FoamFinish> names() {
    var index = new HashMap<String, FoamFinish>();
    for (var finish : values())
      if (index.put(finish.name, finish) != null)
        throw new IllegalStateException("Duplicate saved foam finish");
    return Map.copyOf(index);
  }

  public static FoamFinish get(String name) {
    return name == null ? null : NAMES.get(name);
  }

  public static FoamFinish getHard(DyeColor dye) {
    return dye == null ? null : HARD[dye.ordinal()];
  }

  public boolean isPresent() {
    return true;
  }

  public boolean isSoft() {
    return pigment == null;
  }

  public boolean isHard() {
    return pigment != null;
  }

  public DyeColor getColor() {
    if (pigment == null) throw new IllegalStateException("Uncured foam has no hardened pigment");
    return pigment;
  }

  @Override
  public String getSerializedName() {
    return name;
  }
}
