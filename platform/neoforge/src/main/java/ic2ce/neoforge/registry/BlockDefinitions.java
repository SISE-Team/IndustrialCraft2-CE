package ic2ce.neoforge.registry;

import ic2ce.neoforge.storage.box.TileEntityBronzeStorageBox;
import ic2ce.neoforge.storage.box.TileEntityIridiumStorageBox;
import ic2ce.neoforge.storage.box.TileEntityIronStorageBox;
import ic2ce.neoforge.storage.box.TileEntitySteelStorageBox;
import ic2ce.neoforge.storage.box.TileEntityWoodenStorageBox;
import ic2ce.neoforge.storage.tank.TileEntityBronzeTank;
import ic2ce.neoforge.storage.tank.TileEntityIridiumTank;
import ic2ce.neoforge.storage.tank.TileEntityIronTank;
import ic2ce.neoforge.storage.tank.TileEntitySteelTank;
import ic2ce.core.util.Util;
import ic2ce.neoforge.agriculture.AutoHarvester;
import ic2ce.neoforge.agriculture.CellCropCaretaker;
import ic2ce.neoforge.agriculture.CropPlot;
import ic2ce.neoforge.agriculture.Cultivar;
import ic2ce.neoforge.agriculture.FluidCropCaretaker;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.cable.BareCableBlock;
import ic2ce.neoforge.cable.BareRedstoneGateBlock;
import ic2ce.neoforge.cable.BareTrafficSensorBlock;
import ic2ce.neoforge.cable.ConductorMaterial;
import ic2ce.neoforge.cable.FoamConductorBlock;
import ic2ce.neoforge.cable.FoamedRedstoneGateBlock;
import ic2ce.neoforge.cable.FoamedTrafficSensorBlock;
import ic2ce.neoforge.crafting.AutomaticCrafter;
import ic2ce.neoforge.crafting.IndustrialWorkbench;
import ic2ce.neoforge.crafting.UuAssembler;
import ic2ce.neoforge.energy.storage.ExtremeVoltageBank;
import ic2ce.neoforge.energy.storage.ExtremeVoltagePad;
import ic2ce.neoforge.energy.storage.HighVoltageBank;
import ic2ce.neoforge.energy.storage.HighVoltagePad;
import ic2ce.neoforge.energy.storage.LowVoltageBank;
import ic2ce.neoforge.energy.storage.LowVoltagePad;
import ic2ce.neoforge.energy.storage.MediumVoltageBank;
import ic2ce.neoforge.energy.storage.MediumVoltagePad;
import ic2ce.neoforge.energy.transformer.ExtremeVoltageTransformer;
import ic2ce.neoforge.energy.transformer.HighVoltageTransformer;
import ic2ce.neoforge.energy.transformer.LowVoltageTransformer;
import ic2ce.neoforge.energy.transformer.MediumVoltageTransformer;
import ic2ce.neoforge.explosion.ConfiguredNuclearCharge;
import ic2ce.neoforge.explosion.DemolitionBlockEntity;
import ic2ce.neoforge.explosion.NuclearBlockEntity;
import ic2ce.neoforge.kinetic.ElectricMotor;
import ic2ce.neoforge.kinetic.HeatDrivenCogenerator;
import ic2ce.neoforge.kinetic.ManualCrank;
import ic2ce.neoforge.kinetic.SteamTurbine;
import ic2ce.neoforge.kinetic.WaterTurbine;
import ic2ce.neoforge.kinetic.WindTurbine;
import ic2ce.neoforge.mining.AreaMiner;
import ic2ce.neoforge.mining.MiningColumn;
import ic2ce.neoforge.mining.PipeMiner;
import ic2ce.neoforge.personal.EnergyTradeTerminal;
import ic2ce.neoforge.personal.ItemTradeTerminal;
import ic2ce.neoforge.personal.PrivateChest;
import ic2ce.neoforge.power.BiogasGenerator;
import ic2ce.neoforge.power.CombustionGenerator;
import ic2ce.neoforge.power.CreativePowerSource;
import ic2ce.neoforge.power.GeothermalGenerator;
import ic2ce.neoforge.power.KineticConverter;
import ic2ce.neoforge.power.RadioisotopeGenerator;
import ic2ce.neoforge.power.SolarCollector;
import ic2ce.neoforge.power.ThermalConverter;
import ic2ce.neoforge.power.WaterMill;
import ic2ce.neoforge.power.WindMill;
import ic2ce.neoforge.processing.fabrication.AmplifiedMatterFabricator;
import ic2ce.neoforge.processing.fabrication.ClassicMatterFabricator;
import ic2ce.neoforge.processing.fabrication.LiquidMatterFabricator;
import ic2ce.neoforge.processing.fabrication.PatternLibrary;
import ic2ce.neoforge.processing.fabrication.PatternReplicator;
import ic2ce.neoforge.processing.fabrication.PatternScanner;
import ic2ce.neoforge.processing.fluid.BiomassFermenter;
import ic2ce.neoforge.processing.fluid.CellElectrolyzer;
import ic2ce.neoforge.processing.fluid.FluidCollector;
import ic2ce.neoforge.processing.fluid.FluidElectrolyzer;
import ic2ce.neoforge.processing.fluid.IntegrationSteamConverter;
import ic2ce.neoforge.processing.fluid.LegacyCellCanner;
import ic2ce.neoforge.processing.fluid.LiquidPackager;
import ic2ce.neoforge.processing.fluid.MultiPolicyCanner;
import ic2ce.neoforge.processing.fluid.PressureBoiler;
import ic2ce.neoforge.processing.fluid.SolarDistiller;
import ic2ce.neoforge.processing.fluid.SteamCondenser;
import ic2ce.neoforge.processing.solid.MaterialExtractor;
import ic2ce.neoforge.processing.solid.MaterialRecycler;
import ic2ce.neoforge.processing.solid.MetalFormer;
import ic2ce.neoforge.processing.solid.OreGrinder;
import ic2ce.neoforge.processing.solid.OreWasher;
import ic2ce.neoforge.processing.solid.PlateCutter;
import ic2ce.neoforge.processing.solid.PressureCompressor;
import ic2ce.neoforge.processing.solid.SolidPackager;
import ic2ce.neoforge.processing.solid.ThermalCentrifuge;
import ic2ce.neoforge.processing.thermal.AlloyFurnace;
import ic2ce.neoforge.processing.thermal.ElectricSmelter;
import ic2ce.neoforge.processing.thermal.FuelSmelter;
import ic2ce.neoforge.processing.thermal.InductionSmelter;
import ic2ce.neoforge.reactor.CoolantPort;
import ic2ce.neoforge.reactor.LapisCondensatorService;
import ic2ce.neoforge.reactor.NuclearCore;
import ic2ce.neoforge.reactor.ReactorChamber;
import ic2ce.neoforge.reactor.RedstoneCondensatorService;
import ic2ce.neoforge.reactor.SignalPort;
import ic2ce.neoforge.reactor.VesselHatch;
import ic2ce.neoforge.runtime.ProjectRuntime;
import ic2ce.neoforge.storage.StationaryFluidStore;
import ic2ce.neoforge.terrain.TerrainController;
import ic2ce.neoforge.thermal.CombustionHeater;
import ic2ce.neoforge.thermal.ElectricHeater;
import ic2ce.neoforge.thermal.FluidBurner;
import ic2ce.neoforge.thermal.FluidHeatExchanger;
import ic2ce.neoforge.thermal.RadioisotopeHeater;
import ic2ce.neoforge.transport.BatchItemSorter;
import ic2ce.neoforge.transport.DualItemBuffer;
import ic2ce.neoforge.transport.FairFluidRouter;
import ic2ce.neoforge.transport.MeteredFluidOutlet;
import ic2ce.neoforge.transport.PriorityFluidRouter;
import ic2ce.neoforge.transport.PriorityItemRouter;
import ic2ce.neoforge.utility.ArcCoil;
import ic2ce.neoforge.utility.ChunkAnchor;
import ic2ce.neoforge.utility.IndustrialFence;
import ic2ce.neoforge.utility.LinkedTeleporter;
import ic2ce.neoforge.utility.PoleDrive;
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

