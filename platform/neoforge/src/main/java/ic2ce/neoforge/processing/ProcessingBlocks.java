package ic2ce.neoforge.processing;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
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
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Processing blocks, staged in declaration order. */
public final class ProcessingBlocks {
  public static final Block CONDENSER =
      BlockDefinitions.register(
          "condenser",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("condenser")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SteamCondenser.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block FLUID_BOTTLER =
      BlockDefinitions.register(
          "fluid_bottler",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("fluid_bottler")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LiquidPackager.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block PUMP =
      BlockDefinitions.register(
          "pump",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("pump")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FluidCollector.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));
  public static final Block SOLAR_DISTILLER =
      BlockDefinitions.register(
          "solar_distiller",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("solar_distiller")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SolarDistiller.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.noFacings,
              false));
  public static final Block STEAM_GENERATOR =
      BlockDefinitions.register(
          "steam_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("steam_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PressureBoiler.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              true));
  public static final Block CANNER =
      BlockDefinitions.register(
          "canner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("canner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MultiPolicyCanner.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block CLASSIC_CANNER =
      BlockDefinitions.register(
          "classic_canner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("classic_canner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LegacyCellCanner.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final MachineBlockType COMPRESSOR =
      BlockDefinitions.register(
          "compressor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("compressor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PressureCompressor.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block ELECTRIC_FURNACE =
      BlockDefinitions.register(
          "electric_furnace",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("electric_furnace")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ElectricSmelter.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              true));
  public static final MachineBlockType EXTRACTOR =
      BlockDefinitions.register(
          "extractor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("extractor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MaterialExtractor.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block IRON_FURNACE =
      BlockDefinitions.register(
          "iron_furnace",
          MachineBlockType.create(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("iron_furnace")))
                  .strength(5.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FuelSmelter.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              false));
  public static final MachineBlockType MACERATOR =
      BlockDefinitions.register(
          "macerator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("macerator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              OreGrinder.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block RECYCLER =
      BlockDefinitions.register(
          "recycler",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("recycler")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MaterialRecycler.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block SOLID_CANNER =
      BlockDefinitions.register(
          "solid_canner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("solid_canner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SolidPackager.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final MachineBlockType BLAST_FURNACE =
      BlockDefinitions.register(
          "blast_furnace",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("blast_furnace")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              AlloyFurnace.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));
  public static final MachineBlockType BLOCK_CUTTER =
      BlockDefinitions.register(
          "block_cutter",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("block_cutter")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PlateCutter.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.horizontalFacings,
              true));
  public static final MachineBlockType CENTRIFUGE =
      BlockDefinitions.register(
          "centrifuge",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("centrifuge")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ThermalCentrifuge.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block FERMENTER =
      BlockDefinitions.register(
          "fermenter",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("fermenter")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              BiomassFermenter.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));
  public static final Block INDUCTION_FURNACE =
      BlockDefinitions.register(
          "induction_furnace",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("induction_furnace")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              InductionSmelter.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.horizontalFacings,
              true));
  public static final MachineBlockType METAL_FORMER =
      BlockDefinitions.register(
          "metal_former",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("metal_former")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MetalFormer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final MachineBlockType ORE_WASHING_PLANT =
      BlockDefinitions.register(
          "ore_washing_plant",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("ore_washing_plant")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              OreWasher.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block MASS_FABRICATOR =
      BlockDefinitions.register(
          "mass_fabricator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("mass_fabricator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              AmplifiedMatterFabricator.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block CLASSIC_MASS_FABRICATOR =
      BlockDefinitions.register(
          "classic_mass_fabricator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "classic_mass_fabricator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ClassicMatterFabricator.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block MATTER_GENERATOR =
      BlockDefinitions.register(
          "matter_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("matter_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LiquidMatterFabricator.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block PATTERN_STORAGE =
      BlockDefinitions.register(
          "pattern_storage",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("pattern_storage")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PatternLibrary.class,
              false,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block REPLICATOR =
      BlockDefinitions.register(
          "replicator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("replicator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PatternReplicator.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block UU_SCANNER =
      BlockDefinitions.register(
          "uu_scanner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("uu_scanner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PatternScanner.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block ELECTROLYZER =
      BlockDefinitions.register(
          "electrolyzer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("electrolyzer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FluidElectrolyzer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block CLASSIC_ELECTROLYZER =
      BlockDefinitions.register(
          "classic_electrolyzer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "classic_electrolyzer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              CellElectrolyzer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block STEAM_REPRESSURIZER =
      BlockDefinitions.register(
          "steam_repressurizer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "steam_repressurizer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              IntegrationSteamConverter.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.noFacings,
              true));

  private ProcessingBlocks() {}

  public static void init() {}
}
