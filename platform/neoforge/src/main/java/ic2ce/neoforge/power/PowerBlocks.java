package ic2ce.neoforge.power;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Power blocks, staged in declaration order. */
public final class PowerBlocks {
  public static final Block GENERATOR =
      BlockDefinitions.register(
          "generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              CombustionGenerator.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              true));
  public static final Block GEO_GENERATOR =
      BlockDefinitions.register(
          "geo_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("geo_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              GeothermalGenerator.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              DirectionSets.horizontalFacings,
              true));
  public static final Block KINETIC_GENERATOR =
      BlockDefinitions.register(
          "kinetic_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("kinetic_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              KineticConverter.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              DirectionSets.allFacings,
              true));
  public static final Block RT_GENERATOR =
      BlockDefinitions.register(
          "rt_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rt_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              RadioisotopeGenerator.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              DirectionSets.noFacings,
              false));
  public static final Block SEMIFLUID_GENERATOR =
      BlockDefinitions.register(
          "semifluid_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "semifluid_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              BiogasGenerator.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              DirectionSets.horizontalFacings,
              true));
  public static final Block SOLAR_GENERATOR =
      BlockDefinitions.register(
          "solar_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("solar_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SolarCollector.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              DirectionSets.horizontalFacings,
              true));
  public static final Block STIRLING_GENERATOR =
      BlockDefinitions.register(
          "stirling_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "stirling_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ThermalConverter.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              DirectionSets.allFacings,
              true));
  public static final Block WATER_GENERATOR =
      BlockDefinitions.register(
          "water_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("water_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              WaterMill.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              DirectionSets.horizontalFacings,
              true));
  public static final Block WIND_GENERATOR =
      BlockDefinitions.register(
          "wind_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("wind_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              WindMill.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              DirectionSets.horizontalFacings,
              true));
  public static final Block CREATIVE_GENERATOR =
      BlockDefinitions.register(
          "creative_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "creative_generator")))
                  .strength(-1.0F, Float.POSITIVE_INFINITY)
                  .sound(SoundType.METAL),
              CreativePowerSource.class,
              true,
              MachineBlockType.DefaultDrop.None,
              DirectionSets.noFacings,
              false));

  private PowerBlocks() {}

  public static void init() {}
}
