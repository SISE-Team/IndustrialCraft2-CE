package ic2ce.neoforge.cable;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

/** Cable items, staged in declaration order. */
public final class CableItems {
   public static final Item COPPER_CABLE = ItemDefinitions.register(
      "copper_cable", new BlockItem(CableBlocks.COPPER_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("copper_cable"))))
   );
   public static final Item INSULATED_COPPER_CABLE = ItemDefinitions.register(
      "insulated_copper_cable", new BlockItem(CableBlocks.INSULATED_COPPER_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("insulated_copper_cable"))))
   );
   public static final Item GLASS_FIBRE_CABLE = ItemDefinitions.register(
      "glass_fibre_cable", new BlockItem(CableBlocks.GLASS_FIBRE_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("glass_fibre_cable"))))
   );
   public static final Item GOLD_CABLE = ItemDefinitions.register("gold_cable", new BlockItem(CableBlocks.GOLD_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("gold_cable")))));
   public static final Item INSULATED_GOLD_CABLE = ItemDefinitions.register(
      "insulated_gold_cable", new BlockItem(CableBlocks.INSULATED_GOLD_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("insulated_gold_cable"))))
   );
   public static final Item DOUBLE_INSULATED_GOLD_CABLE = ItemDefinitions.register(
      "double_insulated_gold_cable", new BlockItem(CableBlocks.DOUBLE_INSULATED_GOLD_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("double_insulated_gold_cable"))))
   );
   public static final Item IRON_CABLE = ItemDefinitions.register("iron_cable", new BlockItem(CableBlocks.IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iron_cable")))));
   public static final Item INSULATED_IRON_CABLE = ItemDefinitions.register(
      "insulated_iron_cable", new BlockItem(CableBlocks.INSULATED_IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("insulated_iron_cable"))))
   );
   public static final Item DOUBLE_INSULATED_IRON_CABLE = ItemDefinitions.register(
      "double_insulated_iron_cable", new BlockItem(CableBlocks.DOUBLE_INSULATED_IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("double_insulated_iron_cable"))))
   );
   public static final Item TRIPLE_INSULATED_IRON_CABLE = ItemDefinitions.register(
      "triple_insulated_iron_cable", new BlockItem(CableBlocks.TRIPLE_INSULATED_IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("triple_insulated_iron_cable"))))
   );
   public static final Item TIN_CABLE = ItemDefinitions.register("tin_cable", new BlockItem(CableBlocks.TIN_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("tin_cable")))));
   public static final Item INSULATED_TIN_CABLE = ItemDefinitions.register(
      "insulated_tin_cable", new BlockItem(CableBlocks.INSULATED_TIN_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("insulated_tin_cable"))))
   );
   public static final Item DETECTOR_CABLE = ItemDefinitions.register(
      "detector_cable", new BlockItem(CableBlocks.DETECTOR_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("detector_cable"))))
   );
   public static final Item SPLITTER_CABLE = ItemDefinitions.register(
      "splitter_cable", new BlockItem(CableBlocks.SPLITTER_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("splitter_cable"))))
   );

   private CableItems() {}

   public static void init() {}
}
