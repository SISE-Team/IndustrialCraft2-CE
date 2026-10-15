package ic2ce.neoforge.world;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.world.block.ImprintedWall;
import ic2ce.neoforge.world.block.RubberSignEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** World block entity types, staged in declaration order. */
public final class WorldBlockEntities {
  public static final BlockEntityType<RubberSignEntity> SIGN =
      BlockEntityDefinitions.register(
          "sign",
          RubberSignEntity::new,
          WorldBlocks.RUBBER_SIGN,
          WorldBlocks.RUBBER_WALL_SIGN);
  public static final BlockEntityType<ImprintedWall> WALL =
      BlockEntityDefinitions.register("wall", ImprintedWall::new, WorldBlocks.OBSCURED_WALL);

  private WorldBlockEntities() {}

  public static void init() {}
}
