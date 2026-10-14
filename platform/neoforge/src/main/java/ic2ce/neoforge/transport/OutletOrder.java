package ic2ce.neoforge.transport;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

/** Bounded, owned GUI outlet order. Its use does not merge item and fluid transfer protocols. */
public final class OutletOrder {
  private OutletOrder() {}

  @SuppressWarnings("unchecked")
  public static List<Direction> normalize(List<?> raw, Direction input) {
    if (raw instanceof ArrayList<?> && raw.size() <= 5) {
      int mask = 0;
      boolean valid = true;
      for (int index = 0; index < raw.size(); index++) {
        if (!(raw.get(index) instanceof Direction direction)
            || direction == input
            || (mask & 1 << direction.ordinal()) != 0) {
          valid = false;
          break;
        }
        mask |= 1 << direction.ordinal();
      }
      if (valid) return (List<Direction>) raw;
    }
    var owned = new ArrayList<Direction>(5);
    int mask = 0;
    if (raw != null)
      for (int index = 0, count = Math.min(6, raw.size()); index < count; index++) {
        if (raw.get(index) instanceof Direction direction
            && direction != input
            && (mask & 1 << direction.ordinal()) == 0) {
          owned.add(direction);
          mask |= 1 << direction.ordinal();
        }
      }
    return owned;
  }

  public static List<Direction> read(CompoundTag data, Direction input) {
    var raw = data.getIntArray("priority").orElse(new int[0]);
    var owned = new ArrayList<Direction>(5);
    int mask = 0;
    for (int index = 0, count = Math.min(6, raw.length); index < count; index++) {
      int id = raw[index];
      if (id < 0 || id >= 6) continue;
      var direction = Direction.from3DDataValue(id);
      if (direction != input && (mask & 1 << direction.ordinal()) == 0) {
        owned.add(direction);
        mask |= 1 << direction.ordinal();
      }
    }
    return owned;
  }

  public static void write(CompoundTag data, List<Direction> order) {
    var codes = new int[order.size()];
    for (int index = 0; index < codes.length; index++)
      codes[index] = order.get(index).get3DDataValue();
    data.putIntArray("priority", codes);
  }
}
