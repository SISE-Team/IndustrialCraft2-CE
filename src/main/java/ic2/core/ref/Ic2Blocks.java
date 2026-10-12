package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.block.generator.tileentity.TileEntityCreativeGenerator;
import ic2.core.block.generator.tileentity.TileEntityGenerator;
import ic2.core.block.generator.tileentity.TileEntityGeoGenerator;
import ic2.core.block.generator.tileentity.TileEntityKineticGenerator;
import ic2.core.block.generator.tileentity.TileEntityRTGenerator;
import ic2.core.block.generator.tileentity.TileEntitySemifluidGenerator;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.block.generator.tileentity.TileEntityStirlingGenerator;
import ic2.core.block.generator.tileentity.TileEntityWaterGenerator;
import ic2.core.block.generator.tileentity.TileEntityWindGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityRTHeatGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntitySolidHeatGenerator;
import ic2.core.block.inherit.Ic2FenceBlock;
import ic2.core.block.inherit.Ic2GlassBlock;
import ic2.core.block.inherit.Ic2SheetBlock;
import ic2.core.block.inherit.Ic2SignBlock;
import ic2.core.block.inherit.Ic2WallSignBlock;
import ic2.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityManualKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.block.machine.MiningPipeBlock;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.block.machine.tileentity.TileEntityAssemblyBench;
import ic2.core.block.machine.tileentity.TileEntityBatchCrafter;
import ic2.core.block.machine.tileentity.TileEntityBlastFurnace;
import ic2.core.block.machine.tileentity.TileEntityBlockCutter;
import ic2.core.block.machine.tileentity.TileEntityBridgeNuke;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.block.machine.tileentity.TileEntityCentrifuge;
import ic2.core.block.machine.tileentity.TileEntityChunkloader;
import ic2.core.block.machine.tileentity.TileEntityClassicCanner;
import ic2.core.block.machine.tileentity.TileEntityClassicCropmatron;
import ic2.core.block.machine.tileentity.TileEntityClassicElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityClassicMassFabricator;
import ic2.core.block.machine.tileentity.TileEntityCompressor;
import ic2.core.block.machine.tileentity.TileEntityCondenser;
import ic2.core.block.machine.tileentity.TileEntityCropHarvester;
import ic2.core.block.machine.tileentity.TileEntityCropmatron;
import ic2.core.block.machine.tileentity.TileEntityElectricFurnace;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityExtractor;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.block.machine.tileentity.TileEntityFluidBottler;
import ic2.core.block.machine.tileentity.TileEntityFluidDistributor;
import ic2.core.block.machine.tileentity.TileEntityFluidRegulator;
import ic2.core.block.machine.tileentity.TileEntityITnt;
import ic2.core.block.machine.tileentity.TileEntityInduction;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import ic2.core.block.machine.tileentity.TileEntityIronFurnace;
import ic2.core.block.machine.tileentity.TileEntityItemBuffer;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.block.machine.tileentity.TileEntityMacerator;
import ic2.core.block.machine.tileentity.TileEntityMagnetizer;
import ic2.core.block.machine.tileentity.TileEntityMassFabricator;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.block.machine.tileentity.TileEntityMetalFormer;
import ic2.core.block.machine.tileentity.TileEntityMiner;
import ic2.core.block.machine.tileentity.TileEntityNuke;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import ic2.core.block.machine.tileentity.TileEntityPatternStorage;
import ic2.core.block.machine.tileentity.TileEntityPump;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.block.machine.tileentity.TileEntityReplicator;
import ic2.core.block.machine.tileentity.TileEntityScanner;
import ic2.core.block.machine.tileentity.TileEntitySolarDestiller;
import ic2.core.block.machine.tileentity.TileEntitySolidCanner;
import ic2.core.block.machine.tileentity.TileEntitySortingMachine;
import ic2.core.block.machine.tileentity.TileEntitySteamGenerator;
import ic2.core.block.machine.tileentity.TileEntitySteamRepressurizer;
import ic2.core.block.machine.tileentity.TileEntityTank;
import ic2.core.block.machine.tileentity.TileEntityTeleporter;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.block.machine.tileentity.TileEntityTesla;
import ic2.core.block.machine.tileentity.TileEntityWeightedFluidDistributor;
import ic2.core.block.machine.tileentity.TileEntityWeightedItemDistributor;
import ic2.core.block.misc.FoamBlock;
import ic2.core.block.misc.RubberLogBlock;
import ic2.core.block.misc.RubberWoodBlock;
import ic2.core.block.misc.ScaffoldBlock;
import ic2.core.block.misc.WallBlock;
import ic2.core.block.personal.TileEntityEnergyOMat;
import ic2.core.block.personal.TileEntityPersonalChest;
import ic2.core.block.personal.TileEntityTradeOMat;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.reactor.tileentity.TileEntityRCI_LZH;
import ic2.core.block.reactor.tileentity.TileEntityRCI_RSH;
import ic2.core.block.reactor.tileentity.TileEntityReactorAccessHatch;
import ic2.core.block.reactor.tileentity.TileEntityReactorChamberElectric;
import ic2.core.block.reactor.tileentity.TileEntityReactorFluidPort;
import ic2.core.block.reactor.tileentity.TileEntityReactorRedstonePort;
import ic2.core.block.storage.box.TileEntityBronzeStorageBox;
import ic2.core.block.storage.box.TileEntityIridiumStorageBox;
import ic2.core.block.storage.box.TileEntityIronStorageBox;
import ic2.core.block.storage.box.TileEntitySteelStorageBox;
import ic2.core.block.storage.box.TileEntityWoodenStorageBox;
import ic2.core.block.storage.tank.TileEntityBronzeTank;
import ic2.core.block.storage.tank.TileEntityIridiumTank;
import ic2.core.block.storage.tank.TileEntityIronTank;
import ic2.core.block.storage.tank.TileEntitySteelTank;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.tileentity.TileEntityWall;
import ic2.core.block.wiring.CableBlock;
import ic2.core.block.wiring.CableType;
import ic2.core.block.wiring.DetectorCableBlock;
import ic2.core.block.wiring.DetectorFoamCableBlock;
import ic2.core.block.wiring.FoamCableBlock;
import ic2.core.block.wiring.SplitterCableBlock;
import ic2.core.block.wiring.SplitterFoamCableBlock;
import ic2.core.block.wiring.tileentity.TileEntityChargepadBatBox;
import ic2.core.block.wiring.tileentity.TileEntityChargepadCESU;
import ic2.core.block.wiring.tileentity.TileEntityChargepadMFE;
import ic2.core.block.wiring.tileentity.TileEntityChargepadMFSU;
import ic2.core.block.wiring.tileentity.TileEntityElectricBatBox;
import ic2.core.block.wiring.tileentity.TileEntityElectricCESU;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFE;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFSU;
import ic2.core.block.wiring.tileentity.TileEntityTransformerEV;
import ic2.core.block.wiring.tileentity.TileEntityTransformerHV;
import ic2.core.block.wiring.tileentity.TileEntityTransformerLV;
import ic2.core.block.wiring.tileentity.TileEntityTransformerMV;
import ic2.core.crop.Ic2CropType;
import ic2.core.crop.TileEntityCrop;
import ic2.core.util.Util;
import ic2.core.world.Ic2WorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.grower.TreeGrower;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.MapColor;

