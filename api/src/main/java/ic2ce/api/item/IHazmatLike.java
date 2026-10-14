package ic2ce.api.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Slot-aware sealed protection; a declared whole-body garment can replace the four-piece set. */
public interface IHazmatLike {
  boolean addsProtection(LivingEntity owner, EquipmentSlot slot, ItemStack stack);

  default boolean fullyProtects(LivingEntity owner, EquipmentSlot slot, ItemStack stack) {
    return false;
  }

  static boolean hasCompleteHazmat(LivingEntity owner) {
    if (owner == null) return false;
    var slots = Slots.ALL;
    for (var slot : slots) {
      var stack = owner.getItemBySlot(slot);
      if (stack.getItem() instanceof IHazmatLike protection
          && protection.addsProtection(owner, slot, stack)
          && protection.fullyProtects(owner, slot, stack)) return true;
    }
    for (var slot : slots) {
      var stack = owner.getItemBySlot(slot);
      if (!(stack.getItem() instanceof IHazmatLike protection)
          || !protection.addsProtection(owner, slot, stack)) return false;
    }
    return true;
  }

  final class Slots {
    private Slots() {}

    private static final EquipmentSlot[] ALL = {
      EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };
  }
}
