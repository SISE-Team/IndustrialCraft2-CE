package ic2ce.neoforge.kinetic;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Kinetic block entity types, staged in declaration order. */
public final class KineticBlockEntities {
  public static final BlockEntityType<ElectricMotor> ELECTRIC_KINETIC_GENERATOR =
      BlockEntityDefinitions.register(
          "electric_kinetic_generator",
          ElectricMotor::new,
          KineticBlocks.ELECTRIC_KINETIC_GENERATOR);
  public static final BlockEntityType<ManualCrank> MANUAL_KINETIC_GENERATOR =
      BlockEntityDefinitions.register(
          "manual_kinetic_generator", ManualCrank::new, KineticBlocks.MANUAL_KINETIC_GENERATOR);
  public static final BlockEntityType<SteamTurbine> STEAM_KINETIC_GENERATOR =
      BlockEntityDefinitions.register(
          "steam_kinetic_generator", SteamTurbine::new, KineticBlocks.STEAM_KINETIC_GENERATOR);
  public static final BlockEntityType<HeatDrivenCogenerator> STIRLING_KINETIC_GENERATOR =
      BlockEntityDefinitions.register(
          "stirling_kinetic_generator",
          HeatDrivenCogenerator::new,
          KineticBlocks.STIRLING_KINETIC_GENERATOR);
  public static final BlockEntityType<WaterTurbine> WATER_KINETIC_GENERATOR =
      BlockEntityDefinitions.register(
          "water_kinetic_generator", WaterTurbine::new, KineticBlocks.WATER_KINETIC_GENERATOR);
  public static final BlockEntityType<WindTurbine> WIND_KINETIC_GENERATOR =
      BlockEntityDefinitions.register("wind_kinetic_generator", WindTurbine::new, KineticBlocks.WIND_KINETIC_GENERATOR);

  private KineticBlockEntities() {}

  public static void init() {}
}
