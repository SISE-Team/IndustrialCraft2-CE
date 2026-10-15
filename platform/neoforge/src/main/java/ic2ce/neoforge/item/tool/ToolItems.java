package ic2ce.neoforge.item.tool;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.item.paint.PigmentPalette;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.registry.ToolProfiles;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;

/** Tool items, staged in declaration order. */
public final class ToolItems {
   public static final Item CUTTER = ItemDefinitions.register("cutter", new InsulationCutter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("cutter"))).durability(60)));
   public static final Item DEBUG_ITEM = ItemDefinitions.register(
      "debug_item", new DiagnosticTool(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("debug_item"))).stacksTo(1))
   );
   public static final Item FORGE_HAMMER = ItemDefinitions.register("forge_hammer", new DurableCraftingTool(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("forge_hammer"))).durability(80)));
   public static final Item METER = ItemDefinitions.register("meter", new EnergyProbe(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("meter"))).stacksTo(1)));
   public static final Item TREETAP = ItemDefinitions.register("treetap", new ManualResinTap(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("treetap"))).durability(16)));
   public static final Item WRENCH = ItemDefinitions.register("wrench", new MechanicalWrench(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("wrench"))).durability(120)));
   public static final Item BRONZE_AXE = ItemDefinitions.register(
      "bronze_axe", new net.minecraft.world.item.AxeItem(ToolProfiles.BRONZE, 6.0F, -3.1F, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_axe"))))
   );
   public static final Item BRONZE_HOE = ItemDefinitions.register(
      "bronze_hoe", new net.minecraft.world.item.HoeItem(ToolProfiles.BRONZE, -2, -1.0F, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_hoe"))))
   );
   public static final Item BRONZE_SWORD = ItemDefinitions.register(
      "bronze_sword", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_sword"))).sword(ToolProfiles.BRONZE, 3.0F, -2.4F))
   );
   public static final Item BRONZE_SHOVEL = ItemDefinitions.register(
      "bronze_shovel", new ShovelItem(ToolProfiles.BRONZE, 1.5F, -3.0F, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_shovel"))))
   );
   public static final Item BRONZE_PICKAXE = ItemDefinitions.register(
      "bronze_pickaxe", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_pickaxe"))).pickaxe(ToolProfiles.BRONZE, 1, -2.8F))
   );
   public static final Item FREQUENCY_TRANSMITTER = ItemDefinitions.register(
      "frequency_transmitter", new TeleporterLinkCard(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("frequency_transmitter"))).stacksTo(1))
   );
   public static final Item CROWBAR = ItemDefinitions.register("crowbar", new CoverCrowbar(ToolMaterial.IRON, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("crowbar"))).durability(250)));
   public static final Item ADVANCED_SCANNER = ItemDefinitions.register(
      "advanced_scanner", new AdvancedOreScanner(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("advanced_scanner"))).stacksTo(1))
   );
   public static final Item CHAINSAW = ItemDefinitions.register("chainsaw", new PoweredSaw(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("chainsaw"))).stacksTo(1)));
   public static final Item DIAMOND_DRILL = ItemDefinitions.register(
      "diamond_drill", new MiningDrill(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("diamond_drill"))).stacksTo(1), 80, ToolMaterial.DIAMOND, 30000, 100, 1, 16.0F)
   );
   public static final Item DRILL = ItemDefinitions.register(
      "drill", new MiningDrill(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("drill"))).stacksTo(1), 50, ToolMaterial.IRON, 30000, 100, 1, 8.0F)
   );
   public static final Item MINING_LASER = ItemDefinitions.register("mining_laser", new MiningLaser(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mining_laser"))).stacksTo(1)));
   public static final Item ELECTRIC_TREETAP = ItemDefinitions.register(
      "electric_treetap", new ElectricResinTap(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("electric_treetap"))).stacksTo(1))
   );
   public static final Item ELECTRIC_WRENCH = ItemDefinitions.register(
      "electric_wrench", new ElectricWrench(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("electric_wrench"))).stacksTo(1))
   );
   public static final Item IRIDIUM_DRILL = ItemDefinitions.register("iridium_drill", new IridiumDrill(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iridium_drill"))).stacksTo(1)));
   public static final Item OBSCURATOR = ItemDefinitions.register("obscurator", new SurfaceImprinter(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("obscurator"))).stacksTo(1)));
   public static final Item SCANNER = ItemDefinitions.register(
      "scanner", new OreScanner(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("scanner"))).stacksTo(1), 100000.0, 128.0, 1)
   );
   public static final Item WIND_METER = ItemDefinitions.register("wind_meter", new WindProbe(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("wind_meter"))).stacksTo(1)));
   public static final Item PAINTER = ItemDefinitions.register(
      "painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("painter"))).stacksTo(1).durability(32), null)
   );
   public static final Item BLACK_PAINTER = ItemDefinitions.register(
      "black_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("black_painter"))).stacksTo(1).durability(32), PigmentPalette.BLACK)
   );
   public static final Item BLUE_PAINTER = ItemDefinitions.register(
      "blue_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("blue_painter"))).stacksTo(1).durability(32), PigmentPalette.BLUE)
   );
   public static final Item BROWN_PAINTER = ItemDefinitions.register(
      "brown_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("brown_painter"))).stacksTo(1).durability(32), PigmentPalette.BROWN)
   );
   public static final Item CYAN_PAINTER = ItemDefinitions.register(
      "cyan_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("cyan_painter"))).stacksTo(1).durability(32), PigmentPalette.CYAN)
   );
   public static final Item GRAY_PAINTER = ItemDefinitions.register(
      "gray_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("gray_painter"))).stacksTo(1).durability(32), PigmentPalette.GRAY)
   );
   public static final Item GREEN_PAINTER = ItemDefinitions.register(
      "green_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("green_painter"))).stacksTo(1).durability(32), PigmentPalette.GREEN)
   );
   public static final Item LIGHT_BLUE_PAINTER = ItemDefinitions.register(
      "light_blue_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("light_blue_painter"))).stacksTo(1).durability(32), PigmentPalette.LIGHT_BLUE)
   );
   public static final Item LIGHT_GRAY_PAINTER = ItemDefinitions.register(
      "light_gray_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("light_gray_painter"))).stacksTo(1).durability(32), PigmentPalette.LIGHT_GRAY)
   );
   public static final Item LIME_PAINTER = ItemDefinitions.register(
      "lime_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("lime_painter"))).stacksTo(1).durability(32), PigmentPalette.LIME)
   );
   public static final Item MAGENTA_PAINTER = ItemDefinitions.register(
      "magenta_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("magenta_painter"))).stacksTo(1).durability(32), PigmentPalette.MAGENTA)
   );
   public static final Item ORANGE_PAINTER = ItemDefinitions.register(
      "orange_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("orange_painter"))).stacksTo(1).durability(32), PigmentPalette.ORANGE)
   );
   public static final Item PINK_PAINTER = ItemDefinitions.register(
      "pink_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("pink_painter"))).stacksTo(1).durability(32), PigmentPalette.PINK)
   );
   public static final Item PURPLE_PAINTER = ItemDefinitions.register(
      "purple_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("purple_painter"))).stacksTo(1).durability(32), PigmentPalette.PURPLE)
   );
   public static final Item RED_PAINTER = ItemDefinitions.register(
      "red_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("red_painter"))).stacksTo(1).durability(32), PigmentPalette.RED)
   );
   public static final Item WHITE_PAINTER = ItemDefinitions.register(
      "white_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("white_painter"))).stacksTo(1).durability(32), PigmentPalette.WHITE)
   );
   public static final Item YELLOW_PAINTER = ItemDefinitions.register(
      "yellow_painter", new PigmentBrush(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("yellow_painter"))).stacksTo(1).durability(32), PigmentPalette.YELLOW)
   );
   public static final Item NANO_SABER = ItemDefinitions.register("nano_saber", new NanoBlade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("nano_saber"))).stacksTo(1)));

   private ToolItems() {}

   public static void init() {}
}