public final class Ic2Blocks {
   public static final Block BASALT = register("basalt", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("basalt"))).strength(20.0F, 45.0F).requiresCorrectToolForDrops()));
   public static final Block LEAD_ORE = register("lead_ore", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("lead_ore"))).strength(2.0F, 4.0F).requiresCorrectToolForDrops()));
   public static final Block TIN_ORE = register("tin_ore", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("tin_ore"))).strength(3.0F, 5.0F).requiresCorrectToolForDrops()));
   public static final Block URANIUM_ORE = register("uranium_ore", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("uranium_ore"))).strength(4.0F, 6.0F).requiresCorrectToolForDrops()));
   public static final Block DEEPSLATE_LEAD_ORE = register(
      "deepslate_lead_ore",
      new Block(IC2Material.stone(MapColor.DEEPSLATE).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("deepslate_lead_ore"))).strength(3.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE))
   );
   public static final Block DEEPSLATE_TIN_ORE = register(
      "deepslate_tin_ore",
      new Block(IC2Material.stone(MapColor.DEEPSLATE).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("deepslate_tin_ore"))).strength(4.0F, 7.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE))
   );
   public static final Block DEEPSLATE_URANIUM_ORE = register(
      "deepslate_uranium_ore",
      new Block(IC2Material.stone(MapColor.DEEPSLATE).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("deepslate_uranium_ore"))).strength(5.0F, 8.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE))
   );
   public static final Block RAW_LEAD_BLOCK = register(
      "raw_lead_block", new Block(IC2Material.stone(MapColor.COLOR_LIGHT_GRAY).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("raw_lead_block"))).strength(3.0F, 11.0F).requiresCorrectToolForDrops())
   );
   public static final Block RAW_TIN_BLOCK = register(
      "raw_tin_block", new Block(IC2Material.stone(MapColor.SNOW).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("raw_tin_block"))).strength(4.0F, 12.0F).requiresCorrectToolForDrops())
   );
   public static final Block RAW_URANIUM_BLOCK = register(
      "raw_uranium_block", new Block(IC2Material.stone(MapColor.COLOR_GREEN).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("raw_uranium_block"))).strength(5.0F, 13.0F).requiresCorrectToolForDrops())
   );
   public static final Block BRONZE_BLOCK = register(
      "bronze_block", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("bronze_block"))).strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL))
   );
   public static final Block LEAD_BLOCK = register(
      "lead_block", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("lead_block"))).strength(4.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL))
   );
   public static final Block STEEL_BLOCK = register(
      "steel_block", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("steel_block"))).strength(8.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL))
   );
   public static final Block TIN_BLOCK = register(
      "tin_block", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("tin_block"))).strength(4.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL))
   );
   public static final Block URANIUM_BLOCK = register(
      "uranium_block", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("uranium_block"))).strength(6.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL))
   );
   public static final Block REINFORCED_STONE = register(
      "reinforced_stone", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("reinforced_stone"))).strength(80.0F, 180.0F).requiresCorrectToolForDrops())
   );
   public static final Block MACHINE = register(
      "machine", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("machine"))).strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL))
   );
   public static final Block ADVANCED_MACHINE = register(
      "advanced_machine", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("advanced_machine"))).strength(8.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL))
   );
   public static final Block REACTOR_VESSEL = register("reactor_vessel", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("reactor_vessel"))).strength(40.0F, 90.0F).requiresCorrectToolForDrops()));
   public static final Block SILVER_BLOCK = register(
      "silver_block", new Block(IC2Material.stone().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("silver_block"))).strength(4.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL))
   );
   public static final LeavesBlock RUBBER_LEAVES = register(
      "rubber_leaves",
      new LeavesBlock(
         0.01F,
         IC2Material.leaves().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_leaves")))
            .strength(0.2F)
            .randomTicks()
            .sound(SoundType.GRASS)
            .noOcclusion()
            .isValidSpawn(Ic2Blocks::canSpawnOnLeaves)
            .isSuffocating(Ic2Blocks::never)
            .isViewBlocking(Ic2Blocks::never)
      ) {
                  @Override
                  public com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.LeavesBlock> codec() {
                     return com.mojang.serialization.MapCodec.unit(this);
                  }

                  @Override
                  protected void spawnFallingLeavesParticle(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
                  }
      }
   );
   public static final RubberLogBlock RUBBER_LOG = register(
      "rubber_log",
      new RubberLogBlock(
         Properties.of().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_log")))
               .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? MapColor.PODZOL : MapColor.COLOR_BROWN)
            .randomTicks()
            .strength(1.0F)
            .sound(SoundType.WOOD)
      )
   );
   public static final RotatedPillarBlock STRIPPED_RUBBER_LOG = register(
      "stripped_rubber_log",
      new RotatedPillarBlock(
         Properties.of().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("stripped_rubber_log")))
               .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? MapColor.PODZOL : MapColor.COLOR_BROWN)
            .strength(1.0F)
            .sound(SoundType.WOOD)
      )
   );
   public static final RubberWoodBlock RUBBER_WOOD = register(
      "rubber_wood", new RubberWoodBlock(IC2Material.wood(MapColor.COLOR_BROWN).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_wood"))).strength(1.0F).sound(SoundType.WOOD))
   );
   public static final Block STRIPPED_RUBBER_WOOD = register(
      "stripped_rubber_wood", new Block(IC2Material.wood(MapColor.PODZOL).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("stripped_rubber_wood"))).strength(1.0F).sound(SoundType.WOOD))
   );
   public static final Block RUBBER_SAPLING = register(
      "rubber_sapling",
      new SaplingBlock(
         new TreeGrower(
            "rubber",
            Optional.empty(),
            Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, IC2.getIdentifier("rubber_tree"))),
            Optional.empty()
         ),
         IC2Material.plant().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_sapling"))).noCollision().randomTicks().instabreak().sound(SoundType.GRASS)
      ) {}
   );
   public static final Block RUBBER_PLANKS = register(
      "rubber_planks", new Block(IC2Material.wood(MapColor.PODZOL).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_planks"))).strength(2.0F, 3.0F).sound(SoundType.WOOD))
   );
   public static final Block RUBBER_BUTTON = register(
      "rubber_button", new ButtonBlock(BlockSetType.OAK, 30, IC2Material.decoration().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_button"))).noCollision().strength(0.5F).sound(SoundType.WOOD)) {}
   );
   public static final Block RUBBER_DOOR = register(
      "rubber_door", new DoorBlock(BlockSetType.OAK, IC2Material.wood(MapColor.WOOD).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_door"))).strength(3.0F).sound(SoundType.WOOD).noOcclusion()) {}
   );
   public static final Block RUBBER_FENCE = register(
      "rubber_fence", new FenceBlock(IC2Material.wood(MapColor.WOOD).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_fence"))).strength(2.0F, 3.0F).sound(SoundType.WOOD))
   );
   public static final Block RUBBER_FENCE_GATE = register(
      "rubber_fence_gate",
      new FenceGateBlock(WoodType.OAK, IC2Material.wood(MapColor.WOOD).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_fence_gate"))).strength(2.0F, 3.0F).sound(SoundType.WOOD))
   );
   public static final Block RUBBER_PRESSURE_PLATE = register(
      "rubber_pressure_plate",
      new PressurePlateBlock(
         BlockSetType.OAK, IC2Material.wood(MapColor.WOOD).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_pressure_plate"))).noCollision().strength(0.5F).sound(SoundType.WOOD)
      ) {}
   );
   public static final Block RUBBER_SIGN = register(
      "rubber_sign",
      new Ic2SignBlock(IC2Material.wood(MapColor.COLOR_BROWN).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_sign"))).noCollision().strength(1.0F).sound(SoundType.WOOD), Ic2SignType.RUBBER)
   );
   public static final Block RUBBER_SLAB = register(
      "rubber_slab", new SlabBlock(IC2Material.wood(MapColor.WOOD).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_slab"))).strength(2.0F, 3.0F).sound(SoundType.WOOD))
   );
   public static final Block RUBBER_STAIRS = register(
      "rubber_stairs",
      new StairBlock(RUBBER_PLANKS.defaultBlockState(), IC2Material.wood(MapColor.PODZOL).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_stairs"))).strength(2.0F, 3.0F).sound(SoundType.WOOD)) {}
   );
   public static final Block RUBBER_TRAPDOOR = register(
      "rubber_trapdoor",
      new TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.OAK, 
         IC2Material.wood(MapColor.PODZOL).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_trapdoor"))).strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn(Ic2Blocks::never)
      ) {}
   );
   public static final Block RUBBER_WALL_SIGN = register(
      "rubber_wall_sign",
      new Ic2WallSignBlock(
         IC2Material.wood(MapColor.COLOR_BROWN).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_wall_sign"))).noCollision().strength(1.0F).sound(SoundType.WOOD).overrideLootTable(RUBBER_SIGN.getLootTable()),
         Ic2SignType.RUBBER
      )
   );
   public static final Block WOODEN_SCAFFOLD = register(
      "wooden_scaffold", new ScaffoldBlock(IC2Material.wood().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("wooden_scaffold"))).noOcclusion().strength(0.5F, 0.12F).randomTicks().sound(SoundType.WOOD), 2)
   );
   public static final Block REINFORCED_WOODEN_SCAFFOLD = register(
      "reinforced_wooden_scaffold",
      new ScaffoldBlock(IC2Material.wood().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("reinforced_wooden_scaffold"))).noOcclusion().strength(0.6F, 0.24F).randomTicks().sound(SoundType.WOOD), 5)
   );
   public static final Block IRON_SCAFFOLD = register(
      "iron_scaffold", new ScaffoldBlock(IC2Material.metal().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("iron_scaffold"))).noOcclusion().strength(0.8F, 6.0F).randomTicks().sound(SoundType.METAL), 5)
   );
   public static final Block REINFORCED_IRON_SCAFFOLD = register(
      "reinforced_iron_scaffold",
      new ScaffoldBlock(IC2Material.metal().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("reinforced_iron_scaffold"))).noOcclusion().strength(1.0F, 8.0F).randomTicks().sound(SoundType.METAL), 12)
   );
   public static final Block IRON_FENCE = register("iron_fence", new Ic2FenceBlock(IC2Material.metal().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("iron_fence"))).strength(5.0F, 10.0F), true));
   public static final Block RESIN_SHEET = register("resin_sheet", new Ic2SheetBlock(IC2Material.decoration().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("resin_sheet"))).strength(1.6F, 0.5F)));
   public static final Block RUBBER_SHEET = register("rubber_sheet", new Ic2SheetBlock(IC2Material.decoration().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rubber_sheet"))).strength(0.8F, 2.0F)));
   public static final Block WOOL_SHEET = register("wool_sheet", new Ic2SheetBlock(IC2Material.decoration().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("wool_sheet"))).strength(0.8F, 0.8F)));
   public static final Block REINFORCED_GLASS = register(
      "reinforced_glass",
      new Ic2GlassBlock(
         IC2Material.glass().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("reinforced_glass"))).noOcclusion().strength(5.0F, 180.0F).sound(SoundType.GLASS).isValidSpawn((state, world, pos, type) -> false)
      )
   );
   public static final Block FOAM = register(
      "foam", new FoamBlock(IC2Material.wool().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("foam"))).noOcclusion().strength(0.01F, 10.0F).randomTicks().sound(SoundType.WOOL))
   );
   private static final Properties wallSettings = IC2Material.stone().strength(3.0F, 30.0F).requiresCorrectToolForDrops().sound(SoundType.STONE);
   public static final Block WHITE_WALL = register("white_wall", new WallBlock(withId(wallSettings, "white_wall"), DyeColor.WHITE));
   public static final Block ORANGE_WALL = register("orange_wall", new WallBlock(withId(wallSettings, "orange_wall"), DyeColor.ORANGE));
   public static final Block MAGENTA_WALL = register("magenta_wall", new WallBlock(withId(wallSettings, "magenta_wall"), DyeColor.MAGENTA));
   public static final Block LIGHT_BLUE_WALL = register("light_blue_wall", new WallBlock(withId(wallSettings, "light_blue_wall"), DyeColor.LIGHT_BLUE));
   public static final Block YELLOW_WALL = register("yellow_wall", new WallBlock(withId(wallSettings, "yellow_wall"), DyeColor.YELLOW));
   public static final Block LIME_WALL = register("lime_wall", new WallBlock(withId(wallSettings, "lime_wall"), DyeColor.LIME));
   public static final Block PINK_WALL = register("pink_wall", new WallBlock(withId(wallSettings, "pink_wall"), DyeColor.PINK));
   public static final Block GRAY_WALL = register("gray_wall", new WallBlock(withId(wallSettings, "gray_wall"), DyeColor.GRAY));
   public static final Block LIGHT_GRAY_WALL = register("light_gray_wall", new WallBlock(withId(wallSettings, "light_gray_wall"), DyeColor.LIGHT_GRAY));
   public static final Block CYAN_WALL = register("cyan_wall", new WallBlock(withId(wallSettings, "cyan_wall"), DyeColor.CYAN));
   public static final Block PURPLE_WALL = register("purple_wall", new WallBlock(withId(wallSettings, "purple_wall"), DyeColor.PURPLE));
   public static final Block BLUE_WALL = register("blue_wall", new WallBlock(withId(wallSettings, "blue_wall"), DyeColor.BLUE));
   public static final Block BROWN_WALL = register("brown_wall", new WallBlock(withId(wallSettings, "brown_wall"), DyeColor.BROWN));
   public static final Block GREEN_WALL = register("green_wall", new WallBlock(withId(wallSettings, "green_wall"), DyeColor.GREEN));
   public static final Block RED_WALL = register("red_wall", new WallBlock(withId(wallSettings, "red_wall"), DyeColor.RED));
   public static final Block BLACK_WALL = register("black_wall", new WallBlock(withId(wallSettings, "black_wall"), DyeColor.BLACK));
   public static final Block OBSCURED_WALL = register(
      "obscured_wall", Ic2TileEntityBlock.create(withId(wallSettings, "obscured_wall"), TileEntityWall.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false)
   );
   public static final Block MINING_PIPE = register(
      "mining_pipe", new MiningPipeBlock(IC2Material.metal().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("mining_pipe"))).strength(6.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL))
   );
   public static final Block MINING_PIPE_TIP = register(
      "mining_pipe_tip", new Block(IC2Material.metal().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("mining_pipe_tip"))).strength(6.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL))
   );
   public static final Block REINFORCED_DOOR = register(
      "reinforced_door", new DoorBlock(BlockSetType.IRON, IC2Material.metal().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("reinforced_door"))).strength(50.0F, 150.0F).sound(SoundType.METAL)) {}
   );
   public static final Block ITNT = register(
      "itnt",
      Ic2TileEntityBlock.create(
         IC2Material.explosive().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("itnt"))).strength(0.0F, 0.0F).sound(SoundType.GRASS),
         TileEntityITnt.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );
   public static final Block NUKE = register(
      "nuke",
      Ic2TileEntityBlock.create(
         IC2Material.explosive().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("nuke"))).strength(0.0F, 0.0F).sound(SoundType.GRASS),
         TileEntityNuke.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );
   public static final Block CLASSIC_NUKE = register(
      "classic_nuke",
      Ic2TileEntityBlock.create(
         IC2Material.explosive().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("classic_nuke"))).strength(0.0F, 0.0F).sound(SoundType.GRASS),
         TileEntityBridgeNuke.TileEntityClassicNuke.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );
   public static final Block GENERATOR = register(
      "generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block GEO_GENERATOR = register(
      "geo_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("geo_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityGeoGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Generator,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block KINETIC_GENERATOR = register(
      "kinetic_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("kinetic_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityKineticGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Generator,
         Util.allFacings,
         true
      )
   );
   public static final Block RT_GENERATOR = register(
      "rt_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rt_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityRTGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Generator,
         Util.noFacings,
         false
      )
   );
   public static final Block SEMIFLUID_GENERATOR = register(
      "semifluid_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("semifluid_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntitySemifluidGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Generator,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block SOLAR_GENERATOR = register(
      "solar_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("solar_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntitySolarGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Generator,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block STIRLING_GENERATOR = register(
      "stirling_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("stirling_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityStirlingGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Generator,
         Util.allFacings,
         true
      )
   );
   public static final Block WATER_GENERATOR = register(
      "water_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("water_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityWaterGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Generator,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block WIND_GENERATOR = register(
      "wind_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("wind_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityWindGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Generator,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block ELECTRIC_HEAT_GENERATOR = register(
      "electric_heat_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("electric_heat_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityElectricHeatGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         true
      )
   );
   public static final Block FLUID_HEAT_GENERATOR = register(
      "fluid_heat_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("fluid_heat_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityFluidHeatGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         true
      )
   );
   public static final Block RT_HEAT_GENERATOR = register(
      "rt_heat_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rt_heat_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityRTHeatGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         true
      )
   );
   public static final Block SOLID_HEAT_GENERATOR = register(
      "solid_heat_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("solid_heat_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntitySolidHeatGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         true
      )
   );
   public static final Block ELECTRIC_KINETIC_GENERATOR = register(
      "electric_kinetic_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("electric_kinetic_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityElectricKineticGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         true
      )
   );
   public static final Block MANUAL_KINETIC_GENERATOR = register(
      "manual_kinetic_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("manual_kinetic_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityManualKineticGenerator.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         true
      )
   );
   public static final Block STEAM_KINETIC_GENERATOR = register(
      "steam_kinetic_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("steam_kinetic_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntitySteamKineticGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         true
      )
   );
   public static final Block STIRLING_KINETIC_GENERATOR = register(
      "stirling_kinetic_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("stirling_kinetic_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityStirlingKineticGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         true
      )
   );
   public static final Block WATER_KINETIC_GENERATOR = register(
      "water_kinetic_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("water_kinetic_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityWaterKineticGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block WIND_KINETIC_GENERATOR = register(
      "wind_kinetic_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("wind_kinetic_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityWindKineticGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block NUCLEAR_REACTOR = register(
      "nuclear_reactor",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("nuclear_reactor"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityNuclearReactorElectric.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Generator,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block REACTOR_ACCESS_HATCH = register(
      "reactor_access_hatch",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("reactor_access_hatch"))).strength(40.0F, 90.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityReactorAccessHatch.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.onlyNorth,
         false
      )
   );
   public static final Block REACTOR_CHAMBER = register(
      "reactor_chamber",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("reactor_chamber"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityReactorChamberElectric.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.onlyNorth,
         true
      )
   );
   public static final Block REACTOR_FLUID_PORT = register(
      "reactor_fluid_port",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("reactor_fluid_port"))).strength(40.0F, 90.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityReactorFluidPort.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.onlyNorth,
         false
      )
   );
   public static final Block REACTOR_REDSTONE_PORT = register(
      "reactor_redstone_port",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("reactor_redstone_port"))).strength(40.0F, 90.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityReactorRedstonePort.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.onlyNorth,
         false
      )
   );
   public static final Block CONDENSER = register(
      "condenser",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("condenser"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityCondenser.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block FLUID_BOTTLER = register(
      "fluid_bottler",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("fluid_bottler"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityFluidBottler.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block FLUID_DISTRIBUTOR = register(
      "fluid_distributor",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("fluid_distributor"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityFluidDistributor.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block FLUID_REGULATOR = register(
      "fluid_regulator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("fluid_regulator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityFluidRegulator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block LIQUID_HEAT_EXCHANGER = register(
      "liquid_heat_exchanger",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("liquid_heat_exchanger"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityLiquidHeatExchanger.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block PUMP = register(
      "pump",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("pump"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityPump.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block SOLAR_DISTILLER = register(
      "solar_distiller",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("solar_distiller"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntitySolarDestiller.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.noFacings,
         false
      )
   );
   public static final Block STEAM_GENERATOR = register(
      "steam_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("steam_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntitySteamGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block ITEM_BUFFER = register(
      "item_buffer",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("item_buffer"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityItemBuffer.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.noFacings,
         false
      )
   );
   public static final Block MAGNETIZER = register(
      "magnetizer",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("magnetizer"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityMagnetizer.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block SORTING_MACHINE = register(
      "sorting_machine",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("sorting_machine"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntitySortingMachine.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.onlyNorth,
         false
      )
   );
   public static final Block TELEPORTER = register(
      "teleporter",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("teleporter"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityTeleporter.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.noFacings,
         false
      )
   );
   public static final Block TERRAFORMER = register(
      "terraformer",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("terraformer"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityTerra.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.noFacings,
         false
      )
   );
   public static final Block TESLA_COIL = register(
      "tesla_coil",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("tesla_coil"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityTesla.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block CANNER = register(
      "canner",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("canner"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityCanner.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block CLASSIC_CANNER = register(
      "classic_canner",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("classic_canner"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityClassicCanner.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Ic2TileEntityBlock COMPRESSOR = register(
      "compressor",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("compressor"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityCompressor.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block ELECTRIC_FURNACE = register(
      "electric_furnace",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("electric_furnace"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityElectricFurnace.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         true
      )
   );
   public static final Ic2TileEntityBlock EXTRACTOR = register(
      "extractor",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("extractor"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityExtractor.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block IRON_FURNACE = register(
      "iron_furnace",
      Ic2TileEntityBlock.create(
         IC2Material.metal().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("iron_furnace"))).strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityIronFurnace.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         false
      )
   );
   public static final Ic2TileEntityBlock MACERATOR = register(
      "macerator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("macerator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityMacerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block RECYCLER = register(
      "recycler",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("recycler"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityRecycler.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block SOLID_CANNER = register(
      "solid_canner",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("solid_canner"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntitySolidCanner.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Ic2TileEntityBlock BLAST_FURNACE = register(
      "blast_furnace",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("blast_furnace"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityBlastFurnace.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Ic2TileEntityBlock BLOCK_CUTTER = register(
      "block_cutter",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("block_cutter"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityBlockCutter.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Ic2TileEntityBlock CENTRIFUGE = register(
      "centrifuge",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("centrifuge"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityCentrifuge.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block FERMENTER = register(
      "fermenter",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("fermenter"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityFermenter.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block INDUCTION_FURNACE = register(
      "induction_furnace",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("induction_furnace"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityInduction.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Ic2TileEntityBlock METAL_FORMER = register(
      "metal_former",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("metal_former"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityMetalFormer.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Ic2TileEntityBlock ORE_WASHING_PLANT = register(
      "ore_washing_plant",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("ore_washing_plant"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityOreWashing.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block ADVANCED_MINER = register(
      "advanced_miner",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("advanced_miner"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityAdvMiner.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block CROP_HARVESTER = register(
      "crop_harvester",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("crop_harvester"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityCropHarvester.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block CROPMATRON = register(
      "cropmatron",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("cropmatron"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityCropmatron.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block CLASSIC_CROPMATRON = register(
      "classic_cropmatron",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("classic_cropmatron"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityClassicCropmatron.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block MINER = register(
      "miner",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("miner"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityMiner.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block MASS_FABRICATOR = register(
      "mass_fabricator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("mass_fabricator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityMassFabricator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block CLASSIC_MASS_FABRICATOR = register(
      "classic_mass_fabricator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("classic_mass_fabricator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityClassicMassFabricator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block UU_ASSEMBLY_BENCH = register(
      "uu_assembly_bench",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("uu_assembly_bench"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityAssemblyBench.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block MATTER_GENERATOR = register(
      "matter_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("matter_generator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityMatter.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block PATTERN_STORAGE = register(
      "pattern_storage",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("pattern_storage"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityPatternStorage.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block REPLICATOR = register(
      "replicator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("replicator"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityReplicator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block UU_SCANNER = register(
      "uu_scanner",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("uu_scanner"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityScanner.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block ENERGY_O_MAT = register(
      "energy_o_mat",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("energy_o_mat"))).strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityEnergyOMat.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         false
      )
   );
   public static final Block PERSONAL_CHEST = register(
      "personal_chest",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("personal_chest"))).strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityPersonalChest.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         false
      )
   );
   public static final Block TRADE_O_MAT = register(
      "trade_o_mat",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("trade_o_mat"))).strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityTradeOMat.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         false
      )
   );
   private static final Properties cableSettings = IC2Material.metal().strength(0.5F, 5.0F).sound(SoundType.METAL);
   private static final Properties insulatedCableSettings = IC2Material.wool().strength(0.5F, 5.0F).sound(SoundType.WOOL);
   public static final FoamCableBlock COPPER_FOAM_CABLE = register("copper_foam_cable", FoamCableBlock.create(withId(cableSettings, "copper_foam_cable"), CableType.copper, 0));
   public static final Block COPPER_CABLE = register("copper_cable", CableBlock.create(withId(cableSettings, "copper_cable"), CableType.copper, 0, COPPER_FOAM_CABLE));
   public static final FoamCableBlock INSULATED_COPPER_FOAM_CABLE = register(
      "insulated_copper_foam_cable", FoamCableBlock.create(withId(insulatedCableSettings, "insulated_copper_foam_cable"), CableType.copper, 1)
   );
   public static final Block INSULATED_COPPER_CABLE = register(
      "insulated_copper_cable", CableBlock.create(withId(insulatedCableSettings, "insulated_copper_cable"), CableType.copper, 1, INSULATED_COPPER_FOAM_CABLE)
   );
   public static final FoamCableBlock GLASS_FIBRE_FOAM_CABLE = register(
      "glass_fibre_foam_cable",
      FoamCableBlock.create(IC2Material.glass().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("glass_fibre_foam_cable"))).strength(0.5F, 5.0F).sound(SoundType.GLASS), CableType.glass, 0)
   );
   public static final Block GLASS_FIBRE_CABLE = register(
      "glass_fibre_cable",
      CableBlock.create(IC2Material.glass().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("glass_fibre_cable"))).strength(0.5F, 5.0F).sound(SoundType.GLASS), CableType.glass, 0, GLASS_FIBRE_FOAM_CABLE)
   );
   public static final FoamCableBlock GOLD_FOAM_CABLE = register("gold_foam_cable", FoamCableBlock.create(withId(cableSettings, "gold_foam_cable"), CableType.gold, 0));
   public static final Block GOLD_CABLE = register("gold_cable", CableBlock.create(withId(cableSettings, "gold_cable"), CableType.gold, 0, GOLD_FOAM_CABLE));
   public static final FoamCableBlock INSULATED_GOLD_FOAM_CABLE = register(
      "insulated_gold_foam_cable", FoamCableBlock.create(withId(insulatedCableSettings, "insulated_gold_foam_cable"), CableType.gold, 1)
   );
   public static final Block INSULATED_GOLD_CABLE = register(
      "insulated_gold_cable", CableBlock.create(withId(insulatedCableSettings, "insulated_gold_cable"), CableType.gold, 1, INSULATED_GOLD_FOAM_CABLE)
   );
   public static final FoamCableBlock DOUBLE_INSULATED_GOLD_FOAM_CABLE = register(
      "double_insulated_gold_foam_cable", FoamCableBlock.create(withId(insulatedCableSettings, "double_insulated_gold_foam_cable"), CableType.gold, 2)
   );
   public static final Block DOUBLE_INSULATED_GOLD_CABLE = register(
      "double_insulated_gold_cable", CableBlock.create(withId(insulatedCableSettings, "double_insulated_gold_cable"), CableType.gold, 2, DOUBLE_INSULATED_GOLD_FOAM_CABLE)
   );
   public static final FoamCableBlock IRON_FOAM_CABLE = register("iron_foam_cable", FoamCableBlock.create(withId(cableSettings, "iron_foam_cable"), CableType.iron, 0));
   public static final Block IRON_CABLE = register("iron_cable", CableBlock.create(withId(cableSettings, "iron_cable"), CableType.iron, 0, IRON_FOAM_CABLE));
   public static final FoamCableBlock INSULATED_IRON_FOAM_CABLE = register(
      "insulated_iron_foam_cable", FoamCableBlock.create(withId(insulatedCableSettings, "insulated_iron_foam_cable"), CableType.iron, 1)
   );
   public static final Block INSULATED_IRON_CABLE = register(
      "insulated_iron_cable", CableBlock.create(withId(insulatedCableSettings, "insulated_iron_cable"), CableType.iron, 1, INSULATED_IRON_FOAM_CABLE)
   );
   public static final FoamCableBlock DOUBLE_INSULATED_IRON_FOAM_CABLE = register(
      "double_insulated_iron_foam_cable", FoamCableBlock.create(withId(insulatedCableSettings, "double_insulated_iron_foam_cable"), CableType.iron, 2)
   );
   public static final Block DOUBLE_INSULATED_IRON_CABLE = register(
      "double_insulated_iron_cable", CableBlock.create(withId(insulatedCableSettings, "double_insulated_iron_cable"), CableType.iron, 2, DOUBLE_INSULATED_IRON_FOAM_CABLE)
   );
   public static final FoamCableBlock TRIPLE_INSULATED_IRON_FOAM_CABLE = register(
      "triple_insulated_iron_foam_cable", FoamCableBlock.create(withId(insulatedCableSettings, "triple_insulated_iron_foam_cable"), CableType.iron, 3)
   );
   public static final Block TRIPLE_INSULATED_IRON_CABLE = register(
      "triple_insulated_iron_cable", CableBlock.create(withId(insulatedCableSettings, "triple_insulated_iron_cable"), CableType.iron, 3, TRIPLE_INSULATED_IRON_FOAM_CABLE)
   );
   public static final FoamCableBlock TIN_FOAM_CABLE = register("tin_foam_cable", FoamCableBlock.create(withId(cableSettings, "tin_foam_cable"), CableType.tin, 0));
   public static final Block TIN_CABLE = register("tin_cable", CableBlock.create(withId(cableSettings, "tin_cable"), CableType.tin, 0, TIN_FOAM_CABLE));
   public static final FoamCableBlock INSULATED_TIN_FOAM_CABLE = register(
      "insulated_tin_foam_cable", FoamCableBlock.create(withId(insulatedCableSettings, "insulated_tin_foam_cable"), CableType.tin, 1)
   );
   public static final Block INSULATED_TIN_CABLE = register(
      "insulated_tin_cable", CableBlock.create(withId(insulatedCableSettings, "insulated_tin_cable"), CableType.tin, 1, INSULATED_TIN_FOAM_CABLE)
   );
   public static final DetectorFoamCableBlock DETECTOR_FOAM_CABLE = register("detector_foam_cable", DetectorFoamCableBlock.create(withId(cableSettings, "detector_foam_cable")));
   public static final Block DETECTOR_CABLE = register("detector_cable", DetectorCableBlock.create(withId(cableSettings, "detector_cable"), DETECTOR_FOAM_CABLE));
   public static final SplitterFoamCableBlock SPLITTER_FOAM_CABLE = register("splitter_foam_cable", SplitterFoamCableBlock.create(withId(cableSettings, "splitter_foam_cable")));
   public static final Block SPLITTER_CABLE = register("splitter_cable", SplitterCableBlock.create(withId(cableSettings, "splitter_cable"), SPLITTER_FOAM_CABLE));
   public static final Block BATBOX_CHARGEPAD = register(
      "batbox_chargepad",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("batbox_chargepad"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(),
         TileEntityChargepadBatBox.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.downSideFacings,
         true
      )
   );
   public static final Block CESU_CHARGEPAD = register(
      "cesu_chargepad",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("cesu_chargepad"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(),
         TileEntityChargepadCESU.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.downSideFacings,
         true
      )
   );
   public static final Block MFE_CHARGEPAD = register(
      "mfe_chargepad",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("mfe_chargepad"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(),
         TileEntityChargepadMFE.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.downSideFacings,
         true
      )
   );
   public static final Block MFSU_CHARGEPAD = register(
      "mfsu_chargepad",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("mfsu_chargepad"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(),
         TileEntityChargepadMFSU.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.downSideFacings,
         true
      )
   );
   public static final Block BATBOX = register(
      "batbox",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("batbox"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityElectricBatBox.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         true
      )
   );
   public static final Block CESU = register(
      "cesu",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("cesu"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityElectricCESU.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         true
      )
   );
   public static final Block MFE = register(
      "mfe",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("mfe"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityElectricMFE.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block CLASSIC_MFE = register(
      "classic_mfe",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("classic_mfe"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityElectricMFE.TileEntityElectricClassicMFE.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block MFSU = register(
      "mfsu",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("mfsu"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityElectricMFSU.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.allFacings,
         true
      )
   );
   public static final Block CLASSIC_MFSU = register(
      "classic_mfsu",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("classic_mfsu"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityElectricMFSU.TileEntityElectricClassicMFSU.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.allFacings,
         true
      )
   );
   public static final Block ELECTROLYZER = register(
      "electrolyzer",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("electrolyzer"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityElectrolyzer.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block CLASSIC_ELECTROLYZER = register(
      "classic_electrolyzer",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("classic_electrolyzer"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityClassicElectrolyzer.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block LV_TRANSFORMER = register(
      "lv_transformer",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("lv_transformer"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityTransformerLV.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.allFacings,
         true
      )
   );
   public static final Block MV_TRANSFORMER = register(
      "mv_transformer",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("mv_transformer"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityTransformerMV.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block HV_TRANSFORMER = register(
      "hv_transformer",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("hv_transformer"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityTransformerHV.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block EV_TRANSFORMER = register(
      "ev_transformer",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("ev_transformer"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityTransformerEV.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block TANK = register(
      "tank",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("tank"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityTank.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block CHUNK_LOADER = register(
      "chunk_loader",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("chunk_loader"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityChunkloader.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block CREATIVE_GENERATOR = register(
      "creative_generator",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("creative_generator"))).strength(-1.0F, Float.POSITIVE_INFINITY).sound(SoundType.METAL),
         TileEntityCreativeGenerator.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.None,
         Util.noFacings,
         false
      )
   );
   public static final Block STEAM_REPRESSURIZER = register(
      "steam_repressurizer",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("steam_repressurizer"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntitySteamRepressurizer.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.noFacings,
         true
      )
   );
   public static final Block WEIGHTED_FLUID_DISTRIBUTOR = register(
      "weighted_fluid_distributor",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("weighted_fluid_distributor"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityWeightedFluidDistributor.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block WEIGHTED_ITEM_DISTRIBUTOR = register(
      "weighted_item_distributor",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("weighted_item_distributor"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityWeightedItemDistributor.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Machine,
         Util.allFacings,
         true
      )
   );
   public static final Block RCI_RSH = register(
      "rci_rsh",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rci_rsh"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityRCI_RSH.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.allFacings,
         true
      )
   );
   public static final Block RCI_LZH = register(
      "rci_lzh",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("rci_lzh"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityRCI_LZH.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.allFacings,
         true
      )
   );
   private static final Properties cropSettings = IC2Material.plant().strength(0.8F, 0.2F).sound(SoundType.CROP).noCollision();
   public static final Block CROP_STICK = register(
      "crop_stick",
      Ic2TileEntityBlock.create(withId(cropSettings, "crop_stick"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.none)
   );
   public static final Block WEED_CROP = register(
      "weed_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "weed_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.weed)
   );
   public static final Block WHEAT_CROP = register(
      "wheat_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "wheat_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.wheat)
   );
   public static final Block CARROTS_CROP = register(
      "carrots_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "carrots_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.carrots)
   );
   public static final Block POTATO_CROP = register(
      "potato_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "potato_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.potato)
   );
   public static final Block BEETROOTS_CROP = register(
      "beetroots_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "beetroots_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.beetroots)
   );
   public static final Block PUMPKIN_CROP = register(
      "pumpkin_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "pumpkin_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.pumpkin)
   );
   public static final Block MELON_CROP = register(
      "melon_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "melon_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.melon)
   );
   public static final Block DANDELION_CROP = register(
      "dandelion_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "dandelion_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.dandelion)
   );
   public static final Block POPPY_CROP = register(
      "poppy_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "poppy_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.poppy)
   );
   public static final Block BLACKTHORN_CROP = register(
      "blackthorn_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "blackthorn_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.blackthorn)
   );
   public static final Block TULIP_CROP = register(
      "tulip_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "tulip_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.tulip)
   );
   public static final Block CYAZINT_CROP = register(
      "cyazint_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "cyazint_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.cyazint)
   );
   public static final Block VENOMILIA_CROP = register(
      "venomilia_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "venomilia_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.venomilia)
   );
   public static final Block REED_CROP = register(
      "reed_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "reed_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.reed)
   );
   public static final Block STICKY_REED_CROP = register(
      "sticky_reed_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "sticky_reed_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.stickyReed)
   );
   public static final Block COCOA_CROP = register(
      "cocoa_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "cocoa_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.cocoa)
   );
   public static final Block FLAX_CROP = register(
      "flax_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "flax_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.flax)
   );
   public static final Block RED_MUSHROOM_CROP = register(
      "red_mushroom_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "red_mushroom_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.redMushroom)
   );
   public static final Block BROWN_MUSHROOM_CROP = register(
      "brown_mushroom_crop",
      Ic2TileEntityBlock.create(
         withId(cropSettings, "brown_mushroom_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.brownMushroom
      )
   );
   public static final Block NETHER_WART_CROP = register(
      "nether_wart_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "nether_wart_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.netherWart)
   );
   public static final Block TERRA_WART_CROP = register(
      "terra_wart_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "terra_wart_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.terraWart)
   );
   public static final Block OAK_SAPLING_CROP = register(
      "oak_sapling_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "oak_sapling_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.oakSapling)
   );
   public static final Block SPRUCE_SAPLING_CROP = register(
      "spruce_sapling_crop",
      Ic2TileEntityBlock.create(
         withId(cropSettings, "spruce_sapling_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.spruceSapling
      )
   );
   public static final Block BIRCH_SAPLING_CROP = register(
      "birch_sapling_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "birch_sapling_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.birchSapling)
   );
   public static final Block JUNGLE_SAPLING_CROP = register(
      "jungle_sapling_crop",
      Ic2TileEntityBlock.create(
         withId(cropSettings, "jungle_sapling_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.jungleSapling
      )
   );
   public static final Block ACACIA_SAPLING_CROP = register(
      "acacia_sapling_crop",
      Ic2TileEntityBlock.create(
         withId(cropSettings, "acacia_sapling_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.acaciaSapling
      )
   );
   public static final Block DARK_OAK_SAPLING_CROP = register(
      "dark_oak_sapling_crop",
      Ic2TileEntityBlock.create(
         withId(cropSettings, "dark_oak_sapling_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.darkOakSapling
      )
   );
   public static final Block FERRU_CROP = register(
      "ferru_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "ferru_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.ferru)
   );
   public static final Block CYPRIUM_CROP = register(
      "cyprium_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "cyprium_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.cyprium)
   );
   public static final Block STAGNIUM_CROP = register(
      "stagnium_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "stagnium_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.stagnium)
   );
   public static final Block PLUMBISCUS_CROP = register(
      "plumbiscus_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "plumbiscus_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.plumbiscus)
   );
   public static final Block AURELIA_CROP = register(
      "aurelia_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "aurelia_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.aurelia)
   );
   public static final Block SHINING_CROP = register(
      "shining_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "shining_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.shining)
   );
   public static final Block RED_WHEAT_CROP = register(
      "red_wheat_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "red_wheat_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.redWheat)
   );
   public static final Block COFFEE_CROP = register(
      "coffee_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "coffee_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.coffee)
   );
   public static final Block HOPS_CROP = register(
      "hops_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "hops_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.hops)
   );
   public static final Block EATING_PLANT_CROP = register(
      "eating_plant_crop",
      Ic2TileEntityBlock.create(withId(cropSettings, "eating_plant_crop"), TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.eatingPlant)
   );
   public static final Block INDUSTRIAL_WORKBENCH = register(
      "industrial_workbench",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("industrial_workbench"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityIndustrialWorkbench.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block BATCH_CRAFTER = register(
      "batch_crafter",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("batch_crafter"))).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
         TileEntityBatchCrafter.class,
         true,
         Ic2TileEntityBlock.DefaultDrop.AdvMachine,
         Util.horizontalFacings,
         true
      )
   );
   public static final Block WOODEN_STORAGE_BOX = register(
      "wooden_storage_box",
      Ic2TileEntityBlock.create(
         IC2Material.wood().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("wooden_storage_box"))).strength(1.0F, 10.0F).sound(SoundType.WOOD),
         TileEntityWoodenStorageBox.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );
   public static final Block IRON_STORAGE_BOX = register(
      "iron_storage_box",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("iron_storage_box"))).strength(1.0F, 15.0F).sound(SoundType.METAL),
         TileEntityIronStorageBox.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );
   public static final Block BRONZE_STORAGE_BOX = register(
      "bronze_storage_box",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("bronze_storage_box"))).strength(1.0F, 15.0F).sound(SoundType.METAL),
         TileEntityBronzeStorageBox.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );
   public static final Block STEEL_STORAGE_BOX = register(
      "steel_storage_box",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("steel_storage_box"))).strength(2.0F, 20.0F).sound(SoundType.METAL),
         TileEntitySteelStorageBox.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );
   public static final Block IRIDIUM_STORAGE_BOX = register(
      "iridium_storage_box",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("iridium_storage_box"))).strength(3.0F, 100.0F).sound(SoundType.METAL),
         TileEntityIridiumStorageBox.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );
   public static final Block BRONZE_TANK = register(
      "bronze_tank",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("bronze_tank"))).strength(3.0F, 15.0F).sound(SoundType.METAL),
         TileEntityBronzeTank.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );
   public static final Block IRON_TANK = register(
      "iron_tank",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("iron_tank"))).strength(3.0F, 15.0F).sound(SoundType.METAL),
         TileEntityIronTank.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );
   public static final Block STEEL_TANK = register(
      "steel_tank",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("steel_tank"))).strength(4.0F, 20.0F).sound(SoundType.METAL),
         TileEntitySteelTank.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );
   public static final Block IRIDIUM_TANK = register(
      "iridium_tank",
      Ic2TileEntityBlock.create(
         IC2Material.machine().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ic2.core.IC2.getIdentifier("iridium_tank"))).strength(5.0F, 100.0F).sound(SoundType.METAL),
         TileEntityIridiumTank.class,
         false,
         Ic2TileEntityBlock.DefaultDrop.Self,
         Util.noFacings,
         false
      )
   );

   public static void init() {
   }

   /** 26.1.2: blocks need their registry id set on Properties before construction. */
   private static Properties withId(Properties settings, String name) {
      return settings.setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, IC2.getIdentifier(name)));
   }

   private static <T extends Block> T register(String name, T block) {
      IC2.envProxy.registerBlock(IC2.getIdentifier(name), block);
      return block;
   }

   private static boolean never(BlockState state, BlockGetter world, BlockPos pos) {
      return false;
   }

   private static Boolean never(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
      return false;
   }

   private static Boolean canSpawnOnLeaves(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
      return type == EntityType.OCELOT || type == EntityType.PARROT;
   }

   static {
      IC2.envProxy.registerFlammableBlock(RUBBER_LEAVES, 20, 30);
      IC2.envProxy.registerFlammableBlock(RUBBER_LOG, 20, 4);
      IC2.envProxy.registerFlammableBlock(WOODEN_SCAFFOLD, 8, 20);
      IC2.envProxy.registerFlammableBlock(REINFORCED_WOODEN_SCAFFOLD, 8, 20);
   }
}