public final class BlockDefinitions {
  public static final Block BASALT =
      register(
          "basalt",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("basalt")))
                  .strength(20.0F, 45.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block LEAD_ORE =
      register(
          "lead_ore",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lead_ore")))
                  .strength(2.0F, 4.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block TIN_ORE =
      register(
          "tin_ore",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tin_ore")))
                  .strength(3.0F, 5.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block URANIUM_ORE =
      register(
          "uranium_ore",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uranium_ore")))
                  .strength(4.0F, 6.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block DEEPSLATE_LEAD_ORE =
      register(
          "deepslate_lead_ore",
          new Block(
              BlockProfiles.stone(MapColor.DEEPSLATE)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "deepslate_lead_ore")))
                  .strength(3.0F, 6.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.DEEPSLATE)));
  public static final Block DEEPSLATE_TIN_ORE =
      register(
          "deepslate_tin_ore",
          new Block(
              BlockProfiles.stone(MapColor.DEEPSLATE)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("deepslate_tin_ore")))
                  .strength(4.0F, 7.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.DEEPSLATE)));
  public static final Block DEEPSLATE_URANIUM_ORE =
      register(
          "deepslate_uranium_ore",
          new Block(
              BlockProfiles.stone(MapColor.DEEPSLATE)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "deepslate_uranium_ore")))
                  .strength(5.0F, 8.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.DEEPSLATE)));
  public static final Block RAW_LEAD_BLOCK =
      register(
          "raw_lead_block",
          new Block(
              BlockProfiles.stone(MapColor.COLOR_LIGHT_GRAY)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("raw_lead_block")))
                  .strength(3.0F, 11.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block RAW_TIN_BLOCK =
      register(
          "raw_tin_block",
          new Block(
              BlockProfiles.stone(MapColor.SNOW)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("raw_tin_block")))
                  .strength(4.0F, 12.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block RAW_URANIUM_BLOCK =
      register(
          "raw_uranium_block",
          new Block(
              BlockProfiles.stone(MapColor.COLOR_GREEN)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("raw_uranium_block")))
                  .strength(5.0F, 13.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block BRONZE_BLOCK =
      register(
          "bronze_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_block")))
                  .strength(5.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block LEAD_BLOCK =
      register(
          "lead_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lead_block")))
                  .strength(4.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block STEEL_BLOCK =
      register(
          "steel_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_block")))
                  .strength(8.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block TIN_BLOCK =
      register(
          "tin_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tin_block")))
                  .strength(4.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block URANIUM_BLOCK =
      register(
          "uranium_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uranium_block")))
                  .strength(6.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block REINFORCED_STONE =
      register(
          "reinforced_stone",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reinforced_stone")))
                  .strength(80.0F, 180.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block MACHINE =
      register(
          "machine",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("machine")))
                  .strength(5.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block ADVANCED_MACHINE =
      register(
          "advanced_machine",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("advanced_machine")))
                  .strength(8.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block REACTOR_VESSEL =
      register(
          "reactor_vessel",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_vessel")))
                  .strength(40.0F, 90.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block SILVER_BLOCK =
      register(
          "silver_block",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("silver_block")))
                  .strength(4.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final LeavesBlock RUBBER_LEAVES =
      register(
          "rubber_leaves",
          new ic2ce.neoforge.world.block.ResinLeaves(
              0.01F,
              BlockProfiles.leaves()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_leaves")))
                  .strength(0.2F)
                  .randomTicks()
                  .sound(SoundType.GRASS)
                  .noOcclusion()
                  .isValidSpawn(BlockDefinitions::canSpawnOnLeaves)
                  .isSuffocating(BlockDefinitions::never)
                  .isViewBlocking(BlockDefinitions::never)));
  public static final ResinLog RUBBER_LOG =
      register(
          "rubber_log",
          new ResinLog(
              Properties.of()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_log")))
                  .mapColor(
                      state ->
                          state.getValue(RotatedPillarBlock.AXIS) == Axis.Y
                              ? MapColor.PODZOL
                              : MapColor.COLOR_BROWN)
                  .randomTicks()
                  .strength(1.0F)
                  .sound(SoundType.WOOD)));
  public static final RotatedPillarBlock STRIPPED_RUBBER_LOG =
      register(
          "stripped_rubber_log",
          new RotatedPillarBlock(
              Properties.of()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "stripped_rubber_log")))
                  .mapColor(
                      state ->
                          state.getValue(RotatedPillarBlock.AXIS) == Axis.Y
                              ? MapColor.PODZOL
                              : MapColor.COLOR_BROWN)
                  .strength(1.0F)
                  .sound(SoundType.WOOD)));
  public static final BarkWood RUBBER_WOOD =
      register(
          "rubber_wood",
          new BarkWood(
              BlockProfiles.wood(MapColor.COLOR_BROWN)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_wood")))
                  .strength(1.0F)
                  .sound(SoundType.WOOD)));
  public static final Block STRIPPED_RUBBER_WOOD =
      register(
          "stripped_rubber_wood",
          new Block(
              BlockProfiles.wood(MapColor.PODZOL)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "stripped_rubber_wood")))
                  .strength(1.0F)
                  .sound(SoundType.WOOD)));
  public static final Block RUBBER_SAPLING =
      register(
          "rubber_sapling",
          new SaplingBlock(
              new TreeGrower(
                  "rubber",
                  Optional.empty(),
                  Optional.of(
                      ResourceKey.create(
                          Registries.CONFIGURED_FEATURE,
                          ProjectRuntime.getIdentifier("rubber_tree"))),
                  Optional.empty()),
              BlockProfiles.plant()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_sapling")))
                  .noCollision()
                  .randomTicks()
                  .instabreak()
                  .sound(SoundType.GRASS)) {});
  public static final Block RUBBER_PLANKS =
      register(
          "rubber_planks",
          new Block(
              BlockProfiles.wood(MapColor.PODZOL)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_planks")))
                  .strength(2.0F, 3.0F)
                  .sound(SoundType.WOOD)));
  public static final Block RUBBER_BUTTON =
      register(
          "rubber_button",
          new ButtonBlock(
              BlockSetType.OAK,
              30,
              BlockProfiles.decoration()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_button")))
                  .noCollision()
                  .strength(0.5F)
                  .sound(SoundType.WOOD)) {});
  public static final Block RUBBER_DOOR =
      register(
          "rubber_door",
          new DoorBlock(
              BlockSetType.OAK,
              BlockProfiles.wood(MapColor.WOOD)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_door")))
                  .strength(3.0F)
                  .sound(SoundType.WOOD)
                  .noOcclusion()) {});
  public static final Block RUBBER_FENCE =
      register(
          "rubber_fence",
          new FenceBlock(
              BlockProfiles.wood(MapColor.WOOD)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_fence")))
                  .strength(2.0F, 3.0F)
                  .sound(SoundType.WOOD)));
  public static final Block RUBBER_FENCE_GATE =
      register(
          "rubber_fence_gate",
          new FenceGateBlock(
              WoodType.OAK,
              BlockProfiles.wood(MapColor.WOOD)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_fence_gate")))
                  .strength(2.0F, 3.0F)
                  .sound(SoundType.WOOD)));
  public static final Block RUBBER_PRESSURE_PLATE =
      register(
          "rubber_pressure_plate",
          new PressurePlateBlock(
              BlockSetType.OAK,
              BlockProfiles.wood(MapColor.WOOD)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "rubber_pressure_plate")))
                  .noCollision()
                  .strength(0.5F)
                  .sound(SoundType.WOOD)) {});
  public static final Block RUBBER_SIGN =
      register(
          "rubber_sign",
          new RubberStandingSign(
              BlockProfiles.wood(MapColor.COLOR_BROWN)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_sign")))
                  .noCollision()
                  .strength(1.0F)
                  .sound(SoundType.WOOD),
              SignDefinitions.RUBBER));
  public static final Block RUBBER_SLAB =
      register(
          "rubber_slab",
          new SlabBlock(
              BlockProfiles.wood(MapColor.WOOD)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_slab")))
                  .strength(2.0F, 3.0F)
                  .sound(SoundType.WOOD)));
  public static final Block RUBBER_STAIRS =
      register(
          "rubber_stairs",
          new StairBlock(
              RUBBER_PLANKS.defaultBlockState(),
              BlockProfiles.wood(MapColor.PODZOL)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_stairs")))
                  .strength(2.0F, 3.0F)
                  .sound(SoundType.WOOD)) {});
  public static final Block RUBBER_TRAPDOOR =
      register(
          "rubber_trapdoor",
          new TrapDoorBlock(
              net.minecraft.world.level.block.state.properties.BlockSetType.OAK,
              BlockProfiles.wood(MapColor.PODZOL)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_trapdoor")))
                  .strength(3.0F)
                  .sound(SoundType.WOOD)
                  .noOcclusion()
                  .isValidSpawn(BlockDefinitions::never)) {});
  public static final Block RUBBER_WALL_SIGN =
      register(
          "rubber_wall_sign",
          new RubberWallSign(
              BlockProfiles.wood(MapColor.COLOR_BROWN)
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_wall_sign")))
                  .noCollision()
                  .strength(1.0F)
                  .sound(SoundType.WOOD)
                  .overrideLootTable(RUBBER_SIGN.getLootTable()),
              SignDefinitions.RUBBER));
  public static final Block WOODEN_SCAFFOLD =
      register(
          "wooden_scaffold",
          new StructuralScaffold(
              BlockProfiles.wood()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wooden_scaffold")))
                  .noOcclusion()
                  .strength(0.5F, 0.12F)
                  .randomTicks()
                  .sound(SoundType.WOOD),
              2));
  public static final Block REINFORCED_WOODEN_SCAFFOLD =
      register(
          "reinforced_wooden_scaffold",
          new StructuralScaffold(
              BlockProfiles.wood()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "reinforced_wooden_scaffold")))
                  .noOcclusion()
                  .strength(0.6F, 0.24F)
                  .randomTicks()
                  .sound(SoundType.WOOD),
              5));
  public static final Block IRON_SCAFFOLD =
      register(
          "iron_scaffold",
          new StructuralScaffold(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_scaffold")))
                  .noOcclusion()
                  .strength(0.8F, 6.0F)
                  .randomTicks()
                  .sound(SoundType.METAL),
              5));
  public static final Block REINFORCED_IRON_SCAFFOLD =
      register(
          "reinforced_iron_scaffold",
          new StructuralScaffold(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "reinforced_iron_scaffold")))
                  .noOcclusion()
                  .strength(1.0F, 8.0F)
                  .randomTicks()
                  .sound(SoundType.METAL),
              12));
  public static final Block IRON_FENCE =
      register(
          "iron_fence",
          new IndustrialFence(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_fence")))
                  .strength(5.0F, 10.0F),
              true));
  public static final Block RESIN_SHEET =
      register(
          "resin_sheet",
          new ReactiveSheet(
              BlockProfiles.decoration()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("resin_sheet")))
                  .strength(1.6F, 0.5F),
              ReactiveSheet.Surface.RESIN));
  public static final Block RUBBER_SHEET =
      register(
          "rubber_sheet",
          new ReactiveSheet(
              BlockProfiles.decoration()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rubber_sheet")))
                  .strength(0.8F, 2.0F),
              ReactiveSheet.Surface.RUBBER));
  public static final Block WOOL_SHEET =
      register(
          "wool_sheet",
          new ReactiveSheet(
              BlockProfiles.decoration()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wool_sheet")))
                  .strength(0.8F, 0.8F),
              ReactiveSheet.Surface.WOOL));
  public static final Block REINFORCED_GLASS =
      register(
          "reinforced_glass",
          new StructuralGlass(
              BlockProfiles.glass()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reinforced_glass")))
                  .noOcclusion()
                  .strength(5.0F, 180.0F)
                  .sound(SoundType.GLASS)
                  .isValidSpawn((state, world, pos, type) -> false)));
  public static final Block FOAM =
      register(
          "foam",
          new CuringFoam(
              BlockProfiles.wool()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("foam")))
                  .noOcclusion()
                  .strength(0.01F, 10.0F)
                  .randomTicks()
                  .sound(SoundType.WOOL)));
  private static final java.util.function.Supplier<Properties> wallSettings =
      () ->
          BlockProfiles.stone()
              .strength(3.0F, 30.0F)
              .requiresCorrectToolForDrops()
              .sound(SoundType.STONE);
  public static final Block WHITE_WALL =
      register(
          "white_wall", new ConstructionWall(withId(wallSettings, "white_wall"), DyeColor.WHITE));
  public static final Block ORANGE_WALL =
      register(
          "orange_wall",
          new ConstructionWall(withId(wallSettings, "orange_wall"), DyeColor.ORANGE));
  public static final Block MAGENTA_WALL =
      register(
          "magenta_wall",
          new ConstructionWall(withId(wallSettings, "magenta_wall"), DyeColor.MAGENTA));
  public static final Block LIGHT_BLUE_WALL =
      register(
          "light_blue_wall",
          new ConstructionWall(withId(wallSettings, "light_blue_wall"), DyeColor.LIGHT_BLUE));
  public static final Block YELLOW_WALL =
      register(
          "yellow_wall",
          new ConstructionWall(withId(wallSettings, "yellow_wall"), DyeColor.YELLOW));
  public static final Block LIME_WALL =
      register("lime_wall", new ConstructionWall(withId(wallSettings, "lime_wall"), DyeColor.LIME));
  public static final Block PINK_WALL =
      register("pink_wall", new ConstructionWall(withId(wallSettings, "pink_wall"), DyeColor.PINK));
  public static final Block GRAY_WALL =
      register("gray_wall", new ConstructionWall(withId(wallSettings, "gray_wall"), DyeColor.GRAY));
  public static final Block LIGHT_GRAY_WALL =
      register(
          "light_gray_wall",
          new ConstructionWall(withId(wallSettings, "light_gray_wall"), DyeColor.LIGHT_GRAY));
  public static final Block CYAN_WALL =
      register("cyan_wall", new ConstructionWall(withId(wallSettings, "cyan_wall"), DyeColor.CYAN));
  public static final Block PURPLE_WALL =
      register(
          "purple_wall",
          new ConstructionWall(withId(wallSettings, "purple_wall"), DyeColor.PURPLE));
  public static final Block BLUE_WALL =
      register("blue_wall", new ConstructionWall(withId(wallSettings, "blue_wall"), DyeColor.BLUE));
  public static final Block BROWN_WALL =
      register(
          "brown_wall", new ConstructionWall(withId(wallSettings, "brown_wall"), DyeColor.BROWN));
  public static final Block GREEN_WALL =
      register(
          "green_wall", new ConstructionWall(withId(wallSettings, "green_wall"), DyeColor.GREEN));
  public static final Block RED_WALL =
      register("red_wall", new ConstructionWall(withId(wallSettings, "red_wall"), DyeColor.RED));
  public static final Block BLACK_WALL =
      register(
          "black_wall", new ConstructionWall(withId(wallSettings, "black_wall"), DyeColor.BLACK));
  public static final Block OBSCURED_WALL =
      register(
          "obscured_wall",
          MachineBlockType.create(
              withId(wallSettings, "obscured_wall"),
              ImprintedWall.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block MINING_PIPE =
      register(
          "mining_pipe",
          new MiningColumn(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mining_pipe")))
                  .strength(6.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block MINING_PIPE_TIP =
      register(
          "mining_pipe_tip",
          new Block(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mining_pipe_tip")))
                  .strength(6.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block REINFORCED_DOOR =
      register(
          "reinforced_door",
          new DoorBlock(
              BlockSetType.IRON,
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reinforced_door")))
                  .strength(50.0F, 150.0F)
                  .sound(SoundType.METAL)) {});
  public static final Block ITNT =
      register(
          "itnt",
          MachineBlockType.create(
              BlockProfiles.explosive()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("itnt")))
                  .strength(0.0F, 0.0F)
                  .sound(SoundType.GRASS),
              DemolitionBlockEntity.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block NUKE =
      register(
          "nuke",
          MachineBlockType.create(
              BlockProfiles.explosive()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("nuke")))
                  .strength(0.0F, 0.0F)
                  .sound(SoundType.GRASS),
              ConfiguredNuclearCharge.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block CLASSIC_NUKE =
      register(
          "classic_nuke",
          MachineBlockType.create(
              BlockProfiles.explosive()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("classic_nuke")))
                  .strength(0.0F, 0.0F)
                  .sound(SoundType.GRASS),
              NuclearBlockEntity.ClassicNuclearCharge.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block GENERATOR =
      register(
          "generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              CombustionGenerator.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              true));
  public static final Block GEO_GENERATOR =
      register(
          "geo_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("geo_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              GeothermalGenerator.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              Util.horizontalFacings,
              true));
  public static final Block KINETIC_GENERATOR =
      register(
          "kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              KineticConverter.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              Util.allFacings,
              true));
  public static final Block RT_GENERATOR =
      register(
          "rt_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rt_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              RadioisotopeGenerator.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              Util.noFacings,
              false));
  public static final Block SEMIFLUID_GENERATOR =
      register(
          "semifluid_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "semifluid_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              BiogasGenerator.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              Util.horizontalFacings,
              true));
  public static final Block SOLAR_GENERATOR =
      register(
          "solar_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("solar_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SolarCollector.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              Util.horizontalFacings,
              true));
  public static final Block STIRLING_GENERATOR =
      register(
          "stirling_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "stirling_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ThermalConverter.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              Util.allFacings,
              true));
  public static final Block WATER_GENERATOR =
      register(
          "water_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("water_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              WaterMill.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              Util.horizontalFacings,
              true));
  public static final Block WIND_GENERATOR =
      register(
          "wind_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("wind_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              WindMill.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              Util.horizontalFacings,
              true));
  public static final Block ELECTRIC_HEAT_GENERATOR =
      register(
          "electric_heat_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "electric_heat_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ElectricHeater.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              true));
  public static final Block FLUID_HEAT_GENERATOR =
      register(
          "fluid_heat_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "fluid_heat_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FluidBurner.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              true));
  public static final Block RT_HEAT_GENERATOR =
      register(
          "rt_heat_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rt_heat_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              RadioisotopeHeater.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              true));
  public static final Block SOLID_HEAT_GENERATOR =
      register(
          "solid_heat_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "solid_heat_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              CombustionHeater.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              true));
  public static final Block ELECTRIC_KINETIC_GENERATOR =
      register(
          "electric_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "electric_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ElectricMotor.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              true));
  public static final Block MANUAL_KINETIC_GENERATOR =
      register(
          "manual_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "manual_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ManualCrank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              true));
  public static final Block STEAM_KINETIC_GENERATOR =
      register(
          "steam_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "steam_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SteamTurbine.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              true));
  public static final Block STIRLING_KINETIC_GENERATOR =
      register(
          "stirling_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "stirling_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              HeatDrivenCogenerator.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              true));
  public static final Block WATER_KINETIC_GENERATOR =
      register(
          "water_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "water_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              WaterTurbine.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              true));
  public static final Block WIND_KINETIC_GENERATOR =
      register(
          "wind_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "wind_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              WindTurbine.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              true));
  public static final Block NUCLEAR_REACTOR =
      register(
          "nuclear_reactor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("nuclear_reactor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              NuclearCore.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              Util.horizontalFacings,
              true));
  public static final Block REACTOR_ACCESS_HATCH =
      register(
          "reactor_access_hatch",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "reactor_access_hatch")))
                  .strength(40.0F, 90.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              VesselHatch.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.onlyNorth,
              false));
  public static final Block REACTOR_CHAMBER =
      register(
          "reactor_chamber",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("reactor_chamber")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ReactorChamber.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              Util.onlyNorth,
              true));
  public static final Block REACTOR_FLUID_PORT =
      register(
          "reactor_fluid_port",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "reactor_fluid_port")))
                  .strength(40.0F, 90.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              CoolantPort.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.onlyNorth,
              false));
  public static final Block REACTOR_REDSTONE_PORT =
      register(
          "reactor_redstone_port",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "reactor_redstone_port")))
                  .strength(40.0F, 90.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SignalPort.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.onlyNorth,
              false));
  public static final Block CONDENSER =
      register(
          "condenser",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("condenser")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SteamCondenser.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block FLUID_BOTTLER =
      register(
          "fluid_bottler",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fluid_bottler")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LiquidPackager.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block FLUID_DISTRIBUTOR =
      register(
          "fluid_distributor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fluid_distributor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FairFluidRouter.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block FLUID_REGULATOR =
      register(
          "fluid_regulator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fluid_regulator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MeteredFluidOutlet.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block LIQUID_HEAT_EXCHANGER =
      register(
          "liquid_heat_exchanger",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "liquid_heat_exchanger")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FluidHeatExchanger.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block PUMP =
      register(
          "pump",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("pump")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FluidCollector.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block SOLAR_DISTILLER =
      register(
          "solar_distiller",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("solar_distiller")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SolarDistiller.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              Util.noFacings,
              false));
  public static final Block STEAM_GENERATOR =
      register(
          "steam_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steam_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PressureBoiler.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              true));
  public static final Block ITEM_BUFFER =
      register(
          "item_buffer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("item_buffer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              DualItemBuffer.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              Util.noFacings,
              false));
  public static final Block MAGNETIZER =
      register(
          "magnetizer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("magnetizer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PoleDrive.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              true));
  public static final Block SORTING_MACHINE =
      register(
          "sorting_machine",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("sorting_machine")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              BatchItemSorter.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              Util.onlyNorth,
              false));
  public static final Block TELEPORTER =
      register(
          "teleporter",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("teleporter")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LinkedTeleporter.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.noFacings,
              false));
  public static final Block TERRAFORMER =
      register(
          "terraformer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("terraformer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              TerrainController.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.noFacings,
              false));
  public static final Block TESLA_COIL =
      register(
          "tesla_coil",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tesla_coil")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ArcCoil.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block CANNER =
      register(
          "canner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("canner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MultiPolicyCanner.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block CLASSIC_CANNER =
      register(
          "classic_canner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("classic_canner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LegacyCellCanner.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final MachineBlockType COMPRESSOR =
      register(
          "compressor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("compressor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PressureCompressor.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block ELECTRIC_FURNACE =
      register(
          "electric_furnace",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("electric_furnace")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ElectricSmelter.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              true));
  public static final MachineBlockType EXTRACTOR =
      register(
          "extractor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("extractor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MaterialExtractor.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block IRON_FURNACE =
      register(
          "iron_furnace",
          MachineBlockType.create(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_furnace")))
                  .strength(5.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FuelSmelter.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              false));
  public static final MachineBlockType MACERATOR =
      register(
          "macerator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("macerator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              OreGrinder.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block RECYCLER =
      register(
          "recycler",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("recycler")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MaterialRecycler.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block SOLID_CANNER =
      register(
          "solid_canner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("solid_canner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SolidPackager.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final MachineBlockType BLAST_FURNACE =
      register(
          "blast_furnace",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("blast_furnace")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              AlloyFurnace.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final MachineBlockType BLOCK_CUTTER =
      register(
          "block_cutter",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("block_cutter")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PlateCutter.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.horizontalFacings,
              true));
  public static final MachineBlockType CENTRIFUGE =
      register(
          "centrifuge",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("centrifuge")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ThermalCentrifuge.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block FERMENTER =
      register(
          "fermenter",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("fermenter")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              BiomassFermenter.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block INDUCTION_FURNACE =
      register(
          "induction_furnace",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("induction_furnace")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              InductionSmelter.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.horizontalFacings,
              true));
  public static final MachineBlockType METAL_FORMER =
      register(
          "metal_former",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("metal_former")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MetalFormer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final MachineBlockType ORE_WASHING_PLANT =
      register(
          "ore_washing_plant",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("ore_washing_plant")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              OreWasher.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block ADVANCED_MINER =
      register(
          "advanced_miner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("advanced_miner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              AreaMiner.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              true));
  public static final Block CROP_HARVESTER =
      register(
          "crop_harvester",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("crop_harvester")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              AutoHarvester.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block CROPMATRON =
      register(
          "cropmatron",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cropmatron")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FluidCropCaretaker.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block CLASSIC_CROPMATRON =
      register(
          "classic_cropmatron",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "classic_cropmatron")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              CellCropCaretaker.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block MINER =
      register(
          "miner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("miner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PipeMiner.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              true));
  public static final Block MASS_FABRICATOR =
      register(
          "mass_fabricator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mass_fabricator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              AmplifiedMatterFabricator.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.horizontalFacings,
              true));
  public static final Block CLASSIC_MASS_FABRICATOR =
      register(
          "classic_mass_fabricator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "classic_mass_fabricator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ClassicMatterFabricator.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.horizontalFacings,
              true));
  public static final Block UU_ASSEMBLY_BENCH =
      register(
          "uu_assembly_bench",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uu_assembly_bench")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              UuAssembler.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.horizontalFacings,
              true));
  public static final Block MATTER_GENERATOR =
      register(
          "matter_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("matter_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LiquidMatterFabricator.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.horizontalFacings,
              true));
  public static final Block PATTERN_STORAGE =
      register(
          "pattern_storage",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("pattern_storage")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PatternLibrary.class,
              false,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.horizontalFacings,
              true));
  public static final Block REPLICATOR =
      register(
          "replicator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("replicator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PatternReplicator.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.horizontalFacings,
              true));
  public static final Block UU_SCANNER =
      register(
          "uu_scanner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("uu_scanner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PatternScanner.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.horizontalFacings,
              true));
  public static final Block ENERGY_O_MAT =
      register(
          "energy_o_mat",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("energy_o_mat")))
                  .strength(-1.0F, 3600000.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              EnergyTradeTerminal.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              false));
  public static final Block PERSONAL_CHEST =
      register(
          "personal_chest",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("personal_chest")))
                  .strength(-1.0F, 3600000.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PrivateChest.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              false));
  public static final Block TRADE_O_MAT =
      register(
          "trade_o_mat",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("trade_o_mat")))
                  .strength(-1.0F, 3600000.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ItemTradeTerminal.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              false));
  private static final java.util.function.Supplier<Properties> cableSettings =
      () -> Properties.of().mapColor(MapColor.METAL).strength(0.5F, 5.0F).sound(SoundType.METAL);
  private static final java.util.function.Supplier<Properties> insulatedCableSettings =
      () -> BlockProfiles.wool().strength(0.5F, 5.0F).sound(SoundType.WOOL);
  public static final FoamConductorBlock COPPER_FOAM_CABLE =
      register(
          "copper_foam_cable",
          FoamConductorBlock.create(
              withId(cableSettings, "copper_foam_cable"), ConductorMaterial.copper, 0));
  public static final Block COPPER_CABLE =
      register(
          "copper_cable",
          BareCableBlock.create(
              withId(cableSettings, "copper_cable"),
              ConductorMaterial.copper,
              0,
              COPPER_FOAM_CABLE));
  public static final FoamConductorBlock INSULATED_COPPER_FOAM_CABLE =
      register(
          "insulated_copper_foam_cable",
          FoamConductorBlock.create(
              withId(insulatedCableSettings, "insulated_copper_foam_cable"),
              ConductorMaterial.copper,
              1));
  public static final Block INSULATED_COPPER_CABLE =
      register(
          "insulated_copper_cable",
          BareCableBlock.create(
              withId(insulatedCableSettings, "insulated_copper_cable"),
              ConductorMaterial.copper,
              1,
              INSULATED_COPPER_FOAM_CABLE));
  public static final FoamConductorBlock GLASS_FIBRE_FOAM_CABLE =
      register(
          "glass_fibre_foam_cable",
          FoamConductorBlock.create(
              BlockProfiles.glass()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "glass_fibre_foam_cable")))
                  .strength(0.5F, 5.0F)
                  .sound(SoundType.GLASS),
              ConductorMaterial.glass,
              0));
  public static final Block GLASS_FIBRE_CABLE =
      register(
          "glass_fibre_cable",
          BareCableBlock.create(
              BlockProfiles.glass()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("glass_fibre_cable")))
                  .strength(0.5F, 5.0F)
                  .sound(SoundType.GLASS),
              ConductorMaterial.glass,
              0,
              GLASS_FIBRE_FOAM_CABLE));
  public static final FoamConductorBlock GOLD_FOAM_CABLE =
      register(
          "gold_foam_cable",
          FoamConductorBlock.create(
              withId(cableSettings, "gold_foam_cable"), ConductorMaterial.gold, 0));
  public static final Block GOLD_CABLE =
      register(
          "gold_cable",
          BareCableBlock.create(
              withId(cableSettings, "gold_cable"), ConductorMaterial.gold, 0, GOLD_FOAM_CABLE));
  public static final FoamConductorBlock INSULATED_GOLD_FOAM_CABLE =
      register(
          "insulated_gold_foam_cable",
          FoamConductorBlock.create(
              withId(insulatedCableSettings, "insulated_gold_foam_cable"),
              ConductorMaterial.gold,
              1));
  public static final Block INSULATED_GOLD_CABLE =
      register(
          "insulated_gold_cable",
          BareCableBlock.create(
              withId(insulatedCableSettings, "insulated_gold_cable"),
              ConductorMaterial.gold,
              1,
              INSULATED_GOLD_FOAM_CABLE));
  public static final FoamConductorBlock DOUBLE_INSULATED_GOLD_FOAM_CABLE =
      register(
          "double_insulated_gold_foam_cable",
          FoamConductorBlock.create(
              withId(insulatedCableSettings, "double_insulated_gold_foam_cable"),
              ConductorMaterial.gold,
              2));
  public static final Block DOUBLE_INSULATED_GOLD_CABLE =
      register(
          "double_insulated_gold_cable",
          BareCableBlock.create(
              withId(insulatedCableSettings, "double_insulated_gold_cable"),
              ConductorMaterial.gold,
              2,
              DOUBLE_INSULATED_GOLD_FOAM_CABLE));
  public static final FoamConductorBlock IRON_FOAM_CABLE =
      register(
          "iron_foam_cable",
          FoamConductorBlock.create(
              withId(cableSettings, "iron_foam_cable"), ConductorMaterial.iron, 0));
  public static final Block IRON_CABLE =
      register(
          "iron_cable",
          BareCableBlock.create(
              withId(cableSettings, "iron_cable"), ConductorMaterial.iron, 0, IRON_FOAM_CABLE));
  public static final FoamConductorBlock INSULATED_IRON_FOAM_CABLE =
      register(
          "insulated_iron_foam_cable",
          FoamConductorBlock.create(
              withId(insulatedCableSettings, "insulated_iron_foam_cable"),
              ConductorMaterial.iron,
              1));
  public static final Block INSULATED_IRON_CABLE =
      register(
          "insulated_iron_cable",
          BareCableBlock.create(
              withId(insulatedCableSettings, "insulated_iron_cable"),
              ConductorMaterial.iron,
              1,
              INSULATED_IRON_FOAM_CABLE));
  public static final FoamConductorBlock DOUBLE_INSULATED_IRON_FOAM_CABLE =
      register(
          "double_insulated_iron_foam_cable",
          FoamConductorBlock.create(
              withId(insulatedCableSettings, "double_insulated_iron_foam_cable"),
              ConductorMaterial.iron,
              2));
  public static final Block DOUBLE_INSULATED_IRON_CABLE =
      register(
          "double_insulated_iron_cable",
          BareCableBlock.create(
              withId(insulatedCableSettings, "double_insulated_iron_cable"),
              ConductorMaterial.iron,
              2,
              DOUBLE_INSULATED_IRON_FOAM_CABLE));
  public static final FoamConductorBlock TRIPLE_INSULATED_IRON_FOAM_CABLE =
      register(
          "triple_insulated_iron_foam_cable",
          FoamConductorBlock.create(
              withId(insulatedCableSettings, "triple_insulated_iron_foam_cable"),
              ConductorMaterial.iron,
              3));
  public static final Block TRIPLE_INSULATED_IRON_CABLE =
      register(
          "triple_insulated_iron_cable",
          BareCableBlock.create(
              withId(insulatedCableSettings, "triple_insulated_iron_cable"),
              ConductorMaterial.iron,
              3,
              TRIPLE_INSULATED_IRON_FOAM_CABLE));
  public static final FoamConductorBlock TIN_FOAM_CABLE =
      register(
          "tin_foam_cable",
          FoamConductorBlock.create(
              withId(cableSettings, "tin_foam_cable"), ConductorMaterial.tin, 0));
  public static final Block TIN_CABLE =
      register(
          "tin_cable",
          BareCableBlock.create(
              withId(cableSettings, "tin_cable"), ConductorMaterial.tin, 0, TIN_FOAM_CABLE));
  public static final FoamConductorBlock INSULATED_TIN_FOAM_CABLE =
      register(
          "insulated_tin_foam_cable",
          FoamConductorBlock.create(
              withId(insulatedCableSettings, "insulated_tin_foam_cable"),
              ConductorMaterial.tin,
              1));
  public static final Block INSULATED_TIN_CABLE =
      register(
          "insulated_tin_cable",
          BareCableBlock.create(
              withId(insulatedCableSettings, "insulated_tin_cable"),
              ConductorMaterial.tin,
              1,
              INSULATED_TIN_FOAM_CABLE));
  public static final FoamedTrafficSensorBlock DETECTOR_FOAM_CABLE =
      register(
          "detector_foam_cable",
          FoamedTrafficSensorBlock.create(withId(cableSettings, "detector_foam_cable")));
  public static final Block DETECTOR_CABLE =
      register(
          "detector_cable",
          BareTrafficSensorBlock.create(
              withId(cableSettings, "detector_cable"), DETECTOR_FOAM_CABLE));
  public static final FoamedRedstoneGateBlock SPLITTER_FOAM_CABLE =
      register(
          "splitter_foam_cable",
          FoamedRedstoneGateBlock.create(withId(cableSettings, "splitter_foam_cable")));
  public static final Block SPLITTER_CABLE =
      register(
          "splitter_cable",
          BareRedstoneGateBlock.create(
              withId(cableSettings, "splitter_cable"), SPLITTER_FOAM_CABLE));
  public static final Block BATBOX_CHARGEPAD =
      register(
          "batbox_chargepad",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("batbox_chargepad")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)
                  .noOcclusion(),
              LowVoltagePad.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.downSideFacings,
              true));
  public static final Block CESU_CHARGEPAD =
      register(
          "cesu_chargepad",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cesu_chargepad")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)
                  .noOcclusion(),
              MediumVoltagePad.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.downSideFacings,
              true));
  public static final Block MFE_CHARGEPAD =
      register(
          "mfe_chargepad",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mfe_chargepad")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)
                  .noOcclusion(),
              HighVoltagePad.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.downSideFacings,
              true));
  public static final Block MFSU_CHARGEPAD =
      register(
          "mfsu_chargepad",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mfsu_chargepad")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)
                  .noOcclusion(),
              ExtremeVoltagePad.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.downSideFacings,
              true));
  public static final Block BATBOX =
      register(
          "batbox",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("batbox")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LowVoltageBank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              true));
  public static final Block CESU =
      register(
          "cesu",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("cesu")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MediumVoltageBank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              true));
  public static final Block MFE =
      register(
          "mfe",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mfe")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              HighVoltageBank.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block CLASSIC_MFE =
      register(
          "classic_mfe",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("classic_mfe")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              HighVoltageBank.TileEntityElectricClassicMFE.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block MFSU =
      register(
          "mfsu",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mfsu")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ExtremeVoltageBank.class,
              false,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.allFacings,
              true));
  public static final Block CLASSIC_MFSU =
      register(
          "classic_mfsu",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("classic_mfsu")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ExtremeVoltageBank.TileEntityElectricClassicMFSU.class,
              false,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.allFacings,
              true));
  public static final Block ELECTROLYZER =
      register(
          "electrolyzer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("electrolyzer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FluidElectrolyzer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block CLASSIC_ELECTROLYZER =
      register(
          "classic_electrolyzer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "classic_electrolyzer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              CellElectrolyzer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.horizontalFacings,
              true));
  public static final Block LV_TRANSFORMER =
      register(
          "lv_transformer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("lv_transformer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LowVoltageTransformer.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.allFacings,
              true));
  public static final Block MV_TRANSFORMER =
      register(
          "mv_transformer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("mv_transformer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MediumVoltageTransformer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block HV_TRANSFORMER =
      register(
          "hv_transformer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("hv_transformer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              HighVoltageTransformer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block EV_TRANSFORMER =
      register(
          "ev_transformer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("ev_transformer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ExtremeVoltageTransformer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block TANK =
      register(
          "tank",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("tank")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              StationaryFluidStore.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              true));
  public static final Block CHUNK_LOADER =
      register(
          "chunk_loader",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("chunk_loader")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ChunkAnchor.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              true));
  public static final Block CREATIVE_GENERATOR =
      register(
          "creative_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "creative_generator")))
                  .strength(-1.0F, Float.POSITIVE_INFINITY)
                  .sound(SoundType.METAL),
              CreativePowerSource.class,
              true,
              MachineBlockType.DefaultDrop.None,
              Util.noFacings,
              false));
  public static final Block STEAM_REPRESSURIZER =
      register(
          "steam_repressurizer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "steam_repressurizer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              IntegrationSteamConverter.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              Util.noFacings,
              true));
  public static final Block WEIGHTED_FLUID_DISTRIBUTOR =
      register(
          "weighted_fluid_distributor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "weighted_fluid_distributor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PriorityFluidRouter.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block WEIGHTED_ITEM_DISTRIBUTOR =
      register(
          "weighted_item_distributor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "weighted_item_distributor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PriorityItemRouter.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              Util.allFacings,
              true));
  public static final Block RCI_RSH =
      register(
          "rci_rsh",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rci_rsh")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              RedstoneCondensatorService.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.allFacings,
              true));
  public static final Block RCI_LZH =
      register(
          "rci_lzh",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("rci_lzh")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LapisCondensatorService.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.allFacings,
              true));
  private static final java.util.function.Supplier<Properties> cropSettings =
      () -> BlockProfiles.plant().strength(0.8F, 0.2F).sound(SoundType.CROP).noCollision();
  public static final Block CROP_STICK =
      register(
          "crop_stick",
          MachineBlockType.create(
              withId(cropSettings, "crop_stick"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.NONE));
  public static final Block WEED_CROP =
      register(
          "weed_crop",
          MachineBlockType.create(
              withId(cropSettings, "weed_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.WEED));
  public static final Block WHEAT_CROP =
      register(
          "wheat_crop",
          MachineBlockType.create(
              withId(cropSettings, "wheat_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.WHEAT));
  public static final Block CARROTS_CROP =
      register(
          "carrots_crop",
          MachineBlockType.create(
              withId(cropSettings, "carrots_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.CARROTS));
  public static final Block POTATO_CROP =
      register(
          "potato_crop",
          MachineBlockType.create(
              withId(cropSettings, "potato_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.POTATO));
  public static final Block BEETROOTS_CROP =
      register(
          "beetroots_crop",
          MachineBlockType.create(
              withId(cropSettings, "beetroots_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.BEETROOTS));
  public static final Block PUMPKIN_CROP =
      register(
          "pumpkin_crop",
          MachineBlockType.create(
              withId(cropSettings, "pumpkin_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.PUMPKIN));
  public static final Block MELON_CROP =
      register(
          "melon_crop",
          MachineBlockType.create(
              withId(cropSettings, "melon_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.MELON));
  public static final Block DANDELION_CROP =
      register(
          "dandelion_crop",
          MachineBlockType.create(
              withId(cropSettings, "dandelion_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.DANDELION));
  public static final Block POPPY_CROP =
      register(
          "poppy_crop",
          MachineBlockType.create(
              withId(cropSettings, "poppy_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.POPPY));
  public static final Block BLACKTHORN_CROP =
      register(
          "blackthorn_crop",
          MachineBlockType.create(
              withId(cropSettings, "blackthorn_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.BLACKTHORN));
  public static final Block TULIP_CROP =
      register(
          "tulip_crop",
          MachineBlockType.create(
              withId(cropSettings, "tulip_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.TULIP));
  public static final Block CYAZINT_CROP =
      register(
          "cyazint_crop",
          MachineBlockType.create(
              withId(cropSettings, "cyazint_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.CYAZINT));
  public static final Block VENOMILIA_CROP =
      register(
          "venomilia_crop",
          MachineBlockType.create(
              withId(cropSettings, "venomilia_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.VENOMILIA));
  public static final Block REED_CROP =
      register(
          "reed_crop",
          MachineBlockType.create(
              withId(cropSettings, "reed_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.REED));
  public static final Block STICKY_REED_CROP =
      register(
          "sticky_reed_crop",
          MachineBlockType.create(
              withId(cropSettings, "sticky_reed_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.STICKY_REED));
  public static final Block COCOA_CROP =
      register(
          "cocoa_crop",
          MachineBlockType.create(
              withId(cropSettings, "cocoa_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.COCOA));
  public static final Block FLAX_CROP =
      register(
          "flax_crop",
          MachineBlockType.create(
              withId(cropSettings, "flax_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.FLAX));
  public static final Block RED_MUSHROOM_CROP =
      register(
          "red_mushroom_crop",
          MachineBlockType.create(
              withId(cropSettings, "red_mushroom_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.RED_MUSHROOM));
  public static final Block BROWN_MUSHROOM_CROP =
      register(
          "brown_mushroom_crop",
          MachineBlockType.create(
              withId(cropSettings, "brown_mushroom_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.BROWN_MUSHROOM));
  public static final Block NETHER_WART_CROP =
      register(
          "nether_wart_crop",
          MachineBlockType.create(
              withId(cropSettings, "nether_wart_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.NETHER_WART));
  public static final Block TERRA_WART_CROP =
      register(
          "terra_wart_crop",
          MachineBlockType.create(
              withId(cropSettings, "terra_wart_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.TERRA_WART));
  public static final Block OAK_SAPLING_CROP =
      register(
          "oak_sapling_crop",
          MachineBlockType.create(
              withId(cropSettings, "oak_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.OAK_SAPLING));
  public static final Block SPRUCE_SAPLING_CROP =
      register(
          "spruce_sapling_crop",
          MachineBlockType.create(
              withId(cropSettings, "spruce_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.SPRUCE_SAPLING));
  public static final Block BIRCH_SAPLING_CROP =
      register(
          "birch_sapling_crop",
          MachineBlockType.create(
              withId(cropSettings, "birch_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.BIRCH_SAPLING));
  public static final Block JUNGLE_SAPLING_CROP =
      register(
          "jungle_sapling_crop",
          MachineBlockType.create(
              withId(cropSettings, "jungle_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.JUNGLE_SAPLING));
  public static final Block ACACIA_SAPLING_CROP =
      register(
          "acacia_sapling_crop",
          MachineBlockType.create(
              withId(cropSettings, "acacia_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.ACACIA_SAPLING));
  public static final Block DARK_OAK_SAPLING_CROP =
      register(
          "dark_oak_sapling_crop",
          MachineBlockType.create(
              withId(cropSettings, "dark_oak_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.DARK_OAK_SAPLING));
  public static final Block FERRU_CROP =
      register(
          "ferru_crop",
          MachineBlockType.create(
              withId(cropSettings, "ferru_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.FERRU));
  public static final Block CYPRIUM_CROP =
      register(
          "cyprium_crop",
          MachineBlockType.create(
              withId(cropSettings, "cyprium_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.CYPRIUM));
  public static final Block STAGNIUM_CROP =
      register(
          "stagnium_crop",
          MachineBlockType.create(
              withId(cropSettings, "stagnium_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.STAGNIUM));
  public static final Block PLUMBISCUS_CROP =
      register(
          "plumbiscus_crop",
          MachineBlockType.create(
              withId(cropSettings, "plumbiscus_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.PLUMBISCUS));
  public static final Block AURELIA_CROP =
      register(
          "aurelia_crop",
          MachineBlockType.create(
              withId(cropSettings, "aurelia_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.AURELIA));
  public static final Block SHINING_CROP =
      register(
          "shining_crop",
          MachineBlockType.create(
              withId(cropSettings, "shining_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.SHINING));
  public static final Block RED_WHEAT_CROP =
      register(
          "red_wheat_crop",
          MachineBlockType.create(
              withId(cropSettings, "red_wheat_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.RED_WHEAT));
  public static final Block COFFEE_CROP =
      register(
          "coffee_crop",
          MachineBlockType.create(
              withId(cropSettings, "coffee_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.COFFEE));
  public static final Block HOPS_CROP =
      register(
          "hops_crop",
          MachineBlockType.create(
              withId(cropSettings, "hops_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.HOPS));
  public static final Block EATING_PLANT_CROP =
      register(
          "eating_plant_crop",
          MachineBlockType.create(
              withId(cropSettings, "eating_plant_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false,
              Cultivar.EATING_PLANT));
  public static final Block INDUSTRIAL_WORKBENCH =
      register(
          "industrial_workbench",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "industrial_workbench")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              IndustrialWorkbench.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.horizontalFacings,
              true));
  public static final Block BATCH_CRAFTER =
      register(
          "batch_crafter",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("batch_crafter")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              AutomaticCrafter.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              Util.horizontalFacings,
              true));
  public static final Block WOODEN_STORAGE_BOX =
      register(
          "wooden_storage_box",
          MachineBlockType.create(
              BlockProfiles.wood()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "wooden_storage_box")))
                  .strength(1.0F, 10.0F)
                  .sound(SoundType.WOOD),
              TileEntityWoodenStorageBox.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block IRON_STORAGE_BOX =
      register(
          "iron_storage_box",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_storage_box")))
                  .strength(1.0F, 15.0F)
                  .sound(SoundType.METAL),
              TileEntityIronStorageBox.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block BRONZE_STORAGE_BOX =
      register(
          "bronze_storage_box",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "bronze_storage_box")))
                  .strength(1.0F, 15.0F)
                  .sound(SoundType.METAL),
              TileEntityBronzeStorageBox.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block STEEL_STORAGE_BOX =
      register(
          "steel_storage_box",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_storage_box")))
                  .strength(2.0F, 20.0F)
                  .sound(SoundType.METAL),
              TileEntitySteelStorageBox.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block IRIDIUM_STORAGE_BOX =
      register(
          "iridium_storage_box",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(
                              "iridium_storage_box")))
                  .strength(3.0F, 100.0F)
                  .sound(SoundType.METAL),
              TileEntityIridiumStorageBox.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block BRONZE_TANK =
      register(
          "bronze_tank",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_tank")))
                  .strength(3.0F, 15.0F)
                  .sound(SoundType.METAL),
              TileEntityBronzeTank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block IRON_TANK =
      register(
          "iron_tank",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iron_tank")))
                  .strength(3.0F, 15.0F)
                  .sound(SoundType.METAL),
              TileEntityIronTank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block STEEL_TANK =
      register(
          "steel_tank",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("steel_tank")))
                  .strength(4.0F, 20.0F)
                  .sound(SoundType.METAL),
              TileEntitySteelTank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));
  public static final Block IRIDIUM_TANK =
      register(
          "iridium_tank",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("iridium_tank")))
                  .strength(5.0F, 100.0F)
                  .sound(SoundType.METAL),
              TileEntityIridiumTank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              Util.noFacings,
              false));

  public static void init() {}

  /** 26.1.2: blocks need their registry id set on Properties before construction. */
  private static Properties withId(java.util.function.Supplier<Properties> settings, String name) {
    return settings
        .get()
        .setId(
            net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.BLOCK,
                ProjectRuntime.getIdentifier(name)));
  }

  private static <T extends Block> T register(String name, T block) {
    ProjectRuntime.envProxy.registerBlock(ProjectRuntime.getIdentifier(name), block);
    return block;
  }

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

  static {
    ProjectRuntime.envProxy.registerFlammableBlock(RUBBER_LEAVES, 20, 30);
    ProjectRuntime.envProxy.registerFlammableBlock(RUBBER_LOG, 20, 4);
    ProjectRuntime.envProxy.registerFlammableBlock(WOODEN_SCAFFOLD, 8, 20);
    ProjectRuntime.envProxy.registerFlammableBlock(REINFORCED_WOODEN_SCAFFOLD, 8, 20);
  }
}
