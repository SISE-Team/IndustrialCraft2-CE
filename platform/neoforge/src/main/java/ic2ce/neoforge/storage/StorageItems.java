package ic2ce.neoforge.storage;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Storage items, staged in declaration order. */
public final class StorageItems {
   public static final Item TANK = ItemDefinitions.register("tank", new BlockItem(StorageBlocks.TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("tank")))));
   public static final Item WOODEN_STORAGE_BOX = ItemDefinitions.register(
      "wooden_storage_box", new BlockItem(StorageBlocks.WOODEN_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("wooden_storage_box"))))
   );
   public static final Item IRON_STORAGE_BOX = ItemDefinitions.register(
      "iron_storage_box", new BlockItem(StorageBlocks.IRON_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iron_storage_box"))))
   );
   public static final Item BRONZE_STORAGE_BOX = ItemDefinitions.register(
      "bronze_storage_box", new BlockItem(StorageBlocks.BRONZE_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_storage_box"))))
   );
   public static final Item STEEL_STORAGE_BOX = ItemDefinitions.register(
      "steel_storage_box", new BlockItem(StorageBlocks.STEEL_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("steel_storage_box"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item IRIDIUM_STORAGE_BOX = ItemDefinitions.register(
      "iridium_storage_box", new BlockItem(StorageBlocks.IRIDIUM_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iridium_storage_box"))).rarity(Rarity.RARE))
   );
   public static final Item BRONZE_TANK = ItemDefinitions.register("bronze_tank", new BlockItem(StorageBlocks.BRONZE_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_tank")))));
   public static final Item IRON_TANK = ItemDefinitions.register("iron_tank", new BlockItem(StorageBlocks.IRON_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iron_tank")))));
   public static final Item STEEL_TANK = ItemDefinitions.register(
      "steel_tank", new BlockItem(StorageBlocks.STEEL_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("steel_tank"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item IRIDIUM_TANK = ItemDefinitions.register(
      "iridium_tank", new BlockItem(StorageBlocks.IRIDIUM_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iridium_tank"))).rarity(Rarity.RARE))
   );

   private StorageItems() {}

   public static void init() {}
}
