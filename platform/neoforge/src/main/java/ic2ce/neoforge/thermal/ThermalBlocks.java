package ic2ce.neoforge.thermal;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Thermal blocks, staged in declaration order. */
public final class ThermalBlocks {
  public static final Block ELECTRIC_HEAT_GENERATOR =
      BlockDefinitions.register(
          "electric_heat_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "electric_heat_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ElectricHeater.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              true));
  public static final Block FLUID_HEAT_GENERATOR =
      BlockDefinitions.register(
          "fluid_heat_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "fluid_heat_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FluidBurner.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              true));
  public static final Block RT_HEAT_GENERATOR =
      BlockDefinitions.register(
          "rt_heat_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rt_heat_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              RadioisotopeHeater.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              true));
  public static final Block SOLID_HEAT_GENERATOR =
      BlockDefinitions.register(
          "solid_heat_generator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "solid_heat_generator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              CombustionHeater.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              true));
  public static final Block LIQUID_HEAT_EXCHANGER =
      BlockDefinitions.register(
          "liquid_heat_exchanger",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "liquid_heat_exchanger")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FluidHeatExchanger.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));

  private ThermalBlocks() {}

  public static void init() {}
}
