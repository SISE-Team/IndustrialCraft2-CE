package ic2ce.neoforge.thermal;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

/** Thermal items, staged in declaration order. */
public final class ThermalItems {
   public static final Item ELECTRIC_HEAT_GENERATOR = ItemDefinitions.register(
      "electric_heat_generator", new BlockItem(ThermalBlocks.ELECTRIC_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("electric_heat_generator"))))
   );
   public static final Item FLUID_HEAT_GENERATOR = ItemDefinitions.register(
      "fluid_heat_generator", new BlockItem(ThermalBlocks.FLUID_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("fluid_heat_generator"))))
   );
   public static final Item RT_HEAT_GENERATOR = ItemDefinitions.register(
      "rt_heat_generator", new BlockItem(ThermalBlocks.RT_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rt_heat_generator"))))
   );
   public static final Item SOLID_HEAT_GENERATOR = ItemDefinitions.register(
      "solid_heat_generator", new BlockItem(ThermalBlocks.SOLID_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("solid_heat_generator"))))
   );
   public static final Item LIQUID_HEAT_EXCHANGER = ItemDefinitions.register(
      "liquid_heat_exchanger", new BlockItem(ThermalBlocks.LIQUID_HEAT_EXCHANGER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("liquid_heat_exchanger"))))
   );

   private ThermalItems() {}

   public static void init() {}
}
