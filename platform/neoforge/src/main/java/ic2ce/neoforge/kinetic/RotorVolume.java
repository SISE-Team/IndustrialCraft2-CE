package ic2ce.neoforge.kinetic;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Counts obstructed projected columns while detecting competing turbines anywhere in the sampled
 * volume.
 */
final class RotorVolume {
  private RotorVolume() {}

  static int scan(
      Level world,
      BlockPos origin,
      Direction facing,
      int diameter,
      int depth,
      boolean planeOnly,
      Predicate<BlockState> open,
      Predicate<BlockEntity> competitor) {
    if (world == null || facing.getAxis() == Direction.Axis.Y || diameter <= 0 || diameter > 64)
      return -1;
    int radius = diameter / 2 * (planeOnly ? 1 : 2),
        first = planeOnly ? 1 : -Math.max(0, depth),
        last = planeOnly ? 1 : Math.max(0, depth);
    var right = facing.getClockWise();
    var cursor = new BlockPos.MutableBlockPos();
    int occupiedColumns = 0;
    for (int vertical = -radius; vertical <= radius; vertical++)
      for (int lateral = -radius; lateral <= radius; lateral++) {
        boolean occupied = false;
        for (int forward = first; forward <= last; forward++) {
          cursor.set(
              origin.getX() + facing.getStepX() * forward + right.getStepX() * lateral,
              origin.getY() + vertical,
              origin.getZ() + facing.getStepZ() * forward + right.getStepZ() * lateral);
          var state = world.getBlockState(cursor);
          if (!open.test(state)) {
            occupied = true;
            if (!planeOnly
                && (vertical != 0 || lateral != 0 || forward != 0)
                && competitor.test(world.getBlockEntity(cursor))) return -1;
          }
        }
        if (occupied) occupiedColumns++;
      }
    return occupiedColumns;
  }
}
