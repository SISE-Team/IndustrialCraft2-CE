package ic2.core.item;

import ic2.core.IC2;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DamageHandler {
   public static int getDamage(ItemStack stack) {
      Item item = stack.getItem();
      return item == null ? 0 : stack.getDamageValue();
   }

   public static void setDamage(ItemStack stack, int damage, boolean displayOnly) {
      Item item = stack.getItem();
      if (item != null) {
         if (item instanceof IPseudoDamageItem) {
            if (!displayOnly) {
               throw new IllegalStateException("can't damage " + stack + " physically");
            }

            ((IPseudoDamageItem)item).setStackDamage(stack, damage);
         } else if (stack.isDamageableItem()) {
            stack.setDamageValue(damage);
         }
      }
   }

   public static int getMaxDamage(ItemStack stack) {
      Item item = stack.getItem();
      return item == null ? 0 : stack.getMaxDamage();
   }

   public static boolean damage(ItemStack stack, int damage, LivingEntity src, InteractionHand hand) {
      return damage(stack, damage, src == null ? null : (net.minecraft.server.level.ServerLevel)src.level(), src, hand);
   }

   public static boolean damage(ItemStack stack, int damage, net.minecraft.server.level.ServerLevel level, LivingEntity src, InteractionHand hand) {
      Item item = stack.getItem();
      if (item == null) {
         return false;
      } else if (src != null) {
         stack.hurtAndBreak(damage, (net.minecraft.server.level.ServerLevel)src.level(), src, brokenItem -> {
            if (hand != null) {
               src.onEquippedItemBroken(brokenItem, hand == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            }
         });
         return true;
      } else {
         // Machine slots apply wear without an entity. The slot, as in 1.19,
         // consumes the broken item itself; hurtAndBreak would consume it twice.
         if (!stack.isDamageableItem()) return false;
         int wear = level == null ? damage : net.minecraft.world.item.enchantment.EnchantmentHelper.processDurabilityChange(level, stack, damage);
         stack.setDamageValue(stack.getDamageValue() + wear);
         return stack.getDamageValue() >= stack.getMaxDamage();
      }
   }
}
