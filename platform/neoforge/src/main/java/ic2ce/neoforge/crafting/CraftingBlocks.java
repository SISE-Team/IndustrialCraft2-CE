package ic2ce.neoforge.crafting;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Crafting blocks, staged in declaration order. */
public final class CraftingBlocks {
  public static final Block UU_ASSEMBLY_BENCH =
      BlockDefinitions.register(
          "uu_assembly_bench",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("uu_assembly_bench")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              UuAssembler.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block INDUSTRIAL_WORKBENCH =
      BlockDefinitions.register(
          "industrial_workbench",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "industrial_workbench")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              IndustrialWorkbench.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              true));
  public static final Block BATCH_CRAFTER =
      BlockDefinitions.register(
          "batch_crafter",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("batch_crafter")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              AutomaticCrafter.class,
              true,
              MachineBlockType.DefaultDrop.AdvMachine,
              DirectionSets.horizontalFacings,
              true));

  private CraftingBlocks() {}

  public static void init() {}
}
