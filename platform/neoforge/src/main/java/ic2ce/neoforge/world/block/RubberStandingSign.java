package ic2ce.neoforge.world.block;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * Native rubber signage owns its registered block entity while the vanilla wood-specific
 * placement/editor protocol remains authoritative.
 */
public class RubberStandingSign extends StandingSignBlock {
  public RubberStandingSign(Properties properties, WoodType wood) {
    super(Objects.requireNonNull(wood), properties);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
    return new RubberSignEntity(position.immutable(), state);
  }
}
