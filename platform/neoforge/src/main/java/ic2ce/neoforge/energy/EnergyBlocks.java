package ic2ce.neoforge.energy;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
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
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Energy blocks, staged in declaration order. */
public final class EnergyBlocks {
  public static final Block BATBOX_CHARGEPAD =
      BlockDefinitions.register(
          "batbox_chargepad",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("batbox_chargepad")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)
                  .noOcclusion(),
              LowVoltagePad.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.downSideFacings,
              true));
  public static final Block CESU_CHARGEPAD =
      BlockDefinitions.register(
          "cesu_chargepad",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("cesu_chargepad")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)
                  .noOcclusion(),
              MediumVoltagePad.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.downSideFacings,
              true));
  public static final Block MFE_CHARGEPAD =
      BlockDefinitions.register(
          "mfe_chargepad",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("mfe_chargepad")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)
                  .noOcclusion(),
              HighVoltagePad.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.downSideFacings,
              true));
  public static final Block MFSU_CHARGEPAD =
      BlockDefinitions.register(
          "mfsu_chargepad",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("mfsu_chargepad")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)
                  .noOcclusion(),
              ExtremeVoltagePad.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.downSideFacings,
              true));
  public static final Block BATBOX =
      BlockDefinitions.register(
          "batbox",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("batbox")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LowVoltageBank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              true));
  public static final Block CESU =
      BlockDefinitions.register(
          "cesu",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("cesu")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MediumVoltageBank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              true));
  public static final Block MFE =
      BlockDefinitions.register(
          "mfe",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("mfe")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              HighVoltageBank.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));
  public static final Block CLASSIC_MFE =
      BlockDefinitions.register(
          "classic_mfe",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("classic_mfe")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              HighVoltageBank.TileEntityElectricClassicMFE.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));
  public static final Block MFSU =
      BlockDefinitions.register(
          "mfsu",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("mfsu")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ExtremeVoltageBank.class,
              false,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.allFacings,
              true));
  public static final Block CLASSIC_MFSU =
      BlockDefinitions.register(
          "classic_mfsu",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("classic_mfsu")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ExtremeVoltageBank.TileEntityElectricClassicMFSU.class,
              false,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.allFacings,
              true));
  public static final Block LV_TRANSFORMER =
      BlockDefinitions.register(
          "lv_transformer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("lv_transformer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LowVoltageTransformer.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              true));
  public static final Block MV_TRANSFORMER =
      BlockDefinitions.register(
          "mv_transformer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("mv_transformer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MediumVoltageTransformer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));
  public static final Block HV_TRANSFORMER =
      BlockDefinitions.register(
          "hv_transformer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("hv_transformer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              HighVoltageTransformer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));
  public static final Block EV_TRANSFORMER =
      BlockDefinitions.register(
          "ev_transformer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("ev_transformer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ExtremeVoltageTransformer.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));

  private EnergyBlocks() {}

  public static void init() {}
}
