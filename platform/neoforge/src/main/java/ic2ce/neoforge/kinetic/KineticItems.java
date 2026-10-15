package ic2ce.neoforge.kinetic;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

/** Kinetic items, staged in declaration order. */
public final class KineticItems {
   public static final Item ELECTRIC_KINETIC_GENERATOR = ItemDefinitions.register(
      "electric_kinetic_generator", new BlockItem(KineticBlocks.ELECTRIC_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("electric_kinetic_generator"))))
   );
   public static final Item MANUAL_KINETIC_GENERATOR = ItemDefinitions.register(
      "manual_kinetic_generator", new BlockItem(KineticBlocks.MANUAL_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("manual_kinetic_generator"))))
   );
   public static final Item STEAM_KINETIC_GENERATOR = ItemDefinitions.register(
      "steam_kinetic_generator", new BlockItem(KineticBlocks.STEAM_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("steam_kinetic_generator"))))
   );
   public static final Item STIRLING_KINETIC_GENERATOR = ItemDefinitions.register(
      "stirling_kinetic_generator", new BlockItem(KineticBlocks.STIRLING_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("stirling_kinetic_generator"))))
   );
   public static final Item WATER_KINETIC_GENERATOR = ItemDefinitions.register(
      "water_kinetic_generator", new BlockItem(KineticBlocks.WATER_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("water_kinetic_generator"))))
   );
   public static final Item WIND_KINETIC_GENERATOR = ItemDefinitions.register(
      "wind_kinetic_generator", new BlockItem(KineticBlocks.WIND_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("wind_kinetic_generator"))))
   );
   public static final Item WOODEN_ROTOR = ItemDefinitions.register(
      "wooden_rotor",
      new RotorItem(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("wooden_rotor"))).durability(10800),
         5,
         false,
         0.25F,
         10,
         60,
         ProjectIds.id("textures/item/rotor/wood_rotor_model.png")
      )
   );
   public static final Item BRONZE_ROTOR = ItemDefinitions.register(
      "bronze_rotor",
      new RotorItem(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_rotor"))).durability(86400),
         7,
         true,
         0.5F,
         14,
         75,
         Identifier.fromNamespaceAndPath("ic2", "textures/item/rotor/bronze_rotor_model.png")
      )
   );
   public static final Item IRON_ROTOR = ItemDefinitions.register(
      "iron_rotor",
      new RotorItem(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iron_rotor"))).durability(86400),
         7,
         true,
         0.5F,
         14,
         75,
         Identifier.fromNamespaceAndPath("ic2", "textures/item/rotor/iron_rotor_model.png")
      )
   );
   public static final Item STEEL_ROTOR = ItemDefinitions.register(
      "steel_rotor",
      new RotorItem(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("steel_rotor"))).durability(172800),
         9,
         true,
         0.75F,
         17,
         90,
         Identifier.fromNamespaceAndPath("ic2", "textures/item/rotor/steel_rotor_model.png")
      )
   );
   public static final Item CARBON_ROTOR = ItemDefinitions.register(
      "carbon_rotor",
      new RotorItem(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("carbon_rotor"))).durability(604800),
         11,
         true,
         1.0F,
         20,
         110,
         Identifier.fromNamespaceAndPath("ic2", "textures/item/rotor/carbon_rotor_model.png")
      )
   );

   private KineticItems() {}

   public static void init() {}
}
