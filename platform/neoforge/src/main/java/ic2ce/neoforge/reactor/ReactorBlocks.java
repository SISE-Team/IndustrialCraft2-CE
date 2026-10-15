package ic2ce.neoforge.reactor;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Reactor blocks, staged in declaration order. */
public final class ReactorBlocks {
  public static final Block REACTOR_VESSEL =
      BlockDefinitions.register(
          "reactor_vessel",
          new Block(
              BlockProfiles.stone()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("reactor_vessel")))
                  .strength(40.0F, 90.0F)
                  .requiresCorrectToolForDrops()));
  public static final Block NUCLEAR_REACTOR =
      BlockDefinitions.register(
          "nuclear_reactor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("nuclear_reactor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              NuclearCore.class,
              true,
              MachineBlockType.DefaultDrop.Generator,
              DirectionSets.horizontalFacings,
              true));
  public static final Block REACTOR_ACCESS_HATCH =
      BlockDefinitions.register(
          "reactor_access_hatch",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "reactor_access_hatch")))
                  .strength(40.0F, 90.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              VesselHatch.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.onlyNorth,
              false));
  public static final Block REACTOR_CHAMBER =
      BlockDefinitions.register(
          "reactor_chamber",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("reactor_chamber")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ReactorChamber.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.onlyNorth,
              true));
  public static final Block REACTOR_FLUID_PORT =
      BlockDefinitions.register(
          "reactor_fluid_port",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "reactor_fluid_port")))
                  .strength(40.0F, 90.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              CoolantPort.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.onlyNorth,
              false));
  public static final Block REACTOR_REDSTONE_PORT =
      BlockDefinitions.register(
          "reactor_redstone_port",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "reactor_redstone_port")))
                  .strength(40.0F, 90.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              SignalPort.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.onlyNorth,
              false));
  public static final Block RCI_RSH =
      BlockDefinitions.register(
          "rci_rsh",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rci_rsh")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              RedstoneCondensatorService.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.allFacings,
              true));
  public static final Block RCI_LZH =
      BlockDefinitions.register(
          "rci_lzh",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("rci_lzh")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LapisCondensatorService.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.allFacings,
              true));

  private ReactorBlocks() {}

  public static void init() {}
}
