package ic2ce.neoforge.item.container;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.material.Fluids;

/** Container items, staged in declaration order. */
public final class ContainerItems {
   public static final Item TOOL_BOX = ItemDefinitions.register("tool_box", new PortableToolbox(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("tool_box"))).stacksTo(1)));
   public static final Item EMPTY_CELL = ItemDefinitions.register("empty_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("empty_cell"))), Fluids.EMPTY, 0));
   public static final Item WATER_CELL = ItemDefinitions.register("water_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("water_cell"))), Fluids.WATER, 1));
   public static final Item LAVA_CELL = ItemDefinitions.register("lava_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("lava_cell"))), Fluids.LAVA, 1));
   public static final Item AIR_CELL = ItemDefinitions.register("air_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("air_cell"))), FluidDefinitions.AIR.still, 1));
   public static final Item ELECTROLYZED_WATER_CELL = ItemDefinitions.register(
      "electrolyzed_water_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("electrolyzed_water_cell"))), null, 1)
   );
   public static final Item BIOFUEL_CELL = ItemDefinitions.register("biofuel_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("biofuel_cell"))), null, 1));
   public static final Item COALFUEL_CELL = ItemDefinitions.register("coalfuel_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("coalfuel_cell"))), null, 1));
   public static final Item BIO_CELL = ItemDefinitions.register("bio_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bio_cell"))), null, 1));
   public static final Item HYDRATED_COAL_CELL = ItemDefinitions.register(
      "hydrated_coal_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("hydrated_coal_cell"))), null, 1)
   );
   public static final FixedCell WEED_EX_CELL = ItemDefinitions.register(
      "weed_ex_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("weed_ex_cell"))).stacksTo(1), null, 64)
   );
   public static final FixedCell HYDRATION_CELL = ItemDefinitions.register(
      "hydration_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("hydration_cell"))).stacksTo(1), null, 10000)
   );

   private ContainerItems() {}

   public static void init() {}
}
