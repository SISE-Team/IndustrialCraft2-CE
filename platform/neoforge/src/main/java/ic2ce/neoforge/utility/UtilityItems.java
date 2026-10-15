package ic2ce.neoforge.utility;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Utility items, staged in declaration order. */
public final class UtilityItems {
   public static final Item IRON_FENCE = ItemDefinitions.register("iron_fence", new BlockItem(UtilityBlocks.IRON_FENCE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iron_fence")))));
   public static final Item MAGNETIZER = ItemDefinitions.register("magnetizer", new BlockItem(UtilityBlocks.MAGNETIZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("magnetizer")))));
   public static final Item TELEPORTER = ItemDefinitions.register(
      "teleporter", new BlockItem(UtilityBlocks.TELEPORTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("teleporter"))).rarity(Rarity.RARE))
   );
   public static final Item TESLA_COIL = ItemDefinitions.register("tesla_coil", new BlockItem(UtilityBlocks.TESLA_COIL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("tesla_coil")))));
   public static final Item CHUNK_LOADER = ItemDefinitions.register(
      "chunk_loader", new BlockItem(UtilityBlocks.CHUNK_LOADER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("chunk_loader"))).rarity(Rarity.UNCOMMON))
   );

   private UtilityItems() {}

   public static void init() {}
}
