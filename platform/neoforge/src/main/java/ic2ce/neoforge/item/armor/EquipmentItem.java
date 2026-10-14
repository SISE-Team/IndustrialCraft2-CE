package ic2ce.neoforge.item.armor;


import ic2ce.api.item.IMetalArmor;
import ic2ce.neoforge.registry.ArmorProfiles;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.*;

/** Native equippable material descriptor publishes one immutable attribute set per item. */
public class EquipmentItem extends Item implements IMetalArmor {
  protected final ArmorMaterial material;
  protected final EquipmentSlot slot;
  private final ItemAttributeModifiers attributes;

  public EquipmentItem(ArmorMaterial material, Properties properties, EquipmentSlot slot) {
    this(material, properties, slot, material.createAttributes(ArmorProfiles.typeFor(slot)));
  }

  private EquipmentItem(
      ArmorMaterial material,
      Properties properties,
      EquipmentSlot slot,
      ItemAttributeModifiers attributes) {
    super(equipment(properties, material, slot, attributes));
    this.material = material;
    this.slot = slot;
    this.attributes = attributes;
  }

  private static Properties equipment(
      Properties properties,
      ArmorMaterial material,
      EquipmentSlot slot,
      ItemAttributeModifiers attributes) {
    if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
      throw new IllegalArgumentException("Not a humanoid armor slot");
    var type = ArmorProfiles.typeFor(slot);
    properties
        .stacksTo(1)
        .component(
            DataComponents.EQUIPPABLE,
            Equippable.builder(slot)
                .setEquipSound(material.equipSound())
                .setAsset(material.assetId())
                .build())
        .component(DataComponents.ATTRIBUTE_MODIFIERS, attributes);
    if (material.durability() > 0)
      properties
          .durability(type.getDurability(material.durability()))
          .repairable(material.repairIngredient());
    if (material.enchantmentValue() > 0) properties.enchantable(material.enchantmentValue());
    return properties;
  }

  public final EquipmentSlot getArmorSlot() {
    return slot;
  }

  public final ArmorMaterial getArmorMaterial() {
    return material;
  }

  public ItemAttributeModifiers getArmorAttributes() {
    return attributes;
  }

  @Override
  public boolean isMetalArmor(ItemStack stack, Player owner) {
    return true;
  }
}
