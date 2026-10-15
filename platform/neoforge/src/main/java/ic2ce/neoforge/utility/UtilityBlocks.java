package ic2ce.neoforge.utility;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Utility blocks, staged in declaration order. */
public final class UtilityBlocks {
  public static final Block IRON_FENCE =
      BlockDefinitions.register(
          "iron_fence",
          new IndustrialFence(
              BlockProfiles.metal()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("iron_fence")))
                  .strength(5.0F, 10.0F),
              true));
  public static final Block MAGNETIZER =
      BlockDefinitions.register(
          "magnetizer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("magnetizer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PoleDrive.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              true));
  public static final Block TELEPORTER =
      BlockDefinitions.register(
          "teleporter",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("teleporter")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              LinkedTeleporter.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.noFacings,
              false));
  public static final Block TESLA_COIL =
      BlockDefinitions.register(
          "tesla_coil",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("tesla_coil")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ArcCoil.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block CHUNK_LOADER =
      BlockDefinitions.register(
          "chunk_loader",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("chunk_loader")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ChunkAnchor.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              true));

  private UtilityBlocks() {}

  public static void init() {}
}
