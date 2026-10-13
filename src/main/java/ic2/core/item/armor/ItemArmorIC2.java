package ic2.core.item.armor;

import ic2.api.item.IMetalArmor;
import ic2.core.ref.Ic2ArmorMaterials;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;

/**
 * Base of IC2 armor. 26.1.2 removed {@code ArmorItem}; equippability and
 * attribute modifiers are data components applied here.
 */
public class ItemArmorIC2 extends Item implements IMetalArmor {
   protected final ArmorMaterial material;
   protected final EquipmentSlot slot;

   public ItemArmorIC2(ArmorMaterial material, Properties settings, EquipmentSlot slot) {
      super(applyArmorComponents(settings, material, slot));
      this.material = material;
      this.slot = slot;
   }

   private static Properties applyArmorComponents(Properties settings, ArmorMaterial material, EquipmentSlot slot) {
      ItemAttributeModifiers attributes = material.createAttributes(Ic2ArmorMaterials.typeFor(slot));
      Properties result = settings.stacksTo(1)
         .component(DataComponents.EQUIPPABLE, net.minecraft.world.item.equipment.Equippable.builder(slot)
            .setEquipSound(material.equipSound()).setAsset(material.assetId()).build())
         .component(DataComponents.ATTRIBUTE_MODIFIERS, attributes);
      if (material.durability() > 0) {
         result = result.durability(Ic2ArmorMaterials.typeFor(slot).getDurability(material.durability()))
            .repairable(material.repairIngredient());
         if (material.enchantmentValue() > 0) result = result.enchantable(material.enchantmentValue());
      }
      return result;
   }

   public final EquipmentSlot getArmorSlot() {
      return this.slot;
   }

   public final ArmorMaterial getArmorMaterial() {
      return this.material;
   }

   /** Static armor attributes (26.1.2 keeps attributes in the item components). */
   public ItemAttributeModifiers getArmorAttributes() {
      return this.material.createAttributes(Ic2ArmorMaterials.typeFor(this.slot));
   }

   @Override
   public boolean isMetalArmor(ItemStack itemstack, Player player) {
      return true;
   }
}
