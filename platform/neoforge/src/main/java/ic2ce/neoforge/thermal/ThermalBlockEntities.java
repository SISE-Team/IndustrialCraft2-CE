package ic2ce.neoforge.thermal;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Thermal block entity types, staged in declaration order. */
public final class ThermalBlockEntities {
  public static final BlockEntityType<ElectricHeater> ELECTRIC_HEAT_GENERATOR =
      BlockEntityDefinitions.register(
          "electric_heat_generator", ElectricHeater::new, ThermalBlocks.ELECTRIC_HEAT_GENERATOR);
  public static final BlockEntityType<FluidBurner> FLUID_HEAT_GENERATOR =
      BlockEntityDefinitions.register("fluid_heat_generator", FluidBurner::new, ThermalBlocks.FLUID_HEAT_GENERATOR);
  public static final BlockEntityType<RadioisotopeHeater> RT_HEAT_GENERATOR =
      BlockEntityDefinitions.register("rt_heat_generator", RadioisotopeHeater::new, ThermalBlocks.RT_HEAT_GENERATOR);
  public static final BlockEntityType<CombustionHeater> SOLID_HEAT_GENERATOR =
      BlockEntityDefinitions.register(
          "solid_heat_generator", CombustionHeater::new, ThermalBlocks.SOLID_HEAT_GENERATOR);
  public static final BlockEntityType<FluidHeatExchanger> LIQUID_HEAT_EXCHANGER =
      BlockEntityDefinitions.register(
          "liquid_heat_exchanger", FluidHeatExchanger::new, ThermalBlocks.LIQUID_HEAT_EXCHANGER);

  private ThermalBlockEntities() {}

  public static void init() {}
}
