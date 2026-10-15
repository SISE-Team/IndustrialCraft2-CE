package ic2ce.neoforge.registry;



import ic2ce.neoforge.menu.slot.EquipmentItemSlot;

import ic2ce.neoforge.runtime.ProjectRuntime;
import ic2ce.neoforge.agriculture.item.CropSupportItem;
import ic2ce.neoforge.vehicle.CarbonHull;
import ic2ce.neoforge.vehicle.ElectricHull;
import ic2ce.neoforge.vehicle.RubberHull;
import ic2ce.neoforge.item.energy.RechargeableSupply;
import ic2ce.neoforge.item.energy.DisposableSupply;
import ic2ce.neoforge.item.container.FixedCell;
import ic2ce.neoforge.agriculture.item.EncodedSeed;
import ic2ce.neoforge.processing.uu.ItemCrystalMemory;
import ic2ce.neoforge.item.consumable.CoffeeCup;
import ic2ce.neoforge.item.consumable.RadioactiveMaterial;
import ic2ce.neoforge.item.consumable.MedicinalWart;
import ic2ce.neoforge.item.consumable.CannedFood;
import ic2ce.neoforge.item.armor.FoamReservoir;
import ic2ce.neoforge.item.armor.SealedGarment;
import ic2ce.neoforge.item.armor.EquipmentItem;
import ic2ce.neoforge.item.armor.BiogasJetpack;
import ic2ce.neoforge.item.armor.NanoArmor;
import ic2ce.neoforge.item.armor.NightVisionGoggles;
import ic2ce.neoforge.item.armor.QuantumArmor;
import ic2ce.neoforge.vehicle.HullItem;
import ic2ce.neoforge.item.machining.CuttingBlade;
import ic2ce.neoforge.reactor.part.ChemicalCondensator;
import ic2ce.neoforge.reactor.part.IsotopeBreeder;
import ic2ce.neoforge.reactor.part.HeatCell;
import ic2ce.neoforge.reactor.part.HeatExchanger;
import ic2ce.neoforge.reactor.part.HeatPack;
import ic2ce.neoforge.reactor.part.PermanentReflector;
import ic2ce.neoforge.reactor.part.LithiumBreeder;
import ic2ce.neoforge.reactor.part.MoxFuel;
import ic2ce.neoforge.reactor.part.HullPlating;
import ic2ce.neoforge.reactor.part.NeutronReflector;
import ic2ce.neoforge.reactor.part.UraniumFuel;
import ic2ce.neoforge.reactor.part.HeatVent;
import ic2ce.neoforge.reactor.part.NeighborHeatVent;
import ic2ce.neoforge.kinetic.RotorItem;
import ic2ce.neoforge.terrain.program.FrostProgram;
import ic2ce.neoforge.terrain.program.CultivationProgram;
import ic2ce.neoforge.terrain.program.AridProgram;
import ic2ce.neoforge.terrain.program.LevelingProgram;
import ic2ce.neoforge.terrain.program.IrrigationProgram;
import ic2ce.neoforge.terrain.program.FungalProgram;
import ic2ce.neoforge.terrain.TerrainBlueprint;
import ic2ce.neoforge.item.tool.DiagnosticTool;
import ic2ce.neoforge.item.tool.MiningDrill;
import ic2ce.neoforge.item.tool.IridiumDrill;
import ic2ce.neoforge.item.tool.PoweredSaw;
import ic2ce.neoforge.item.tool.TeleporterLinkCard;
import ic2ce.neoforge.item.tool.NanoBlade;
import ic2ce.neoforge.item.tool.SurfaceImprinter;
import ic2ce.neoforge.item.tool.OreScanner;
import ic2ce.neoforge.item.tool.AdvancedOreScanner;
import ic2ce.neoforge.item.tool.DurableCraftingTool;
import ic2ce.neoforge.item.tool.CoverCrowbar;
import ic2ce.neoforge.item.tool.InsulationCutter;
import ic2ce.neoforge.item.tool.EnergyProbe;
import ic2ce.neoforge.item.tool.MiningLaser;
import ic2ce.neoforge.item.tool.PigmentBrush;
import ic2ce.neoforge.item.tool.MechanicalWrench;
import ic2ce.neoforge.item.tool.ElectricWrench;
import ic2ce.neoforge.item.container.PortableToolbox;
import ic2ce.neoforge.item.tool.ManualResinTap;
import ic2ce.neoforge.item.tool.ElectricResinTap;
import ic2ce.neoforge.item.tool.WindProbe;
import ic2ce.neoforge.item.machining.BladeMaterial;
import ic2ce.neoforge.item.upgrade.MachineUpgrade;
import ic2ce.neoforge.item.paint.PigmentPalette;
import ic2ce.core.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.material.Fluids;

