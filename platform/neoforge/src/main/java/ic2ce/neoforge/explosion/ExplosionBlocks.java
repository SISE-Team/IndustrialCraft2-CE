package ic2ce.neoforge.explosion;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Explosion blocks, staged in declaration order. */
public final class ExplosionBlocks {
  public static final Block ITNT =
      BlockDefinitions.register(
          "itnt",
          MachineBlockType.create(
              BlockProfiles.explosive()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("itnt")))
                  .strength(0.0F, 0.0F)
                  .sound(SoundType.GRASS),
              DemolitionBlockEntity.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));
  public static final Block NUKE =
      BlockDefinitions.register(
          "nuke",
          MachineBlockType.create(
              BlockProfiles.explosive()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("nuke")))
                  .strength(0.0F, 0.0F)
                  .sound(SoundType.GRASS),
              ConfiguredNuclearCharge.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));
  public static final Block CLASSIC_NUKE =
      BlockDefinitions.register(
          "classic_nuke",
          MachineBlockType.create(
              BlockProfiles.explosive()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("classic_nuke")))
                  .strength(0.0F, 0.0F)
                  .sound(SoundType.GRASS),
              NuclearBlockEntity.ClassicNuclearCharge.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));

  private ExplosionBlocks() {}

  public static void init() {}
}
