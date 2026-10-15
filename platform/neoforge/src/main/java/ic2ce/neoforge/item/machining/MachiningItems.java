package ic2ce.neoforge.item.machining;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

/** Machining items, staged in declaration order. */
public final class MachiningItems {
   public static final Item IRON_CUTTING_BLADE = ItemDefinitions.register(
      "iron_cutting_blade", new CuttingBlade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iron_cutting_blade"))), BladeMaterial.iron)
   );
   public static final Item DIAMOND_CUTTING_BLADE = ItemDefinitions.register(
      "diamond_cutting_blade", new CuttingBlade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("diamond_cutting_blade"))), BladeMaterial.diamond)
   );
   public static final Item STEEL_CUTTING_BLADE = ItemDefinitions.register(
      "steel_cutting_blade", new CuttingBlade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("steel_cutting_blade"))), BladeMaterial.steel)
   );

   private MachiningItems() {}

   public static void init() {}
}
