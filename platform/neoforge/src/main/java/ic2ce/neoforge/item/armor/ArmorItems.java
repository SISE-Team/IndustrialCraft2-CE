package ic2ce.neoforge.item.armor;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ArmorProfiles;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Armor items, staged in declaration order. */
public final class ArmorItems {
   public static final Item ALLOY_CHESTPLATE = ItemDefinitions.register(
      "alloy_chestplate", new EquipmentItem(ArmorProfiles.ALLOY, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("alloy_chestplate"))), EquipmentSlot.CHEST)
   );
   public static final Item BRONZE_BOOTS = ItemDefinitions.register(
      "bronze_boots", new EquipmentItem(ArmorProfiles.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_boots"))), EquipmentSlot.FEET)
   );
   public static final Item BRONZE_CHESTPLATE = ItemDefinitions.register(
      "bronze_chestplate", new EquipmentItem(ArmorProfiles.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_chestplate"))), EquipmentSlot.CHEST)
   );
   public static final Item BRONZE_HELMET = ItemDefinitions.register(
      "bronze_helmet", new EquipmentItem(ArmorProfiles.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_helmet"))), EquipmentSlot.HEAD)
   );
   public static final Item BRONZE_LEGGINGS = ItemDefinitions.register(
      "bronze_leggings", new EquipmentItem(ArmorProfiles.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_leggings"))), EquipmentSlot.LEGS)
   );
   public static final Item CF_PACK = ItemDefinitions.register("cf_pack", new FoamReservoir(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("cf_pack")))));
   public static final Item HAZMAT_CHESTPLATE = ItemDefinitions.register(
      "hazmat_chestplate", new SealedGarment(EquipmentSlot.CHEST, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("hazmat_chestplate"))))
   );
   public static final Item HAZMAT_HELMET = ItemDefinitions.register("hazmat_helmet", new SealedGarment(EquipmentSlot.HEAD, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("hazmat_helmet")))));
   public static final Item HAZMAT_LEGGINGS = ItemDefinitions.register("hazmat_leggings", new SealedGarment(EquipmentSlot.LEGS, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("hazmat_leggings")))));
   public static final Item JETPACK = ItemDefinitions.register("jetpack", new BiogasJetpack(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("jetpack")))));
   public static final Item NANO_BOOTS = ItemDefinitions.register(
      "nano_boots",
      new NanoArmor(ArmorProfiles.NANO_SUIT, EquipmentSlot.FEET, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("nano_boots"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item NANO_CHESTPLATE = ItemDefinitions.register(
      "nano_chestplate",
      new NanoArmor(ArmorProfiles.NANO_SUIT, EquipmentSlot.CHEST, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("nano_chestplate"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item NANO_HELMET = ItemDefinitions.register(
      "nano_helmet",
      new NanoArmor(ArmorProfiles.NANO_SUIT, EquipmentSlot.HEAD, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("nano_helmet"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item NANO_LEGGINGS = ItemDefinitions.register(
      "nano_leggings",
      new NanoArmor(ArmorProfiles.NANO_SUIT, EquipmentSlot.LEGS, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("nano_leggings"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_BOOTS = ItemDefinitions.register(
      "quantum_boots",
      new QuantumArmor(ArmorProfiles.QUANTUM_SUIT, EquipmentSlot.FEET, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("quantum_boots"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_CHESTPLATE = ItemDefinitions.register(
      "quantum_chestplate",
      new QuantumArmor(ArmorProfiles.QUANTUM_SUIT, EquipmentSlot.CHEST, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("quantum_chestplate"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_HELMET = ItemDefinitions.register(
      "quantum_helmet",
      new QuantumArmor(ArmorProfiles.QUANTUM_SUIT, EquipmentSlot.HEAD, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("quantum_helmet"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_LEGGINGS = ItemDefinitions.register(
      "quantum_leggings",
      new QuantumArmor(ArmorProfiles.QUANTUM_SUIT, EquipmentSlot.LEGS, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("quantum_leggings"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item RUBBER_BOOTS = ItemDefinitions.register("rubber_boots", new SealedGarment(EquipmentSlot.FEET, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_boots")))));
   public static final Item NIGHT_VISION_GOGGLES = ItemDefinitions.register(
      "night_vision_goggles", new NightVisionGoggles(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("night_vision_goggles"))).durability(27))
   );

   private ArmorItems() {}

   public static void init() {}
}
