package ic2ce.neoforge.vehicle;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.EntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

/** Vehicle items, staged in declaration order. */
public final class VehicleItems {
   public static final Item BROKEN_RUBBER_BOAT = ItemDefinitions.register("broken_rubber_boat", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("broken_rubber_boat"))).stacksTo(1)));
   public static final Item RUBBER_BOAT = ItemDefinitions.register(
      "rubber_boat", new HullItem(RubberHull.class, EntityDefinitions.RUBBER_BOAT, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_boat"))).stacksTo(1))
   );
   public static final Item ELECTRIC_BOAT = ItemDefinitions.register(
      "electric_boat", new HullItem(ElectricHull.class, EntityDefinitions.ELECTRIC_BOAT, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("electric_boat"))).stacksTo(1))
   );
   public static final Item CARBON_BOAT = ItemDefinitions.register(
      "carbon_boat", new HullItem(CarbonHull.class, EntityDefinitions.CARBON_BOAT, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("carbon_boat"))).stacksTo(1))
   );

   private VehicleItems() {}

   public static void init() {}
}
