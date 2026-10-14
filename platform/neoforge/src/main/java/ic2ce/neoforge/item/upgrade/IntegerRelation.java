package ic2ce.neoforge.item.upgrade;

import java.util.Locale;

/** Ordinal-stable integer relations with bounded saved-byte decoding. */
public enum IntegerRelation {
  LESS_OR_EQUAL("<="),
  LESS("<"),
  GREATER(">"),
  GREATER_OR_EQUAL(">=");
  final String symbol, name;
  public static final IntegerRelation DEFAULT = LESS;
  public static final IntegerRelation[] VALUES = values();

  IntegerRelation(String symbol) {
    this.symbol = symbol;
    name = "ic2.upgrade.advancedGUI." + name().toLowerCase(Locale.ROOT);
  }

  public boolean compare(int first, int second) {
    return switch (this) {
      case LESS_OR_EQUAL -> first <= second;
      case LESS -> first < second;
      case GREATER -> first > second;
      case GREATER_OR_EQUAL -> first >= second;
    };
  }

  public byte getForNBT() {
    return (byte) ordinal();
  }

  public static IntegerRelation getFromNBT(byte id) {
    return id >= 0 && id < VALUES.length ? VALUES[id] : DEFAULT;
  }
}
