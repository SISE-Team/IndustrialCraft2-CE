package ic2ce.neoforge.crafting;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Crafting block entity types, staged in declaration order. */
public final class CraftingBlockEntities {
  public static final BlockEntityType<UuAssembler> UU_ASSEMBLY_BENCH =
      BlockEntityDefinitions.register("uu_assembly_bench", UuAssembler::new, CraftingBlocks.UU_ASSEMBLY_BENCH);
  public static final BlockEntityType<IndustrialWorkbench> INDUSTRIAL_WORKBENCH =
      BlockEntityDefinitions.register(
          "industrial_workbench", IndustrialWorkbench::new, CraftingBlocks.INDUSTRIAL_WORKBENCH);
  public static final BlockEntityType<AutomaticCrafter> BATCH_CRAFTER =
      BlockEntityDefinitions.register("batch_crafter", AutomaticCrafter::new, CraftingBlocks.BATCH_CRAFTER);

  private CraftingBlockEntities() {}

  public static void init() {}
}
