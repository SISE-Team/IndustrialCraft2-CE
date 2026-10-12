package ic2.core.slot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;

public class ArmorSlot {
   private static final EquipmentSlot[] armorSlots = getArmorSlots();
   private static final List<EquipmentSlot> armorSlotList = Collections.unmodifiableList(Arrays.asList(armorSlots));

   public static EquipmentSlot get(int index) {
      return armorSlots[index];
   }

   public static int getCount() {
      return armorSlots.length;
   }

   public static Iterable<EquipmentSlot> getAll() {
      return armorSlotList;
   }

   private static EquipmentSlot[] getArmorSlots() {
      EquipmentSlot[] values = EquipmentSlot.values();
      int count = 0;

      for (EquipmentSlot slot : values) {
         if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            count++;
         }
      }

      EquipmentSlot[] ret = new EquipmentSlot[count];

      for (int i = 0; i < ret.length; i++) {
         for (EquipmentSlot slotx : values) {
            if (slotx.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && slotx.getIndex() == i) {
               ret[i] = slotx;
               break;
            }
         }
      }

      for (int i = 0; i < ret.length; i++) {
         if (ret[i] == null) {
            throw new RuntimeException("Can't find an armor mapping for idx " + i);
         }
      }

      return ret;
   }
}
