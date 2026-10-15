package ic2ce.neoforge.power;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

/** Power items, staged in declaration order. */
public final class PowerItems {
   public static final Item GENERATOR = ItemDefinitions.register("generator", new BlockItem(PowerBlocks.GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("generator")))));
   public static final Item GEO_GENERATOR = ItemDefinitions.register(
      "geo_generator", new BlockItem(PowerBlocks.GEO_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("geo_generator"))))
   );
   public static final Item KINETIC_GENERATOR = ItemDefinitions.register(
      "kinetic_generator", new BlockItem(PowerBlocks.KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("kinetic_generator"))))
   );
   public static final Item RT_GENERATOR = ItemDefinitions.register(
      "rt_generator", new BlockItem(PowerBlocks.RT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rt_generator"))))
   );
   public static final Item SEMIFLUID_GENERATOR = ItemDefinitions.register(
      "semifluid_generator", new BlockItem(PowerBlocks.SEMIFLUID_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("semifluid_generator"))))
   );
   public static final Item SOLAR_GENERATOR = ItemDefinitions.register(
      "solar_generator", new BlockItem(PowerBlocks.SOLAR_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("solar_generator"))))
   );
   public static final Item STIRLING_GENERATOR = ItemDefinitions.register(
      "stirling_generator", new BlockItem(PowerBlocks.STIRLING_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("stirling_generator"))))
   );
   public static final Item WATER_GENERATOR = ItemDefinitions.register(
      "water_generator", new BlockItem(PowerBlocks.WATER_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("water_generator"))))
   );
   public static final Item WIND_GENERATOR = ItemDefinitions.register(
      "wind_generator", new BlockItem(PowerBlocks.WIND_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("wind_generator"))))
   );
   public static final Item CREATIVE_GENERATOR = ItemDefinitions.register(
      "creative_generator", new BlockItem(PowerBlocks.CREATIVE_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("creative_generator"))))
   );

   private PowerItems() {}

   public static void init() {}
}
