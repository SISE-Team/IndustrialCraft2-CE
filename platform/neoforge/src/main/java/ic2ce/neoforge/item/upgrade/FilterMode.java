package ic2ce.neoforge.item.upgrade;

import java.util.Locale;

/** Saved comparison modes distinguish ignored, prototype equality, scalar and interval tests. */
public enum FilterMode {
  IGNORED,
  DIRECT,
  COMPARISON,
  RANGE;
  final String name = "ic2.upgrade.advancedGUI." + name().toLowerCase(Locale.ROOT);
  public static final FilterMode[] VALUES = values();

  public boolean enabled() {
    return this != IGNORED;
  }

  public boolean ignoreFilters() {
    return ordinal() < COMPARISON.ordinal();
  }

  public byte getForNBT() {
    return (byte) ordinal();
  }

  public static FilterMode getFromNBT(byte id) {
    return id >= 0 && id < VALUES.length ? VALUES[id] : IGNORED;
  }
}
