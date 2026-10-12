package ic2.core.ref;

import ic2.core.IC2;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * IC2 armor materials as 26.1.2 {@link ArmorMaterial} records. Defense values
 * keep the 1.19.2 amounts in helmet/chestplate/leggings/boots order.
 */
public final class Ic2ArmorMaterials {
   /** Repair items are matched through tags in 26.1.2; IC2 repairs via its own upgrade system. */
   private static final TagKey<Item> NO_REPAIR = TagKey.create(Registries.ITEM, IC2.getIdentifier("no_repair"));

   private Ic2ArmorMaterials() {}

   private static ArmorMaterial material(
      int durability, int helmet, int chest, int legs, int boots,
      int enchantability, Holder<SoundEvent> sound, float toughness, float knockback
   ) {
      return new ArmorMaterial(
         durability,
         Map.of(ArmorType.HELMET, helmet, ArmorType.CHESTPLATE, chest, ArmorType.LEGGINGS, legs, ArmorType.BOOTS, boots),
         enchantability, sound, toughness, knockback, NO_REPAIR, EquipmentAssets.IRON
      );
   }

   private static ArmorMaterial soundOnly(Holder<SoundEvent> sound) {
      return material(0, 0, 0, 0, 0, 0, sound, 0.0F, 0.0F);
   }

   public static final ArmorMaterial BRONZE = material(15, 2, 5, 6, 2, 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F);
   public static final ArmorMaterial ALLOY = material(50, 4, 7, 9, 4, 12, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F);
   public static final ArmorMaterial NANO_SUIT = material(0, 0, 0, 0, 0, 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F);
   public static final ArmorMaterial QUANTUM_SUIT = material(0, 0, 0, 0, 0, 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F);
   public static final ArmorMaterial NIGHT_VISION_GOGGLES = material(0, 3, 0, 0, 0, 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F);
   public static final ArmorMaterial HAZMAT = soundOnly(SoundEvents.ARMOR_EQUIP_LEATHER);
   public static final ArmorMaterial CF_PACK = soundOnly(SoundEvents.ARMOR_EQUIP_IRON);
   public static final ArmorMaterial JET_PACK = soundOnly(SoundEvents.ARMOR_EQUIP_IRON);

   public static ArmorType typeFor(EquipmentSlot slot) {
      return switch (slot) {
         case HEAD -> ArmorType.HELMET;
         case CHEST -> ArmorType.CHESTPLATE;
         case LEGS -> ArmorType.LEGGINGS;
         case FEET -> ArmorType.BOOTS;
         default -> ArmorType.CHESTPLATE;
      };
   }
}
