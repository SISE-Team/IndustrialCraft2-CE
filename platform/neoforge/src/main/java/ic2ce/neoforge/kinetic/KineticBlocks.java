package ic2ce.neoforge.kinetic;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Kinetic blocks, staged in declaration order. */
public final class KineticBlocks {
  public static final Block ELECTRIC_KINETIC_GENERATOR =
      BlockDefinitions.register(
          "electric_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "electric_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ElectricMotor.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              true));
  public static final Block MANUAL_KINETIC_GENERATOR =
      BlockDefinitions.register(
          "manual_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "manual_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ManualCrank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              true));
  public static final Block STEAM_KINETIC_GENERATOR =
      BlockDefinitions.register(
          "steam_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "steam_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SteamTurbine.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              true));
  public static final Block STIRLING_KINETIC_GENERATOR =
      BlockDefinitions.register(
          "stirling_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "stirling_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              HeatDrivenCogenerator.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              true));
  public static final Block WATER_KINETIC_GENERATOR =
      BlockDefinitions.register(
          "water_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "water_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              WaterTurbine.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              true));
  public static final Block WIND_KINETIC_GENERATOR =
      BlockDefinitions.register(
          "wind_kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "wind_kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              WindTurbine.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              true));

  private KineticBlocks() {}

  public static void init() {}
}
