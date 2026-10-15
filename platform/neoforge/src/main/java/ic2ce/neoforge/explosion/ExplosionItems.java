package ic2ce.neoforge.explosion;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Explosion items, staged in declaration order. */
public final class ExplosionItems {
   public static final Item ITNT = ItemDefinitions.register("itnt", new BlockItem(ExplosionBlocks.ITNT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("itnt")))));
   public static final Item NUKE = ItemDefinitions.register("nuke", new BlockItem(ExplosionBlocks.NUKE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("nuke"))).rarity(Rarity.UNCOMMON)));
   public static final Item CLASSIC_NUKE = ItemDefinitions.register(
      "classic_nuke", new BlockItem(ExplosionBlocks.CLASSIC_NUKE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("classic_nuke"))).rarity(Rarity.UNCOMMON))
   );

   private ExplosionItems() {}

   public static void init() {}
}