public final class ItemDefinitions {
   public static final Item BASALT = register("basalt", new BlockItem(BlockDefinitions.BASALT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("basalt")))));
   public static final Item LEAD_ORE = register("lead_ore", new BlockItem(BlockDefinitions.LEAD_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lead_ore")))));
   public static final Item TIN_ORE = register("tin_ore", new BlockItem(BlockDefinitions.TIN_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tin_ore")))));
   public static final Item URANIUM_ORE = register("uranium_ore", new BlockItem(BlockDefinitions.URANIUM_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uranium_ore")))));
   public static final Item DEEPSLATE_URANIUM_ORE = register(
      "deepslate_uranium_ore", new BlockItem(BlockDefinitions.DEEPSLATE_URANIUM_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("deepslate_uranium_ore"))))
   );
   public static final Item DEEPSLATE_TIN_ORE = register(
      "deepslate_tin_ore", new BlockItem(BlockDefinitions.DEEPSLATE_TIN_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("deepslate_tin_ore"))))
   );
   public static final Item DEEPSLATE_LEAD_ORE = register(
      "deepslate_lead_ore", new BlockItem(BlockDefinitions.DEEPSLATE_LEAD_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("deepslate_lead_ore"))))
   );
   public static final Item RAW_LEAD_BLOCK = register("raw_lead_block", new BlockItem(BlockDefinitions.RAW_LEAD_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("raw_lead_block")))));
   public static final Item RAW_TIN_BLOCK = register("raw_tin_block", new BlockItem(BlockDefinitions.RAW_TIN_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("raw_tin_block")))));
   public static final Item RAW_URANIUM_BLOCK = register(
      "raw_uranium_block", new BlockItem(BlockDefinitions.RAW_URANIUM_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("raw_uranium_block"))))
   );
   public static final Item BRONZE_BLOCK = register("bronze_block", new BlockItem(BlockDefinitions.BRONZE_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_block")))));
   public static final Item LEAD_BLOCK = register("lead_block", new BlockItem(BlockDefinitions.LEAD_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lead_block")))));
   public static final Item STEEL_BLOCK = register("steel_block", new BlockItem(BlockDefinitions.STEEL_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_block")))));
   public static final Item TIN_BLOCK = register("tin_block", new BlockItem(BlockDefinitions.TIN_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tin_block")))));
   public static final Item URANIUM_BLOCK = register("uranium_block", new BlockItem(BlockDefinitions.URANIUM_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uranium_block")))));
   public static final Item REINFORCED_STONE = register(
      "reinforced_stone", new BlockItem(BlockDefinitions.REINFORCED_STONE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reinforced_stone"))))
   );
   public static final Item MACHINE = register("machine", new BlockItem(BlockDefinitions.MACHINE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("machine")))));
   public static final Item ADVANCED_MACHINE = register(
      "advanced_machine", new BlockItem(BlockDefinitions.ADVANCED_MACHINE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("advanced_machine"))))
   );
   public static final Item SILVER_BLOCK = register("silver_block", new BlockItem(BlockDefinitions.SILVER_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("silver_block")))));
   public static final Item RUBBER_LEAVES = register("rubber_leaves", new BlockItem(BlockDefinitions.RUBBER_LEAVES, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_leaves")))));
   public static final Item RUBBER_LOG = register("rubber_log", new BlockItem(BlockDefinitions.RUBBER_LOG, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_log")))));
   public static final Item STRIPPED_RUBBER_LOG = register(
      "stripped_rubber_log", new BlockItem(BlockDefinitions.STRIPPED_RUBBER_LOG, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("stripped_rubber_log"))))
   );
   public static final Item RUBBER_WOOD = register("rubber_wood", new BlockItem(BlockDefinitions.RUBBER_WOOD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_wood")))));
   public static final Item STRIPPED_RUBBER_WOOD = register(
      "stripped_rubber_wood", new BlockItem(BlockDefinitions.STRIPPED_RUBBER_WOOD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("stripped_rubber_wood"))))
   );
   public static final Item RUBBER_PLANKS = register("rubber_planks", new BlockItem(BlockDefinitions.RUBBER_PLANKS, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_planks")))));
   public static final Item RUBBER_SAPLING = register("rubber_sapling", new BlockItem(BlockDefinitions.RUBBER_SAPLING, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_sapling")))));
   public static final Item RUBBER_BUTTON = register("rubber_button", new BlockItem(BlockDefinitions.RUBBER_BUTTON, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_button")))));
   public static final Item RUBBER_DOOR = register("rubber_door", new BlockItem(BlockDefinitions.RUBBER_DOOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_door")))));
   public static final Item RUBBER_FENCE = register("rubber_fence", new BlockItem(BlockDefinitions.RUBBER_FENCE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_fence")))));
   public static final Item RUBBER_FENCE_GATE = register(
      "rubber_fence_gate", new BlockItem(BlockDefinitions.RUBBER_FENCE_GATE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_fence_gate"))))
   );
   public static final Item RUBBER_PRESSURE_PLATE = register(
      "rubber_pressure_plate", new BlockItem(BlockDefinitions.RUBBER_PRESSURE_PLATE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_pressure_plate"))))
   );
   public static final Item RUBBER_SIGN = register(
      "rubber_sign", new SignItem(BlockDefinitions.RUBBER_SIGN, BlockDefinitions.RUBBER_WALL_SIGN, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_sign"))))
   );
   public static final Item RUBBER_SLAB = register("rubber_slab", new BlockItem(BlockDefinitions.RUBBER_SLAB, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_slab")))));
   public static final Item RUBBER_STAIRS = register("rubber_stairs", new BlockItem(BlockDefinitions.RUBBER_STAIRS, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_stairs")))));
   public static final Item RUBBER_TRAPDOOR = register(
      "rubber_trapdoor", new BlockItem(BlockDefinitions.RUBBER_TRAPDOOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_trapdoor"))))
   );
   public static final Item MINING_PIPE = register("mining_pipe", new BlockItem(BlockDefinitions.MINING_PIPE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mining_pipe")))));
   public static final Item WOODEN_SCAFFOLD = register(
      "wooden_scaffold", new BlockItem(BlockDefinitions.WOODEN_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wooden_scaffold"))))
   );
   public static final Item REINFORCED_WOODEN_SCAFFOLD = register(
      "reinforced_wooden_scaffold", new BlockItem(BlockDefinitions.REINFORCED_WOODEN_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reinforced_wooden_scaffold"))))
   );
   public static final Item IRON_SCAFFOLD = register("iron_scaffold", new BlockItem(BlockDefinitions.IRON_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_scaffold")))));
   public static final Item REINFORCED_IRON_SCAFFOLD = register(
      "reinforced_iron_scaffold", new BlockItem(BlockDefinitions.REINFORCED_IRON_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reinforced_iron_scaffold"))))
   );
   public static final Item IRON_FENCE = register("iron_fence", new BlockItem(BlockDefinitions.IRON_FENCE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_fence")))));
   public static final Item RESIN_SHEET = register("resin_sheet", new BlockItem(BlockDefinitions.RESIN_SHEET, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("resin_sheet")))));
   public static final Item RUBBER_SHEET = register("rubber_sheet", new BlockItem(BlockDefinitions.RUBBER_SHEET, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_sheet")))));
   public static final Item WOOL_SHEET = register("wool_sheet", new BlockItem(BlockDefinitions.WOOL_SHEET, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wool_sheet")))));
   public static final Item REINFORCED_GLASS = register(
      "reinforced_glass", new BlockItem(BlockDefinitions.REINFORCED_GLASS, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reinforced_glass"))))
   );
   public static final Item REINFORCED_DOOR = register(
      "reinforced_door", new DoubleHighBlockItem(BlockDefinitions.REINFORCED_DOOR, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reinforced_door"))))
   );
   public static final Item FOAM = register("foam", new BlockItem(BlockDefinitions.FOAM, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("foam")))));
   public static final Item WHITE_WALL = register("white_wall", new BlockItem(BlockDefinitions.WHITE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("white_wall")))));
   public static final Item ORANGE_WALL = register("orange_wall", new BlockItem(BlockDefinitions.ORANGE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("orange_wall")))));
   public static final Item MAGENTA_WALL = register("magenta_wall", new BlockItem(BlockDefinitions.MAGENTA_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("magenta_wall")))));
   public static final Item LIGHT_BLUE_WALL = register(
      "light_blue_wall", new BlockItem(BlockDefinitions.LIGHT_BLUE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("light_blue_wall"))))
   );
   public static final Item YELLOW_WALL = register("yellow_wall", new BlockItem(BlockDefinitions.YELLOW_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("yellow_wall")))));
   public static final Item LIME_WALL = register("lime_wall", new BlockItem(BlockDefinitions.LIME_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lime_wall")))));
   public static final Item PINK_WALL = register("pink_wall", new BlockItem(BlockDefinitions.PINK_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("pink_wall")))));
   public static final Item GRAY_WALL = register("gray_wall", new BlockItem(BlockDefinitions.GRAY_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("gray_wall")))));
   public static final Item LIGHT_GRAY_WALL = register(
      "light_gray_wall", new BlockItem(BlockDefinitions.LIGHT_GRAY_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("light_gray_wall"))))
   );
   public static final Item CYAN_WALL = register("cyan_wall", new BlockItem(BlockDefinitions.CYAN_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cyan_wall")))));
   public static final Item PURPLE_WALL = register("purple_wall", new BlockItem(BlockDefinitions.PURPLE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("purple_wall")))));
   public static final Item BLUE_WALL = register("blue_wall", new BlockItem(BlockDefinitions.BLUE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("blue_wall")))));
   public static final Item BROWN_WALL = register("brown_wall", new BlockItem(BlockDefinitions.BROWN_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("brown_wall")))));
   public static final Item GREEN_WALL = register("green_wall", new BlockItem(BlockDefinitions.GREEN_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("green_wall")))));
   public static final Item RED_WALL = register("red_wall", new BlockItem(BlockDefinitions.RED_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("red_wall")))));
   public static final Item BLACK_WALL = register("black_wall", new BlockItem(BlockDefinitions.BLACK_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("black_wall")))));
   public static final Item ITNT = register("itnt", new BlockItem(BlockDefinitions.ITNT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("itnt")))));
   public static final Item NUKE = register("nuke", new BlockItem(BlockDefinitions.NUKE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("nuke"))).rarity(Rarity.UNCOMMON)));
   public static final Item CLASSIC_NUKE = register(
      "classic_nuke", new BlockItem(BlockDefinitions.CLASSIC_NUKE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("classic_nuke"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item COPPER_CABLE = register(
      "copper_cable", new BlockItem(BlockDefinitions.COPPER_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("copper_cable"))))
   );
   public static final Item INSULATED_COPPER_CABLE = register(
      "insulated_copper_cable", new BlockItem(BlockDefinitions.INSULATED_COPPER_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("insulated_copper_cable"))))
   );
   public static final Item GLASS_FIBRE_CABLE = register(
      "glass_fibre_cable", new BlockItem(BlockDefinitions.GLASS_FIBRE_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("glass_fibre_cable"))))
   );
   public static final Item GOLD_CABLE = register("gold_cable", new BlockItem(BlockDefinitions.GOLD_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("gold_cable")))));
   public static final Item INSULATED_GOLD_CABLE = register(
      "insulated_gold_cable", new BlockItem(BlockDefinitions.INSULATED_GOLD_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("insulated_gold_cable"))))
   );
   public static final Item DOUBLE_INSULATED_GOLD_CABLE = register(
      "double_insulated_gold_cable", new BlockItem(BlockDefinitions.DOUBLE_INSULATED_GOLD_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("double_insulated_gold_cable"))))
   );
   public static final Item IRON_CABLE = register("iron_cable", new BlockItem(BlockDefinitions.IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_cable")))));
   public static final Item INSULATED_IRON_CABLE = register(
      "insulated_iron_cable", new BlockItem(BlockDefinitions.INSULATED_IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("insulated_iron_cable"))))
   );
   public static final Item DOUBLE_INSULATED_IRON_CABLE = register(
      "double_insulated_iron_cable", new BlockItem(BlockDefinitions.DOUBLE_INSULATED_IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("double_insulated_iron_cable"))))
   );
   public static final Item TRIPLE_INSULATED_IRON_CABLE = register(
      "triple_insulated_iron_cable", new BlockItem(BlockDefinitions.TRIPLE_INSULATED_IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("triple_insulated_iron_cable"))))
   );
   public static final Item TIN_CABLE = register("tin_cable", new BlockItem(BlockDefinitions.TIN_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tin_cable")))));
   public static final Item INSULATED_TIN_CABLE = register(
      "insulated_tin_cable", new BlockItem(BlockDefinitions.INSULATED_TIN_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("insulated_tin_cable"))))
   );
   public static final Item DETECTOR_CABLE = register(
      "detector_cable", new BlockItem(BlockDefinitions.DETECTOR_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("detector_cable"))))
   );
   public static final Item SPLITTER_CABLE = register(
      "splitter_cable", new BlockItem(BlockDefinitions.SPLITTER_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("splitter_cable"))))
   );
   public static final Item BATBOX_CHARGEPAD = register(
      "batbox_chargepad", new BlockItem(BlockDefinitions.BATBOX_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("batbox_chargepad"))))
   );
   public static final Item CESU_CHARGEPAD = register(
      "cesu_chargepad", new BlockItem(BlockDefinitions.CESU_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cesu_chargepad"))))
   );
   public static final Item MFE_CHARGEPAD = register(
      "mfe_chargepad", new BlockItem(BlockDefinitions.MFE_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mfe_chargepad"))))
   );
   public static final Item MFSU_CHARGEPAD = register(
      "mfsu_chargepad", new BlockItem(BlockDefinitions.MFSU_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mfsu_chargepad"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item BATBOX = register("batbox", new BlockItem(BlockDefinitions.BATBOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("batbox")))));
   public static final Item CESU = register("cesu", new BlockItem(BlockDefinitions.CESU, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cesu")))));
   public static final Item MFE = register("mfe", new BlockItem(BlockDefinitions.MFE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mfe")))));
   public static final Item CLASSIC_MFE = register(
      "classic_mfe", new BlockItem(BlockDefinitions.CLASSIC_MFE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("classic_mfe"))))
   );
   public static final Item MFSU = register(
      "mfsu", new BlockItem(BlockDefinitions.MFSU, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mfsu"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item CLASSIC_MFSU = register(
      "classic_mfsu", new BlockItem(BlockDefinitions.CLASSIC_MFSU, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("classic_mfsu"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item LV_TRANSFORMER = register(
      "lv_transformer", new BlockItem(BlockDefinitions.LV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lv_transformer"))))
   );
   public static final Item MV_TRANSFORMER = register(
      "mv_transformer", new BlockItem(BlockDefinitions.MV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mv_transformer"))))
   );
   public static final Item HV_TRANSFORMER = register(
      "hv_transformer", new BlockItem(BlockDefinitions.HV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("hv_transformer"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item EV_TRANSFORMER = register(
      "ev_transformer", new BlockItem(BlockDefinitions.EV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("ev_transformer"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item TANK = register("tank", new BlockItem(BlockDefinitions.TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tank")))));
   public static final Item STEAM_REPRESSURIZER = register(
      "steam_repressurizer", new BlockItem(BlockDefinitions.STEAM_REPRESSURIZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steam_repressurizer"))))
   );
   public static final Item WEIGHTED_FLUID_DISTRIBUTOR = register(
      "weighted_fluid_distributor", new BlockItem(BlockDefinitions.WEIGHTED_FLUID_DISTRIBUTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("weighted_fluid_distributor"))))
   );
   public static final Item WEIGHTED_ITEM_DISTRIBUTOR = register(
      "weighted_item_distributor", new BlockItem(BlockDefinitions.WEIGHTED_ITEM_DISTRIBUTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("weighted_item_distributor"))))
   );
   public static final Item RCI_RSH = register("rci_rsh", new BlockItem(BlockDefinitions.RCI_RSH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rci_rsh")))));
   public static final Item RCI_LZH = register("rci_lzh", new BlockItem(BlockDefinitions.RCI_LZH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rci_lzh")))));
   public static final Item INDUSTRIAL_WORKBENCH = register(
      "industrial_workbench", new BlockItem(BlockDefinitions.INDUSTRIAL_WORKBENCH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("industrial_workbench"))))
   );
   public static final Item BATCH_CRAFTER = register(
      "batch_crafter", new BlockItem(BlockDefinitions.BATCH_CRAFTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("batch_crafter"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item WOODEN_STORAGE_BOX = register(
      "wooden_storage_box", new BlockItem(BlockDefinitions.WOODEN_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wooden_storage_box"))))
   );
   public static final Item IRON_STORAGE_BOX = register(
      "iron_storage_box", new BlockItem(BlockDefinitions.IRON_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_storage_box"))))
   );
   public static final Item BRONZE_STORAGE_BOX = register(
      "bronze_storage_box", new BlockItem(BlockDefinitions.BRONZE_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_storage_box"))))
   );
   public static final Item STEEL_STORAGE_BOX = register(
      "steel_storage_box", new BlockItem(BlockDefinitions.STEEL_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_storage_box"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item IRIDIUM_STORAGE_BOX = register(
      "iridium_storage_box", new BlockItem(BlockDefinitions.IRIDIUM_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iridium_storage_box"))).rarity(Rarity.RARE))
   );
   public static final Item BRONZE_TANK = register("bronze_tank", new BlockItem(BlockDefinitions.BRONZE_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_tank")))));
   public static final Item IRON_TANK = register("iron_tank", new BlockItem(BlockDefinitions.IRON_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_tank")))));
   public static final Item STEEL_TANK = register(
      "steel_tank", new BlockItem(BlockDefinitions.STEEL_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_tank"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item IRIDIUM_TANK = register(
      "iridium_tank", new BlockItem(BlockDefinitions.IRIDIUM_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iridium_tank"))).rarity(Rarity.RARE))
   );
   public static final Item GENERATOR = register("generator", new BlockItem(BlockDefinitions.GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("generator")))));
   public static final Item GEO_GENERATOR = register(
      "geo_generator", new BlockItem(BlockDefinitions.GEO_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("geo_generator"))))
   );
   public static final Item KINETIC_GENERATOR = register(
      "kinetic_generator", new BlockItem(BlockDefinitions.KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("kinetic_generator"))))
   );
   public static final Item RT_GENERATOR = register(
      "rt_generator", new BlockItem(BlockDefinitions.RT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rt_generator"))))
   );
   public static final Item SEMIFLUID_GENERATOR = register(
      "semifluid_generator", new BlockItem(BlockDefinitions.SEMIFLUID_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("semifluid_generator"))))
   );
   public static final Item SOLAR_GENERATOR = register(
      "solar_generator", new BlockItem(BlockDefinitions.SOLAR_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("solar_generator"))))
   );
   public static final Item STIRLING_GENERATOR = register(
      "stirling_generator", new BlockItem(BlockDefinitions.STIRLING_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("stirling_generator"))))
   );
   public static final Item WATER_GENERATOR = register(
      "water_generator", new BlockItem(BlockDefinitions.WATER_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("water_generator"))))
   );
   public static final Item WIND_GENERATOR = register(
      "wind_generator", new BlockItem(BlockDefinitions.WIND_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wind_generator"))))
   );
   public static final Item CREATIVE_GENERATOR = register(
      "creative_generator", new BlockItem(BlockDefinitions.CREATIVE_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("creative_generator"))))
   );
   public static final Item ELECTRIC_HEAT_GENERATOR = register(
      "electric_heat_generator", new BlockItem(BlockDefinitions.ELECTRIC_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("electric_heat_generator"))))
   );
   public static final Item FLUID_HEAT_GENERATOR = register(
      "fluid_heat_generator", new BlockItem(BlockDefinitions.FLUID_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fluid_heat_generator"))))
   );
   public static final Item RT_HEAT_GENERATOR = register(
      "rt_heat_generator", new BlockItem(BlockDefinitions.RT_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rt_heat_generator"))))
   );
   public static final Item SOLID_HEAT_GENERATOR = register(
      "solid_heat_generator", new BlockItem(BlockDefinitions.SOLID_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("solid_heat_generator"))))
   );
   public static final Item ELECTRIC_KINETIC_GENERATOR = register(
      "electric_kinetic_generator", new BlockItem(BlockDefinitions.ELECTRIC_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("electric_kinetic_generator"))))
   );
   public static final Item MANUAL_KINETIC_GENERATOR = register(
      "manual_kinetic_generator", new BlockItem(BlockDefinitions.MANUAL_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("manual_kinetic_generator"))))
   );
   public static final Item STEAM_KINETIC_GENERATOR = register(
      "steam_kinetic_generator", new BlockItem(BlockDefinitions.STEAM_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steam_kinetic_generator"))))
   );
   public static final Item STIRLING_KINETIC_GENERATOR = register(
      "stirling_kinetic_generator", new BlockItem(BlockDefinitions.STIRLING_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("stirling_kinetic_generator"))))
   );
   public static final Item WATER_KINETIC_GENERATOR = register(
      "water_kinetic_generator", new BlockItem(BlockDefinitions.WATER_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("water_kinetic_generator"))))
   );
   public static final Item WIND_KINETIC_GENERATOR = register(
      "wind_kinetic_generator", new BlockItem(BlockDefinitions.WIND_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wind_kinetic_generator"))))
   );
   public static final Item NUCLEAR_REACTOR = register(
      "nuclear_reactor", new BlockItem(BlockDefinitions.NUCLEAR_REACTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("nuclear_reactor"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_ACCESS_HATCH = register(
      "reactor_access_hatch", new BlockItem(BlockDefinitions.REACTOR_ACCESS_HATCH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_access_hatch"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_CHAMBER = register(
      "reactor_chamber", new BlockItem(BlockDefinitions.REACTOR_CHAMBER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_chamber"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_FLUID_PORT = register(
      "reactor_fluid_port", new BlockItem(BlockDefinitions.REACTOR_FLUID_PORT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_fluid_port"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_REDSTONE_PORT = register(
      "reactor_redstone_port", new BlockItem(BlockDefinitions.REACTOR_REDSTONE_PORT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_redstone_port"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item CONDENSER = register("condenser", new BlockItem(BlockDefinitions.CONDENSER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("condenser")))));
   public static final Item FLUID_BOTTLER = register("fluid_bottler", new BlockItem(BlockDefinitions.FLUID_BOTTLER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fluid_bottler")))));
   public static final Item FLUID_DISTRIBUTOR = register(
      "fluid_distributor", new BlockItem(BlockDefinitions.FLUID_DISTRIBUTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fluid_distributor"))))
   );
   public static final Item FLUID_REGULATOR = register(
      "fluid_regulator", new BlockItem(BlockDefinitions.FLUID_REGULATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fluid_regulator"))))
   );
   public static final Item LIQUID_HEAT_EXCHANGER = register(
      "liquid_heat_exchanger", new BlockItem(BlockDefinitions.LIQUID_HEAT_EXCHANGER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("liquid_heat_exchanger"))))
   );
   public static final Item PUMP = register("pump", new BlockItem(BlockDefinitions.PUMP, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("pump")))));
   public static final Item SOLAR_DISTILLER = register(
      "solar_distiller", new BlockItem(BlockDefinitions.SOLAR_DISTILLER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("solar_distiller"))))
   );
   public static final Item STEAM_GENERATOR = register(
      "steam_generator", new BlockItem(BlockDefinitions.STEAM_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steam_generator"))))
   );
   public static final Item ITEM_BUFFER = register("item_buffer", new BlockItem(BlockDefinitions.ITEM_BUFFER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("item_buffer")))));
   public static final Item MAGNETIZER = register("magnetizer", new BlockItem(BlockDefinitions.MAGNETIZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("magnetizer")))));
   public static final Item SORTING_MACHINE = register(
      "sorting_machine", new BlockItem(BlockDefinitions.SORTING_MACHINE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("sorting_machine"))))
   );
   public static final Item TELEPORTER = register(
      "teleporter", new BlockItem(BlockDefinitions.TELEPORTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("teleporter"))).rarity(Rarity.RARE))
   );
   public static final Item TERRAFORMER = register(
      "terraformer", new BlockItem(BlockDefinitions.TERRAFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("terraformer"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item TESLA_COIL = register("tesla_coil", new BlockItem(BlockDefinitions.TESLA_COIL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tesla_coil")))));
   public static final Item ELECTROLYZER = register("electrolyzer", new BlockItem(BlockDefinitions.ELECTROLYZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("electrolyzer")))));
   public static final Item CLASSIC_ELECTROLYZER = register(
      "classic_electrolyzer", new BlockItem(BlockDefinitions.CLASSIC_ELECTROLYZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("classic_electrolyzer"))))
   );
   public static final Item CHUNK_LOADER = register(
      "chunk_loader", new BlockItem(BlockDefinitions.CHUNK_LOADER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("chunk_loader"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item CANNER = register("canner", new BlockItem(BlockDefinitions.CANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("canner")))));
   public static final Item CLASSIC_CANNER = register("classic_canner", new BlockItem(BlockDefinitions.CLASSIC_CANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("classic_canner")))));
   public static final Item COMPRESSOR = register("compressor", new BlockItem(BlockDefinitions.COMPRESSOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("compressor")))));
   public static final Item ELECTRIC_FURNACE = register(
      "electric_furnace", new BlockItem(BlockDefinitions.ELECTRIC_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("electric_furnace"))))
   );
   public static final Item EXTRACTOR = register("extractor", new BlockItem(BlockDefinitions.EXTRACTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("extractor")))));
   public static final Item IRON_FURNACE = register("iron_furnace", new BlockItem(BlockDefinitions.IRON_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_furnace")))));
   public static final Item MACERATOR = register("macerator", new BlockItem(BlockDefinitions.MACERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("macerator")))));
   public static final Item RECYCLER = register("recycler", new BlockItem(BlockDefinitions.RECYCLER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("recycler")))));
   public static final Item SOLID_CANNER = register("solid_canner", new BlockItem(BlockDefinitions.SOLID_CANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("solid_canner")))));
   public static final Item BLAST_FURNACE = register("blast_furnace", new BlockItem(BlockDefinitions.BLAST_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("blast_furnace")))));
   public static final Item BLOCK_CUTTER = register("block_cutter", new BlockItem(BlockDefinitions.BLOCK_CUTTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("block_cutter")))));
   public static final Item CENTRIFUGE = register("centrifuge", new BlockItem(BlockDefinitions.CENTRIFUGE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("centrifuge")))));
   public static final Item FERMENTER = register("fermenter", new BlockItem(BlockDefinitions.FERMENTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fermenter")))));
   public static final Item INDUCTION_FURNACE = register(
      "induction_furnace", new BlockItem(BlockDefinitions.INDUCTION_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("induction_furnace"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item METAL_FORMER = register("metal_former", new BlockItem(BlockDefinitions.METAL_FORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("metal_former")))));
   public static final Item ORE_WASHING_PLANT = register(
      "ore_washing_plant", new BlockItem(BlockDefinitions.ORE_WASHING_PLANT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("ore_washing_plant"))))
   );
   public static final Item ADVANCED_MINER = register("advanced_miner", new BlockItem(BlockDefinitions.ADVANCED_MINER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("advanced_miner")))));
   public static final Item CROP_HARVESTER = register("crop_harvester", new BlockItem(BlockDefinitions.CROP_HARVESTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crop_harvester")))));
   public static final Item CROPMATRON = register("cropmatron", new BlockItem(BlockDefinitions.CROPMATRON, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cropmatron")))));
   public static final Item CLASSIC_CROPMATRON = register(
      "classic_cropmatron", new BlockItem(BlockDefinitions.CLASSIC_CROPMATRON, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("classic_cropmatron"))))
   );
   public static final Item MINER = register("miner", new BlockItem(BlockDefinitions.MINER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("miner")))));
   public static final Item MASS_FABRICATOR = register(
      "mass_fabricator", new BlockItem(BlockDefinitions.MASS_FABRICATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mass_fabricator"))).rarity(Rarity.RARE))
   );
   public static final Item CLASSIC_MASS_FABRICATOR = register(
      "classic_mass_fabricator", new BlockItem(BlockDefinitions.CLASSIC_MASS_FABRICATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("classic_mass_fabricator"))).rarity(Rarity.RARE))
   );
   public static final Item UU_ASSEMBLY_BENCH = register(
      "uu_assembly_bench", new BlockItem(BlockDefinitions.UU_ASSEMBLY_BENCH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uu_assembly_bench"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item MATTER_GENERATOR = register(
      "matter_generator", new BlockItem(BlockDefinitions.MATTER_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("matter_generator"))).rarity(Rarity.RARE))
   );
   public static final Item PATTERN_STORAGE = register(
      "pattern_storage", new BlockItem(BlockDefinitions.PATTERN_STORAGE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("pattern_storage"))))
   );
   public static final Item REPLICATOR = register(
      "replicator", new BlockItem(BlockDefinitions.REPLICATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("replicator"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item UU_SCANNER = register(
      "uu_scanner", new BlockItem(BlockDefinitions.UU_SCANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uu_scanner"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item ENERGY_O_MAT = register("energy_o_mat", new BlockItem(BlockDefinitions.ENERGY_O_MAT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("energy_o_mat")))));
   public static final Item PERSONAL_CHEST = register(
      "personal_chest", new BlockItem(BlockDefinitions.PERSONAL_CHEST, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("personal_chest"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item TRADE_O_MAT = register("trade_o_mat", new BlockItem(BlockDefinitions.TRADE_O_MAT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("trade_o_mat")))));
   public static final Item RAW_LEAD = register("raw_lead", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("raw_lead")))));
   public static final Item RAW_TIN = register("raw_tin", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("raw_tin")))));
   public static final Item RAW_URANIUM = register("raw_uranium", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("raw_uranium")))));
   public static final Item CRUSHED_COPPER = register("crushed_copper", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crushed_copper")))));
   public static final Item CRUSHED_GOLD = register("crushed_gold", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crushed_gold")))));
   public static final Item CRUSHED_IRON = register("crushed_iron", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crushed_iron")))));
   public static final Item CRUSHED_LEAD = register("crushed_lead", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crushed_lead")))));
   public static final Item CRUSHED_SILVER = register("crushed_silver", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crushed_silver")))));
   public static final Item CRUSHED_TIN = register("crushed_tin", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crushed_tin")))));
   public static final Item CRUSHED_URANIUM = register("crushed_uranium", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crushed_uranium")))));
   public static final Item PURIFIED_COPPER = register("purified_copper", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("purified_copper")))));
   public static final Item PURIFIED_GOLD = register("purified_gold", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("purified_gold")))));
   public static final Item PURIFIED_IRON = register("purified_iron", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("purified_iron")))));
   public static final Item PURIFIED_LEAD = register("purified_lead", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("purified_lead")))));
   public static final Item PURIFIED_SILVER = register("purified_silver", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("purified_silver")))));
   public static final Item PURIFIED_TIN = register("purified_tin", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("purified_tin")))));
   public static final Item PURIFIED_URANIUM = register("purified_uranium", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("purified_uranium")))));
   public static final Item BRONZE_DUST = register("bronze_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_dust")))));
   public static final Item CLAY_DUST = register("clay_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("clay_dust")))));
   public static final Item COAL_DUST = register("coal_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("coal_dust")))));
   public static final Item COAL_FUEL_DUST = register("coal_fuel_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("coal_fuel_dust")))));
   public static final Item COPPER_DUST = register("copper_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("copper_dust")))));
   public static final Item DIAMOND_DUST = register("diamond_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("diamond_dust")))));
   public static final Item ENERGIUM_DUST = register("energium_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("energium_dust")))));
   public static final Item GOLD_DUST = register("gold_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("gold_dust")))));
   public static final Item IRON_DUST = register("iron_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_dust")))));
   public static final Item LAPIS_DUST = register("lapis_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lapis_dust")))));
   public static final Item LEAD_DUST = register("lead_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lead_dust")))));
   public static final Item LITHIUM_DUST = register("lithium_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lithium_dust")))));
   public static final Item OBSIDIAN_DUST = register("obsidian_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("obsidian_dust")))));
   public static final Item SILICON_DIOXIDE_DUST = register("silicon_dioxide_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("silicon_dioxide_dust")))));
   public static final Item SILVER_DUST = register("silver_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("silver_dust")))));
   public static final Item STONE_DUST = register("stone_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("stone_dust")))));
   public static final Item SULFUR_DUST = register("sulfur_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("sulfur_dust")))));
   public static final Item TIN_DUST = register("tin_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tin_dust")))));
   public static final Item SMALL_BRONZE_DUST = register("small_bronze_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_bronze_dust")))));
   public static final Item SMALL_COPPER_DUST = register("small_copper_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_copper_dust")))));
   public static final Item SMALL_GOLD_DUST = register("small_gold_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_gold_dust")))));
   public static final Item SMALL_IRON_DUST = register("small_iron_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_iron_dust")))));
   public static final Item SMALL_LAPIS_DUST = register("small_lapis_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_lapis_dust")))));
   public static final Item SMALL_LEAD_DUST = register("small_lead_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_lead_dust")))));
   public static final Item SMALL_LITHIUM_DUST = register("small_lithium_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_lithium_dust")))));
   public static final Item SMALL_OBSIDIAN_DUST = register("small_obsidian_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_obsidian_dust")))));
   public static final Item SMALL_SILVER_DUST = register("small_silver_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_silver_dust")))));
   public static final Item SMALL_SULFUR_DUST = register("small_sulfur_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_sulfur_dust")))));
   public static final Item SMALL_TIN_DUST = register("small_tin_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_tin_dust")))));
   public static final Item HYDRATED_TIN_DUST = register("hydrated_tin_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("hydrated_tin_dust")))));
   public static final Item NETHERRACK_DUST = register("netherrack_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("netherrack_dust")))));
   public static final Item MIXED_METAL_INGOT = register("mixed_metal_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mixed_metal_ingot")))));
   public static final Item BRONZE_INGOT = register("bronze_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_ingot")))));
   public static final Item COPPER_INGOT = register("copper_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("copper_ingot")))));
   public static final Item LEAD_INGOT = register("lead_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lead_ingot")))));
   public static final Item SILVER_INGOT = register("silver_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("silver_ingot")))));
   public static final Item STEEL_INGOT = register("steel_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_ingot")))));
   public static final Item TIN_INGOT = register("tin_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tin_ingot")))));
   public static final Item REFINED_IRON_INGOT = register("refined_iron_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("refined_iron_ingot")))));
   public static final Item URANIUM_INGOT = register("uranium_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uranium_ingot")))));
   public static final Item BRONZE_PLATE = register("bronze_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_plate")))));
   public static final Item COPPER_PLATE = register("copper_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("copper_plate")))));
   public static final Item GOLD_PLATE = register("gold_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("gold_plate")))));
   public static final Item IRON_PLATE = register("iron_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_plate")))));
   public static final Item LAPIS_PLATE = register("lapis_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lapis_plate")))));
   public static final Item LEAD_PLATE = register("lead_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lead_plate")))));
   public static final Item OBSIDIAN_PLATE = register("obsidian_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("obsidian_plate")))));
   public static final Item STEEL_PLATE = register("steel_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_plate")))));
   public static final Item TIN_PLATE = register("tin_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tin_plate")))));
   public static final Item DENSE_BRONZE_PLATE = register("dense_bronze_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dense_bronze_plate")))));
   public static final Item DENSE_COPPER_PLATE = register("dense_copper_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dense_copper_plate")))));
   public static final Item DENSE_GOLD_PLATE = register("dense_gold_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dense_gold_plate")))));
   public static final Item DENSE_IRON_PLATE = register("dense_iron_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dense_iron_plate")))));
   public static final Item DENSE_LAPIS_PLATE = register("dense_lapis_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dense_lapis_plate")))));
   public static final Item DENSE_LEAD_PLATE = register("dense_lead_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dense_lead_plate")))));
   public static final Item DENSE_OBSIDIAN_PLATE = register("dense_obsidian_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dense_obsidian_plate")))));
   public static final Item DENSE_STEEL_PLATE = register("dense_steel_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dense_steel_plate")))));
   public static final Item DENSE_TIN_PLATE = register("dense_tin_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dense_tin_plate")))));
   public static final Item BRONZE_CASING = register("bronze_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_casing")))));
   public static final Item COPPER_CASING = register("copper_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("copper_casing")))));
   public static final Item GOLD_CASING = register("gold_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("gold_casing")))));
   public static final Item IRON_CASING = register("iron_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_casing")))));
   public static final Item LEAD_CASING = register("lead_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lead_casing")))));
   public static final Item STEEL_CASING = register("steel_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_casing")))));
   public static final Item TIN_CASING = register("tin_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tin_casing")))));
   public static final Item URANIUM = register("uranium", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uranium"))), 60, 100));
   public static final Item URANIUM_235 = register("uranium_235", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uranium_235"))), 150, 100));
   public static final Item URANIUM_238 = register("uranium_238", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uranium_238"))), 10, 90));
   public static final Item PLUTONIUM = register("plutonium", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("plutonium"))), 150, 100));
   public static final Item MOX = register("mox", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mox"))), 300, 100));
   public static final Item SMALL_URANIUM_235 = register("small_uranium_235", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_uranium_235"))), 150, 100));
   public static final Item SMALL_URANIUM_238 = register("small_uranium_238", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_uranium_238"))), 10, 90));
   public static final Item SMALL_PLUTONIUM = register("small_plutonium", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_plutonium"))), 150, 100));
   public static final Item URANIUM_PELLET = register("uranium_pellet", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uranium_pellet"))), 60, 100));
   public static final Item MOX_PELLET = register("mox_pellet", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mox_pellet"))), 300, 100));
   public static final Item RTG_PELLET = register("rtg_pellet", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rtg_pellet"))).stacksTo(1), 2, 90));
   public static final Item DEPLETED_URANIUM_FUEL_ROD = register(
      "depleted_uranium_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("depleted_uranium_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_DUAL_URANIUM_FUEL_ROD = register(
      "depleted_dual_uranium_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("depleted_dual_uranium_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_QUAD_URANIUM_FUEL_ROD = register(
      "depleted_quad_uranium_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("depleted_quad_uranium_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_MOX_FUEL_ROD = register(
      "depleted_mox_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("depleted_mox_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_DUAL_MOX_FUEL_ROD = register(
      "depleted_dual_mox_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("depleted_dual_mox_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_QUAD_MOX_FUEL_ROD = register(
      "depleted_quad_mox_fuel_rod", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("depleted_quad_mox_fuel_rod"))), 10, 100)
   );
   public static final Item NEAR_DEPLETED_URANIUM = register(
      "near_depleted_uranium", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("near_depleted_uranium"))), 15, 100)
   );
   public static final Item RE_ENRICHED_URANIUM = register(
      "re_enriched_uranium", new RadioactiveMaterial(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("re_enriched_uranium"))), 30, 100)
   );
   public static final Item ASHES = register("ashes", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("ashes")))));
   public static final Item IRIDIUM_ORE = register("iridium_ore", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iridium_ore"))).rarity(Rarity.RARE)));
   public static final Item IRIDIUM_SHARD = register("iridium_shard", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iridium_shard"))).rarity(Rarity.UNCOMMON)));
   public static final Item UU_MATTER = register("uu_matter", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uu_matter"))).rarity(Rarity.RARE)));
   public static final Item RESIN = register("resin", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("resin")))));
   public static final Item SLAG = register("slag", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("slag")))));
   public static final Item IODINE = register("iodine", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iodine")))));
   public static final Item WATER_SHEET = register("water_sheet", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("water_sheet")))));
   public static final Item LAVA_SHEET = register("lava_sheet", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lava_sheet")))));
   public static final Item RUBBER = register("rubber", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber")))));
   public static final Item CIRCUIT = register("circuit", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("circuit")))));
   public static final Item ADVANCED_CIRCUIT = register("advanced_circuit", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("advanced_circuit"))).rarity(Rarity.UNCOMMON)));
   public static final Item ALLOY = register("alloy", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("alloy")))));
   public static final Item IRIDIUM = register("iridium", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iridium"))).rarity(Rarity.RARE)));
   public static final Item COIL = register("coil", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("coil")))));
   public static final Item ELECTRIC_MOTOR = register("electric_motor", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("electric_motor")))));
   public static final Item HEAT_CONDUCTOR = register("heat_conductor", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("heat_conductor")))));
   public static final Item COPPER_BOILER = register("copper_boiler", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("copper_boiler")))));
   public static final Item FUEL_ROD = register("fuel_rod", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fuel_rod")))));
   public static final Item TIN_CAN = register("tin_can", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tin_can")))));
   public static final Item IRON_CUTTING_BLADE = register(
      "iron_cutting_blade", new CuttingBlade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_cutting_blade"))), BladeMaterial.iron)
   );
   public static final Item DIAMOND_CUTTING_BLADE = register(
      "diamond_cutting_blade", new CuttingBlade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("diamond_cutting_blade"))), BladeMaterial.diamond)
   );
   public static final Item STEEL_CUTTING_BLADE = register(
      "steel_cutting_blade", new CuttingBlade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_cutting_blade"))), BladeMaterial.steel)
   );
   public static final Item SMALL_POWER_UNIT = register("small_power_unit", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("small_power_unit")))));
   public static final Item POWER_UNIT = register("power_unit", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("power_unit")))));
   public static final Item CARBON_FIBRE = register("carbon_fibre", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("carbon_fibre")))));
   public static final Item CARBON_MESH = register("carbon_mesh", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("carbon_mesh")))));
   public static final Item CARBON_PLATE = register("carbon_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("carbon_plate")))));
   public static final Item COAL_BALL = register("coal_ball", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("coal_ball")))));
   public static final Item COAL_BLOCK = register("coal_block", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("coal_block")))));
   public static final Item COAL_CHUNK = register("coal_chunk", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("coal_chunk")))));
   public static final Item INDUTRIAL_DIAMOND = register("industrial_diamond", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("industrial_diamond")))));
   public static final Item PLANT_BALL = register("plant_ball", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("plant_ball")))));
   public static final Item COMPRESSED_PLANTS = register("compressed_plants", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("compressed_plants")))));
   public static final Item BIO_CHAFF = register("bio_chaff", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bio_chaff")))));
   public static final Item COMPRESSED_HYDRATED_COAL = register("compressed_hydrated_coal", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("compressed_hydrated_coal")))));
   public static final Item SCRAP = register("scrap", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("scrap")))));
   public static final Item SCRAP_BOX = register("scrap_box", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("scrap_box")))));
   public static final Item CF_POWDER = register("cf_powder", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cf_powder")))));
   public static final Item PELLET = register("pellet", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("pellet")))));
   public static final Item RAW_CRYSTAL_MEMORY = register("raw_crystal_memory", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("raw_crystal_memory")))));
   public static final Item CRYSTAL_MEMORY = register("crystal_memory", new ItemCrystalMemory(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crystal_memory"))).stacksTo(1)));
   public static final Item IRON_SHAFT = register("iron_shaft", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_shaft")))));
   public static final Item STEEL_SHAFT = register("steel_shaft", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_shaft")))));
   public static final Item WOODEN_ROTOR_BLADE = register("wooden_rotor_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wooden_rotor_blade")))));
   public static final Item IRON_ROTOR_BLADE = register("iron_rotor_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_rotor_blade")))));
   public static final Item STEEL_ROTOR_BLADE = register("steel_rotor_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_rotor_blade")))));
   public static final Item CARBON_ROTOR_BLADE = register("carbon_rotor_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("carbon_rotor_blade")))));
   public static final Item STEAM_TURBINE_BLADE = register("steam_turbine_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steam_turbine_blade")))));
   public static final Item STEAM_TURBINE = register("steam_turbine", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steam_turbine")))));
   public static final Item JETPACK_ATTACHMENT_PLATE = register("jetpack_attachment_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("jetpack_attachment_plate")))));
   public static final Item COIN = register("coin", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("coin")))));
   public static final Item EMPTY_FUEL_CAN = register("empty_fuel_can", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("empty_fuel_can")))));
   public static final Item BRONZE_ROTOR_BLADE = register("bronze_rotor_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_rotor_blade")))));
   public static final Item BRONZE_SHAFT = register("bronze_shaft", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_shaft")))));
   public static final Item REACTOR_COOLANT_CELL = register(
      "reactor_coolant_cell", new HeatCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_coolant_cell"))), 10000)
   );
   public static final Item TRIPLE_REACTOR_COOLANT_CELL = register(
      "triple_reactor_coolant_cell", new HeatCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("triple_reactor_coolant_cell"))), 30000)
   );
   public static final Item SEXTUPLE_REACTOR_COOLANT_CELL = register(
      "sextuple_reactor_coolant_cell", new HeatCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("sextuple_reactor_coolant_cell"))), 60000)
   );
   public static final Item REACTOR_PLATING = register("reactor_plating", new HullPlating(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_plating"))), 1000, 0.95F));
   public static final Item REACTOR_HEAT_PLATING = register(
      "reactor_heat_plating", new HullPlating(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_heat_plating"))), 2000, 0.99F)
   );
   public static final Item CONTAINMENT_REACTOR_PLATING = register(
      "containment_reactor_plating", new HullPlating(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("containment_reactor_plating"))), 500, 0.9F)
   );
   public static final Item HEAT_EXCHANGER = register("heat_exchanger", new HeatExchanger(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("heat_exchanger"))), 2500, 12, 4));
   public static final Item REACTOR_HEAT_EXCHANGER = register(
      "reactor_heat_exchanger", new HeatExchanger(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_heat_exchanger"))), 5000, 0, 72)
   );
   public static final Item COMPONENT_HEAT_EXCHANGER = register(
      "component_heat_exchanger", new HeatExchanger(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("component_heat_exchanger"))), 5000, 36, 0)
   );
   public static final Item ADVANCED_HEAT_EXCHANGER = register(
      "advanced_heat_exchanger", new HeatExchanger(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("advanced_heat_exchanger"))), 10000, 24, 8)
   );
   public static final Item HEAT_VENT = register("heat_vent", new HeatVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("heat_vent"))), 1000, 6, 0));
   public static final Item REACTOR_HEAT_VENT = register("reactor_heat_vent", new HeatVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_heat_vent"))), 1000, 5, 5));
   public static final Item OVERCLOCKED_HEAT_VENT = register(
      "overclocked_heat_vent", new HeatVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("overclocked_heat_vent"))), 1000, 20, 36)
   );
   public static final Item COMPONENT_HEAT_VENT = register("component_heat_vent", new NeighborHeatVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("component_heat_vent"))), 4));
   public static final Item ADVANCED_HEAT_VENT = register("advanced_heat_vent", new HeatVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("advanced_heat_vent"))), 1000, 12, 0));
   public static final Item NEUTRON_REFLECTOR = register("neutron_reflector", new NeutronReflector(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("neutron_reflector"))), 30000));
   public static final Item THICK_NEUTRON_REFLECTOR = register(
      "thick_neutron_reflector", new NeutronReflector(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("thick_neutron_reflector"))), 120000)
   );
   public static final Item IRIDIUM_NEUTRON_REFLECTOR = register(
      "iridium_neutron_reflector", new PermanentReflector(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iridium_neutron_reflector"))))
   );
   public static final Item RSH_CONDENSATOR = register("rsh_condensator", new ChemicalCondensator(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rsh_condensator"))), 20000));
   public static final Item LZH_CONDENSATOR = register("lzh_condensator", new ChemicalCondensator(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lzh_condensator"))), 100000));
   public static final Item HEATPACK = register("heatpack", new HeatPack(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("heatpack"))), 1000, 1));
   public static final Item REACTOR_VESSEL = register("reactor_vessel", new BlockItem(BlockDefinitions.REACTOR_VESSEL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_vessel")))));
   public static final Item URANIUM_FUEL_ROD = register("uranium_fuel_rod", new UraniumFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uranium_fuel_rod"))), 1));
   public static final Item DUAL_URANIUM_FUEL_ROD = register("dual_uranium_fuel_rod", new UraniumFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dual_uranium_fuel_rod"))), 2));
   public static final Item QUAD_URANIUM_FUEL_ROD = register("quad_uranium_fuel_rod", new UraniumFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("quad_uranium_fuel_rod"))), 4));
   public static final Item MOX_FUEL_ROD = register("mox_fuel_rod", new MoxFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mox_fuel_rod"))), 1));
   public static final Item DUAL_MOX_FUEL_ROD = register("dual_mox_fuel_rod", new MoxFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dual_mox_fuel_rod"))), 2));
   public static final Item QUAD_MOX_FUEL_ROD = register("quad_mox_fuel_rod", new MoxFuel(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("quad_mox_fuel_rod"))), 4));
   public static final Item LITHIUM_FUEL_ROD = register("lithium_fuel_rod", new LithiumBreeder(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lithium_fuel_rod")))));
   public static final Item DEPLETED_ISOTOPE_FUEL_ROD = register(
      "depleted_isotope_fuel_rod", new IsotopeBreeder(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("depleted_isotope_fuel_rod"))))
   );
   public static final Item EMPTY_MUG = register("empty_mug", new CoffeeCup(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("empty_mug"))).stacksTo(1), CoffeeCup.MugType.empty));
   public static final Item COFFEE_MUG = register("coffee_mug", new CoffeeCup(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("coffee_mug"))).stacksTo(1), CoffeeCup.MugType.coffee));
   public static final Item COLD_COFFEE_MUG = register(
      "cold_coffee_mug", new CoffeeCup(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cold_coffee_mug"))).stacksTo(1), CoffeeCup.MugType.cold_coffee)
   );
   public static final Item DARK_COFFEE_MUG = register(
      "dark_coffee_mug", new CoffeeCup(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("dark_coffee_mug"))).stacksTo(1), CoffeeCup.MugType.dark_coffee)
   );
   public static final Item CROP_STICK = register("crop_stick", new CropSupportItem(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crop_stick")))));
   public static final Item CROP_SEED_BACK = register("crop_seed_bag", new EncodedSeed(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crop_seed_bag"))).stacksTo(1)));
   public static final Item COFFEE_BEANS = register("coffee_beans", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("coffee_beans")))));
   public static final Item COFFEE_POWDER = register("coffee_powder", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("coffee_powder")))));
   public static final Item FERTILIZER = register("fertilizer", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fertilizer")))));
   public static final Item GRIN_POWDER = register("grin_powder", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("grin_powder")))));
   public static final Item HOPS = register("hops", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("hops")))));
   public static final Item WEED = register("weed", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("weed")))));
   public static final Item TERRA_WART = register(
      "terra_wart",
      new MedicinalWart(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("terra_wart"))).food(new FoodProperties.Builder().nutrition(0).saturationModifier(1.0F).alwaysEdible().build()).rarity(Rarity.RARE)
      )
   );
   public static final Item CUTTER = register("cutter", new InsulationCutter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cutter"))).durability(60)));
   public static final Item DEBUG_ITEM = register(
      "debug_item", new DiagnosticTool(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("debug_item"))).stacksTo(1))
   );
   public static final Item FORGE_HAMMER = register("forge_hammer", new DurableCraftingTool(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("forge_hammer"))).durability(80)));
   public static final Item TOOL_BOX = register("tool_box", new PortableToolbox(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tool_box"))).stacksTo(1)));
   public static final Item METER = register("meter", new EnergyProbe(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("meter"))).stacksTo(1)));
   public static final Item TREETAP = register("treetap", new ManualResinTap(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("treetap"))).durability(16)));
   public static final Item WRENCH = register("wrench", new MechanicalWrench(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wrench"))).durability(120)));
   public static final Item BRONZE_AXE = register(
      "bronze_axe", new net.minecraft.world.item.AxeItem(ToolProfiles.BRONZE, 6.0F, -3.1F, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_axe"))))
   );
   public static final Item BRONZE_HOE = register(
      "bronze_hoe", new net.minecraft.world.item.HoeItem(ToolProfiles.BRONZE, -2, -1.0F, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_hoe"))))
   );
   public static final Item BRONZE_SWORD = register(
      "bronze_sword", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_sword"))).sword(ToolProfiles.BRONZE, 3.0F, -2.4F))
   );
   public static final Item BRONZE_SHOVEL = register(
      "bronze_shovel", new ShovelItem(ToolProfiles.BRONZE, 1.5F, -3.0F, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_shovel"))))
   );
   public static final Item BRONZE_PICKAXE = register(
      "bronze_pickaxe", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_pickaxe"))).pickaxe(ToolProfiles.BRONZE, 1, -2.8F))
   );
   public static final Item FREQUENCY_TRANSMITTER = register(
      "frequency_transmitter", new TeleporterLinkCard(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("frequency_transmitter"))).stacksTo(1))
   );
   public static final Item CROWBAR = register("crowbar", new CoverCrowbar(ToolMaterial.IRON, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crowbar"))).durability(250)));
   public static final Item ADVANCED_SCANNER = register(
      "advanced_scanner", new AdvancedOreScanner(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("advanced_scanner"))).stacksTo(1))
   );
   public static final Item CHAINSAW = register("chainsaw", new PoweredSaw(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("chainsaw"))).stacksTo(1)));
   public static final Item DIAMOND_DRILL = register(
      "diamond_drill", new MiningDrill(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("diamond_drill"))).stacksTo(1), 80, ToolMaterial.DIAMOND, 30000, 100, 1, 16.0F)
   );
   public static final Item DRILL = register(
      "drill", new MiningDrill(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("drill"))).stacksTo(1), 50, ToolMaterial.IRON, 30000, 100, 1, 8.0F)
   );
   public static final Item MINING_LASER = register("mining_laser", new MiningLaser(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mining_laser"))).stacksTo(1)));
   public static final Item ELECTRIC_TREETAP = register(
      "electric_treetap", new ElectricResinTap(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("electric_treetap"))).stacksTo(1))
   );
   public static final Item ELECTRIC_WRENCH = register(
      "electric_wrench", new ElectricWrench(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("electric_wrench"))).stacksTo(1))
   );
   public static final Item IRIDIUM_DRILL = register("iridium_drill", new IridiumDrill(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iridium_drill"))).stacksTo(1)));
   public static final Item OBSCURATOR = register("obscurator", new SurfaceImprinter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("obscurator"))).stacksTo(1)));
   public static final Item SCANNER = register(
      "scanner", new OreScanner(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("scanner"))).stacksTo(1), 100000.0, 128.0, 1)
   );
   public static final Item WIND_METER = register("wind_meter", new WindProbe(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wind_meter"))).stacksTo(1)));
   public static final Item PAINTER = register(
      "painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("painter"))).stacksTo(1).durability(32), null)
   );
   public static final Item BLACK_PAINTER = register(
      "black_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("black_painter"))).stacksTo(1).durability(32), PigmentPalette.BLACK)
   );
   public static final Item BLUE_PAINTER = register(
      "blue_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("blue_painter"))).stacksTo(1).durability(32), PigmentPalette.BLUE)
   );
   public static final Item BROWN_PAINTER = register(
      "brown_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("brown_painter"))).stacksTo(1).durability(32), PigmentPalette.BROWN)
   );
   public static final Item CYAN_PAINTER = register(
      "cyan_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cyan_painter"))).stacksTo(1).durability(32), PigmentPalette.CYAN)
   );
   public static final Item GRAY_PAINTER = register(
      "gray_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("gray_painter"))).stacksTo(1).durability(32), PigmentPalette.GRAY)
   );
   public static final Item GREEN_PAINTER = register(
      "green_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("green_painter"))).stacksTo(1).durability(32), PigmentPalette.GREEN)
   );
   public static final Item LIGHT_BLUE_PAINTER = register(
      "light_blue_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("light_blue_painter"))).stacksTo(1).durability(32), PigmentPalette.LIGHT_BLUE)
   );
   public static final Item LIGHT_GRAY_PAINTER = register(
      "light_gray_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("light_gray_painter"))).stacksTo(1).durability(32), PigmentPalette.LIGHT_GRAY)
   );
   public static final Item LIME_PAINTER = register(
      "lime_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lime_painter"))).stacksTo(1).durability(32), PigmentPalette.LIME)
   );
   public static final Item MAGENTA_PAINTER = register(
      "magenta_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("magenta_painter"))).stacksTo(1).durability(32), PigmentPalette.MAGENTA)
   );
   public static final Item ORANGE_PAINTER = register(
      "orange_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("orange_painter"))).stacksTo(1).durability(32), PigmentPalette.ORANGE)
   );
   public static final Item PINK_PAINTER = register(
      "pink_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("pink_painter"))).stacksTo(1).durability(32), PigmentPalette.PINK)
   );
   public static final Item PURPLE_PAINTER = register(
      "purple_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("purple_painter"))).stacksTo(1).durability(32), PigmentPalette.PURPLE)
   );
   public static final Item RED_PAINTER = register(
      "red_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("red_painter"))).stacksTo(1).durability(32), PigmentPalette.RED)
   );
   public static final Item WHITE_PAINTER = register(
      "white_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("white_painter"))).stacksTo(1).durability(32), PigmentPalette.WHITE)
   );
   public static final Item YELLOW_PAINTER = register(
      "yellow_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("yellow_painter"))).stacksTo(1).durability(32), PigmentPalette.YELLOW)
   );
   public static final Item BROKEN_RUBBER_BOAT = register("broken_rubber_boat", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("broken_rubber_boat"))).stacksTo(1)));
   public static final Item RUBBER_BOAT = register(
      "rubber_boat", new HullItem(RubberHull.class, EntityDefinitions.RUBBER_BOAT, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_boat"))).stacksTo(1))
   );
   public static final Item ELECTRIC_BOAT = register(
      "electric_boat", new HullItem(ElectricHull.class, EntityDefinitions.ELECTRIC_BOAT, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("electric_boat"))).stacksTo(1))
   );
   public static final Item CARBON_BOAT = register(
      "carbon_boat", new HullItem(CarbonHull.class, EntityDefinitions.CARBON_BOAT, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("carbon_boat"))).stacksTo(1))
   );
   public static final Item RE_BATTERY = register("re_battery", new RechargeableSupply(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("re_battery"))), 10000.0, 100.0, 1));
   public static final Item ADVANCED_RE_BATTERY = register(
      "advanced_re_battery", new RechargeableSupply(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("advanced_re_battery"))).stacksTo(16), 100000.0, 256.0, 2)
   );
   public static final Item ENERGY_CRYSTAL = register(
      "energy_crystal", new RechargeableSupply(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("energy_crystal"))).stacksTo(16), 1000000.0, 2048.0, 3)
   );
   public static final Item LAPOTRON_CRYSTAL = register(
      "lapotron_crystal", new RechargeableSupply(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lapotron_crystal"))).stacksTo(16).rarity(Rarity.UNCOMMON), 1.0E7, 8092.0, 4)
   );
   public static final Item SINGLE_USE_BATTERY = register(
      "single_use_battery", new DisposableSupply(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("single_use_battery"))), 1200, 1)
   );
   public static final Item WOODEN_ROTOR = register(
      "wooden_rotor",
      new RotorItem(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wooden_rotor"))).durability(10800),
         5,
         false,
         0.25F,
         10,
         60,
         ProjectRuntime.getIdentifier("textures/item/rotor/wood_rotor_model.png")
      )
   );
   public static final Item BRONZE_ROTOR = register(
      "bronze_rotor",
      new RotorItem(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_rotor"))).durability(86400),
         7,
         true,
         0.5F,
         14,
         75,
         Identifier.fromNamespaceAndPath("ic2", "textures/item/rotor/bronze_rotor_model.png")
      )
   );
   public static final Item IRON_ROTOR = register(
      "iron_rotor",
      new RotorItem(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_rotor"))).durability(86400),
         7,
         true,
         0.5F,
         14,
         75,
         Identifier.fromNamespaceAndPath("ic2", "textures/item/rotor/iron_rotor_model.png")
      )
   );
   public static final Item STEEL_ROTOR = register(
      "steel_rotor",
      new RotorItem(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_rotor"))).durability(172800),
         9,
         true,
         0.75F,
         17,
         90,
         Identifier.fromNamespaceAndPath("ic2", "textures/item/rotor/steel_rotor_model.png")
      )
   );
   public static final Item CARBON_ROTOR = register(
      "carbon_rotor",
      new RotorItem(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("carbon_rotor"))).durability(604800),
         11,
         true,
         1.0F,
         20,
         110,
         Identifier.fromNamespaceAndPath("ic2", "textures/item/rotor/carbon_rotor_model.png")
      )
   );
   public static final Item EMPTY_CELL = register("empty_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("empty_cell"))), Fluids.EMPTY, 0));
   public static final Item WATER_CELL = register("water_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("water_cell"))), Fluids.WATER, 1));
   public static final Item LAVA_CELL = register("lava_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lava_cell"))), Fluids.LAVA, 1));
   public static final Item AIR_CELL = register("air_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("air_cell"))), FluidDefinitions.AIR.still, 1));
   public static final Item ELECTROLYZED_WATER_CELL = register(
      "electrolyzed_water_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("electrolyzed_water_cell"))), null, 1)
   );
   public static final Item BIOFUEL_CELL = register("biofuel_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("biofuel_cell"))), null, 1));
   public static final Item COALFUEL_CELL = register("coalfuel_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("coalfuel_cell"))), null, 1));
   public static final Item BIO_CELL = register("bio_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bio_cell"))), null, 1));
   public static final Item HYDRATED_COAL_CELL = register(
      "hydrated_coal_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("hydrated_coal_cell"))), null, 1)
   );
   public static final FixedCell WEED_EX_CELL = register(
      "weed_ex_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("weed_ex_cell"))).stacksTo(1), null, 64)
   );
   public static final FixedCell HYDRATION_CELL = register(
      "hydration_cell", new FixedCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("hydration_cell"))).stacksTo(1), null, 10000)
   );
   public static final Item OVERCLOCKER_UPGRADE = register(
      "overclocker_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("overclocker_upgrade"))), MachineUpgrade.UpgradeType.overclocker)
   );
   public static final Item TRANSFORMER_UPGRADE = register(
      "transformer_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("transformer_upgrade"))), MachineUpgrade.UpgradeType.transformer)
   );
   public static final Item ENERGY_STORAGE_UPGRADE = register(
      "energy_storage_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("energy_storage_upgrade"))), MachineUpgrade.UpgradeType.energy_storage)
   );
   public static final Item REDSTONE_INVERTER_UPGRADE = register(
      "redstone_inverter_upgrade",
      new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("redstone_inverter_upgrade"))), MachineUpgrade.UpgradeType.redstone_inverter)
   );
   public static final Item EJECTOR_UPGRADE = register(
      "ejector_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("ejector_upgrade"))), MachineUpgrade.UpgradeType.ejector)
   );
   public static final Item ADVANCED_EJECTOR_UPGRADE = register(
      "advanced_ejector_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("advanced_ejector_upgrade"))), MachineUpgrade.UpgradeType.advanced_ejector)
   );
   public static final Item PULLING_UPGRADE = register(
      "pulling_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("pulling_upgrade"))), MachineUpgrade.UpgradeType.pulling)
   );
   public static final Item ADVANCED_PULLING_UPGRADE = register(
      "advanced_pulling_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("advanced_pulling_upgrade"))), MachineUpgrade.UpgradeType.advanced_pulling)
   );
   public static final Item FLUID_EJECTOR_UPGRADE = register(
      "fluid_ejector_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fluid_ejector_upgrade"))), MachineUpgrade.UpgradeType.fluid_ejector)
   );
   public static final Item FLUID_PULLING_UPGRADE = register(
      "fluid_pulling_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fluid_pulling_upgrade"))), MachineUpgrade.UpgradeType.fluid_pulling)
   );
   public static final Item REMOTE_INTERFACE_UPGRADE = register(
      "remote_interface_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("remote_interface_upgrade"))), MachineUpgrade.UpgradeType.remote_interface)
   );
   public static final Item FILLED_TIN_CAN = register("filled_tin_can", new CannedFood(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("filled_tin_can")))));
   public static final Item FILLED_FUEL_CAN=register("filled_fuel_can",new ic2ce.neoforge.item.consumable.FuelCan(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,ProjectRuntime.getIdentifier("filled_fuel_can"))).stacksTo(1)));
   public static final Item BLANK_TFBP = register("blank_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("blank_tfbp"))).stacksTo(1), 0.0, 0, null));
   public static final Item CHILLING_TFBP = register(
      "chilling_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("chilling_tfbp"))).stacksTo(1), 2000.0, 50, new FrostProgram())
   );
   public static final Item CULTIVATION_TFBP = register(
      "cultivation_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cultivation_tfbp"))).stacksTo(1), 4000.0, 40, new CultivationProgram())
   );
   public static final Item DESERTIFICATION_TFBP = register(
      "desertification_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("desertification_tfbp"))).stacksTo(1), 2500.0, 40, new AridProgram())
   );
   public static final Item FLATIFICATION_TFBP = register(
      "flatification_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("flatification_tfbp"))).stacksTo(1), 4000.0, 40, new LevelingProgram())
   );
   public static final Item IRRIGATION_TFBP = register(
      "irrigation_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("irrigation_tfbp"))).stacksTo(1), 3000.0, 60, new IrrigationProgram())
   );
   public static final Item MUSHROOM_TFBP = register(
      "mushroom_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mushroom_tfbp"))).stacksTo(1), 8000.0, 25, new FungalProgram())
   );
   public static final Item NANO_SABER = register("nano_saber", new NanoBlade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("nano_saber"))).stacksTo(1)));
   public static final Item ALLOY_CHESTPLATE = register(
      "alloy_chestplate", new EquipmentItem(ArmorProfiles.ALLOY, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("alloy_chestplate"))), EquipmentSlot.CHEST)
   );
   public static final Item BRONZE_BOOTS = register(
      "bronze_boots", new EquipmentItem(ArmorProfiles.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_boots"))), EquipmentSlot.FEET)
   );
   public static final Item BRONZE_CHESTPLATE = register(
      "bronze_chestplate", new EquipmentItem(ArmorProfiles.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_chestplate"))), EquipmentSlot.CHEST)
   );
   public static final Item BRONZE_HELMET = register(
      "bronze_helmet", new EquipmentItem(ArmorProfiles.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_helmet"))), EquipmentSlot.HEAD)
   );
   public static final Item BRONZE_LEGGINGS = register(
      "bronze_leggings", new EquipmentItem(ArmorProfiles.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_leggings"))), EquipmentSlot.LEGS)
   );
   public static final Item CF_PACK = register("cf_pack", new FoamReservoir(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cf_pack")))));
   public static final Item HAZMAT_CHESTPLATE = register(
      "hazmat_chestplate", new SealedGarment(EquipmentSlot.CHEST, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("hazmat_chestplate"))))
   );
   public static final Item HAZMAT_HELMET = register("hazmat_helmet", new SealedGarment(EquipmentSlot.HEAD, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("hazmat_helmet")))));
   public static final Item HAZMAT_LEGGINGS = register("hazmat_leggings", new SealedGarment(EquipmentSlot.LEGS, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("hazmat_leggings")))));
   public static final Item JETPACK = register("jetpack", new BiogasJetpack(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("jetpack")))));
   public static final Item NANO_BOOTS = register(
      "nano_boots",
      new NanoArmor(ArmorProfiles.NANO_SUIT, EquipmentSlot.FEET, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("nano_boots"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item NANO_CHESTPLATE = register(
      "nano_chestplate",
      new NanoArmor(ArmorProfiles.NANO_SUIT, EquipmentSlot.CHEST, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("nano_chestplate"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item NANO_HELMET = register(
      "nano_helmet",
      new NanoArmor(ArmorProfiles.NANO_SUIT, EquipmentSlot.HEAD, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("nano_helmet"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item NANO_LEGGINGS = register(
      "nano_leggings",
      new NanoArmor(ArmorProfiles.NANO_SUIT, EquipmentSlot.LEGS, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("nano_leggings"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_BOOTS = register(
      "quantum_boots",
      new QuantumArmor(ArmorProfiles.QUANTUM_SUIT, EquipmentSlot.FEET, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("quantum_boots"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_CHESTPLATE = register(
      "quantum_chestplate",
      new QuantumArmor(ArmorProfiles.QUANTUM_SUIT, EquipmentSlot.CHEST, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("quantum_chestplate"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_HELMET = register(
      "quantum_helmet",
      new QuantumArmor(ArmorProfiles.QUANTUM_SUIT, EquipmentSlot.HEAD, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("quantum_helmet"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_LEGGINGS = register(
      "quantum_leggings",
      new QuantumArmor(ArmorProfiles.QUANTUM_SUIT, EquipmentSlot.LEGS, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("quantum_leggings"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item RUBBER_BOOTS = register("rubber_boots", new SealedGarment(EquipmentSlot.FEET, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_boots")))));
   public static final Item NIGHT_VISION_GOGGLES = register(
      "night_vision_goggles", new NightVisionGoggles(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("night_vision_goggles"))).durability(27))
   );

   public static void init() {
      ProjectRuntime.envProxy.registerBurnTime(Items.SUGAR_CANE, 50);
      ProjectRuntime.envProxy.registerBurnTime(Items.CACTUS, 50);
      ProjectRuntime.envProxy.registerBurnTime(RUBBER_SAPLING, 80);
      ProjectRuntime.envProxy.registerBurnTime(WOODEN_SCAFFOLD, 300);
      ProjectRuntime.envProxy.registerBurnTime(WOODEN_STORAGE_BOX, 1200);
      ProjectRuntime.envProxy.registerBurnTime(WOODEN_ROTOR_BLADE, 300);
      ProjectRuntime.envProxy.registerBurnTime(WOODEN_ROTOR, 300);
      ProjectRuntime.envProxy.registerBurnTime(SCRAP, 350);
      ProjectRuntime.envProxy.registerBurnTime(SCRAP_BOX, 3150);
   }

   private static <T extends Item> T register(String name, T item) {
      ProjectRuntime.envProxy.registerItem(ProjectRuntime.getIdentifier(name), item);
      return item;
   }
}
