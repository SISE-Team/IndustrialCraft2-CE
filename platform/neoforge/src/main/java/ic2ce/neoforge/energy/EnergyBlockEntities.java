package ic2ce.neoforge.energy;

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
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Energy block entity types, staged in declaration order. */
public final class EnergyBlockEntities {
  public static final BlockEntityType<LowVoltagePad> BATBOX_CHARGEPAD =
      BlockEntityDefinitions.register("batbox_chargepad", LowVoltagePad::new, EnergyBlocks.BATBOX_CHARGEPAD);
  public static final BlockEntityType<MediumVoltagePad> CESU_CHARGEPAD =
      BlockEntityDefinitions.register("cesu_chargepad", MediumVoltagePad::new, EnergyBlocks.CESU_CHARGEPAD);
  public static final BlockEntityType<HighVoltagePad> MFE_CHARGEPAD =
      BlockEntityDefinitions.register("mfe_chargepad", HighVoltagePad::new, EnergyBlocks.MFE_CHARGEPAD);
  public static final BlockEntityType<ExtremeVoltagePad> MFSU_CHARGEPAD =
      BlockEntityDefinitions.register("mfsu_chargepad", ExtremeVoltagePad::new, EnergyBlocks.MFSU_CHARGEPAD);
  public static final BlockEntityType<LowVoltageBank> BATBOX =
      BlockEntityDefinitions.register("batbox", LowVoltageBank::new, EnergyBlocks.BATBOX);
  public static final BlockEntityType<MediumVoltageBank> CESU =
      BlockEntityDefinitions.register("cesu", MediumVoltageBank::new, EnergyBlocks.CESU);
  public static final BlockEntityType<HighVoltageBank> MFE =
      BlockEntityDefinitions.register("mfe", HighVoltageBank::new, EnergyBlocks.MFE);
  public static final BlockEntityType<HighVoltageBank.TileEntityElectricClassicMFE> CLASSIC_MFE =
      BlockEntityDefinitions.register(
          "classic_mfe",
          HighVoltageBank.TileEntityElectricClassicMFE::new,
          EnergyBlocks.CLASSIC_MFE);
  public static final BlockEntityType<ExtremeVoltageBank> MFSU =
      BlockEntityDefinitions.register("mfsu", ExtremeVoltageBank::new, EnergyBlocks.MFSU);
  public static final BlockEntityType<ExtremeVoltageBank.TileEntityElectricClassicMFSU>
      CLASSIC_MFSU =
          BlockEntityDefinitions.register(
              "classic_mfsu",
              ExtremeVoltageBank.TileEntityElectricClassicMFSU::new,
              EnergyBlocks.CLASSIC_MFSU);
  public static final BlockEntityType<LowVoltageTransformer> LV_TRANSFORMER =
      BlockEntityDefinitions.register("lv_transformer", LowVoltageTransformer::new, EnergyBlocks.LV_TRANSFORMER);
  public static final BlockEntityType<MediumVoltageTransformer> MV_TRANSFORMER =
      BlockEntityDefinitions.register("mv_transformer", MediumVoltageTransformer::new, EnergyBlocks.MV_TRANSFORMER);
  public static final BlockEntityType<HighVoltageTransformer> HV_TRANSFORMER =
      BlockEntityDefinitions.register("hv_transformer", HighVoltageTransformer::new, EnergyBlocks.HV_TRANSFORMER);
  public static final BlockEntityType<ExtremeVoltageTransformer> EV_TRANSFORMER =
      BlockEntityDefinitions.register("ev_transformer", ExtremeVoltageTransformer::new, EnergyBlocks.EV_TRANSFORMER);

  private EnergyBlockEntities() {}

  public static void init() {}
}
