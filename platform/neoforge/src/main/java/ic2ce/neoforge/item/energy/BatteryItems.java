package ic2ce.neoforge.item.energy;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Battery items, staged in declaration order. */
public final class BatteryItems {
   public static final Item RE_BATTERY = ItemDefinitions.register("re_battery", new RechargeableSupply(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("re_battery"))), 10000.0, 100.0, 1));
   public static final Item ADVANCED_RE_BATTERY = ItemDefinitions.register(
      "advanced_re_battery", new RechargeableSupply(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("advanced_re_battery"))).stacksTo(16), 100000.0, 256.0, 2)
   );
   public static final Item ENERGY_CRYSTAL = ItemDefinitions.register(
      "energy_crystal", new RechargeableSupply(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("energy_crystal"))).stacksTo(16), 1000000.0, 2048.0, 3)
   );
   public static final Item LAPOTRON_CRYSTAL = ItemDefinitions.register(
      "lapotron_crystal", new RechargeableSupply(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("lapotron_crystal"))).stacksTo(16).rarity(Rarity.UNCOMMON), 1.0E7, 8092.0, 4)
   );
   public static final Item SINGLE_USE_BATTERY = ItemDefinitions.register(
      "single_use_battery", new DisposableSupply(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("single_use_battery"))), 1200, 1)
   );

   private BatteryItems() {}

   public static void init() {}
}
