package ic2ce.neoforge.world.block;

import ic2ce.neoforge.world.WorldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The native entity type is installed in the actual superclass, including serialization and
 * validity checks.
 */
public class RubberSignEntity extends SignBlockEntity {
  public RubberSignEntity(BlockPos position, BlockState state) {
    super(WorldBlockEntities.SIGN, position.immutable(), state);
  }
}
