package ic2ce.neoforge.item.consumable;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Consumable items, staged in declaration order. */
public final class ConsumableItems {
   public static final Item URANIUM = ItemDefinitions.register("uranium", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("uranium"))), 60, 100));
   public static final Item URANIUM_235 = ItemDefinitions.register("uranium_235", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("uranium_235"))), 150, 100));
   public static final Item URANIUM_238 = ItemDefinitions.register("uranium_238", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("uranium_238"))), 10, 90));
   public static final Item PLUTONIUM = ItemDefinitions.register("plutonium", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("plutonium"))), 150, 100));
   public static final Item MOX = ItemDefinitions.register("mox", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mox"))), 300, 100));
   public static final Item SMALL_URANIUM_235 = ItemDefinitions.register("small_uranium_235", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("small_uranium_235"))), 150, 100));
   public static final Item SMALL_URANIUM_238 = ItemDefinitions.register("small_uranium_238", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("small_uranium_238"))), 10, 90));
   public static final Item SMALL_PLUTONIUM = ItemDefinitions.register("small_plutonium", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("small_plutonium"))), 150, 100));
   public static final Item URANIUM_PELLET = ItemDefinitions.register("uranium_pellet", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("uranium_pellet"))), 60, 100));
   public static final Item MOX_PELLET = ItemDefinitions.register("mox_pellet", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mox_pellet"))), 300, 100));
   public static final Item RTG_PELLET = ItemDefinitions.register("rtg_pellet", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rtg_pellet"))).stacksTo(1), 2, 90));
   public static final Item DEPLETED_URANIUM_FUEL_ROD = ItemDefinitions.register(
      "depleted_uranium_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("depleted_uranium_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_DUAL_URANIUM_FUEL_ROD = ItemDefinitions.register(
      "depleted_dual_uranium_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("depleted_dual_uranium_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_QUAD_URANIUM_FUEL_ROD = ItemDefinitions.register(
      "depleted_quad_uranium_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("depleted_quad_uranium_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_MOX_FUEL_ROD = ItemDefinitions.register(
      "depleted_mox_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("depleted_mox_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_DUAL_MOX_FUEL_ROD = ItemDefinitions.register(
      "depleted_dual_mox_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("depleted_dual_mox_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_QUAD_MOX_FUEL_ROD = ItemDefinitions.register(
      "depleted_quad_mox_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("depleted_quad_mox_fuel_rod"))), 10, 100)
   );
   public static final Item NEAR_DEPLETED_URANIUM = ItemDefinitions.register(
      "near_depleted_uranium", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("near_depleted_uranium"))), 15, 100)
   );
   public static final Item RE_ENRICHED_URANIUM = ItemDefinitions.register(
      "re_enriched_uranium", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("re_enriched_uranium"))), 30, 100)
   );
   public static final Item EMPTY_FUEL_CAN = ItemDefinitions.register("empty_fuel_can", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("empty_fuel_can")))));
   public static final Item EMPTY_MUG = ItemDefinitions.register("empty_mug", new CoffeeCup(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("empty_mug"))).stacksTo(1), CoffeeCup.MugType.empty));
   public static final Item COFFEE_MUG = ItemDefinitions.register("coffee_mug", new CoffeeCup(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("coffee_mug"))).stacksTo(1), CoffeeCup.MugType.coffee));
   public static final Item COLD_COFFEE_MUG = ItemDefinitions.register(
      "cold_coffee_mug", new CoffeeCup(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("cold_coffee_mug"))).stacksTo(1), CoffeeCup.MugType.cold_coffee)
   );
   public static final Item DARK_COFFEE_MUG = ItemDefinitions.register(
      "dark_coffee_mug", new CoffeeCup(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("dark_coffee_mug"))).stacksTo(1), CoffeeCup.MugType.dark_coffee)
   );
   public static final Item TERRA_WART = ItemDefinitions.register(
      "terra_wart",
      new MedicinalWart(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("terra_wart"))).food(new FoodProperties.Builder().nutrition(0).saturationModifier(1.0F).alwaysEdible().build()).rarity(Rarity.RARE)
      )
   );
   public static final Item FILLED_TIN_CAN = ItemDefinitions.register("filled_tin_can", new CannedFood(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("filled_tin_can")))));
   public static final Item FILLED_FUEL_CAN=ItemDefinitions.register("filled_fuel_can",new ic2ce.neoforge.item.consumable.FuelCan(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,ProjectIds.id("filled_fuel_can"))).stacksTo(1)));

   private ConsumableItems() {}

   public static void init() {}
}
