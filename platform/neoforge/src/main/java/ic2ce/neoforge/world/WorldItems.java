package ic2ce.neoforge.world;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.SignItem;

/** World items, staged in declaration order. */
public final class WorldItems {
   public static final Item BASALT = ItemDefinitions.register("basalt", new BlockItem(WorldBlocks.BASALT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("basalt")))));
   public static final Item LEAD_ORE = ItemDefinitions.register("lead_ore", new BlockItem(WorldBlocks.LEAD_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("lead_ore")))));
   public static final Item TIN_ORE = ItemDefinitions.register("tin_ore", new BlockItem(WorldBlocks.TIN_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("tin_ore")))));
   public static final Item URANIUM_ORE = ItemDefinitions.register("uranium_ore", new BlockItem(WorldBlocks.URANIUM_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("uranium_ore")))));
   public static final Item DEEPSLATE_URANIUM_ORE = ItemDefinitions.register(
      "deepslate_uranium_ore", new BlockItem(WorldBlocks.DEEPSLATE_URANIUM_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("deepslate_uranium_ore"))))
   );
   public static final Item DEEPSLATE_TIN_ORE = ItemDefinitions.register(
      "deepslate_tin_ore", new BlockItem(WorldBlocks.DEEPSLATE_TIN_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("deepslate_tin_ore"))))
   );
   public static final Item DEEPSLATE_LEAD_ORE = ItemDefinitions.register(
      "deepslate_lead_ore", new BlockItem(WorldBlocks.DEEPSLATE_LEAD_ORE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("deepslate_lead_ore"))))
   );
   public static final Item RAW_LEAD_BLOCK = ItemDefinitions.register("raw_lead_block", new BlockItem(WorldBlocks.RAW_LEAD_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("raw_lead_block")))));
   public static final Item RAW_TIN_BLOCK = ItemDefinitions.register("raw_tin_block", new BlockItem(WorldBlocks.RAW_TIN_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("raw_tin_block")))));
   public static final Item RAW_URANIUM_BLOCK = ItemDefinitions.register(
      "raw_uranium_block", new BlockItem(WorldBlocks.RAW_URANIUM_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("raw_uranium_block"))))
   );
   public static final Item BRONZE_BLOCK = ItemDefinitions.register("bronze_block", new BlockItem(WorldBlocks.BRONZE_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("bronze_block")))));
   public static final Item LEAD_BLOCK = ItemDefinitions.register("lead_block", new BlockItem(WorldBlocks.LEAD_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("lead_block")))));
   public static final Item STEEL_BLOCK = ItemDefinitions.register("steel_block", new BlockItem(WorldBlocks.STEEL_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("steel_block")))));
   public static final Item TIN_BLOCK = ItemDefinitions.register("tin_block", new BlockItem(WorldBlocks.TIN_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("tin_block")))));
   public static final Item URANIUM_BLOCK = ItemDefinitions.register("uranium_block", new BlockItem(WorldBlocks.URANIUM_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("uranium_block")))));
   public static final Item REINFORCED_STONE = ItemDefinitions.register(
      "reinforced_stone", new BlockItem(WorldBlocks.REINFORCED_STONE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reinforced_stone"))))
   );
   public static final Item MACHINE = ItemDefinitions.register("machine", new BlockItem(WorldBlocks.MACHINE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("machine")))));
   public static final Item ADVANCED_MACHINE = ItemDefinitions.register(
      "advanced_machine", new BlockItem(WorldBlocks.ADVANCED_MACHINE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("advanced_machine"))))
   );
   public static final Item SILVER_BLOCK = ItemDefinitions.register("silver_block", new BlockItem(WorldBlocks.SILVER_BLOCK, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("silver_block")))));
   public static final Item RUBBER_LEAVES = ItemDefinitions.register("rubber_leaves", new BlockItem(WorldBlocks.RUBBER_LEAVES, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_leaves")))));
   public static final Item RUBBER_LOG = ItemDefinitions.register("rubber_log", new BlockItem(WorldBlocks.RUBBER_LOG, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_log")))));
   public static final Item STRIPPED_RUBBER_LOG = ItemDefinitions.register(
      "stripped_rubber_log", new BlockItem(WorldBlocks.STRIPPED_RUBBER_LOG, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("stripped_rubber_log"))))
   );
   public static final Item RUBBER_WOOD = ItemDefinitions.register("rubber_wood", new BlockItem(WorldBlocks.RUBBER_WOOD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_wood")))));
   public static final Item STRIPPED_RUBBER_WOOD = ItemDefinitions.register(
      "stripped_rubber_wood", new BlockItem(WorldBlocks.STRIPPED_RUBBER_WOOD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("stripped_rubber_wood"))))
   );
   public static final Item RUBBER_PLANKS = ItemDefinitions.register("rubber_planks", new BlockItem(WorldBlocks.RUBBER_PLANKS, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_planks")))));
   public static final Item RUBBER_SAPLING = ItemDefinitions.register("rubber_sapling", new BlockItem(WorldBlocks.RUBBER_SAPLING, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_sapling")))));
   public static final Item RUBBER_BUTTON = ItemDefinitions.register("rubber_button", new BlockItem(WorldBlocks.RUBBER_BUTTON, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_button")))));
   public static final Item RUBBER_DOOR = ItemDefinitions.register("rubber_door", new BlockItem(WorldBlocks.RUBBER_DOOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_door")))));
   public static final Item RUBBER_FENCE = ItemDefinitions.register("rubber_fence", new BlockItem(WorldBlocks.RUBBER_FENCE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_fence")))));
   public static final Item RUBBER_FENCE_GATE = ItemDefinitions.register(
      "rubber_fence_gate", new BlockItem(WorldBlocks.RUBBER_FENCE_GATE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_fence_gate"))))
   );
   public static final Item RUBBER_PRESSURE_PLATE = ItemDefinitions.register(
      "rubber_pressure_plate", new BlockItem(WorldBlocks.RUBBER_PRESSURE_PLATE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_pressure_plate"))))
   );
   public static final Item RUBBER_SIGN = ItemDefinitions.register(
      "rubber_sign", new SignItem(WorldBlocks.RUBBER_SIGN, WorldBlocks.RUBBER_WALL_SIGN, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_sign"))))
   );
   public static final Item RUBBER_SLAB = ItemDefinitions.register("rubber_slab", new BlockItem(WorldBlocks.RUBBER_SLAB, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_slab")))));
   public static final Item RUBBER_STAIRS = ItemDefinitions.register("rubber_stairs", new BlockItem(WorldBlocks.RUBBER_STAIRS, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_stairs")))));
   public static final Item RUBBER_TRAPDOOR = ItemDefinitions.register(
      "rubber_trapdoor", new BlockItem(WorldBlocks.RUBBER_TRAPDOOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_trapdoor"))))
   );
   public static final Item WOODEN_SCAFFOLD = ItemDefinitions.register(
      "wooden_scaffold", new BlockItem(WorldBlocks.WOODEN_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("wooden_scaffold"))))
   );
   public static final Item REINFORCED_WOODEN_SCAFFOLD = ItemDefinitions.register(
      "reinforced_wooden_scaffold", new BlockItem(WorldBlocks.REINFORCED_WOODEN_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reinforced_wooden_scaffold"))))
   );
   public static final Item IRON_SCAFFOLD = ItemDefinitions.register("iron_scaffold", new BlockItem(WorldBlocks.IRON_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iron_scaffold")))));
   public static final Item REINFORCED_IRON_SCAFFOLD = ItemDefinitions.register(
      "reinforced_iron_scaffold", new BlockItem(WorldBlocks.REINFORCED_IRON_SCAFFOLD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reinforced_iron_scaffold"))))
   );
   public static final Item RESIN_SHEET = ItemDefinitions.register("resin_sheet", new BlockItem(WorldBlocks.RESIN_SHEET, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("resin_sheet")))));
   public static final Item RUBBER_SHEET = ItemDefinitions.register("rubber_sheet", new BlockItem(WorldBlocks.RUBBER_SHEET, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("rubber_sheet")))));
   public static final Item WOOL_SHEET = ItemDefinitions.register("wool_sheet", new BlockItem(WorldBlocks.WOOL_SHEET, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("wool_sheet")))));
   public static final Item REINFORCED_GLASS = ItemDefinitions.register(
      "reinforced_glass", new BlockItem(WorldBlocks.REINFORCED_GLASS, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reinforced_glass"))))
   );
   public static final Item REINFORCED_DOOR = ItemDefinitions.register(
      "reinforced_door", new DoubleHighBlockItem(WorldBlocks.REINFORCED_DOOR, new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("reinforced_door"))))
   );
   public static final Item FOAM = ItemDefinitions.register("foam", new BlockItem(WorldBlocks.FOAM, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("foam")))));
   public static final Item WHITE_WALL = ItemDefinitions.register("white_wall", new BlockItem(WorldBlocks.WHITE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("white_wall")))));
   public static final Item ORANGE_WALL = ItemDefinitions.register("orange_wall", new BlockItem(WorldBlocks.ORANGE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("orange_wall")))));
   public static final Item MAGENTA_WALL = ItemDefinitions.register("magenta_wall", new BlockItem(WorldBlocks.MAGENTA_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("magenta_wall")))));
   public static final Item LIGHT_BLUE_WALL = ItemDefinitions.register(
      "light_blue_wall", new BlockItem(WorldBlocks.LIGHT_BLUE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("light_blue_wall"))))
   );
   public static final Item YELLOW_WALL = ItemDefinitions.register("yellow_wall", new BlockItem(WorldBlocks.YELLOW_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("yellow_wall")))));
   public static final Item LIME_WALL = ItemDefinitions.register("lime_wall", new BlockItem(WorldBlocks.LIME_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("lime_wall")))));
   public static final Item PINK_WALL = ItemDefinitions.register("pink_wall", new BlockItem(WorldBlocks.PINK_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("pink_wall")))));
   public static final Item GRAY_WALL = ItemDefinitions.register("gray_wall", new BlockItem(WorldBlocks.GRAY_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("gray_wall")))));
   public static final Item LIGHT_GRAY_WALL = ItemDefinitions.register(
      "light_gray_wall", new BlockItem(WorldBlocks.LIGHT_GRAY_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("light_gray_wall"))))
   );
   public static final Item CYAN_WALL = ItemDefinitions.register("cyan_wall", new BlockItem(WorldBlocks.CYAN_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("cyan_wall")))));
   public static final Item PURPLE_WALL = ItemDefinitions.register("purple_wall", new BlockItem(WorldBlocks.PURPLE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("purple_wall")))));
   public static final Item BLUE_WALL = ItemDefinitions.register("blue_wall", new BlockItem(WorldBlocks.BLUE_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("blue_wall")))));
   public static final Item BROWN_WALL = ItemDefinitions.register("brown_wall", new BlockItem(WorldBlocks.BROWN_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("brown_wall")))));
   public static final Item GREEN_WALL = ItemDefinitions.register("green_wall", new BlockItem(WorldBlocks.GREEN_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("green_wall")))));
   public static final Item RED_WALL = ItemDefinitions.register("red_wall", new BlockItem(WorldBlocks.RED_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("red_wall")))));
   public static final Item BLACK_WALL = ItemDefinitions.register("black_wall", new BlockItem(WorldBlocks.BLACK_WALL, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("black_wall")))));

   private WorldItems() {}

   public static void init() {}
}
