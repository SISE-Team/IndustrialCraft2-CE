package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.crop.ItemCrop;
import ic2.core.entity.boat.CarbonBoatEntity;
import ic2.core.entity.boat.ElectricBoatEntity;
import ic2.core.entity.boat.RubberBoatEntity;
import ic2.core.item.ItemBattery;
import ic2.core.item.ItemBatterySU;
import ic2.core.item.ItemClassicCell;
import ic2.core.item.ItemCropSeed;
import ic2.core.item.ItemCrystalMemory;
import ic2.core.item.ItemMug;
import ic2.core.item.ItemNuclearResource;
import ic2.core.item.ItemTerraWart;
import ic2.core.item.ItemTinCan;
import ic2.core.item.armor.ItemArmorCFPack;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.item.armor.ItemArmorIC2;
import ic2.core.item.armor.ItemArmorJetpack;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.item.armor.ItemArmorNightVisionGoggles;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import ic2.core.item.boat.BoatItem;
import ic2.core.item.crafting.BlockCuttingBlade;
import ic2.core.item.reactor.ItemReactorCondensator;
import ic2.core.item.reactor.ItemReactorDepletedUranium;
import ic2.core.item.reactor.ItemReactorHeatStorage;
import ic2.core.item.reactor.ItemReactorHeatSwitch;
import ic2.core.item.reactor.ItemReactorHeatpack;
import ic2.core.item.reactor.ItemReactorIridiumReflector;
import ic2.core.item.reactor.ItemReactorLithiumCell;
import ic2.core.item.reactor.ItemReactorMOX;
import ic2.core.item.reactor.ItemReactorPlating;
import ic2.core.item.reactor.ItemReactorReflector;
import ic2.core.item.reactor.ItemReactorUranium;
import ic2.core.item.reactor.ItemReactorVent;
import ic2.core.item.reactor.ItemReactorVentSpread;
import ic2.core.item.resources.ItemWindRotor;
import ic2.core.item.tfbp.Chilling;
import ic2.core.item.tfbp.Cultivation;
import ic2.core.item.tfbp.Desertification;
import ic2.core.item.tfbp.Flatification;
import ic2.core.item.tfbp.Irrigation;
import ic2.core.item.tfbp.Mushroom;
import ic2.core.item.tfbp.Tfbp;
import ic2.core.item.tool.Ic2Axe;
import ic2.core.item.tool.Ic2Hoe;
import ic2.core.item.tool.Ic2Pickaxe;
import ic2.core.item.tool.ItemDebug;
import ic2.core.item.tool.ItemDrill;
import ic2.core.item.tool.ItemDrillIridium;
import ic2.core.item.tool.ItemElectricToolChainsaw;
import ic2.core.item.tool.ItemFrequencyTransmitter;
import ic2.core.item.tool.ItemNanoSaber;
import ic2.core.item.tool.ItemObscurator;
import ic2.core.item.tool.ItemScanner;
import ic2.core.item.tool.ItemScannerAdv;
import ic2.core.item.tool.ItemToolCrafting;
import ic2.core.item.tool.ItemToolCrowbar;
import ic2.core.item.tool.ItemToolCutter;
import ic2.core.item.tool.ItemToolMeter;
import ic2.core.item.tool.ItemToolMiningLaser;
import ic2.core.item.tool.ItemToolPainter;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.item.tool.ItemToolWrenchElectric;
import ic2.core.item.tool.ItemToolbox;
import ic2.core.item.tool.ItemTreetap;
import ic2.core.item.tool.ItemTreetapElectric;
import ic2.core.item.tool.ItemWindMeter;
import ic2.core.item.type.BlockCuttingBladeType;
import ic2.core.item.upgrade.ItemUpgradeModule;
import ic2.core.util.Ic2Color;
import ic2.core.util.Util;
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

