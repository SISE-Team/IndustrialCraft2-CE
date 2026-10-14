package ic2ce.neoforge.menu.slot;

import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;

/** Stable native humanoid armor ordering is independent of non-humanoid/body slots. */
public final class EquipmentOrder {
  private static final List<EquipmentSlot> SLOTS =
      List.of(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);

  private EquipmentOrder() {}

  public static EquipmentSlot get(int index) {
    return SLOTS.get(index);
  }

  public static int getCount() {
    return SLOTS.size();
  }

  public static Iterable<EquipmentSlot> getAll() {
    return SLOTS;
  }
}
