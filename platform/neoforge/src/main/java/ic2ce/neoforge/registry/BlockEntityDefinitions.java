package ic2ce.neoforge.registry;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.storage.box.TileEntityBronzeStorageBox;
import ic2ce.neoforge.storage.box.TileEntityIridiumStorageBox;
import ic2ce.neoforge.storage.box.TileEntityIronStorageBox;
import ic2ce.neoforge.storage.box.TileEntitySteelStorageBox;
import ic2ce.neoforge.storage.box.TileEntityWoodenStorageBox;
import ic2ce.neoforge.storage.tank.TileEntityBronzeTank;
import ic2ce.neoforge.storage.tank.TileEntityIridiumTank;
import ic2ce.neoforge.storage.tank.TileEntityIronTank;
import ic2ce.neoforge.storage.tank.TileEntitySteelTank;
import ic2ce.neoforge.agriculture.AutoHarvester;
import ic2ce.neoforge.agriculture.CellCropCaretaker;
import ic2ce.neoforge.agriculture.CropPlot;
import ic2ce.neoforge.agriculture.FluidCropCaretaker;
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
import ic2ce.neoforge.utility.LinkedTeleporter;
import ic2ce.neoforge.utility.PoleDrive;
import ic2ce.neoforge.world.block.ImprintedWall;
import ic2ce.neoforge.world.block.RubberSignEntity;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityDefinitions {
  private static final java.util.concurrent.ConcurrentHashMap<Identifier, BlockEntityType<?>>
      TYPES = new java.util.concurrent.ConcurrentHashMap<>();

  public static final BlockEntityType<RubberSignEntity> SIGN =
      register(
          "sign",
          RubberSignEntity::new,
          BlockDefinitions.RUBBER_SIGN,
          BlockDefinitions.RUBBER_WALL_SIGN);
  public static final BlockEntityType<ImprintedWall> WALL =
      register("wall", ImprintedWall::new, BlockDefinitions.OBSCURED_WALL);
  public static final BlockEntityType<DemolitionBlockEntity> ITNT =
      register("itnt", DemolitionBlockEntity::new, BlockDefinitions.ITNT);
  public static final BlockEntityType<ConfiguredNuclearCharge> NUKE =
      register("nuke", ConfiguredNuclearCharge::new, BlockDefinitions.NUKE);
  public static final BlockEntityType<NuclearBlockEntity.ClassicNuclearCharge> CLASSIC_NUKE =
      register(
          "classic_nuke",
          NuclearBlockEntity.ClassicNuclearCharge::new,
          BlockDefinitions.CLASSIC_NUKE);
  public static final BlockEntityType<CombustionGenerator> GENERATOR =
      register("generator", CombustionGenerator::new, BlockDefinitions.GENERATOR);
  public static final BlockEntityType<GeothermalGenerator> GEO_GENERATOR =
      register("geo_generator", GeothermalGenerator::new, BlockDefinitions.GEO_GENERATOR);
  public static final BlockEntityType<KineticConverter> KINETIC_GENERATOR =
      register("kinetic_generator", KineticConverter::new, BlockDefinitions.KINETIC_GENERATOR);
  public static final BlockEntityType<RadioisotopeGenerator> RT_GENERATOR =
      register("rt_generator", RadioisotopeGenerator::new, BlockDefinitions.RT_GENERATOR);
  public static final BlockEntityType<BiogasGenerator> SEMIFLUID_GENERATOR =
      register("semifluid_generator", BiogasGenerator::new, BlockDefinitions.SEMIFLUID_GENERATOR);
  public static final BlockEntityType<SolarCollector> SOLAR_GENERATOR =
      register("solar_generator", SolarCollector::new, BlockDefinitions.SOLAR_GENERATOR);
  public static final BlockEntityType<ThermalConverter> STIRLING_GENERATOR =
      register("stirling_generator", ThermalConverter::new, BlockDefinitions.STIRLING_GENERATOR);
  public static final BlockEntityType<WaterMill> WATER_GENERATOR =
      register("water_generator", WaterMill::new, BlockDefinitions.WATER_GENERATOR);
  public static final BlockEntityType<WindMill> WIND_GENERATOR =
      register("wind_generator", WindMill::new, BlockDefinitions.WIND_GENERATOR);
  public static final BlockEntityType<ElectricHeater> ELECTRIC_HEAT_GENERATOR =
      register(
          "electric_heat_generator", ElectricHeater::new, BlockDefinitions.ELECTRIC_HEAT_GENERATOR);
  public static final BlockEntityType<FluidBurner> FLUID_HEAT_GENERATOR =
      register("fluid_heat_generator", FluidBurner::new, BlockDefinitions.FLUID_HEAT_GENERATOR);
  public static final BlockEntityType<RadioisotopeHeater> RT_HEAT_GENERATOR =
      register("rt_heat_generator", RadioisotopeHeater::new, BlockDefinitions.RT_HEAT_GENERATOR);
  public static final BlockEntityType<CombustionHeater> SOLID_HEAT_GENERATOR =
      register(
          "solid_heat_generator", CombustionHeater::new, BlockDefinitions.SOLID_HEAT_GENERATOR);
  public static final BlockEntityType<ElectricMotor> ELECTRIC_KINETIC_GENERATOR =
      register(
          "electric_kinetic_generator",
          ElectricMotor::new,
          BlockDefinitions.ELECTRIC_KINETIC_GENERATOR);
  public static final BlockEntityType<ManualCrank> MANUAL_KINETIC_GENERATOR =
      register(
          "manual_kinetic_generator", ManualCrank::new, BlockDefinitions.MANUAL_KINETIC_GENERATOR);
  public static final BlockEntityType<SteamTurbine> STEAM_KINETIC_GENERATOR =
      register(
          "steam_kinetic_generator", SteamTurbine::new, BlockDefinitions.STEAM_KINETIC_GENERATOR);
  public static final BlockEntityType<HeatDrivenCogenerator> STIRLING_KINETIC_GENERATOR =
      register(
          "stirling_kinetic_generator",
          HeatDrivenCogenerator::new,
          BlockDefinitions.STIRLING_KINETIC_GENERATOR);
  public static final BlockEntityType<WaterTurbine> WATER_KINETIC_GENERATOR =
      register(
          "water_kinetic_generator", WaterTurbine::new, BlockDefinitions.WATER_KINETIC_GENERATOR);
  public static final BlockEntityType<WindTurbine> WIND_KINETIC_GENERATOR =
      register("wind_kinetic_generator", WindTurbine::new, BlockDefinitions.WIND_KINETIC_GENERATOR);
  public static final BlockEntityType<NuclearCore> NUCLEAR_REACTOR =
      register("nuclear_reactor", NuclearCore::new, BlockDefinitions.NUCLEAR_REACTOR);
  public static final BlockEntityType<VesselHatch> REACTOR_ACCESS_HATCH =
      register("reactor_access_hatch", VesselHatch::new, BlockDefinitions.REACTOR_ACCESS_HATCH);
  public static final BlockEntityType<ReactorChamber> REACTOR_CHAMBER =
      register("reactor_chamber", ReactorChamber::new, BlockDefinitions.REACTOR_CHAMBER);
  public static final BlockEntityType<CoolantPort> REACTOR_FLUID_PORT =
      register("reactor_fluid_port", CoolantPort::new, BlockDefinitions.REACTOR_FLUID_PORT);
  public static final BlockEntityType<SignalPort> REACTOR_REDSTONE_PORT =
      register("reactor_redstone_port", SignalPort::new, BlockDefinitions.REACTOR_REDSTONE_PORT);
  public static final BlockEntityType<SteamCondenser> CONDENSER =
      register("condenser", SteamCondenser::new, BlockDefinitions.CONDENSER);
  public static final BlockEntityType<LiquidPackager> FLUID_BOTTLER =
      register("fluid_bottler", LiquidPackager::new, BlockDefinitions.FLUID_BOTTLER);
  public static final BlockEntityType<FairFluidRouter> FLUID_DISTRIBUTOR =
      register("fluid_distributor", FairFluidRouter::new, BlockDefinitions.FLUID_DISTRIBUTOR);
  public static final BlockEntityType<MeteredFluidOutlet> FLUID_REGULATOR =
      register("fluid_regulator", MeteredFluidOutlet::new, BlockDefinitions.FLUID_REGULATOR);
  public static final BlockEntityType<FluidHeatExchanger> LIQUID_HEAT_EXCHANGER =
      register(
          "liquid_heat_exchanger", FluidHeatExchanger::new, BlockDefinitions.LIQUID_HEAT_EXCHANGER);
  public static final BlockEntityType<FluidCollector> PUMP =
      register("pump", FluidCollector::new, BlockDefinitions.PUMP);
  public static final BlockEntityType<SolarDistiller> SOLAR_DISTILLER =
      register("solar_distiller", SolarDistiller::new, BlockDefinitions.SOLAR_DISTILLER);
  public static final BlockEntityType<PressureBoiler> STEAM_GENERATOR =
      register("steam_generator", PressureBoiler::new, BlockDefinitions.STEAM_GENERATOR);
  public static final BlockEntityType<DualItemBuffer> ITEM_BUFFER =
      register("item_buffer", DualItemBuffer::new, BlockDefinitions.ITEM_BUFFER);
  public static final BlockEntityType<PoleDrive> MAGNETIZER =
      register("magnetizer", PoleDrive::new, BlockDefinitions.MAGNETIZER);
  public static final BlockEntityType<BatchItemSorter> SORTING_MACHINE =
      register("sorting_machine", BatchItemSorter::new, BlockDefinitions.SORTING_MACHINE);
  public static final BlockEntityType<LinkedTeleporter> TELEPORTER =
      register("teleporter", LinkedTeleporter::new, BlockDefinitions.TELEPORTER);
  public static final BlockEntityType<TerrainController> TERRAFORMER =
      register("terraformer", TerrainController::new, BlockDefinitions.TERRAFORMER);
  public static final BlockEntityType<ArcCoil> TESLA_COIL =
      register("tesla_coil", ArcCoil::new, BlockDefinitions.TESLA_COIL);
  public static final BlockEntityType<MultiPolicyCanner> CANNER =
      register("canner", MultiPolicyCanner::new, BlockDefinitions.CANNER);
  public static final BlockEntityType<LegacyCellCanner> CLASSIC_CANNER =
      register("classic_canner", LegacyCellCanner::new, BlockDefinitions.CLASSIC_CANNER);
  public static final BlockEntityType<PressureCompressor> COMPRESSOR =
      register("compressor", PressureCompressor::new, BlockDefinitions.COMPRESSOR);
  public static final BlockEntityType<ElectricSmelter> ELECTRIC_FURNACE =
      register("electric_furnace", ElectricSmelter::new, BlockDefinitions.ELECTRIC_FURNACE);
  public static final BlockEntityType<MaterialExtractor> EXTRACTOR =
      register("extractor", MaterialExtractor::new, BlockDefinitions.EXTRACTOR);
  public static final BlockEntityType<FuelSmelter> IRON_FURNACE =
      register("iron_furnace", FuelSmelter::new, BlockDefinitions.IRON_FURNACE);
  public static final BlockEntityType<OreGrinder> MACERATOR =
      register("macerator", OreGrinder::new, BlockDefinitions.MACERATOR);
  public static final BlockEntityType<MaterialRecycler> RECYCLER =
      register("recycler", MaterialRecycler::new, BlockDefinitions.RECYCLER);
  public static final BlockEntityType<SolidPackager> SOLID_CANNER =
      register("solid_canner", SolidPackager::new, BlockDefinitions.SOLID_CANNER);
  public static final BlockEntityType<AlloyFurnace> BLAST_FURNACE =
      register("blast_furnace", AlloyFurnace::new, BlockDefinitions.BLAST_FURNACE);
  public static final BlockEntityType<PlateCutter> BLOCK_CUTTER =
      register("block_cutter", PlateCutter::new, BlockDefinitions.BLOCK_CUTTER);
  public static final BlockEntityType<ThermalCentrifuge> CENTRIFUGE =
      register("centrifuge", ThermalCentrifuge::new, BlockDefinitions.CENTRIFUGE);
  public static final BlockEntityType<BiomassFermenter> FERMENTER =
      register("fermenter", BiomassFermenter::new, BlockDefinitions.FERMENTER);
  public static final BlockEntityType<InductionSmelter> INDUCTION_FURNACE =
      register("induction_furnace", InductionSmelter::new, BlockDefinitions.INDUCTION_FURNACE);
  public static final BlockEntityType<MetalFormer> METAL_FORMER =
      register("metal_former", MetalFormer::new, BlockDefinitions.METAL_FORMER);
  public static final BlockEntityType<OreWasher> ORE_WASHING_PLANT =
      register("ore_washing_plant", OreWasher::new, BlockDefinitions.ORE_WASHING_PLANT);
  public static final BlockEntityType<AreaMiner> ADVANCED_MINER =
      register("advanced_miner", AreaMiner::new, BlockDefinitions.ADVANCED_MINER);
  public static final BlockEntityType<AutoHarvester> CROP_HARVESTER =
      register("crop_harvester", AutoHarvester::new, BlockDefinitions.CROP_HARVESTER);
  public static final BlockEntityType<FluidCropCaretaker> CROPMATRON =
      register("cropmatron", FluidCropCaretaker::new, BlockDefinitions.CROPMATRON);
  public static final BlockEntityType<CellCropCaretaker> CLASSIC_CROPMATRON =
      register("classic_cropmatron", CellCropCaretaker::new, BlockDefinitions.CLASSIC_CROPMATRON);
  public static final BlockEntityType<PipeMiner> MINER =
      register("miner", PipeMiner::new, BlockDefinitions.MINER);
  public static final BlockEntityType<AmplifiedMatterFabricator> MASS_FABRICATOR =
      register("mass_fabricator", AmplifiedMatterFabricator::new, BlockDefinitions.MASS_FABRICATOR);
  public static final BlockEntityType<ClassicMatterFabricator> CLASSIC_MASS_FABRICATOR =
      register(
          "classic_mass_fabricator",
          ClassicMatterFabricator::new,
          BlockDefinitions.CLASSIC_MASS_FABRICATOR);
  public static final BlockEntityType<UuAssembler> UU_ASSEMBLY_BENCH =
      register("uu_assembly_bench", UuAssembler::new, BlockDefinitions.UU_ASSEMBLY_BENCH);
  public static final BlockEntityType<LiquidMatterFabricator> MATTER_GENERATOR =
      register("matter_generator", LiquidMatterFabricator::new, BlockDefinitions.MATTER_GENERATOR);
  public static final BlockEntityType<PatternLibrary> PATTERN_STORAGE =
      register("pattern_storage", PatternLibrary::new, BlockDefinitions.PATTERN_STORAGE);
  public static final BlockEntityType<PatternReplicator> REPLICATOR =
      register("replicator", PatternReplicator::new, BlockDefinitions.REPLICATOR);
  public static final BlockEntityType<PatternScanner> UU_SCANNER =
      register("uu_scanner", PatternScanner::new, BlockDefinitions.UU_SCANNER);
  public static final BlockEntityType<EnergyTradeTerminal> ENERGY_O_MAT =
      register("energy_o_mat", EnergyTradeTerminal::new, BlockDefinitions.ENERGY_O_MAT);
  public static final BlockEntityType<PrivateChest> PERSONAL_CHEST =
      register("personal_chest", PrivateChest::new, BlockDefinitions.PERSONAL_CHEST);
  public static final BlockEntityType<ItemTradeTerminal> TRADE_O_MAT =
      register("trade_o_mat", ItemTradeTerminal::new, BlockDefinitions.TRADE_O_MAT);
  public static final BlockEntityType<LowVoltagePad> BATBOX_CHARGEPAD =
      register("batbox_chargepad", LowVoltagePad::new, BlockDefinitions.BATBOX_CHARGEPAD);
  public static final BlockEntityType<MediumVoltagePad> CESU_CHARGEPAD =
      register("cesu_chargepad", MediumVoltagePad::new, BlockDefinitions.CESU_CHARGEPAD);
  public static final BlockEntityType<HighVoltagePad> MFE_CHARGEPAD =
      register("mfe_chargepad", HighVoltagePad::new, BlockDefinitions.MFE_CHARGEPAD);
  public static final BlockEntityType<ExtremeVoltagePad> MFSU_CHARGEPAD =
      register("mfsu_chargepad", ExtremeVoltagePad::new, BlockDefinitions.MFSU_CHARGEPAD);
  public static final BlockEntityType<LowVoltageBank> BATBOX =
      register("batbox", LowVoltageBank::new, BlockDefinitions.BATBOX);
  public static final BlockEntityType<MediumVoltageBank> CESU =
      register("cesu", MediumVoltageBank::new, BlockDefinitions.CESU);
  public static final BlockEntityType<HighVoltageBank> MFE =
      register("mfe", HighVoltageBank::new, BlockDefinitions.MFE);
  public static final BlockEntityType<HighVoltageBank.TileEntityElectricClassicMFE> CLASSIC_MFE =
      register(
          "classic_mfe",
          HighVoltageBank.TileEntityElectricClassicMFE::new,
          BlockDefinitions.CLASSIC_MFE);
  public static final BlockEntityType<ExtremeVoltageBank> MFSU =
      register("mfsu", ExtremeVoltageBank::new, BlockDefinitions.MFSU);
  public static final BlockEntityType<ExtremeVoltageBank.TileEntityElectricClassicMFSU>
      CLASSIC_MFSU =
          register(
              "classic_mfsu",
              ExtremeVoltageBank.TileEntityElectricClassicMFSU::new,
              BlockDefinitions.CLASSIC_MFSU);
  public static final BlockEntityType<FluidElectrolyzer> ELECTROLYZER =
      register("electrolyzer", FluidElectrolyzer::new, BlockDefinitions.ELECTROLYZER);
  public static final BlockEntityType<CellElectrolyzer> CLASSIC_ELECTROLYZER =
      register(
          "classic_electrolyzer", CellElectrolyzer::new, BlockDefinitions.CLASSIC_ELECTROLYZER);
  public static final BlockEntityType<LowVoltageTransformer> LV_TRANSFORMER =
      register("lv_transformer", LowVoltageTransformer::new, BlockDefinitions.LV_TRANSFORMER);
  public static final BlockEntityType<MediumVoltageTransformer> MV_TRANSFORMER =
      register("mv_transformer", MediumVoltageTransformer::new, BlockDefinitions.MV_TRANSFORMER);
  public static final BlockEntityType<HighVoltageTransformer> HV_TRANSFORMER =
      register("hv_transformer", HighVoltageTransformer::new, BlockDefinitions.HV_TRANSFORMER);
  public static final BlockEntityType<ExtremeVoltageTransformer> EV_TRANSFORMER =
      register("ev_transformer", ExtremeVoltageTransformer::new, BlockDefinitions.EV_TRANSFORMER);
  public static final BlockEntityType<StationaryFluidStore> TANK =
      register("tank", StationaryFluidStore::new, BlockDefinitions.TANK);
  public static final BlockEntityType<ChunkAnchor> CHUNK_LOADER =
      register("chunk_loader", ChunkAnchor::new, BlockDefinitions.CHUNK_LOADER);
  public static final BlockEntityType<CreativePowerSource> CREATIVE_GENERATOR =
      register("creative_generator", CreativePowerSource::new, BlockDefinitions.CREATIVE_GENERATOR);
  public static final BlockEntityType<IntegrationSteamConverter> STEAM_REPRESSURIZER =
      register(
          "steam_repressurizer",
          IntegrationSteamConverter::new,
          BlockDefinitions.STEAM_REPRESSURIZER);
  public static final BlockEntityType<PriorityFluidRouter> WEIGHTED_FLUID_DISTRIBUTOR =
      register(
          "weighted_fluid_distributor",
          PriorityFluidRouter::new,
          BlockDefinitions.WEIGHTED_FLUID_DISTRIBUTOR);
  public static final BlockEntityType<PriorityItemRouter> WEIGHTED_ITEM_DISTRIBUTOR =
      register(
          "weighted_item_distributor",
          PriorityItemRouter::new,
          BlockDefinitions.WEIGHTED_ITEM_DISTRIBUTOR);
  public static final BlockEntityType<RedstoneCondensatorService> RCI_RSH =
      register("rci_rsh", RedstoneCondensatorService::new, BlockDefinitions.RCI_RSH);
  public static final BlockEntityType<LapisCondensatorService> RCI_LZH =
      register("rci_lzh", LapisCondensatorService::new, BlockDefinitions.RCI_LZH);
  public static final BlockEntityType<CropPlot> CROP_STICK =
      register("crop_stick", CropPlot::new, BlockDefinitions.CROP_STICK);
  public static final BlockEntityType<CropPlot> WEED_CROP =
      register("weed_crop", CropPlot::new, BlockDefinitions.WEED_CROP);
  public static final BlockEntityType<CropPlot> WHEAT_CROP =
      register("wheat_crop", CropPlot::new, BlockDefinitions.WHEAT_CROP);
  public static final BlockEntityType<CropPlot> CARROTS_CROP =
      register("carrots_crop", CropPlot::new, BlockDefinitions.CARROTS_CROP);
  public static final BlockEntityType<CropPlot> POTATO_CROP =
      register("potato_crop", CropPlot::new, BlockDefinitions.POTATO_CROP);
  public static final BlockEntityType<CropPlot> BEETROOTS_CROP =
      register("beetroots_crop", CropPlot::new, BlockDefinitions.BEETROOTS_CROP);
  public static final BlockEntityType<CropPlot> PUMPKIN_CROP =
      register("pumpkin_crop", CropPlot::new, BlockDefinitions.PUMPKIN_CROP);
  public static final BlockEntityType<CropPlot> MELON_CROP =
      register("melon_crop", CropPlot::new, BlockDefinitions.MELON_CROP);
  public static final BlockEntityType<CropPlot> DANDELION_CROP =
      register("dandelion_crop", CropPlot::new, BlockDefinitions.DANDELION_CROP);
  public static final BlockEntityType<CropPlot> POPPY_CROP =
      register("poppy_crop", CropPlot::new, BlockDefinitions.POPPY_CROP);
  public static final BlockEntityType<CropPlot> BLACKTHORN_CROP =
      register("blackthorn_crop", CropPlot::new, BlockDefinitions.BLACKTHORN_CROP);
  public static final BlockEntityType<CropPlot> TULIP_CROP =
      register("tulip_crop", CropPlot::new, BlockDefinitions.TULIP_CROP);
  public static final BlockEntityType<CropPlot> CYAZINT_CROP =
      register("cyazint_crop", CropPlot::new, BlockDefinitions.CYAZINT_CROP);
  public static final BlockEntityType<CropPlot> VENOMILIA_CROP =
      register("venomilia_crop", CropPlot::new, BlockDefinitions.VENOMILIA_CROP);
  public static final BlockEntityType<CropPlot> REED_CROP =
      register("reed_crop", CropPlot::new, BlockDefinitions.REED_CROP);
  public static final BlockEntityType<CropPlot> STICKY_REED_CROP =
      register("sticky_reed_crop", CropPlot::new, BlockDefinitions.STICKY_REED_CROP);
  public static final BlockEntityType<CropPlot> COCOA_CROP =
      register("cocoa_crop", CropPlot::new, BlockDefinitions.COCOA_CROP);
  public static final BlockEntityType<CropPlot> FLAX_CROP =
      register("flax_crop", CropPlot::new, BlockDefinitions.FLAX_CROP);
  public static final BlockEntityType<CropPlot> RED_MUSHROOM_CROP =
      register("red_mushroom_crop", CropPlot::new, BlockDefinitions.RED_MUSHROOM_CROP);
  public static final BlockEntityType<CropPlot> BROWN_MUSHROOM_CROP =
      register("brown_mushroom_crop", CropPlot::new, BlockDefinitions.BROWN_MUSHROOM_CROP);
  public static final BlockEntityType<CropPlot> NETHER_WART_CROP =
      register("nether_wart_crop", CropPlot::new, BlockDefinitions.NETHER_WART_CROP);
  public static final BlockEntityType<CropPlot> TERRA_WART_CROP =
      register("terra_wart_crop", CropPlot::new, BlockDefinitions.TERRA_WART_CROP);
  public static final BlockEntityType<CropPlot> OAK_SAPLING_CROP =
      register("oak_sapling_crop", CropPlot::new, BlockDefinitions.OAK_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> SPRUCE_SAPLING_CROP =
      register("spruce_sapling_crop", CropPlot::new, BlockDefinitions.SPRUCE_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> BIRCH_SAPLING_CROP =
      register("birch_sapling_crop", CropPlot::new, BlockDefinitions.BIRCH_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> JUNGLE_SAPLING_CROP =
      register("jungle_sapling_crop", CropPlot::new, BlockDefinitions.JUNGLE_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> ACACIA_SAPLING_CROP =
      register("acacia_sapling_crop", CropPlot::new, BlockDefinitions.ACACIA_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> DARK_OAK_SAPLING_CROP =
      register("dark_oak_sapling_crop", CropPlot::new, BlockDefinitions.DARK_OAK_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> FERRU_CROP =
      register("ferru_crop", CropPlot::new, BlockDefinitions.FERRU_CROP);
  public static final BlockEntityType<CropPlot> CYPRIUM_CROP =
      register("cyprium_crop", CropPlot::new, BlockDefinitions.CYPRIUM_CROP);
  public static final BlockEntityType<CropPlot> STAGNIUM_CROP =
      register("stagnium_crop", CropPlot::new, BlockDefinitions.STAGNIUM_CROP);
  public static final BlockEntityType<CropPlot> PLUMBISCUS_CROP =
      register("plumbiscus_crop", CropPlot::new, BlockDefinitions.PLUMBISCUS_CROP);
  public static final BlockEntityType<CropPlot> AURELIA_CROP =
      register("aurelia_crop", CropPlot::new, BlockDefinitions.AURELIA_CROP);
  public static final BlockEntityType<CropPlot> SHINING_CROP =
      register("shining_crop", CropPlot::new, BlockDefinitions.SHINING_CROP);
  public static final BlockEntityType<CropPlot> RED_WHEAT_CROP =
      register("red_wheat_crop", CropPlot::new, BlockDefinitions.RED_WHEAT_CROP);
  public static final BlockEntityType<CropPlot> COFFEE_CROP =
      register("coffee_crop", CropPlot::new, BlockDefinitions.COFFEE_CROP);
  public static final BlockEntityType<CropPlot> HOPS_CROP =
      register("hops_crop", CropPlot::new, BlockDefinitions.HOPS_CROP);
  public static final BlockEntityType<CropPlot> EATING_PLANT_CROP =
      register("eating_plant_crop", CropPlot::new, BlockDefinitions.EATING_PLANT_CROP);
  public static final BlockEntityType<IndustrialWorkbench> INDUSTRIAL_WORKBENCH =
      register(
          "industrial_workbench", IndustrialWorkbench::new, BlockDefinitions.INDUSTRIAL_WORKBENCH);
  public static final BlockEntityType<AutomaticCrafter> BATCH_CRAFTER =
      register("batch_crafter", AutomaticCrafter::new, BlockDefinitions.BATCH_CRAFTER);
  public static final BlockEntityType<TileEntityWoodenStorageBox> WOODEN_STORAGE_BOX =
      register(
          "wooden_storage_box",
          TileEntityWoodenStorageBox::new,
          BlockDefinitions.WOODEN_STORAGE_BOX);
  public static final BlockEntityType<TileEntityIronStorageBox> IRON_STORAGE_BOX =
      register(
          "iron_storage_box", TileEntityIronStorageBox::new, BlockDefinitions.IRON_STORAGE_BOX);
  public static final BlockEntityType<TileEntityBronzeStorageBox> BRONZE_STORAGE_BOX =
      register(
          "bronze_storage_box",
          TileEntityBronzeStorageBox::new,
          BlockDefinitions.BRONZE_STORAGE_BOX);
  public static final BlockEntityType<TileEntitySteelStorageBox> STEEL_STORAGE_BOX =
      register(
          "steel_storage_box", TileEntitySteelStorageBox::new, BlockDefinitions.STEEL_STORAGE_BOX);
  public static final BlockEntityType<TileEntityIridiumStorageBox> IRIDIUM_STORAGE_BOX =
      register(
          "iridium_storage_box",
          TileEntityIridiumStorageBox::new,
          BlockDefinitions.IRIDIUM_STORAGE_BOX);
  public static final BlockEntityType<TileEntityBronzeTank> BRONZE_TANK =
      register("bronze_tank", TileEntityBronzeTank::new, BlockDefinitions.BRONZE_TANK);
  public static final BlockEntityType<TileEntityIronTank> IRON_TANK =
      register("iron_tank", TileEntityIronTank::new, BlockDefinitions.IRON_TANK);
  public static final BlockEntityType<TileEntitySteelTank> STEEL_TANK =
      register("steel_tank", TileEntitySteelTank::new, BlockDefinitions.STEEL_TANK);
  public static final BlockEntityType<TileEntityIridiumTank> IRIDIUM_TANK =
      register("iridium_tank", TileEntityIridiumTank::new, BlockDefinitions.IRIDIUM_TANK);

  public static void init() {}

  private static <T extends BlockEntity> BlockEntityType<T> register(
      String name, BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
    var id = ProjectIds.id(name);
    var type = EnvProxy.get().registerBlockEntity(id, factory, blocks);
    if (TYPES.putIfAbsent(id, type) != null)
      throw new IllegalStateException("Repeated project block entity: " + id);
    return type;
  }

  public static BlockEntityType<?> get(Identifier id) {
    return id == null ? null : TYPES.get(id);
  }
}
