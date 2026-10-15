package ic2ce.neoforge.crafting;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Crafting items, staged in declaration order. */
public final class CraftingItems {
   public static final Item INDUSTRIAL_WORKBENCH = ItemDefinitions.register(
      "industrial_workbench", new BlockItem(CraftingBlocks.INDUSTRIAL_WORKBENCH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("industrial_workbench"))))
   );
   public static final Item BATCH_CRAFTER = ItemDefinitions.register(
      "batch_crafter", new BlockItem(CraftingBlocks.BATCH_CRAFTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("batch_crafter"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item UU_ASSEMBLY_BENCH = ItemDefinitions.register(
      "uu_assembly_bench", new BlockItem(CraftingBlocks.UU_ASSEMBLY_BENCH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("uu_assembly_bench"))).rarity(Rarity.UNCOMMON))
   );

   private CraftingItems() {}

   public static void init() {}
}
