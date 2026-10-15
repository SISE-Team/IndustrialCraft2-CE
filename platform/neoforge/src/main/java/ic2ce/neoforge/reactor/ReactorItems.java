package ic2ce.neoforge.reactor;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.reactor.part.ChemicalCondensator;
import ic2ce.neoforge.reactor.part.HeatCell;
import ic2ce.neoforge.reactor.part.HeatExchanger;
import ic2ce.neoforge.reactor.part.HeatPack;
import ic2ce.neoforge.reactor.part.HeatVent;
import ic2ce.neoforge.reactor.part.HullPlating;
import ic2ce.neoforge.reactor.part.IsotopeBreeder;
import ic2ce.neoforge.reactor.part.LithiumBreeder;
import ic2ce.neoforge.reactor.part.MoxFuel;
import ic2ce.neoforge.reactor.part.NeighborHeatVent;
import ic2ce.neoforge.reactor.part.NeutronReflector;
import ic2ce.neoforge.reactor.part.PermanentReflector;
import ic2ce.neoforge.reactor.part.UraniumFuel;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Reactor items, staged in declaration order. */
public final class ReactorItems {
   public static final Item RCI_RSH = ItemDefinitions.register("rci_rsh", new BlockItem(ReactorBlocks.RCI_RSH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rci_rsh")))));
   public static final Item RCI_LZH = ItemDefinitions.register("rci_lzh", new BlockItem(ReactorBlocks.RCI_LZH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rci_lzh")))));
   public static final Item NUCLEAR_REACTOR = ItemDefinitions.register(
      "nuclear_reactor", new BlockItem(ReactorBlocks.NUCLEAR_REACTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("nuclear_reactor"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_ACCESS_HATCH = ItemDefinitions.register(
      "reactor_access_hatch", new BlockItem(ReactorBlocks.REACTOR_ACCESS_HATCH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reactor_access_hatch"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_CHAMBER = ItemDefinitions.register(
      "reactor_chamber", new BlockItem(ReactorBlocks.REACTOR_CHAMBER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reactor_chamber"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_FLUID_PORT = ItemDefinitions.register(
      "reactor_fluid_port", new BlockItem(ReactorBlocks.REACTOR_FLUID_PORT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reactor_fluid_port"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_REDSTONE_PORT = ItemDefinitions.register(
      "reactor_redstone_port", new BlockItem(ReactorBlocks.REACTOR_REDSTONE_PORT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reactor_redstone_port"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_COOLANT_CELL = ItemDefinitions.register(
      "reactor_coolant_cell", new HeatCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reactor_coolant_cell"))), 10000)
   );
   public static final Item TRIPLE_REACTOR_COOLANT_CELL = ItemDefinitions.register(
      "triple_reactor_coolant_cell", new HeatCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("triple_reactor_coolant_cell"))), 30000)
   );
   public static final Item SEXTUPLE_REACTOR_COOLANT_CELL = ItemDefinitions.register(
      "sextuple_reactor_coolant_cell", new HeatCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("sextuple_reactor_coolant_cell"))), 60000)
   );
   public static final Item REACTOR_PLATING = ItemDefinitions.register("reactor_plating", new HullPlating(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reactor_plating"))), 1000, 0.95F));
   public static final Item REACTOR_HEAT_PLATING = ItemDefinitions.register(
      "reactor_heat_plating", new HullPlating(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reactor_heat_plating"))), 2000, 0.99F)
   );
   public static final Item CONTAINMENT_REACTOR_PLATING = ItemDefinitions.register(
      "containment_reactor_plating", new HullPlating(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("containment_reactor_plating"))), 500, 0.9F)
   );
   public static final Item HEAT_EXCHANGER = ItemDefinitions.register("heat_exchanger", new HeatExchanger(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("heat_exchanger"))), 2500, 12, 4));
   public static final Item REACTOR_HEAT_EXCHANGER = ItemDefinitions.register(
      "reactor_heat_exchanger", new HeatExchanger(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reactor_heat_exchanger"))), 5000, 0, 72)
   );
   public static final Item COMPONENT_HEAT_EXCHANGER = ItemDefinitions.register(
      "component_heat_exchanger", new HeatExchanger(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("component_heat_exchanger"))), 5000, 36, 0)
   );
   public static final Item ADVANCED_HEAT_EXCHANGER = ItemDefinitions.register(
      "advanced_heat_exchanger", new HeatExchanger(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("advanced_heat_exchanger"))), 10000, 24, 8)
   );
   public static final Item HEAT_VENT = ItemDefinitions.register("heat_vent", new HeatVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("heat_vent"))), 1000, 6, 0));
   public static final Item REACTOR_HEAT_VENT = ItemDefinitions.register("reactor_heat_vent", new HeatVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reactor_heat_vent"))), 1000, 5, 5));
   public static final Item OVERCLOCKED_HEAT_VENT = ItemDefinitions.register(
      "overclocked_heat_vent", new HeatVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("overclocked_heat_vent"))), 1000, 20, 36)
   );
   public static final Item COMPONENT_HEAT_VENT = ItemDefinitions.register("component_heat_vent", new NeighborHeatVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("component_heat_vent"))), 4));
   public static final Item ADVANCED_HEAT_VENT = ItemDefinitions.register("advanced_heat_vent", new HeatVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("advanced_heat_vent"))), 1000, 12, 0));
   public static final Item NEUTRON_REFLECTOR = ItemDefinitions.register("neutron_reflector", new NeutronReflector(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("neutron_reflector"))), 30000));
   public static final Item THICK_NEUTRON_REFLECTOR = ItemDefinitions.register(
      "thick_neutron_reflector", new NeutronReflector(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("thick_neutron_reflector"))), 120000)
   );
   public static final Item IRIDIUM_NEUTRON_REFLECTOR = ItemDefinitions.register(
      "iridium_neutron_reflector", new PermanentReflector(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iridium_neutron_reflector"))))
   );
   public static final Item RSH_CONDENSATOR = ItemDefinitions.register("rsh_condensator", new ChemicalCondensator(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rsh_condensator"))), 20000));
   public static final Item LZH_CONDENSATOR = ItemDefinitions.register("lzh_condensator", new ChemicalCondensator(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("lzh_condensator"))), 100000));
   public static final Item HEATPACK = ItemDefinitions.register("heatpack", new HeatPack(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("heatpack"))), 1000, 1));
   public static final Item REACTOR_VESSEL = ItemDefinitions.register("reactor_vessel", new BlockItem(ReactorBlocks.REACTOR_VESSEL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reactor_vessel")))));
   public static final Item URANIUM_FUEL_ROD = ItemDefinitions.register("uranium_fuel_rod", new UraniumFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("uranium_fuel_rod"))), 1));
   public static final Item DUAL_URANIUM_FUEL_ROD = ItemDefinitions.register("dual_uranium_fuel_rod", new UraniumFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("dual_uranium_fuel_rod"))), 2));
   public static final Item QUAD_URANIUM_FUEL_ROD = ItemDefinitions.register("quad_uranium_fuel_rod", new UraniumFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("quad_uranium_fuel_rod"))), 4));
   public static final Item MOX_FUEL_ROD = ItemDefinitions.register("mox_fuel_rod", new MoxFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mox_fuel_rod"))), 1));
   public static final Item DUAL_MOX_FUEL_ROD = ItemDefinitions.register("dual_mox_fuel_rod", new MoxFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("dual_mox_fuel_rod"))), 2));
   public static final Item QUAD_MOX_FUEL_ROD = ItemDefinitions.register("quad_mox_fuel_rod", new MoxFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("quad_mox_fuel_rod"))), 4));
   public static final Item LITHIUM_FUEL_ROD = ItemDefinitions.register("lithium_fuel_rod", new LithiumBreeder(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("lithium_fuel_rod")))));
   public static final Item DEPLETED_ISOTOPE_FUEL_ROD = ItemDefinitions.register(
      "depleted_isotope_fuel_rod", new IsotopeBreeder(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("depleted_isotope_fuel_rod"))))
   );

   private ReactorItems() {}

   public static void init() {}
}