public final class Ic2Items {
   public static final Item BASALT = register("basalt", new BlockItem(Ic2Blocks.BASALT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("basalt")))));
   public static final Item LEAD_ORE = register("lead_ore", new BlockItem(Ic2Blocks.LEAD_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lead_ore")))));
   public static final Item TIN_ORE = register("tin_ore", new BlockItem(Ic2Blocks.TIN_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("tin_ore")))));
   public static final Item URANIUM_ORE = register("uranium_ore", new BlockItem(Ic2Blocks.URANIUM_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("uranium_ore")))));
   public static final Item DEEPSLATE_URANIUM_ORE = register(
      "deepslate_uranium_ore", new BlockItem(Ic2Blocks.DEEPSLATE_URANIUM_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("deepslate_uranium_ore"))))
   );
   public static final Item DEEPSLATE_TIN_ORE = register(
      "deepslate_tin_ore", new BlockItem(Ic2Blocks.DEEPSLATE_TIN_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("deepslate_tin_ore"))))
   );
   public static final Item DEEPSLATE_LEAD_ORE = register(
      "deepslate_lead_ore", new BlockItem(Ic2Blocks.DEEPSLATE_LEAD_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("deepslate_lead_ore"))))
   );
   public static final Item RAW_LEAD_BLOCK = register("raw_lead_block", new BlockItem(Ic2Blocks.RAW_LEAD_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("raw_lead_block")))));
   public static final Item RAW_TIN_BLOCK = register("raw_tin_block", new BlockItem(Ic2Blocks.RAW_TIN_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("raw_tin_block")))));
   public static final Item RAW_URANIUM_BLOCK = register(
      "raw_uranium_block", new BlockItem(Ic2Blocks.RAW_URANIUM_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("raw_uranium_block"))))
   );
   public static final Item BRONZE_BLOCK = register("bronze_block", new BlockItem(Ic2Blocks.BRONZE_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_block")))));
   public static final Item LEAD_BLOCK = register("lead_block", new BlockItem(Ic2Blocks.LEAD_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lead_block")))));
   public static final Item STEEL_BLOCK = register("steel_block", new BlockItem(Ic2Blocks.STEEL_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steel_block")))));
   public static final Item TIN_BLOCK = register("tin_block", new BlockItem(Ic2Blocks.TIN_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("tin_block")))));
   public static final Item URANIUM_BLOCK = register("uranium_block", new BlockItem(Ic2Blocks.URANIUM_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("uranium_block")))));
   public static final Item REINFORCED_STONE = register(
      "reinforced_stone", new BlockItem(Ic2Blocks.REINFORCED_STONE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reinforced_stone"))))
   );
   public static final Item MACHINE = register("machine", new BlockItem(Ic2Blocks.MACHINE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("machine")))));
   public static final Item ADVANCED_MACHINE = register(
      "advanced_machine", new BlockItem(Ic2Blocks.ADVANCED_MACHINE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("advanced_machine"))))
   );
   public static final Item SILVER_BLOCK = register("silver_block", new BlockItem(Ic2Blocks.SILVER_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("silver_block")))));
   public static final Item RUBBER_LEAVES = register("rubber_leaves", new BlockItem(Ic2Blocks.RUBBER_LEAVES, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_leaves")))));
   public static final Item RUBBER_LOG = register("rubber_log", new BlockItem(Ic2Blocks.RUBBER_LOG, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_log")))));
   public static final Item STRIPPED_RUBBER_LOG = register(
      "stripped_rubber_log", new BlockItem(Ic2Blocks.STRIPPED_RUBBER_LOG, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("stripped_rubber_log"))))
   );
   public static final Item RUBBER_WOOD = register("rubber_wood", new BlockItem(Ic2Blocks.RUBBER_WOOD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_wood")))));
   public static final Item STRIPPED_RUBBER_WOOD = register(
      "stripped_rubber_wood", new BlockItem(Ic2Blocks.STRIPPED_RUBBER_WOOD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("stripped_rubber_wood"))))
   );
   public static final Item RUBBER_PLANKS = register("rubber_planks", new BlockItem(Ic2Blocks.RUBBER_PLANKS, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_planks")))));
   public static final Item RUBBER_SAPLING = register("rubber_sapling", new BlockItem(Ic2Blocks.RUBBER_SAPLING, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_sapling")))));
   public static final Item RUBBER_BUTTON = register("rubber_button", new BlockItem(Ic2Blocks.RUBBER_BUTTON, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_button")))));
   public static final Item RUBBER_DOOR = register("rubber_door", new BlockItem(Ic2Blocks.RUBBER_DOOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_door")))));
   public static final Item RUBBER_FENCE = register("rubber_fence", new BlockItem(Ic2Blocks.RUBBER_FENCE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_fence")))));
   public static final Item RUBBER_FENCE_GATE = register(
      "rubber_fence_gate", new BlockItem(Ic2Blocks.RUBBER_FENCE_GATE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_fence_gate"))))
   );
   public static final Item RUBBER_PRESSURE_PLATE = register(
      "rubber_pressure_plate", new BlockItem(Ic2Blocks.RUBBER_PRESSURE_PLATE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_pressure_plate"))))
   );
   public static final Item RUBBER_SIGN = register(
      "rubber_sign", new SignItem(Ic2Blocks.RUBBER_SIGN, Ic2Blocks.RUBBER_WALL_SIGN, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_sign"))))
   );
   public static final Item RUBBER_SLAB = register("rubber_slab", new BlockItem(Ic2Blocks.RUBBER_SLAB, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_slab")))));
   public static final Item RUBBER_STAIRS = register("rubber_stairs", new BlockItem(Ic2Blocks.RUBBER_STAIRS, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_stairs")))));
   public static final Item RUBBER_TRAPDOOR = register(
      "rubber_trapdoor", new BlockItem(Ic2Blocks.RUBBER_TRAPDOOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_trapdoor"))))
   );
   public static final Item MINING_PIPE = register("mining_pipe", new BlockItem(Ic2Blocks.MINING_PIPE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mining_pipe")))));
   public static final Item WOODEN_SCAFFOLD = register(
      "wooden_scaffold", new BlockItem(Ic2Blocks.WOODEN_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("wooden_scaffold"))))
   );
   public static final Item REINFORCED_WOODEN_SCAFFOLD = register(
      "reinforced_wooden_scaffold", new BlockItem(Ic2Blocks.REINFORCED_WOODEN_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reinforced_wooden_scaffold"))))
   );
   public static final Item IRON_SCAFFOLD = register("iron_scaffold", new BlockItem(Ic2Blocks.IRON_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_scaffold")))));
   public static final Item REINFORCED_IRON_SCAFFOLD = register(
      "reinforced_iron_scaffold", new BlockItem(Ic2Blocks.REINFORCED_IRON_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reinforced_iron_scaffold"))))
   );
   public static final Item IRON_FENCE = register("iron_fence", new BlockItem(Ic2Blocks.IRON_FENCE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_fence")))));
   public static final Item RESIN_SHEET = register("resin_sheet", new BlockItem(Ic2Blocks.RESIN_SHEET, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("resin_sheet")))));
   public static final Item RUBBER_SHEET = register("rubber_sheet", new BlockItem(Ic2Blocks.RUBBER_SHEET, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_sheet")))));
   public static final Item WOOL_SHEET = register("wool_sheet", new BlockItem(Ic2Blocks.WOOL_SHEET, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("wool_sheet")))));
   public static final Item REINFORCED_GLASS = register(
      "reinforced_glass", new BlockItem(Ic2Blocks.REINFORCED_GLASS, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reinforced_glass"))))
   );
   public static final Item REINFORCED_DOOR = register(
      "reinforced_door", new DoubleHighBlockItem(Ic2Blocks.REINFORCED_DOOR, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reinforced_door"))))
   );
   public static final Item FOAM = register("foam", new BlockItem(Ic2Blocks.FOAM, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("foam")))));
   public static final Item WHITE_WALL = register("white_wall", new BlockItem(Ic2Blocks.WHITE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("white_wall")))));
   public static final Item ORANGE_WALL = register("orange_wall", new BlockItem(Ic2Blocks.ORANGE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("orange_wall")))));
   public static final Item MAGENTA_WALL = register("magenta_wall", new BlockItem(Ic2Blocks.MAGENTA_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("magenta_wall")))));
   public static final Item LIGHT_BLUE_WALL = register(
      "light_blue_wall", new BlockItem(Ic2Blocks.LIGHT_BLUE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("light_blue_wall"))))
   );
   public static final Item YELLOW_WALL = register("yellow_wall", new BlockItem(Ic2Blocks.YELLOW_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("yellow_wall")))));
   public static final Item LIME_WALL = register("lime_wall", new BlockItem(Ic2Blocks.LIME_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lime_wall")))));
   public static final Item PINK_WALL = register("pink_wall", new BlockItem(Ic2Blocks.PINK_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("pink_wall")))));
   public static final Item GRAY_WALL = register("gray_wall", new BlockItem(Ic2Blocks.GRAY_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("gray_wall")))));
   public static final Item LIGHT_GRAY_WALL = register(
      "light_gray_wall", new BlockItem(Ic2Blocks.LIGHT_GRAY_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("light_gray_wall"))))
   );
   public static final Item CYAN_WALL = register("cyan_wall", new BlockItem(Ic2Blocks.CYAN_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("cyan_wall")))));
   public static final Item PURPLE_WALL = register("purple_wall", new BlockItem(Ic2Blocks.PURPLE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("purple_wall")))));
   public static final Item BLUE_WALL = register("blue_wall", new BlockItem(Ic2Blocks.BLUE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("blue_wall")))));
   public static final Item BROWN_WALL = register("brown_wall", new BlockItem(Ic2Blocks.BROWN_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("brown_wall")))));
   public static final Item GREEN_WALL = register("green_wall", new BlockItem(Ic2Blocks.GREEN_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("green_wall")))));
   public static final Item RED_WALL = register("red_wall", new BlockItem(Ic2Blocks.RED_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("red_wall")))));
   public static final Item BLACK_WALL = register("black_wall", new BlockItem(Ic2Blocks.BLACK_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("black_wall")))));
   public static final Item ITNT = register("itnt", new BlockItem(Ic2Blocks.ITNT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("itnt")))));
   public static final Item NUKE = register("nuke", new BlockItem(Ic2Blocks.NUKE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("nuke"))).rarity(Rarity.UNCOMMON)));
   public static final Item CLASSIC_NUKE = register(
      "classic_nuke", new BlockItem(Ic2Blocks.CLASSIC_NUKE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("classic_nuke"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item COPPER_CABLE = register(
      "copper_cable", new BlockItem(Ic2Blocks.COPPER_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("copper_cable"))))
   );
   public static final Item INSULATED_COPPER_CABLE = register(
      "insulated_copper_cable", new BlockItem(Ic2Blocks.INSULATED_COPPER_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("insulated_copper_cable"))))
   );
   public static final Item GLASS_FIBRE_CABLE = register(
      "glass_fibre_cable", new BlockItem(Ic2Blocks.GLASS_FIBRE_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("glass_fibre_cable"))))
   );
   public static final Item GOLD_CABLE = register("gold_cable", new BlockItem(Ic2Blocks.GOLD_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("gold_cable")))));
   public static final Item INSULATED_GOLD_CABLE = register(
      "insulated_gold_cable", new BlockItem(Ic2Blocks.INSULATED_GOLD_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("insulated_gold_cable"))))
   );
   public static final Item DOUBLE_INSULATED_GOLD_CABLE = register(
      "double_insulated_gold_cable", new BlockItem(Ic2Blocks.DOUBLE_INSULATED_GOLD_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("double_insulated_gold_cable"))))
   );
   public static final Item IRON_CABLE = register("iron_cable", new BlockItem(Ic2Blocks.IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_cable")))));
   public static final Item INSULATED_IRON_CABLE = register(
      "insulated_iron_cable", new BlockItem(Ic2Blocks.INSULATED_IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("insulated_iron_cable"))))
   );
   public static final Item DOUBLE_INSULATED_IRON_CABLE = register(
      "double_insulated_iron_cable", new BlockItem(Ic2Blocks.DOUBLE_INSULATED_IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("double_insulated_iron_cable"))))
   );
   public static final Item TRIPLE_INSULATED_IRON_CABLE = register(
      "triple_insulated_iron_cable", new BlockItem(Ic2Blocks.TRIPLE_INSULATED_IRON_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("triple_insulated_iron_cable"))))
   );
   public static final Item TIN_CABLE = register("tin_cable", new BlockItem(Ic2Blocks.TIN_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("tin_cable")))));
   public static final Item INSULATED_TIN_CABLE = register(
      "insulated_tin_cable", new BlockItem(Ic2Blocks.INSULATED_TIN_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("insulated_tin_cable"))))
   );
   public static final Item DETECTOR_CABLE = register(
      "detector_cable", new BlockItem(Ic2Blocks.DETECTOR_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("detector_cable"))))
   );
   public static final Item SPLITTER_CABLE = register(
      "splitter_cable", new BlockItem(Ic2Blocks.SPLITTER_CABLE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("splitter_cable"))))
   );
   public static final Item BATBOX_CHARGEPAD = register(
      "batbox_chargepad", new BlockItem(Ic2Blocks.BATBOX_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("batbox_chargepad"))))
   );
   public static final Item CESU_CHARGEPAD = register(
      "cesu_chargepad", new BlockItem(Ic2Blocks.CESU_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("cesu_chargepad"))))
   );
   public static final Item MFE_CHARGEPAD = register(
      "mfe_chargepad", new BlockItem(Ic2Blocks.MFE_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mfe_chargepad"))))
   );
   public static final Item MFSU_CHARGEPAD = register(
      "mfsu_chargepad", new BlockItem(Ic2Blocks.MFSU_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mfsu_chargepad"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item BATBOX = register("batbox", new BlockItem(Ic2Blocks.BATBOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("batbox")))));
   public static final Item CESU = register("cesu", new BlockItem(Ic2Blocks.CESU, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("cesu")))));
   public static final Item MFE = register("mfe", new BlockItem(Ic2Blocks.MFE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mfe")))));
   public static final Item CLASSIC_MFE = register(
      "classic_mfe", new BlockItem(Ic2Blocks.CLASSIC_MFE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("classic_mfe"))))
   );
   public static final Item MFSU = register(
      "mfsu", new BlockItem(Ic2Blocks.MFSU, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mfsu"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item CLASSIC_MFSU = register(
      "classic_mfsu", new BlockItem(Ic2Blocks.CLASSIC_MFSU, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("classic_mfsu"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item LV_TRANSFORMER = register(
      "lv_transformer", new BlockItem(Ic2Blocks.LV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lv_transformer"))))
   );
   public static final Item MV_TRANSFORMER = register(
      "mv_transformer", new BlockItem(Ic2Blocks.MV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mv_transformer"))))
   );
   public static final Item HV_TRANSFORMER = register(
      "hv_transformer", new BlockItem(Ic2Blocks.HV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("hv_transformer"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item EV_TRANSFORMER = register(
      "ev_transformer", new BlockItem(Ic2Blocks.EV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("ev_transformer"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item TANK = register("tank", new BlockItem(Ic2Blocks.TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("tank")))));
   public static final Item STEAM_REPRESSURIZER = register(
      "steam_repressurizer", new BlockItem(Ic2Blocks.STEAM_REPRESSURIZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steam_repressurizer"))))
   );
   public static final Item WEIGHTED_FLUID_DISTRIBUTOR = register(
      "weighted_fluid_distributor", new BlockItem(Ic2Blocks.WEIGHTED_FLUID_DISTRIBUTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("weighted_fluid_distributor"))))
   );
   public static final Item WEIGHTED_ITEM_DISTRIBUTOR = register(
      "weighted_item_distributor", new BlockItem(Ic2Blocks.WEIGHTED_ITEM_DISTRIBUTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("weighted_item_distributor"))))
   );
   public static final Item RCI_RSH = register("rci_rsh", new BlockItem(Ic2Blocks.RCI_RSH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rci_rsh")))));
   public static final Item RCI_LZH = register("rci_lzh", new BlockItem(Ic2Blocks.RCI_LZH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rci_lzh")))));
   public static final Item INDUSTRIAL_WORKBENCH = register(
      "industrial_workbench", new BlockItem(Ic2Blocks.INDUSTRIAL_WORKBENCH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("industrial_workbench"))))
   );
   public static final Item BATCH_CRAFTER = register(
      "batch_crafter", new BlockItem(Ic2Blocks.BATCH_CRAFTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("batch_crafter"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item WOODEN_STORAGE_BOX = register(
      "wooden_storage_box", new BlockItem(Ic2Blocks.WOODEN_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("wooden_storage_box"))))
   );
   public static final Item IRON_STORAGE_BOX = register(
      "iron_storage_box", new BlockItem(Ic2Blocks.IRON_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_storage_box"))))
   );
   public static final Item BRONZE_STORAGE_BOX = register(
      "bronze_storage_box", new BlockItem(Ic2Blocks.BRONZE_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_storage_box"))))
   );
   public static final Item STEEL_STORAGE_BOX = register(
      "steel_storage_box", new BlockItem(Ic2Blocks.STEEL_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steel_storage_box"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item IRIDIUM_STORAGE_BOX = register(
      "iridium_storage_box", new BlockItem(Ic2Blocks.IRIDIUM_STORAGE_BOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iridium_storage_box"))).rarity(Rarity.RARE))
   );
   public static final Item BRONZE_TANK = register("bronze_tank", new BlockItem(Ic2Blocks.BRONZE_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_tank")))));
   public static final Item IRON_TANK = register("iron_tank", new BlockItem(Ic2Blocks.IRON_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_tank")))));
   public static final Item STEEL_TANK = register(
      "steel_tank", new BlockItem(Ic2Blocks.STEEL_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steel_tank"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item IRIDIUM_TANK = register(
      "iridium_tank", new BlockItem(Ic2Blocks.IRIDIUM_TANK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iridium_tank"))).rarity(Rarity.RARE))
   );
   public static final Item GENERATOR = register("generator", new BlockItem(Ic2Blocks.GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("generator")))));
   public static final Item GEO_GENERATOR = register(
      "geo_generator", new BlockItem(Ic2Blocks.GEO_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("geo_generator"))))
   );
   public static final Item KINETIC_GENERATOR = register(
      "kinetic_generator", new BlockItem(Ic2Blocks.KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("kinetic_generator"))))
   );
   public static final Item RT_GENERATOR = register(
      "rt_generator", new BlockItem(Ic2Blocks.RT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rt_generator"))))
   );
   public static final Item SEMIFLUID_GENERATOR = register(
      "semifluid_generator", new BlockItem(Ic2Blocks.SEMIFLUID_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("semifluid_generator"))))
   );
   public static final Item SOLAR_GENERATOR = register(
      "solar_generator", new BlockItem(Ic2Blocks.SOLAR_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("solar_generator"))))
   );
   public static final Item STIRLING_GENERATOR = register(
      "stirling_generator", new BlockItem(Ic2Blocks.STIRLING_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("stirling_generator"))))
   );
   public static final Item WATER_GENERATOR = register(
      "water_generator", new BlockItem(Ic2Blocks.WATER_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("water_generator"))))
   );
   public static final Item WIND_GENERATOR = register(
      "wind_generator", new BlockItem(Ic2Blocks.WIND_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("wind_generator"))))
   );
   public static final Item CREATIVE_GENERATOR = register(
      "creative_generator", new BlockItem(Ic2Blocks.CREATIVE_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("creative_generator"))))
   );
   public static final Item ELECTRIC_HEAT_GENERATOR = register(
      "electric_heat_generator", new BlockItem(Ic2Blocks.ELECTRIC_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("electric_heat_generator"))))
   );
   public static final Item FLUID_HEAT_GENERATOR = register(
      "fluid_heat_generator", new BlockItem(Ic2Blocks.FLUID_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("fluid_heat_generator"))))
   );
   public static final Item RT_HEAT_GENERATOR = register(
      "rt_heat_generator", new BlockItem(Ic2Blocks.RT_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rt_heat_generator"))))
   );
   public static final Item SOLID_HEAT_GENERATOR = register(
      "solid_heat_generator", new BlockItem(Ic2Blocks.SOLID_HEAT_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("solid_heat_generator"))))
   );
   public static final Item ELECTRIC_KINETIC_GENERATOR = register(
      "electric_kinetic_generator", new BlockItem(Ic2Blocks.ELECTRIC_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("electric_kinetic_generator"))))
   );
   public static final Item MANUAL_KINETIC_GENERATOR = register(
      "manual_kinetic_generator", new BlockItem(Ic2Blocks.MANUAL_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("manual_kinetic_generator"))))
   );
   public static final Item STEAM_KINETIC_GENERATOR = register(
      "steam_kinetic_generator", new BlockItem(Ic2Blocks.STEAM_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steam_kinetic_generator"))))
   );
   public static final Item STIRLING_KINETIC_GENERATOR = register(
      "stirling_kinetic_generator", new BlockItem(Ic2Blocks.STIRLING_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("stirling_kinetic_generator"))))
   );
   public static final Item WATER_KINETIC_GENERATOR = register(
      "water_kinetic_generator", new BlockItem(Ic2Blocks.WATER_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("water_kinetic_generator"))))
   );
   public static final Item WIND_KINETIC_GENERATOR = register(
      "wind_kinetic_generator", new BlockItem(Ic2Blocks.WIND_KINETIC_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("wind_kinetic_generator"))))
   );
   public static final Item NUCLEAR_REACTOR = register(
      "nuclear_reactor", new BlockItem(Ic2Blocks.NUCLEAR_REACTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("nuclear_reactor"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_ACCESS_HATCH = register(
      "reactor_access_hatch", new BlockItem(Ic2Blocks.REACTOR_ACCESS_HATCH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reactor_access_hatch"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_CHAMBER = register(
      "reactor_chamber", new BlockItem(Ic2Blocks.REACTOR_CHAMBER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reactor_chamber"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_FLUID_PORT = register(
      "reactor_fluid_port", new BlockItem(Ic2Blocks.REACTOR_FLUID_PORT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reactor_fluid_port"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item REACTOR_REDSTONE_PORT = register(
      "reactor_redstone_port", new BlockItem(Ic2Blocks.REACTOR_REDSTONE_PORT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reactor_redstone_port"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item CONDENSER = register("condenser", new BlockItem(Ic2Blocks.CONDENSER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("condenser")))));
   public static final Item FLUID_BOTTLER = register("fluid_bottler", new BlockItem(Ic2Blocks.FLUID_BOTTLER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("fluid_bottler")))));
   public static final Item FLUID_DISTRIBUTOR = register(
      "fluid_distributor", new BlockItem(Ic2Blocks.FLUID_DISTRIBUTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("fluid_distributor"))))
   );
   public static final Item FLUID_REGULATOR = register(
      "fluid_regulator", new BlockItem(Ic2Blocks.FLUID_REGULATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("fluid_regulator"))))
   );
   public static final Item LIQUID_HEAT_EXCHANGER = register(
      "liquid_heat_exchanger", new BlockItem(Ic2Blocks.LIQUID_HEAT_EXCHANGER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("liquid_heat_exchanger"))))
   );
   public static final Item PUMP = register("pump", new BlockItem(Ic2Blocks.PUMP, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("pump")))));
   public static final Item SOLAR_DISTILLER = register(
      "solar_distiller", new BlockItem(Ic2Blocks.SOLAR_DISTILLER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("solar_distiller"))))
   );
   public static final Item STEAM_GENERATOR = register(
      "steam_generator", new BlockItem(Ic2Blocks.STEAM_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steam_generator"))))
   );
   public static final Item ITEM_BUFFER = register("item_buffer", new BlockItem(Ic2Blocks.ITEM_BUFFER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("item_buffer")))));
   public static final Item MAGNETIZER = register("magnetizer", new BlockItem(Ic2Blocks.MAGNETIZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("magnetizer")))));
   public static final Item SORTING_MACHINE = register(
      "sorting_machine", new BlockItem(Ic2Blocks.SORTING_MACHINE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("sorting_machine"))))
   );
   public static final Item TELEPORTER = register(
      "teleporter", new BlockItem(Ic2Blocks.TELEPORTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("teleporter"))).rarity(Rarity.RARE))
   );
   public static final Item TERRAFORMER = register(
      "terraformer", new BlockItem(Ic2Blocks.TERRAFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("terraformer"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item TESLA_COIL = register("tesla_coil", new BlockItem(Ic2Blocks.TESLA_COIL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("tesla_coil")))));
   public static final Item ELECTROLYZER = register("electrolyzer", new BlockItem(Ic2Blocks.ELECTROLYZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("electrolyzer")))));
   public static final Item CLASSIC_ELECTROLYZER = register(
      "classic_electrolyzer", new BlockItem(Ic2Blocks.CLASSIC_ELECTROLYZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("classic_electrolyzer"))))
   );
   public static final Item CHUNK_LOADER = register(
      "chunk_loader", new BlockItem(Ic2Blocks.CHUNK_LOADER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("chunk_loader"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item CANNER = register("canner", new BlockItem(Ic2Blocks.CANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("canner")))));
   public static final Item CLASSIC_CANNER = register("classic_canner", new BlockItem(Ic2Blocks.CLASSIC_CANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("classic_canner")))));
   public static final Item COMPRESSOR = register("compressor", new BlockItem(Ic2Blocks.COMPRESSOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("compressor")))));
   public static final Item ELECTRIC_FURNACE = register(
      "electric_furnace", new BlockItem(Ic2Blocks.ELECTRIC_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("electric_furnace"))))
   );
   public static final Item EXTRACTOR = register("extractor", new BlockItem(Ic2Blocks.EXTRACTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("extractor")))));
   public static final Item IRON_FURNACE = register("iron_furnace", new BlockItem(Ic2Blocks.IRON_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_furnace")))));
   public static final Item MACERATOR = register("macerator", new BlockItem(Ic2Blocks.MACERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("macerator")))));
   public static final Item RECYCLER = register("recycler", new BlockItem(Ic2Blocks.RECYCLER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("recycler")))));
   public static final Item SOLID_CANNER = register("solid_canner", new BlockItem(Ic2Blocks.SOLID_CANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("solid_canner")))));
   public static final Item BLAST_FURNACE = register("blast_furnace", new BlockItem(Ic2Blocks.BLAST_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("blast_furnace")))));
   public static final Item BLOCK_CUTTER = register("block_cutter", new BlockItem(Ic2Blocks.BLOCK_CUTTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("block_cutter")))));
   public static final Item CENTRIFUGE = register("centrifuge", new BlockItem(Ic2Blocks.CENTRIFUGE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("centrifuge")))));
   public static final Item FERMENTER = register("fermenter", new BlockItem(Ic2Blocks.FERMENTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("fermenter")))));
   public static final Item INDUCTION_FURNACE = register(
      "induction_furnace", new BlockItem(Ic2Blocks.INDUCTION_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("induction_furnace"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item METAL_FORMER = register("metal_former", new BlockItem(Ic2Blocks.METAL_FORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("metal_former")))));
   public static final Item ORE_WASHING_PLANT = register(
      "ore_washing_plant", new BlockItem(Ic2Blocks.ORE_WASHING_PLANT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("ore_washing_plant"))))
   );
   public static final Item ADVANCED_MINER = register("advanced_miner", new BlockItem(Ic2Blocks.ADVANCED_MINER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("advanced_miner")))));
   public static final Item CROP_HARVESTER = register("crop_harvester", new BlockItem(Ic2Blocks.CROP_HARVESTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crop_harvester")))));
   public static final Item CROPMATRON = register("cropmatron", new BlockItem(Ic2Blocks.CROPMATRON, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("cropmatron")))));
   public static final Item CLASSIC_CROPMATRON = register(
      "classic_cropmatron", new BlockItem(Ic2Blocks.CLASSIC_CROPMATRON, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("classic_cropmatron"))))
   );
   public static final Item MINER = register("miner", new BlockItem(Ic2Blocks.MINER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("miner")))));
   public static final Item MASS_FABRICATOR = register(
      "mass_fabricator", new BlockItem(Ic2Blocks.MASS_FABRICATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mass_fabricator"))).rarity(Rarity.RARE))
   );
   public static final Item CLASSIC_MASS_FABRICATOR = register(
      "classic_mass_fabricator", new BlockItem(Ic2Blocks.CLASSIC_MASS_FABRICATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("classic_mass_fabricator"))).rarity(Rarity.RARE))
   );
   public static final Item UU_ASSEMBLY_BENCH = register(
      "uu_assembly_bench", new BlockItem(Ic2Blocks.UU_ASSEMBLY_BENCH, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("uu_assembly_bench"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item MATTER_GENERATOR = register(
      "matter_generator", new BlockItem(Ic2Blocks.MATTER_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("matter_generator"))).rarity(Rarity.RARE))
   );
   public static final Item PATTERN_STORAGE = register(
      "pattern_storage", new BlockItem(Ic2Blocks.PATTERN_STORAGE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("pattern_storage"))))
   );
   public static final Item REPLICATOR = register(
      "replicator", new BlockItem(Ic2Blocks.REPLICATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("replicator"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item UU_SCANNER = register(
      "uu_scanner", new BlockItem(Ic2Blocks.UU_SCANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("uu_scanner"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item ENERGY_O_MAT = register("energy_o_mat", new BlockItem(Ic2Blocks.ENERGY_O_MAT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("energy_o_mat")))));
   public static final Item PERSONAL_CHEST = register(
      "personal_chest", new BlockItem(Ic2Blocks.PERSONAL_CHEST, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("personal_chest"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item TRADE_O_MAT = register("trade_o_mat", new BlockItem(Ic2Blocks.TRADE_O_MAT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("trade_o_mat")))));
   public static final Item RAW_LEAD = register("raw_lead", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("raw_lead")))));
   public static final Item RAW_TIN = register("raw_tin", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("raw_tin")))));
   public static final Item RAW_URANIUM = register("raw_uranium", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("raw_uranium")))));
   public static final Item CRUSHED_COPPER = register("crushed_copper", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crushed_copper")))));
   public static final Item CRUSHED_GOLD = register("crushed_gold", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crushed_gold")))));
   public static final Item CRUSHED_IRON = register("crushed_iron", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crushed_iron")))));
   public static final Item CRUSHED_LEAD = register("crushed_lead", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crushed_lead")))));
   public static final Item CRUSHED_SILVER = register("crushed_silver", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crushed_silver")))));
   public static final Item CRUSHED_TIN = register("crushed_tin", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crushed_tin")))));
   public static final Item CRUSHED_URANIUM = register("crushed_uranium", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crushed_uranium")))));
   public static final Item PURIFIED_COPPER = register("purified_copper", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("purified_copper")))));
   public static final Item PURIFIED_GOLD = register("purified_gold", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("purified_gold")))));
   public static final Item PURIFIED_IRON = register("purified_iron", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("purified_iron")))));
   public static final Item PURIFIED_LEAD = register("purified_lead", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("purified_lead")))));
   public static final Item PURIFIED_SILVER = register("purified_silver", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("purified_silver")))));
   public static final Item PURIFIED_TIN = register("purified_tin", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("purified_tin")))));
   public static final Item PURIFIED_URANIUM = register("purified_uranium", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("purified_uranium")))));
   public static final Item BRONZE_DUST = register("bronze_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_dust")))));
   public static final Item CLAY_DUST = register("clay_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("clay_dust")))));
   public static final Item COAL_DUST = register("coal_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("coal_dust")))));
   public static final Item COAL_FUEL_DUST = register("coal_fuel_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("coal_fuel_dust")))));
   public static final Item COPPER_DUST = register("copper_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("copper_dust")))));
   public static final Item DIAMOND_DUST = register("diamond_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("diamond_dust")))));
   public static final Item ENERGIUM_DUST = register("energium_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("energium_dust")))));
   public static final Item GOLD_DUST = register("gold_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("gold_dust")))));
   public static final Item IRON_DUST = register("iron_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_dust")))));
   public static final Item LAPIS_DUST = register("lapis_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lapis_dust")))));
   public static final Item LEAD_DUST = register("lead_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lead_dust")))));
   public static final Item LITHIUM_DUST = register("lithium_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lithium_dust")))));
   public static final Item OBSIDIAN_DUST = register("obsidian_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("obsidian_dust")))));
   public static final Item SILICON_DIOXIDE_DUST = register("silicon_dioxide_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("silicon_dioxide_dust")))));
   public static final Item SILVER_DUST = register("silver_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("silver_dust")))));
   public static final Item STONE_DUST = register("stone_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("stone_dust")))));
   public static final Item SULFUR_DUST = register("sulfur_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("sulfur_dust")))));
   public static final Item TIN_DUST = register("tin_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("tin_dust")))));
   public static final Item SMALL_BRONZE_DUST = register("small_bronze_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_bronze_dust")))));
   public static final Item SMALL_COPPER_DUST = register("small_copper_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_copper_dust")))));
   public static final Item SMALL_GOLD_DUST = register("small_gold_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_gold_dust")))));
   public static final Item SMALL_IRON_DUST = register("small_iron_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_iron_dust")))));
   public static final Item SMALL_LAPIS_DUST = register("small_lapis_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_lapis_dust")))));
   public static final Item SMALL_LEAD_DUST = register("small_lead_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_lead_dust")))));
   public static final Item SMALL_LITHIUM_DUST = register("small_lithium_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_lithium_dust")))));
   public static final Item SMALL_OBSIDIAN_DUST = register("small_obsidian_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_obsidian_dust")))));
   public static final Item SMALL_SILVER_DUST = register("small_silver_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_silver_dust")))));
   public static final Item SMALL_SULFUR_DUST = register("small_sulfur_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_sulfur_dust")))));
   public static final Item SMALL_TIN_DUST = register("small_tin_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_tin_dust")))));
   public static final Item HYDRATED_TIN_DUST = register("hydrated_tin_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("hydrated_tin_dust")))));
   public static final Item NETHERRACK_DUST = register("netherrack_dust", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("netherrack_dust")))));
   public static final Item MIXED_METAL_INGOT = register("mixed_metal_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mixed_metal_ingot")))));
   public static final Item BRONZE_INGOT = register("bronze_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_ingot")))));
   public static final Item COPPER_INGOT = register("copper_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("copper_ingot")))));
   public static final Item LEAD_INGOT = register("lead_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lead_ingot")))));
   public static final Item SILVER_INGOT = register("silver_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("silver_ingot")))));
   public static final Item STEEL_INGOT = register("steel_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steel_ingot")))));
   public static final Item TIN_INGOT = register("tin_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("tin_ingot")))));
   public static final Item REFINED_IRON_INGOT = register("refined_iron_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("refined_iron_ingot")))));
   public static final Item URANIUM_INGOT = register("uranium_ingot", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("uranium_ingot")))));
   public static final Item BRONZE_PLATE = register("bronze_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_plate")))));
   public static final Item COPPER_PLATE = register("copper_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("copper_plate")))));
   public static final Item GOLD_PLATE = register("gold_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("gold_plate")))));
   public static final Item IRON_PLATE = register("iron_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_plate")))));
   public static final Item LAPIS_PLATE = register("lapis_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lapis_plate")))));
   public static final Item LEAD_PLATE = register("lead_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lead_plate")))));
   public static final Item OBSIDIAN_PLATE = register("obsidian_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("obsidian_plate")))));
   public static final Item STEEL_PLATE = register("steel_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steel_plate")))));
   public static final Item TIN_PLATE = register("tin_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("tin_plate")))));
   public static final Item DENSE_BRONZE_PLATE = register("dense_bronze_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dense_bronze_plate")))));
   public static final Item DENSE_COPPER_PLATE = register("dense_copper_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dense_copper_plate")))));
   public static final Item DENSE_GOLD_PLATE = register("dense_gold_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dense_gold_plate")))));
   public static final Item DENSE_IRON_PLATE = register("dense_iron_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dense_iron_plate")))));
   public static final Item DENSE_LAPIS_PLATE = register("dense_lapis_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dense_lapis_plate")))));
   public static final Item DENSE_LEAD_PLATE = register("dense_lead_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dense_lead_plate")))));
   public static final Item DENSE_OBSIDIAN_PLATE = register("dense_obsidian_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dense_obsidian_plate")))));
   public static final Item DENSE_STEEL_PLATE = register("dense_steel_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dense_steel_plate")))));
   public static final Item DENSE_TIN_PLATE = register("dense_tin_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dense_tin_plate")))));
   public static final Item BRONZE_CASING = register("bronze_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_casing")))));
   public static final Item COPPER_CASING = register("copper_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("copper_casing")))));
   public static final Item GOLD_CASING = register("gold_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("gold_casing")))));
   public static final Item IRON_CASING = register("iron_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_casing")))));
   public static final Item LEAD_CASING = register("lead_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lead_casing")))));
   public static final Item STEEL_CASING = register("steel_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steel_casing")))));
   public static final Item TIN_CASING = register("tin_casing", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("tin_casing")))));
   public static final Item URANIUM = register("uranium", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("uranium"))), 60, 100));
   public static final Item URANIUM_235 = register("uranium_235", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("uranium_235"))), 150, 100));
   public static final Item URANIUM_238 = register("uranium_238", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("uranium_238"))), 10, 90));
   public static final Item PLUTONIUM = register("plutonium", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("plutonium"))), 150, 100));
   public static final Item MOX = register("mox", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mox"))), 300, 100));
   public static final Item SMALL_URANIUM_235 = register("small_uranium_235", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_uranium_235"))), 150, 100));
   public static final Item SMALL_URANIUM_238 = register("small_uranium_238", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_uranium_238"))), 10, 90));
   public static final Item SMALL_PLUTONIUM = register("small_plutonium", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_plutonium"))), 150, 100));
   public static final Item URANIUM_PELLET = register("uranium_pellet", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("uranium_pellet"))), 60, 100));
   public static final Item MOX_PELLET = register("mox_pellet", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mox_pellet"))), 300, 100));
   public static final Item RTG_PELLET = register("rtg_pellet", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rtg_pellet"))).stacksTo(1), 2, 90));
   public static final Item DEPLETED_URANIUM_FUEL_ROD = register(
      "depleted_uranium_fuel_rod", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("depleted_uranium_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_DUAL_URANIUM_FUEL_ROD = register(
      "depleted_dual_uranium_fuel_rod", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("depleted_dual_uranium_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_QUAD_URANIUM_FUEL_ROD = register(
      "depleted_quad_uranium_fuel_rod", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("depleted_quad_uranium_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_MOX_FUEL_ROD = register(
      "depleted_mox_fuel_rod", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("depleted_mox_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_DUAL_MOX_FUEL_ROD = register(
      "depleted_dual_mox_fuel_rod", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("depleted_dual_mox_fuel_rod"))), 10, 100)
   );
   public static final Item DEPLETED_QUAD_MOX_FUEL_ROD = register(
      "depleted_quad_mox_fuel_rod", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("depleted_quad_mox_fuel_rod"))), 10, 100)
   );
   public static final Item NEAR_DEPLETED_URANIUM = register(
      "near_depleted_uranium", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("near_depleted_uranium"))), 15, 100)
   );
   public static final Item RE_ENRICHED_URANIUM = register(
      "re_enriched_uranium", new ItemNuclearResource(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("re_enriched_uranium"))), 30, 100)
   );
   public static final Item ASHES = register("ashes", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("ashes")))));
   public static final Item IRIDIUM_ORE = register("iridium_ore", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iridium_ore"))).rarity(Rarity.RARE)));
   public static final Item IRIDIUM_SHARD = register("iridium_shard", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iridium_shard"))).rarity(Rarity.UNCOMMON)));
   public static final Item UU_MATTER = register("uu_matter", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("uu_matter"))).rarity(Rarity.RARE)));
   public static final Item RESIN = register("resin", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("resin")))));
   public static final Item SLAG = register("slag", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("slag")))));
   public static final Item IODINE = register("iodine", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iodine")))));
   public static final Item WATER_SHEET = register("water_sheet", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("water_sheet")))));
   public static final Item LAVA_SHEET = register("lava_sheet", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lava_sheet")))));
   public static final Item RUBBER = register("rubber", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber")))));
   public static final Item CIRCUIT = register("circuit", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("circuit")))));
   public static final Item ADVANCED_CIRCUIT = register("advanced_circuit", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("advanced_circuit"))).rarity(Rarity.UNCOMMON)));
   public static final Item ALLOY = register("alloy", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("alloy")))));
   public static final Item IRIDIUM = register("iridium", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iridium"))).rarity(Rarity.RARE)));
   public static final Item COIL = register("coil", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("coil")))));
   public static final Item ELECTRIC_MOTOR = register("electric_motor", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("electric_motor")))));
   public static final Item HEAT_CONDUCTOR = register("heat_conductor", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("heat_conductor")))));
   public static final Item COPPER_BOILER = register("copper_boiler", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("copper_boiler")))));
   public static final Item FUEL_ROD = register("fuel_rod", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("fuel_rod")))));
   public static final Item TIN_CAN = register("tin_can", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("tin_can")))));
   public static final Item IRON_CUTTING_BLADE = register(
      "iron_cutting_blade", new BlockCuttingBlade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_cutting_blade"))), BlockCuttingBladeType.iron)
   );
   public static final Item DIAMOND_CUTTING_BLADE = register(
      "diamond_cutting_blade", new BlockCuttingBlade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("diamond_cutting_blade"))), BlockCuttingBladeType.diamond)
   );
   public static final Item STEEL_CUTTING_BLADE = register(
      "steel_cutting_blade", new BlockCuttingBlade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steel_cutting_blade"))), BlockCuttingBladeType.steel)
   );
   public static final Item SMALL_POWER_UNIT = register("small_power_unit", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("small_power_unit")))));
   public static final Item POWER_UNIT = register("power_unit", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("power_unit")))));
   public static final Item CARBON_FIBRE = register("carbon_fibre", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("carbon_fibre")))));
   public static final Item CARBON_MESH = register("carbon_mesh", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("carbon_mesh")))));
   public static final Item CARBON_PLATE = register("carbon_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("carbon_plate")))));
   public static final Item COAL_BALL = register("coal_ball", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("coal_ball")))));
   public static final Item COAL_BLOCK = register("coal_block", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("coal_block")))));
   public static final Item COAL_CHUNK = register("coal_chunk", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("coal_chunk")))));
   public static final Item INDUTRIAL_DIAMOND = register("industrial_diamond", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("industrial_diamond")))));
   public static final Item PLANT_BALL = register("plant_ball", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("plant_ball")))));
   public static final Item COMPRESSED_PLANTS = register("compressed_plants", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("compressed_plants")))));
   public static final Item BIO_CHAFF = register("bio_chaff", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bio_chaff")))));
   public static final Item COMPRESSED_HYDRATED_COAL = register("compressed_hydrated_coal", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("compressed_hydrated_coal")))));
   public static final Item SCRAP = register("scrap", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("scrap")))));
   public static final Item SCRAP_BOX = register("scrap_box", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("scrap_box")))));
   public static final Item CF_POWDER = register("cf_powder", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("cf_powder")))));
   public static final Item PELLET = register("pellet", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("pellet")))));
   public static final Item RAW_CRYSTAL_MEMORY = register("raw_crystal_memory", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("raw_crystal_memory")))));
   public static final Item CRYSTAL_MEMORY = register("crystal_memory", new ItemCrystalMemory(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crystal_memory"))).stacksTo(1)));
   public static final Item IRON_SHAFT = register("iron_shaft", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_shaft")))));
   public static final Item STEEL_SHAFT = register("steel_shaft", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steel_shaft")))));
   public static final Item WOODEN_ROTOR_BLADE = register("wooden_rotor_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("wooden_rotor_blade")))));
   public static final Item IRON_ROTOR_BLADE = register("iron_rotor_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_rotor_blade")))));
   public static final Item STEEL_ROTOR_BLADE = register("steel_rotor_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steel_rotor_blade")))));
   public static final Item CARBON_ROTOR_BLADE = register("carbon_rotor_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("carbon_rotor_blade")))));
   public static final Item STEAM_TURBINE_BLADE = register("steam_turbine_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steam_turbine_blade")))));
   public static final Item STEAM_TURBINE = register("steam_turbine", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steam_turbine")))));
   public static final Item JETPACK_ATTACHMENT_PLATE = register("jetpack_attachment_plate", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("jetpack_attachment_plate")))));
   public static final Item COIN = register("coin", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("coin")))));
   public static final Item EMPTY_FUEL_CAN = register("empty_fuel_can", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("empty_fuel_can")))));
   public static final Item BRONZE_ROTOR_BLADE = register("bronze_rotor_blade", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_rotor_blade")))));
   public static final Item BRONZE_SHAFT = register("bronze_shaft", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_shaft")))));
   public static final Item REACTOR_COOLANT_CELL = register(
      "reactor_coolant_cell", new ItemReactorHeatStorage(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reactor_coolant_cell"))), 10000)
   );
   public static final Item TRIPLE_REACTOR_COOLANT_CELL = register(
      "triple_reactor_coolant_cell", new ItemReactorHeatStorage(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("triple_reactor_coolant_cell"))), 30000)
   );
   public static final Item SEXTUPLE_REACTOR_COOLANT_CELL = register(
      "sextuple_reactor_coolant_cell", new ItemReactorHeatStorage(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("sextuple_reactor_coolant_cell"))), 60000)
   );
   public static final Item REACTOR_PLATING = register("reactor_plating", new ItemReactorPlating(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reactor_plating"))), 1000, 0.95F));
   public static final Item REACTOR_HEAT_PLATING = register(
      "reactor_heat_plating", new ItemReactorPlating(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reactor_heat_plating"))), 2000, 0.99F)
   );
   public static final Item CONTAINMENT_REACTOR_PLATING = register(
      "containment_reactor_plating", new ItemReactorPlating(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("containment_reactor_plating"))), 500, 0.9F)
   );
   public static final Item HEAT_EXCHANGER = register("heat_exchanger", new ItemReactorHeatSwitch(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("heat_exchanger"))), 2500, 12, 4));
   public static final Item REACTOR_HEAT_EXCHANGER = register(
      "reactor_heat_exchanger", new ItemReactorHeatSwitch(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reactor_heat_exchanger"))), 5000, 0, 72)
   );
   public static final Item COMPONENT_HEAT_EXCHANGER = register(
      "component_heat_exchanger", new ItemReactorHeatSwitch(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("component_heat_exchanger"))), 5000, 36, 0)
   );
   public static final Item ADVANCED_HEAT_EXCHANGER = register(
      "advanced_heat_exchanger", new ItemReactorHeatSwitch(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("advanced_heat_exchanger"))), 10000, 24, 8)
   );
   public static final Item HEAT_VENT = register("heat_vent", new ItemReactorVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("heat_vent"))), 1000, 6, 0));
   public static final Item REACTOR_HEAT_VENT = register("reactor_heat_vent", new ItemReactorVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reactor_heat_vent"))), 1000, 5, 5));
   public static final Item OVERCLOCKED_HEAT_VENT = register(
      "overclocked_heat_vent", new ItemReactorVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("overclocked_heat_vent"))), 1000, 20, 36)
   );
   public static final Item COMPONENT_HEAT_VENT = register("component_heat_vent", new ItemReactorVentSpread(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("component_heat_vent"))), 4));
   public static final Item ADVANCED_HEAT_VENT = register("advanced_heat_vent", new ItemReactorVent(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("advanced_heat_vent"))), 1000, 12, 0));
   public static final Item NEUTRON_REFLECTOR = register("neutron_reflector", new ItemReactorReflector(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("neutron_reflector"))), 30000));
   public static final Item THICK_NEUTRON_REFLECTOR = register(
      "thick_neutron_reflector", new ItemReactorReflector(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("thick_neutron_reflector"))), 120000)
   );
   public static final Item IRIDIUM_NEUTRON_REFLECTOR = register(
      "iridium_neutron_reflector", new ItemReactorIridiumReflector(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iridium_neutron_reflector"))))
   );
   public static final Item RSH_CONDENSATOR = register("rsh_condensator", new ItemReactorCondensator(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rsh_condensator"))), 20000));
   public static final Item LZH_CONDENSATOR = register("lzh_condensator", new ItemReactorCondensator(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lzh_condensator"))), 100000));
   public static final Item HEATPACK = register("heatpack", new ItemReactorHeatpack(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("heatpack"))), 1000, 1));
   public static final Item REACTOR_VESSEL = register("reactor_vessel", new BlockItem(Ic2Blocks.REACTOR_VESSEL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("reactor_vessel")))));
   public static final Item URANIUM_FUEL_ROD = register("uranium_fuel_rod", new ItemReactorUranium(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("uranium_fuel_rod"))), 1));
   public static final Item DUAL_URANIUM_FUEL_ROD = register("dual_uranium_fuel_rod", new ItemReactorUranium(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dual_uranium_fuel_rod"))), 2));
   public static final Item QUAD_URANIUM_FUEL_ROD = register("quad_uranium_fuel_rod", new ItemReactorUranium(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("quad_uranium_fuel_rod"))), 4));
   public static final Item MOX_FUEL_ROD = register("mox_fuel_rod", new ItemReactorMOX(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mox_fuel_rod"))), 1));
   public static final Item DUAL_MOX_FUEL_ROD = register("dual_mox_fuel_rod", new ItemReactorMOX(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dual_mox_fuel_rod"))), 2));
   public static final Item QUAD_MOX_FUEL_ROD = register("quad_mox_fuel_rod", new ItemReactorMOX(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("quad_mox_fuel_rod"))), 4));
   public static final Item LITHIUM_FUEL_ROD = register("lithium_fuel_rod", new ItemReactorLithiumCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lithium_fuel_rod")))));
   public static final Item DEPLETED_ISOTOPE_FUEL_ROD = register(
      "depleted_isotope_fuel_rod", new ItemReactorDepletedUranium(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("depleted_isotope_fuel_rod"))))
   );
   public static final Item EMPTY_MUG = register("empty_mug", new ItemMug(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("empty_mug"))).stacksTo(1), ItemMug.MugType.empty));
   public static final Item COFFEE_MUG = register("coffee_mug", new ItemMug(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("coffee_mug"))).stacksTo(1), ItemMug.MugType.coffee));
   public static final Item COLD_COFFEE_MUG = register(
      "cold_coffee_mug", new ItemMug(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("cold_coffee_mug"))).stacksTo(1), ItemMug.MugType.cold_coffee)
   );
   public static final Item DARK_COFFEE_MUG = register(
      "dark_coffee_mug", new ItemMug(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("dark_coffee_mug"))).stacksTo(1), ItemMug.MugType.dark_coffee)
   );
   public static final Item CROP_STICK = register("crop_stick", new ItemCrop(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crop_stick")))));
   public static final Item CROP_SEED_BACK = register("crop_seed_bag", new ItemCropSeed(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crop_seed_bag"))).stacksTo(1)));
   public static final Item COFFEE_BEANS = register("coffee_beans", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("coffee_beans")))));
   public static final Item COFFEE_POWDER = register("coffee_powder", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("coffee_powder")))));
   public static final Item FERTILIZER = register("fertilizer", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("fertilizer")))));
   public static final Item GRIN_POWDER = register("grin_powder", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("grin_powder")))));
   public static final Item HOPS = register("hops", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("hops")))));
   public static final Item WEED = register("weed", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("weed")))));
   public static final Item TERRA_WART = register(
      "terra_wart",
      new ItemTerraWart(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("terra_wart"))).food(new FoodProperties.Builder().nutrition(0).saturationModifier(1.0F).alwaysEdible().build()).rarity(Rarity.RARE)
      )
   );
   public static final Item CUTTER = register("cutter", new ItemToolCutter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("cutter"))).durability(60)));
   public static final Item DEBUG_ITEM = register(
      "debug_item", new ItemDebug(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("debug_item"))).stacksTo(1))
   );
   public static final Item FORGE_HAMMER = register("forge_hammer", new ItemToolCrafting(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("forge_hammer"))).durability(80)));
   public static final Item TOOL_BOX = register("tool_box", new ItemToolbox(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("tool_box"))).stacksTo(1)));
   public static final Item METER = register("meter", new ItemToolMeter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("meter"))).stacksTo(1)));
   public static final Item TREETAP = register("treetap", new ItemTreetap(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("treetap"))).durability(16)));
   public static final Item WRENCH = register("wrench", new ItemToolWrench(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("wrench"))).durability(120)));
   public static final Item BRONZE_AXE = register(
      "bronze_axe", new Ic2Axe(Ic2ToolMaterials.BRONZE, 6.0F, -3.1F, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_axe"))))
   );
   public static final Item BRONZE_HOE = register(
      "bronze_hoe", new Ic2Hoe(Ic2ToolMaterials.BRONZE, -2, -1.0F, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_hoe"))))
   );
   public static final Item BRONZE_SWORD = register(
      "bronze_sword", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_sword"))).sword(Ic2ToolMaterials.BRONZE, 3.0F, -2.4F))
   );
   public static final Item BRONZE_SHOVEL = register(
      "bronze_shovel", new ShovelItem(Ic2ToolMaterials.BRONZE, 1.5F, -3.0F, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_shovel"))))
   );
   public static final Item BRONZE_PICKAXE = register(
      "bronze_pickaxe", new Ic2Pickaxe(Ic2ToolMaterials.BRONZE, 1, -2.8F, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_pickaxe"))))
   );
   public static final Item FREQUENCY_TRANSMITTER = register(
      "frequency_transmitter", new ItemFrequencyTransmitter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("frequency_transmitter"))).stacksTo(1))
   );
   public static final Item CROWBAR = register("crowbar", new ItemToolCrowbar(ToolMaterial.IRON, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("crowbar"))).durability(250)));
   public static final Item ADVANCED_SCANNER = register(
      "advanced_scanner", new ItemScannerAdv(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("advanced_scanner"))).stacksTo(1))
   );
   public static final Item CHAINSAW = register("chainsaw", new ItemElectricToolChainsaw(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("chainsaw"))).stacksTo(1)));
   public static final Item DIAMOND_DRILL = register(
      "diamond_drill", new ItemDrill(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("diamond_drill"))).stacksTo(1), 80, ToolMaterial.DIAMOND, 30000, 100, 1, 16.0F)
   );
   public static final Item DRILL = register(
      "drill", new ItemDrill(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("drill"))).stacksTo(1), 50, ToolMaterial.IRON, 30000, 100, 1, 8.0F)
   );
   public static final Item MINING_LASER = register("mining_laser", new ItemToolMiningLaser(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mining_laser"))).stacksTo(1)));
   public static final Item ELECTRIC_TREETAP = register(
      "electric_treetap", new ItemTreetapElectric(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("electric_treetap"))).stacksTo(1))
   );
   public static final Item ELECTRIC_WRENCH = register(
      "electric_wrench", new ItemToolWrenchElectric(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("electric_wrench"))).stacksTo(1))
   );
   public static final Item IRIDIUM_DRILL = register("iridium_drill", new ItemDrillIridium(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iridium_drill"))).stacksTo(1)));
   public static final Item OBSCURATOR = register("obscurator", new ItemObscurator(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("obscurator"))).stacksTo(1)));
   public static final Item SCANNER = register(
      "scanner", new ItemScanner(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("scanner"))).stacksTo(1), 100000.0, 128.0, 1)
   );
   public static final Item WIND_METER = register("wind_meter", new ItemWindMeter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("wind_meter"))).stacksTo(1)));
   public static final Item PAINTER = register(
      "painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("painter"))).stacksTo(1).durability(32), null)
   );
   public static final Item BLACK_PAINTER = register(
      "black_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("black_painter"))).stacksTo(1).durability(32), Ic2Color.BLACK)
   );
   public static final Item BLUE_PAINTER = register(
      "blue_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("blue_painter"))).stacksTo(1).durability(32), Ic2Color.BLUE)
   );
   public static final Item BROWN_PAINTER = register(
      "brown_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("brown_painter"))).stacksTo(1).durability(32), Ic2Color.BROWN)
   );
   public static final Item CYAN_PAINTER = register(
      "cyan_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("cyan_painter"))).stacksTo(1).durability(32), Ic2Color.CYAN)
   );
   public static final Item GRAY_PAINTER = register(
      "gray_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("gray_painter"))).stacksTo(1).durability(32), Ic2Color.GRAY)
   );
   public static final Item GREEN_PAINTER = register(
      "green_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("green_painter"))).stacksTo(1).durability(32), Ic2Color.GREEN)
   );
   public static final Item LIGHT_BLUE_PAINTER = register(
      "light_blue_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("light_blue_painter"))).stacksTo(1).durability(32), Ic2Color.LIGHT_BLUE)
   );
   public static final Item LIGHT_GRAY_PAINTER = register(
      "light_gray_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("light_gray_painter"))).stacksTo(1).durability(32), Ic2Color.LIGHT_GRAY)
   );
   public static final Item LIME_PAINTER = register(
      "lime_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lime_painter"))).stacksTo(1).durability(32), Ic2Color.LIME)
   );
   public static final Item MAGENTA_PAINTER = register(
      "magenta_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("magenta_painter"))).stacksTo(1).durability(32), Ic2Color.MAGENTA)
   );
   public static final Item ORANGE_PAINTER = register(
      "orange_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("orange_painter"))).stacksTo(1).durability(32), Ic2Color.ORANGE)
   );
   public static final Item PINK_PAINTER = register(
      "pink_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("pink_painter"))).stacksTo(1).durability(32), Ic2Color.PINK)
   );
   public static final Item PURPLE_PAINTER = register(
      "purple_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("purple_painter"))).stacksTo(1).durability(32), Ic2Color.PURPLE)
   );
   public static final Item RED_PAINTER = register(
      "red_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("red_painter"))).stacksTo(1).durability(32), Ic2Color.RED)
   );
   public static final Item WHITE_PAINTER = register(
      "white_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("white_painter"))).stacksTo(1).durability(32), Ic2Color.WHITE)
   );
   public static final Item YELLOW_PAINTER = register(
      "yellow_painter", new ItemToolPainter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("yellow_painter"))).stacksTo(1).durability(32), Ic2Color.YELLOW)
   );
   public static final Item BROKEN_RUBBER_BOAT = register("broken_rubber_boat", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("broken_rubber_boat"))).stacksTo(1)));
   public static final Item RUBBER_BOAT = register(
      "rubber_boat", new BoatItem(RubberBoatEntity.class, Ic2Entities.RUBBER_BOAT, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_boat"))).stacksTo(1))
   );
   public static final Item ELECTRIC_BOAT = register(
      "electric_boat", new BoatItem(ElectricBoatEntity.class, Ic2Entities.ELECTRIC_BOAT, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("electric_boat"))).stacksTo(1))
   );
   public static final Item CARBON_BOAT = register(
      "carbon_boat", new BoatItem(CarbonBoatEntity.class, Ic2Entities.CARBON_BOAT, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("carbon_boat"))).stacksTo(1))
   );
   public static final Item RE_BATTERY = register("re_battery", new ItemBattery(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("re_battery"))), 10000.0, 100.0, 1));
   public static final Item ADVANCED_RE_BATTERY = register(
      "advanced_re_battery", new ItemBattery(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("advanced_re_battery"))).stacksTo(16), 100000.0, 256.0, 2)
   );
   public static final Item ENERGY_CRYSTAL = register(
      "energy_crystal", new ItemBattery(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("energy_crystal"))).stacksTo(16), 1000000.0, 2048.0, 3)
   );
   public static final Item LAPOTRON_CRYSTAL = register(
      "lapotron_crystal", new ItemBattery(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lapotron_crystal"))).stacksTo(16).rarity(Rarity.UNCOMMON), 1.0E7, 8092.0, 4)
   );
   public static final Item SINGLE_USE_BATTERY = register(
      "single_use_battery", new ItemBatterySU(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("single_use_battery"))), 1200, 1)
   );
   public static final Item WOODEN_ROTOR = register(
      "wooden_rotor",
      new ItemWindRotor(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("wooden_rotor"))).durability(10800),
         5,
         false,
         0.25F,
         10,
         60,
         IC2.getIdentifier("textures/item/rotor/wood_rotor_model.png")
      )
   );
   public static final Item BRONZE_ROTOR = register(
      "bronze_rotor",
      new ItemWindRotor(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_rotor"))).durability(86400),
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
      new ItemWindRotor(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iron_rotor"))).durability(86400),
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
      new ItemWindRotor(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("steel_rotor"))).durability(172800),
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
      new ItemWindRotor(
         new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("carbon_rotor"))).durability(604800),
         11,
         true,
         1.0F,
         20,
         110,
         Identifier.fromNamespaceAndPath("ic2", "textures/item/rotor/carbon_rotor_model.png")
      )
   );
   public static final Item EMPTY_CELL = register("empty_cell", new ItemClassicCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("empty_cell"))), Fluids.EMPTY, 0));
   public static final Item WATER_CELL = register("water_cell", new ItemClassicCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("water_cell"))), Fluids.WATER, 1));
   public static final Item LAVA_CELL = register("lava_cell", new ItemClassicCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("lava_cell"))), Fluids.LAVA, 1));
   public static final Item AIR_CELL = register("air_cell", new ItemClassicCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("air_cell"))), Ic2Fluids.AIR.still, 1));
   public static final Item ELECTROLYZED_WATER_CELL = register(
      "electrolyzed_water_cell", new ItemClassicCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("electrolyzed_water_cell"))), null, 1)
   );
   public static final Item BIOFUEL_CELL = register("biofuel_cell", new ItemClassicCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("biofuel_cell"))), null, 1));
   public static final Item COALFUEL_CELL = register("coalfuel_cell", new ItemClassicCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("coalfuel_cell"))), null, 1));
   public static final Item BIO_CELL = register("bio_cell", new ItemClassicCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bio_cell"))), null, 1));
   public static final Item HYDRATED_COAL_CELL = register(
      "hydrated_coal_cell", new ItemClassicCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("hydrated_coal_cell"))), null, 1)
   );
   public static final ItemClassicCell WEED_EX_CELL = register(
      "weed_ex_cell", new ItemClassicCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("weed_ex_cell"))).stacksTo(1), null, 64)
   );
   public static final ItemClassicCell HYDRATION_CELL = register(
      "hydration_cell", new ItemClassicCell(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("hydration_cell"))).stacksTo(1), null, 10000)
   );
   public static final Item OVERCLOCKER_UPGRADE = register(
      "overclocker_upgrade", new ItemUpgradeModule(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("overclocker_upgrade"))), ItemUpgradeModule.UpgradeType.overclocker)
   );
   public static final Item TRANSFORMER_UPGRADE = register(
      "transformer_upgrade", new ItemUpgradeModule(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("transformer_upgrade"))), ItemUpgradeModule.UpgradeType.transformer)
   );
   public static final Item ENERGY_STORAGE_UPGRADE = register(
      "energy_storage_upgrade", new ItemUpgradeModule(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("energy_storage_upgrade"))), ItemUpgradeModule.UpgradeType.energy_storage)
   );
   public static final Item REDSTONE_INVERTER_UPGRADE = register(
      "redstone_inverter_upgrade",
      new ItemUpgradeModule(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("redstone_inverter_upgrade"))), ItemUpgradeModule.UpgradeType.redstone_inverter)
   );
   public static final Item EJECTOR_UPGRADE = register(
      "ejector_upgrade", new ItemUpgradeModule(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("ejector_upgrade"))), ItemUpgradeModule.UpgradeType.ejector)
   );
   public static final Item ADVANCED_EJECTOR_UPGRADE = register(
      "advanced_ejector_upgrade", new ItemUpgradeModule(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("advanced_ejector_upgrade"))), ItemUpgradeModule.UpgradeType.advanced_ejector)
   );
   public static final Item PULLING_UPGRADE = register(
      "pulling_upgrade", new ItemUpgradeModule(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("pulling_upgrade"))), ItemUpgradeModule.UpgradeType.pulling)
   );
   public static final Item ADVANCED_PULLING_UPGRADE = register(
      "advanced_pulling_upgrade", new ItemUpgradeModule(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("advanced_pulling_upgrade"))), ItemUpgradeModule.UpgradeType.advanced_pulling)
   );
   public static final Item FLUID_EJECTOR_UPGRADE = register(
      "fluid_ejector_upgrade", new ItemUpgradeModule(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("fluid_ejector_upgrade"))), ItemUpgradeModule.UpgradeType.fluid_ejector)
   );
   public static final Item FLUID_PULLING_UPGRADE = register(
      "fluid_pulling_upgrade", new ItemUpgradeModule(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("fluid_pulling_upgrade"))), ItemUpgradeModule.UpgradeType.fluid_pulling)
   );
   public static final Item REMOTE_INTERFACE_UPGRADE = register(
      "remote_interface_upgrade", new ItemUpgradeModule(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("remote_interface_upgrade"))), ItemUpgradeModule.UpgradeType.remote_interface)
   );
   public static final Item FILLED_TIN_CAN = register("filled_tin_can", new ItemTinCan(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("filled_tin_can")))));
   public static final Item FILLED_FUEL_CAN = register(
      "filled_fuel_can", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("filled_fuel_can"))).stacksTo(1)) {
         @Override
         public net.minecraft.world.item.ItemStackTemplate getCraftingRemainder(net.minecraft.world.item.ItemInstance instance) {
            return new net.minecraft.world.item.ItemStackTemplate(EMPTY_FUEL_CAN);
         }
      }
   );
   public static final Item BLANK_TFBP = register("blank_tfbp", new Tfbp(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("blank_tfbp"))).stacksTo(1), 0.0, 0, null));
   public static final Item CHILLING_TFBP = register(
      "chilling_tfbp", new Tfbp(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("chilling_tfbp"))).stacksTo(1), 2000.0, 50, new Chilling())
   );
   public static final Item CULTIVATION_TFBP = register(
      "cultivation_tfbp", new Tfbp(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("cultivation_tfbp"))).stacksTo(1), 4000.0, 40, new Cultivation())
   );
   public static final Item DESERTIFICATION_TFBP = register(
      "desertification_tfbp", new Tfbp(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("desertification_tfbp"))).stacksTo(1), 2500.0, 40, new Desertification())
   );
   public static final Item FLATIFICATION_TFBP = register(
      "flatification_tfbp", new Tfbp(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("flatification_tfbp"))).stacksTo(1), 4000.0, 40, new Flatification())
   );
   public static final Item IRRIGATION_TFBP = register(
      "irrigation_tfbp", new Tfbp(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("irrigation_tfbp"))).stacksTo(1), 3000.0, 60, new Irrigation())
   );
   public static final Item MUSHROOM_TFBP = register(
      "mushroom_tfbp", new Tfbp(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("mushroom_tfbp"))).stacksTo(1), 8000.0, 25, new Mushroom())
   );
   public static final Item NANO_SABER = register("nano_saber", new ItemNanoSaber(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("nano_saber"))).stacksTo(1)));
   public static final Item ALLOY_CHESTPLATE = register(
      "alloy_chestplate", new ItemArmorIC2(Ic2ArmorMaterials.ALLOY, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("alloy_chestplate"))), EquipmentSlot.CHEST)
   );
   public static final Item BRONZE_BOOTS = register(
      "bronze_boots", new ItemArmorIC2(Ic2ArmorMaterials.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_boots"))), EquipmentSlot.FEET)
   );
   public static final Item BRONZE_CHESTPLATE = register(
      "bronze_chestplate", new ItemArmorIC2(Ic2ArmorMaterials.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_chestplate"))), EquipmentSlot.CHEST)
   );
   public static final Item BRONZE_HELMET = register(
      "bronze_helmet", new ItemArmorIC2(Ic2ArmorMaterials.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_helmet"))), EquipmentSlot.HEAD)
   );
   public static final Item BRONZE_LEGGINGS = register(
      "bronze_leggings", new ItemArmorIC2(Ic2ArmorMaterials.BRONZE, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("bronze_leggings"))), EquipmentSlot.LEGS)
   );
   public static final Item CF_PACK = register("cf_pack", new ItemArmorCFPack(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("cf_pack")))));
   public static final Item HAZMAT_CHESTPLATE = register(
      "hazmat_chestplate", new ItemArmorHazmat(EquipmentSlot.CHEST, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("hazmat_chestplate"))))
   );
   public static final Item HAZMAT_HELMET = register("hazmat_helmet", new ItemArmorHazmat(EquipmentSlot.HEAD, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("hazmat_helmet")))));
   public static final Item HAZMAT_LEGGINGS = register("hazmat_leggings", new ItemArmorHazmat(EquipmentSlot.LEGS, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("hazmat_leggings")))));
   public static final Item JETPACK = register("jetpack", new ItemArmorJetpack(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("jetpack")))));
   public static final Item NANO_BOOTS = register(
      "nano_boots",
      new ItemArmorNanoSuit(Ic2ArmorMaterials.NANO_SUIT, EquipmentSlot.FEET, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("nano_boots"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item NANO_CHESTPLATE = register(
      "nano_chestplate",
      new ItemArmorNanoSuit(Ic2ArmorMaterials.NANO_SUIT, EquipmentSlot.CHEST, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("nano_chestplate"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item NANO_HELMET = register(
      "nano_helmet",
      new ItemArmorNanoSuit(Ic2ArmorMaterials.NANO_SUIT, EquipmentSlot.HEAD, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("nano_helmet"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item NANO_LEGGINGS = register(
      "nano_leggings",
      new ItemArmorNanoSuit(Ic2ArmorMaterials.NANO_SUIT, EquipmentSlot.LEGS, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("nano_leggings"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_BOOTS = register(
      "quantum_boots",
      new ItemArmorQuantumSuit(Ic2ArmorMaterials.QUANTUM_SUIT, EquipmentSlot.FEET, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("quantum_boots"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_CHESTPLATE = register(
      "quantum_chestplate",
      new ItemArmorQuantumSuit(Ic2ArmorMaterials.QUANTUM_SUIT, EquipmentSlot.CHEST, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("quantum_chestplate"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_HELMET = register(
      "quantum_helmet",
      new ItemArmorQuantumSuit(Ic2ArmorMaterials.QUANTUM_SUIT, EquipmentSlot.HEAD, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("quantum_helmet"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item QUANTUM_LEGGINGS = register(
      "quantum_leggings",
      new ItemArmorQuantumSuit(Ic2ArmorMaterials.QUANTUM_SUIT, EquipmentSlot.LEGS, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("quantum_leggings"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item RUBBER_BOOTS = register("rubber_boots", new ItemArmorHazmat(EquipmentSlot.FEET, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("rubber_boots")))));
   public static final Item NIGHT_VISION_GOGGLES = register(
      "night_vision_goggles", new ItemArmorNightVisionGoggles(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("night_vision_goggles"))).durability(27))
   );

   public static void init() {
      IC2.envProxy.registerBurnTime(Items.SUGAR_CANE, 50);
      IC2.envProxy.registerBurnTime(Items.CACTUS, 50);
      IC2.envProxy.registerBurnTime(RUBBER_SAPLING, 80);
      IC2.envProxy.registerBurnTime(WOODEN_SCAFFOLD, 300);
      IC2.envProxy.registerBurnTime(WOODEN_STORAGE_BOX, 1200);
      IC2.envProxy.registerBurnTime(WOODEN_ROTOR_BLADE, 300);
      IC2.envProxy.registerBurnTime(WOODEN_ROTOR, 300);
      IC2.envProxy.registerBurnTime(SCRAP, 350);
      IC2.envProxy.registerBurnTime(SCRAP_BOX, 3150);
   }

   private static <T extends Item> T register(String name, T item) {
      IC2.envProxy.registerItem(IC2.getIdentifier(name), item);
      return item;
   }
}
