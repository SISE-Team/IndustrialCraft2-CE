package ic2ce.neoforge.registry;

import ic2ce.neoforge.ProjectIds;
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
 * IC2 armor materials as 26.1.2 {@link ArmorMaterial} records. Defense values keep the 1.19.2
 * amounts in helmet/chestplate/leggings/boots order.
 */
public final class ArmorProfiles {
  /** Repair items are matched through tags in 26.1.2; IC2 repairs via its own upgrade system. */
  private static final TagKey<Item> NO_REPAIR =
      TagKey.create(Registries.ITEM, ProjectIds.id("no_repair"));

  private ArmorProfiles() {}

  private static ArmorMaterial material(
      String asset,
      int durability,
      int head,
      int chest,
      int legs,
      int feet,
      int enchantability,
      Holder<SoundEvent> sound,
      float toughness,
      float knockback) {
    if (durability < 0
        || head < 0
        || chest < 0
        || legs < 0
        || feet < 0
        || enchantability < 0
        || !Float.isFinite(toughness)
        || toughness < 0
        || !Float.isFinite(knockback)
        || knockback < 0) throw new IllegalArgumentException("Invalid physical armor profile");
    var repair =
        switch (asset) {
          case "bronze" ->
              TagKey.create(Registries.ITEM, ProjectIds.id("bronze_ingot"));
          case "alloy" -> TagKey.create(Registries.ITEM, ProjectIds.id("alloy"));
          default -> NO_REPAIR;
        };
    return new ArmorMaterial(
        durability,
        Map.of(
            ArmorType.HELMET,
            head,
            ArmorType.CHESTPLATE,
            chest,
            ArmorType.LEGGINGS,
            legs,
            ArmorType.BOOTS,
            feet),
        enchantability,
        java.util.Objects.requireNonNull(sound),
        toughness,
        knockback,
        repair,
        net.minecraft.resources.ResourceKey.create(
            EquipmentAssets.ROOT_ID, ProjectIds.id(asset)));
  }

  private static ArmorMaterial soundOnly(String asset, Holder<SoundEvent> sound) {
    return material(asset, 0, 0, 0, 0, 0, 0, sound, 0, 0);
  }

  public static final ArmorMaterial BRONZE =
      material("bronze", 15, 2, 5, 6, 2, 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F);
  public static final ArmorMaterial ALLOY =
      material("alloy", 50, 4, 7, 9, 4, 12, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F);
  public static final ArmorMaterial NANO_SUIT =
      material("nano_suit", 0, 0, 0, 0, 0, 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F);
  public static final ArmorMaterial QUANTUM_SUIT =
      material("quantum_suit", 0, 0, 0, 0, 0, 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F);
  public static final ArmorMaterial NIGHT_VISION_GOGGLES =
      material("night_vision", 0, 3, 0, 0, 0, 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F);
  public static final ArmorMaterial HAZMAT = soundOnly("hazmat", SoundEvents.ARMOR_EQUIP_LEATHER);
  public static final ArmorMaterial CF_PACK = soundOnly("cf_pack", SoundEvents.ARMOR_EQUIP_IRON);
  public static final ArmorMaterial JET_PACK = soundOnly("jetpack", SoundEvents.ARMOR_EQUIP_IRON);

  public static final ArmorMaterial RUBBER_BOOTS =
      soundOnly("rubber_boots", SoundEvents.ARMOR_EQUIP_LEATHER);

  public static ArmorType typeFor(EquipmentSlot slot) {
    return switch (slot) {
      case HEAD -> ArmorType.HELMET;
      case CHEST -> ArmorType.CHESTPLATE;
      case LEGS -> ArmorType.LEGGINGS;
      case FEET -> ArmorType.BOOTS;
      default ->
          throw new IllegalArgumentException(
              "Equipment slot has no player armor geometry: " + slot);
    };
  }
}
