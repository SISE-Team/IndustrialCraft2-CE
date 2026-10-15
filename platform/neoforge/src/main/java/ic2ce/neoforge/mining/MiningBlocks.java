package ic2ce.neoforge.mining;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Mining blocks, staged in declaration order. */
public final class MiningBlocks {
  public static final Block MINING_PIPE =
      BlockDefinitions.register(
          "mining_pipe",
          new MiningColumn(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("mining_pipe")))
                  .strength(6.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block MINING_PIPE_TIP =
      BlockDefinitions.register(
          "mining_pipe_tip",
          new Block(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("mining_pipe_tip")))
                  .strength(6.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL)));
  public static final Block ADVANCED_MINER =
      BlockDefinitions.register(
          "advanced_miner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("advanced_miner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              AreaMiner.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              true));
  public static final Block MINER =
      BlockDefinitions.register(
          "miner",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("miner")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PipeMiner.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              true));

  private MiningBlocks() {}

  public static void init() {}
}
