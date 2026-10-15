package ic2ce.neoforge.processing;

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
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Processing block entity types, staged in declaration order. */
public final class ProcessingBlockEntities {
  public static final BlockEntityType<SteamCondenser> CONDENSER =
      BlockEntityDefinitions.register("condenser", SteamCondenser::new, ProcessingBlocks.CONDENSER);
  public static final BlockEntityType<LiquidPackager> FLUID_BOTTLER =
      BlockEntityDefinitions.register("fluid_bottler", LiquidPackager::new, ProcessingBlocks.FLUID_BOTTLER);
  public static final BlockEntityType<FluidCollector> PUMP =
      BlockEntityDefinitions.register("pump", FluidCollector::new, ProcessingBlocks.PUMP);
  public static final BlockEntityType<SolarDistiller> SOLAR_DISTILLER =
      BlockEntityDefinitions.register("solar_distiller", SolarDistiller::new, ProcessingBlocks.SOLAR_DISTILLER);
  public static final BlockEntityType<PressureBoiler> STEAM_GENERATOR =
      BlockEntityDefinitions.register("steam_generator", PressureBoiler::new, ProcessingBlocks.STEAM_GENERATOR);
  public static final BlockEntityType<MultiPolicyCanner> CANNER =
      BlockEntityDefinitions.register("canner", MultiPolicyCanner::new, ProcessingBlocks.CANNER);
  public static final BlockEntityType<LegacyCellCanner> CLASSIC_CANNER =
      BlockEntityDefinitions.register("classic_canner", LegacyCellCanner::new, ProcessingBlocks.CLASSIC_CANNER);
  public static final BlockEntityType<PressureCompressor> COMPRESSOR =
      BlockEntityDefinitions.register("compressor", PressureCompressor::new, ProcessingBlocks.COMPRESSOR);
  public static final BlockEntityType<ElectricSmelter> ELECTRIC_FURNACE =
      BlockEntityDefinitions.register("electric_furnace", ElectricSmelter::new, ProcessingBlocks.ELECTRIC_FURNACE);
  public static final BlockEntityType<MaterialExtractor> EXTRACTOR =
      BlockEntityDefinitions.register("extractor", MaterialExtractor::new, ProcessingBlocks.EXTRACTOR);
  public static final BlockEntityType<FuelSmelter> IRON_FURNACE =
      BlockEntityDefinitions.register("iron_furnace", FuelSmelter::new, ProcessingBlocks.IRON_FURNACE);
  public static final BlockEntityType<OreGrinder> MACERATOR =
      BlockEntityDefinitions.register("macerator", OreGrinder::new, ProcessingBlocks.MACERATOR);
  public static final BlockEntityType<MaterialRecycler> RECYCLER =
      BlockEntityDefinitions.register("recycler", MaterialRecycler::new, ProcessingBlocks.RECYCLER);
  public static final BlockEntityType<SolidPackager> SOLID_CANNER =
      BlockEntityDefinitions.register("solid_canner", SolidPackager::new, ProcessingBlocks.SOLID_CANNER);
  public static final BlockEntityType<AlloyFurnace> BLAST_FURNACE =
      BlockEntityDefinitions.register("blast_furnace", AlloyFurnace::new, ProcessingBlocks.BLAST_FURNACE);
  public static final BlockEntityType<PlateCutter> BLOCK_CUTTER =
      BlockEntityDefinitions.register("block_cutter", PlateCutter::new, ProcessingBlocks.BLOCK_CUTTER);
  public static final BlockEntityType<ThermalCentrifuge> CENTRIFUGE =
      BlockEntityDefinitions.register("centrifuge", ThermalCentrifuge::new, ProcessingBlocks.CENTRIFUGE);
  public static final BlockEntityType<BiomassFermenter> FERMENTER =
      BlockEntityDefinitions.register("fermenter", BiomassFermenter::new, ProcessingBlocks.FERMENTER);
  public static final BlockEntityType<InductionSmelter> INDUCTION_FURNACE =
      BlockEntityDefinitions.register("induction_furnace", InductionSmelter::new, ProcessingBlocks.INDUCTION_FURNACE);
  public static final BlockEntityType<MetalFormer> METAL_FORMER =
      BlockEntityDefinitions.register("metal_former", MetalFormer::new, ProcessingBlocks.METAL_FORMER);
  public static final BlockEntityType<OreWasher> ORE_WASHING_PLANT =
      BlockEntityDefinitions.register("ore_washing_plant", OreWasher::new, ProcessingBlocks.ORE_WASHING_PLANT);
  public static final BlockEntityType<AmplifiedMatterFabricator> MASS_FABRICATOR =
      BlockEntityDefinitions.register("mass_fabricator", AmplifiedMatterFabricator::new, ProcessingBlocks.MASS_FABRICATOR);
  public static final BlockEntityType<ClassicMatterFabricator> CLASSIC_MASS_FABRICATOR =
      BlockEntityDefinitions.register(
          "classic_mass_fabricator",
          ClassicMatterFabricator::new,
          ProcessingBlocks.CLASSIC_MASS_FABRICATOR);
  public static final BlockEntityType<LiquidMatterFabricator> MATTER_GENERATOR =
      BlockEntityDefinitions.register("matter_generator", LiquidMatterFabricator::new, ProcessingBlocks.MATTER_GENERATOR);
  public static final BlockEntityType<PatternLibrary> PATTERN_STORAGE =
      BlockEntityDefinitions.register("pattern_storage", PatternLibrary::new, ProcessingBlocks.PATTERN_STORAGE);
  public static final BlockEntityType<PatternReplicator> REPLICATOR =
      BlockEntityDefinitions.register("replicator", PatternReplicator::new, ProcessingBlocks.REPLICATOR);
  public static final BlockEntityType<PatternScanner> UU_SCANNER =
      BlockEntityDefinitions.register("uu_scanner", PatternScanner::new, ProcessingBlocks.UU_SCANNER);
  public static final BlockEntityType<FluidElectrolyzer> ELECTROLYZER =
      BlockEntityDefinitions.register("electrolyzer", FluidElectrolyzer::new, ProcessingBlocks.ELECTROLYZER);
  public static final BlockEntityType<CellElectrolyzer> CLASSIC_ELECTROLYZER =
      BlockEntityDefinitions.register(
          "classic_electrolyzer", CellElectrolyzer::new, ProcessingBlocks.CLASSIC_ELECTROLYZER);
  public static final BlockEntityType<IntegrationSteamConverter> STEAM_REPRESSURIZER =
      BlockEntityDefinitions.register(
          "steam_repressurizer",
          IntegrationSteamConverter::new,
          ProcessingBlocks.STEAM_REPRESSURIZER);

  private ProcessingBlockEntities() {}

  public static void init() {}
}
