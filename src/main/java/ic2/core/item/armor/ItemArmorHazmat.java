package ic2.core.item.armor;

import ic2.api.item.IHazmatLike;
import ic2.core.Ic2DamageSource;
import ic2.core.ref.Ic2ArmorMaterials;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemArmorHazmat extends ItemArmorUtility implements IHazmatLike {
   public ItemArmorHazmat(EquipmentSlot type, Properties settings) {
      super(type == EquipmentSlot.FEET ? Ic2ArmorMaterials.RUBBER_BOOTS : Ic2ArmorMaterials.HAZMAT, settings, type);
   }

   @Override
   public boolean addsProtection(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
      return true;
   }

   public static boolean hasCompleteHazmat(LivingEntity living) {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            ItemStack stack = living.getItemBySlot(slot);
            if (stack == null || !(stack.getItem() instanceof IHazmatLike)) {
               return false;
            }

            IHazmatLike hazmat = (IHazmatLike)stack.getItem();
            if (!hazmat.addsProtection(living, slot, stack)) {
               return false;
            }

            if (hazmat.fullyProtects(living, slot, stack)) {
               return true;
            }
         }
      }

      return true;
   }

   public static boolean hazmatAbsorbs(DamageSource source) {
      return source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE)
         || source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
         || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
         || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)
         || source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)
         || source == Ic2DamageSource.electricity()
         || source == Ic2DamageSource.radiation();
   }

   public boolean absorbFall(ItemStack stack, LivingEntity entity, float distance) {
      int fallDamage = Math.max((int)distance - 3, 0);
      if (fallDamage >= 8) {
         return false;
      } else {
         int armorDamage = (fallDamage + 1) / 2;
         if (armorDamage > 0 && armorDamage <= stack.getMaxDamage() - stack.getDamageValue()) {
            stack.hurtAndBreak(armorDamage, (net.minecraft.server.level.ServerLevel)entity.level(), entity, brokenItem -> entity.onEquippedItemBroken(brokenItem, this.slot));
            return true;
         } else {
            return false;
         }
      }
   }
}
