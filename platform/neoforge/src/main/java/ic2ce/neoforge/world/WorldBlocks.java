package ic2ce.neoforge.world;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.registry.SignDefinitions;
import ic2ce.neoforge.world.block.BarkWood;
import ic2ce.neoforge.world.block.ConstructionWall;
import ic2ce.neoforge.world.block.CuringFoam;
import ic2ce.neoforge.world.block.ImprintedWall;
import ic2ce.neoforge.world.block.ReactiveSheet;
import ic2ce.neoforge.world.block.ResinLog;
import ic2ce.neoforge.world.block.RubberStandingSign;
import ic2ce.neoforge.world.block.RubberWallSign;
import ic2ce.neoforge.world.block.StructuralGlass;
import ic2ce.neoforge.world.block.StructuralScaffold;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

/** World blocks, staged in declaration order. */
public final class WorldBlocks {
  private static final java.util.function.Supplier<Properties> wallSettings =
      () ->
          BlockProfiles.stone()
              .strength(3.0F, 30.0F)
              .requiresCorrectToolForDrops()
              .sound(SoundType.STONE);
  private static boolean never(BlockState state, BlockGetter world, BlockPos pos) {
    return false;
  }
  private static Boolean never(
      BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
    return false;
  }
  private static Boolean canSpawnOnLeaves(
      BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
    return type == EntityType.OCELOT || type == EntityType.PARROT;
  }
  public static final Block BASALT =
      BlockDefinitions.register(
          "basalt",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("basalt")))
                  .strength(20.0F, 45.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block LEAD_ORE =
      BlockDefinitions.register(
          "lead_ore",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("lead_ore")))
                  .strength(2.0F, 4.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block TIN_ORE =
      BlockDefinitions.register(
          "tin_ore",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("tin_ore")))
                  .strength(3.0F, 5.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block URANIUM_ORE =
      BlockDefinitions.register(
          "uranium_ore",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("uranium_ore")))
                  .strength(4.0F, 6.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block DEEPSLATE_LEAD_ORE =
      BlockDefinitions.register(
          "deepslate_lead_ore",
          new Block(
              BlockProfiles.stone(MapColor.DEEPSLATE)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "deepslate_lead_ore")))
                  .strength(3.0F, 6.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.DEEPSLATE)));
  public static final Block DEEPSLATE_TIN_ORE =
      BlockDefinitions.register(
          "deepslate_tin_ore",
          new Block(
              BlockProfiles.stone(MapColor.DEEPSLATE)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("deepslate_tin_ore")))
                  .strength(4.0F, 7.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.DEEPSLATE)));
  public static final Block DEEPSLATE_URANIUM_ORE =
      BlockDefinitions.register(
          "deepslate_uranium_ore",
          new Block(
              BlockProfiles.stone(MapColor.DEEPSLATE)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "deepslate_uranium_ore")))
                  .strength(5.0F, 8.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.DEEPSLATE)));
  public static final Block RAW_LEAD_BLOCK =
      BlockDefinitions.register(
          "raw_lead_block",
          new Block(
              BlockProfiles.stone(MapColor.COLOR_LIGHT_GRAY)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("raw_lead_block")))
                  .strength(3.0F, 11.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block RAW_TIN_BLOCK =
      BlockDefinitions.register(
          "raw_tin_block",
          new Block(
              BlockProfiles.stone(MapColor.SNOW)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("raw_tin_block")))
                  .strength(4.0F, 12.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block RAW_URANIUM_BLOCK =
      BlockDefinitions.register(
          "raw_uranium_block",
          new Block(
              BlockProfiles.stone(MapColor.COLOR_GREEN)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("raw_uranium_block")))
                  .strength(5.0F, 13.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block BRONZE_BLOCK =
      BlockDefinitions.register(
          "bronze_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("bronze_block")))
                  .strength(5.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block LEAD_BLOCK =
      BlockDefinitions.register(
          "lead_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("lead_block")))
                  .strength(4.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block STEEL_BLOCK =
      BlockDefinitions.register(
          "steel_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("steel_block")))
                  .strength(8.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block TIN_BLOCK =
      BlockDefinitions.register(
          "tin_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("tin_block")))
                  .strength(4.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block URANIUM_BLOCK =
      BlockDefinitions.register(
          "uranium_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("uranium_block")))
                  .strength(6.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block REINFORCED_STONE =
      BlockDefinitions.register(
          "reinforced_stone",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("reinforced_stone")))
                  .strength(80.0F, 180.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block MACHINE =
      BlockDefinitions.register(
          "machine",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("machine")))
                  .strength(5.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block ADVANCED_MACHINE =
      BlockDefinitions.register(
          "advanced_machine",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("advanced_machine")))
                  .strength(8.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block SILVER_BLOCK =
      BlockDefinitions.register(
          "silver_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("silver_block")))
                  .strength(4.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final LeavesBlock RUBBER_LEAVES =
      BlockDefinitions.register(
          "rubber_leaves",
          new ic2ce.neoforge.world.block.ResinLeaves(
              0.01F,
              BlockProfiles.leaves()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_leaves")))
                  .strength(0.2F)
                  .randomTicks()
                  .sound(SoundType.GRASS)
                  .noOcclusion()
                  .isValidSpawn(WorldBlocks::canSpawnOnLeaves)
                  .isSuffocating(WorldBlocks::never)
                  .isViewBlocking(WorldBlocks::never)));
  public static final ResinLog RUBBER_LOG =
      BlockDefinitions.register(
          "rubber_log",
          new ResinLog(
              Properties.of()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_log")))
                  .mapColor(
                      state ->
                          state.getValue(RotatedPillarBlock.AXIS) == Axis.Y
                              ? MapColor.PODZOL
                              : MapColor.COLOR_BROWN)
                  .randomTicks()
                  .strength(1.0F)
                  .sound(SoundType.WOOD)));
  public static final RotatedPillarBlock STRIPPED_RUBBER_LOG =
      BlockDefinitions.register(
          "stripped_rubber_log",
          new RotatedPillarBlock(
              Properties.of()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "stripped_rubber_log")))
                  .mapColor(
                      state ->
                          state.getValue(RotatedPillarBlock.AXIS) == Axis.Y
                              ? MapColor.PODZOL
                              : MapColor.COLOR_BROWN)
                  .strength(1.0F)
                  .sound(SoundType.WOOD)));
  public static final BarkWood RUBBER_WOOD =
      BlockDefinitions.register(
          "rubber_wood",
          new BarkWood(
              BlockProfiles.wood(MapColor.COLOR_BROWN)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_wood")))
                  .strength(1.0F)
                  .sound(SoundType.WOOD)));
  public static final Block STRIPPED_RUBBER_WOOD =
      BlockDefinitions.register(
          "stripped_rubber_wood",
          new Block(
              BlockProfiles.wood(MapColor.PODZOL)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "stripped_rubber_wood")))
                  .strength(1.0F)
                  .sound(SoundType.WOOD)));
  public static final Block RUBBER_SAPLING =
      BlockDefinitions.register(
          "rubber_sapling",
          new SaplingBlock(
              new TreeGrower(
                  "rubber",
                  Optional.empty(),
                  Optional.of(
                      ResourceKey.create(
                          Registries.CONFIGURED_FEATURE,
                          ProjectIds.id("rubber_tree"))),
                  Optional.empty()),
              BlockProfiles.plant()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_sapling")))
                  .noCollision()
                  .randomTicks()
                  .instabreak()
                  .sound(SoundType.GRASS)) {});
  public static final Block RUBBER_PLANKS =
      BlockDefinitions.register(
          "rubber_planks",
          new Block(
              BlockProfiles.wood(MapColor.PODZOL)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_planks")))
                  .strength(2.0F, 3.0F)
                  .sound(SoundType.WOOD)));
  public static final Block RUBBER_BUTTON =
      BlockDefinitions.register(
          "rubber_button",
          new ButtonBlock(
              BlockSetType.OAK,
              30,
              BlockProfiles.decoration()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_button")))
                  .noCollision()
                  .strength(0.5F)
                  .sound(SoundType.WOOD)) {});
  public static final Block RUBBER_DOOR =
      BlockDefinitions.register(
          "rubber_door",
          new DoorBlock(
              BlockSetType.OAK,
              BlockProfiles.wood(MapColor.WOOD)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_door")))
                  .strength(3.0F)
                  .sound(SoundType.WOOD)
                  .noOcclusion()) {});
  public static final Block RUBBER_FENCE =
      BlockDefinitions.register(
          "rubber_fence",
          new FenceBlock(
              BlockProfiles.wood(MapColor.WOOD)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_fence")))
                  .strength(2.0F, 3.0F)
                  .sound(SoundType.WOOD)));
  public static final Block RUBBER_FENCE_GATE =
      BlockDefinitions.register(
          "rubber_fence_gate",
          new FenceGateBlock(
              WoodType.OAK,
              BlockProfiles.wood(MapColor.WOOD)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_fence_gate")))
                  .strength(2.0F, 3.0F)
                  .sound(SoundType.WOOD)));
  public static final Block RUBBER_PRESSURE_PLATE =
      BlockDefinitions.register(
          "rubber_pressure_plate",
          new PressurePlateBlock(
              BlockSetType.OAK,
              BlockProfiles.wood(MapColor.WOOD)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "rubber_pressure_plate")))
                  .noCollision()
                  .strength(0.5F)
                  .sound(SoundType.WOOD)) {});
  public static final Block RUBBER_SIGN =
      BlockDefinitions.register(
          "rubber_sign",
          new RubberStandingSign(
              BlockProfiles.wood(MapColor.COLOR_BROWN)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_sign")))
                  .noCollision()
                  .strength(1.0F)
                  .sound(SoundType.WOOD),
              SignDefinitions.RUBBER));
  public static final Block RUBBER_SLAB =
      BlockDefinitions.register(
          "rubber_slab",
          new SlabBlock(
              BlockProfiles.wood(MapColor.WOOD)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_slab")))
                  .strength(2.0F, 3.0F)
                  .sound(SoundType.WOOD)));
  public static final Block RUBBER_STAIRS =
      BlockDefinitions.register(
          "rubber_stairs",
          new StairBlock(
              RUBBER_PLANKS.defaultBlockState(),
              BlockProfiles.wood(MapColor.PODZOL)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_stairs")))
                  .strength(2.0F, 3.0F)
                  .sound(SoundType.WOOD)) {});
  public static final Block RUBBER_TRAPDOOR =
      BlockDefinitions.register(
          "rubber_trapdoor",
          new TrapDoorBlock(
              net.minecraft.world.level.block.state.properties.BlockSetType.OAK,
              BlockProfiles.wood(MapColor.PODZOL)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_trapdoor")))
                  .strength(3.0F)
                  .sound(SoundType.WOOD)
                  .noOcclusion()
                  .isValidSpawn(WorldBlocks::never)) {});
  public static final Block RUBBER_WALL_SIGN =
      BlockDefinitions.register(
          "rubber_wall_sign",
          new RubberWallSign(
              BlockProfiles.wood(MapColor.COLOR_BROWN)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_wall_sign")))
                  .noCollision()
                  .strength(1.0F)
                  .sound(SoundType.WOOD)
                  .overrideLootTable(RUBBER_SIGN.getLootTable()),
              SignDefinitions.RUBBER));
  public static final Block WOODEN_SCAFFOLD =
      BlockDefinitions.register(
          "wooden_scaffold",
          new StructuralScaffold(
              BlockProfiles.wood()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("wooden_scaffold")))
                  .noOcclusion()
                  .strength(0.5F, 0.12F)
                  .randomTicks()
                  .sound(SoundType.WOOD),
              2));
  public static final Block REINFORCED_WOODEN_SCAFFOLD =
      BlockDefinitions.register(
          "reinforced_wooden_scaffold",
          new StructuralScaffold(
              BlockProfiles.wood()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "reinforced_wooden_scaffold")))
                  .noOcclusion()
                  .strength(0.6F, 0.24F)
                  .randomTicks()
                  .sound(SoundType.WOOD),
              5));
  public static final Block IRON_SCAFFOLD =
      BlockDefinitions.register(
          "iron_scaffold",
          new StructuralScaffold(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("iron_scaffold")))
                  .noOcclusion()
                  .strength(0.8F, 6.0F)
                  .randomTicks()
                  .sound(SoundType.METAL),
              5));
  public static final Block REINFORCED_IRON_SCAFFOLD =
      BlockDefinitions.register(
          "reinforced_iron_scaffold",
          new StructuralScaffold(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "reinforced_iron_scaffold")))
                  .noOcclusion()
                  .strength(1.0F, 8.0F)
                  .randomTicks()
                  .sound(SoundType.METAL),
              12));
  public static final Block RESIN_SHEET =
      BlockDefinitions.register(
          "resin_sheet",
          new ReactiveSheet(
              BlockProfiles.decoration()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("resin_sheet")))
                  .strength(1.6F, 0.5F),
              ReactiveSheet.Surface.RESIN));
  public static final Block RUBBER_SHEET =
      BlockDefinitions.register(
          "rubber_sheet",
          new ReactiveSheet(
              BlockProfiles.decoration()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rubber_sheet")))
                  .strength(0.8F, 2.0F),
              ReactiveSheet.Surface.RUBBER));
  public static final Block WOOL_SHEET =
      BlockDefinitions.register(
          "wool_sheet",
          new ReactiveSheet(
              BlockProfiles.decoration()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("wool_sheet")))
                  .strength(0.8F, 0.8F),
              ReactiveSheet.Surface.WOOL));
  public static final Block REINFORCED_GLASS =
      BlockDefinitions.register(
          "reinforced_glass",
          new StructuralGlass(
              BlockProfiles.glass()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("reinforced_glass")))
                  .noOcclusion()
                  .strength(5.0F, 180.0F)
                  .sound(SoundType.GLASS)
                  .isValidSpawn((state, world, pos, type) -> false)));
  public static final Block FOAM =
      BlockDefinitions.register(
          "foam",
          new CuringFoam(
              BlockProfiles.wool()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("foam")))
                  .noOcclusion()
                  .strength(0.01F, 10.0F)
                  .randomTicks()
                  .sound(SoundType.WOOL)));
  public static final Block WHITE_WALL =
      BlockDefinitions.register(
          "white_wall", new ConstructionWall(BlockDefinitions.withId(wallSettings, "white_wall"), DyeColor.WHITE));
  public static final Block ORANGE_WALL =
      BlockDefinitions.register(
          "orange_wall",
          new ConstructionWall(BlockDefinitions.withId(wallSettings, "orange_wall"), DyeColor.ORANGE));
  public static final Block MAGENTA_WALL =
      BlockDefinitions.register(
          "magenta_wall",
          new ConstructionWall(BlockDefinitions.withId(wallSettings, "magenta_wall"), DyeColor.MAGENTA));
  public static final Block LIGHT_BLUE_WALL =
      BlockDefinitions.register(
          "light_blue_wall",
          new ConstructionWall(BlockDefinitions.withId(wallSettings, "light_blue_wall"), DyeColor.LIGHT_BLUE));
  public static final Block YELLOW_WALL =
      BlockDefinitions.register(
          "yellow_wall",
          new ConstructionWall(BlockDefinitions.withId(wallSettings, "yellow_wall"), DyeColor.YELLOW));
  public static final Block LIME_WALL =
      BlockDefinitions.register("lime_wall", new ConstructionWall(BlockDefinitions.withId(wallSettings, "lime_wall"), DyeColor.LIME));
  public static final Block PINK_WALL =
      BlockDefinitions.register("pink_wall", new ConstructionWall(BlockDefinitions.withId(wallSettings, "pink_wall"), DyeColor.PINK));
  public static final Block GRAY_WALL =
      BlockDefinitions.register("gray_wall", new ConstructionWall(BlockDefinitions.withId(wallSettings, "gray_wall"), DyeColor.GRAY));
  public static final Block LIGHT_GRAY_WALL =
      BlockDefinitions.register(
          "light_gray_wall",
          new ConstructionWall(BlockDefinitions.withId(wallSettings, "light_gray_wall"), DyeColor.LIGHT_GRAY));
  public static final Block CYAN_WALL =
      BlockDefinitions.register("cyan_wall", new ConstructionWall(BlockDefinitions.withId(wallSettings, "cyan_wall"), DyeColor.CYAN));
  public static final Block PURPLE_WALL =
      BlockDefinitions.register(
          "purple_wall",
          new ConstructionWall(BlockDefinitions.withId(wallSettings, "purple_wall"), DyeColor.PURPLE));
  public static final Block BLUE_WALL =
      BlockDefinitions.register("blue_wall", new ConstructionWall(BlockDefinitions.withId(wallSettings, "blue_wall"), DyeColor.BLUE));
  public static final Block BROWN_WALL =
      BlockDefinitions.register(
          "brown_wall", new ConstructionWall(BlockDefinitions.withId(wallSettings, "brown_wall"), DyeColor.BROWN));
  public static final Block GREEN_WALL =
      BlockDefinitions.register(
          "green_wall", new ConstructionWall(BlockDefinitions.withId(wallSettings, "green_wall"), DyeColor.GREEN));
  public static final Block RED_WALL =
      BlockDefinitions.register("red_wall", new ConstructionWall(BlockDefinitions.withId(wallSettings, "red_wall"), DyeColor.RED));
  public static final Block BLACK_WALL =
      BlockDefinitions.register(
          "black_wall", new ConstructionWall(BlockDefinitions.withId(wallSettings, "black_wall"), DyeColor.BLACK));
  public static final Block OBSCURED_WALL =
      BlockDefinitions.register(
          "obscured_wall",
          MachineBlockType.create(
              BlockDefinitions.withId(wallSettings, "obscured_wall"),
              ImprintedWall.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));
  public static final Block REINFORCED_DOOR =
      BlockDefinitions.register(
          "reinforced_door",
          new DoorBlock(
              BlockSetType.IRON,
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("reinforced_door")))
                  .strength(50.0F, 150.0F)
                  .sound(SoundType.METAL)) {});

  private WorldBlocks() {}

  public static void init() {}
}
