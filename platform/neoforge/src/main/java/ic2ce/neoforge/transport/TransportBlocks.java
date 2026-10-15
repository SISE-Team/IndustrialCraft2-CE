package ic2ce.neoforge.transport;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Transport blocks, staged in declaration order. */
public final class TransportBlocks {
  public static final Block FLUID_DISTRIBUTOR =
      BlockDefinitions.register(
          "fluid_distributor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("fluid_distributor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FairFluidRouter.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));
  public static final Block FLUID_REGULATOR =
      BlockDefinitions.register(
          "fluid_regulator",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("fluid_regulator")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              MeteredFluidOutlet.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));
  public static final Block ITEM_BUFFER =
      BlockDefinitions.register(
          "item_buffer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("item_buffer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              DualItemBuffer.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.noFacings,
              false));
  public static final Block SORTING_MACHINE =
      BlockDefinitions.register(
          "sorting_machine",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("sorting_machine")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              BatchItemSorter.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.onlyNorth,
              false));
  public static final Block WEIGHTED_FLUID_DISTRIBUTOR =
      BlockDefinitions.register(
          "weighted_fluid_distributor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "weighted_fluid_distributor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PriorityFluidRouter.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));
  public static final Block WEIGHTED_ITEM_DISTRIBUTOR =
      BlockDefinitions.register(
          "weighted_item_distributor",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "weighted_item_distributor")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PriorityItemRouter.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.allFacings,
              true));

  private TransportBlocks() {}

  public static void init() {}
}
