package ic2ce.neoforge.item.upgrade;

import java.util.Locale;

/** Persisted metadata comparison policy with an explicit safe unsupported-state fallback. */
public enum MetadataMatch {
  IGNORED,
  FUZZY,
  EXACT;
  final String name = "ic2.upgrade.advancedGUI." + name().toLowerCase(Locale.ROOT);
  public static final MetadataMatch[] VALUES = values();

  public boolean enabled() {
    return ordinal() != 0;
  }

  public byte getForNBT() {
    return (byte) ordinal();
  }

  public static MetadataMatch getFromNBT(byte id) {
    return id >= 0 && id < VALUES.length ? VALUES[id] : IGNORED;
  }
}
