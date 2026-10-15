package ic2ce.neoforge.transport;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

/** Transport items, staged in declaration order. */
public final class TransportItems {
   public static final Item WEIGHTED_FLUID_DISTRIBUTOR = ItemDefinitions.register(
      "weighted_fluid_distributor", new BlockItem(TransportBlocks.WEIGHTED_FLUID_DISTRIBUTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("weighted_fluid_distributor"))))
   );
   public static final Item WEIGHTED_ITEM_DISTRIBUTOR = ItemDefinitions.register(
      "weighted_item_distributor", new BlockItem(TransportBlocks.WEIGHTED_ITEM_DISTRIBUTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("weighted_item_distributor"))))
   );
   public static final Item FLUID_DISTRIBUTOR = ItemDefinitions.register(
      "fluid_distributor", new BlockItem(TransportBlocks.FLUID_DISTRIBUTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("fluid_distributor"))))
   );
   public static final Item FLUID_REGULATOR = ItemDefinitions.register(
      "fluid_regulator", new BlockItem(TransportBlocks.FLUID_REGULATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("fluid_regulator"))))
   );
   public static final Item ITEM_BUFFER = ItemDefinitions.register("item_buffer", new BlockItem(TransportBlocks.ITEM_BUFFER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("item_buffer")))));
   public static final Item SORTING_MACHINE = ItemDefinitions.register(
      "sorting_machine", new BlockItem(TransportBlocks.SORTING_MACHINE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("sorting_machine"))))
   );

   private TransportItems() {}

   public static void init() {}
}
