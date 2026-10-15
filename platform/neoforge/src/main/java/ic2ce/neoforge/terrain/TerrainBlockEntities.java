package ic2ce.neoforge.terrain;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Terrain block entity types, staged in declaration order. */
public final class TerrainBlockEntities {
  public static final BlockEntityType<TerrainController> TERRAFORMER =
      BlockEntityDefinitions.register("terraformer", TerrainController::new, TerrainBlocks.TERRAFORMER);

  private TerrainBlockEntities() {}

  public static void init() {}
}
