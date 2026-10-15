package ic2ce.neoforge.power;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Power block entity types, staged in declaration order. */
public final class PowerBlockEntities {
  public static final BlockEntityType<CombustionGenerator> GENERATOR =
      BlockEntityDefinitions.register("generator", CombustionGenerator::new, PowerBlocks.GENERATOR);
  public static final BlockEntityType<GeothermalGenerator> GEO_GENERATOR =
      BlockEntityDefinitions.register("geo_generator", GeothermalGenerator::new, PowerBlocks.GEO_GENERATOR);
  public static final BlockEntityType<KineticConverter> KINETIC_GENERATOR =
      BlockEntityDefinitions.register("kinetic_generator", KineticConverter::new, PowerBlocks.KINETIC_GENERATOR);
  public static final BlockEntityType<RadioisotopeGenerator> RT_GENERATOR =
      BlockEntityDefinitions.register("rt_generator", RadioisotopeGenerator::new, PowerBlocks.RT_GENERATOR);
  public static final BlockEntityType<BiogasGenerator> SEMIFLUID_GENERATOR =
      BlockEntityDefinitions.register("semifluid_generator", BiogasGenerator::new, PowerBlocks.SEMIFLUID_GENERATOR);
  public static final BlockEntityType<SolarCollector> SOLAR_GENERATOR =
      BlockEntityDefinitions.register("solar_generator", SolarCollector::new, PowerBlocks.SOLAR_GENERATOR);
  public static final BlockEntityType<ThermalConverter> STIRLING_GENERATOR =
      BlockEntityDefinitions.register("stirling_generator", ThermalConverter::new, PowerBlocks.STIRLING_GENERATOR);
  public static final BlockEntityType<WaterMill> WATER_GENERATOR =
      BlockEntityDefinitions.register("water_generator", WaterMill::new, PowerBlocks.WATER_GENERATOR);
  public static final BlockEntityType<WindMill> WIND_GENERATOR =
      BlockEntityDefinitions.register("wind_generator", WindMill::new, PowerBlocks.WIND_GENERATOR);
  public static final BlockEntityType<CreativePowerSource> CREATIVE_GENERATOR =
      BlockEntityDefinitions.register("creative_generator", CreativePowerSource::new, PowerBlocks.CREATIVE_GENERATOR);

  private PowerBlockEntities() {}

  public static void init() {}
}
