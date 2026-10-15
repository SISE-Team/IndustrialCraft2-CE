package ic2ce.neoforge.terrain;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Terrain blocks, staged in declaration order. */
public final class TerrainBlocks {
  public static final Block TERRAFORMER =
      BlockDefinitions.register(
          "terraformer",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("terraformer")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              TerrainController.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.noFacings,
              false));

  private TerrainBlocks() {}

  public static void init() {}
}
