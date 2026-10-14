package ic2ce.neoforge.item.upgrade;

import net.minecraft.nbt.CompoundTag;

/**
 * Immutable compiled comparison fields; malformed user values reject matches instead of crashing
 * machine ticks.
 */
public final class NumericFilter {
  public final boolean active;
  public final FilterMode comparison;
  public final String mainBox, extraBox;
  public final IntegerRelation main, extra;
  private final int mainValue, extraValue;
  private final boolean valid;

  public NumericFilter(CompoundTag data) {
    active = data != null && data.getBooleanOr("active", false);
    comparison =
        !active
            ? FilterMode.IGNORED
            : !data.contains("type")
                ? FilterMode.DIRECT
                : FilterMode.getFromNBT(data.getByteOr("type", (byte) 0));
    boolean numeric = comparison == FilterMode.COMPARISON || comparison == FilterMode.RANGE;
    mainBox = numeric ? data.getStringOr("normal", "") : "";
    extraBox = comparison == FilterMode.RANGE ? data.getStringOr("extra", "") : "";
    main =
        numeric
            ? IntegerRelation.getFromNBT(data.getByteOr("normalComp", (byte) 0))
            : IntegerRelation.DEFAULT;
    extra =
        comparison == FilterMode.RANGE
            ? IntegerRelation.getFromNBT(data.getByteOr("extraComp", (byte) 0))
            : IntegerRelation.DEFAULT;
    int first = 0, second = 0;
    boolean parsed = true;
    try {
      if (numeric) first = Integer.parseInt(mainBox);
      if (comparison == FilterMode.RANGE) second = Integer.parseInt(extraBox);
    } catch (NumberFormatException ignored) {
      parsed = false;
    }
    mainValue = first;
    extraValue = second;
    valid = parsed;
  }

  public boolean doComparison(int value) {
    return valid
        && switch (comparison) {
          case COMPARISON -> main.compare(mainValue, value);
          case RANGE -> main.compare(mainValue, value) && extra.compare(value, extraValue);
          default -> false;
        };
  }
}
