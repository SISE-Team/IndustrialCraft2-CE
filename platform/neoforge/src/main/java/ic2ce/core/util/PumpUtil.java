package ic2ce.core.util;

import ic2ce.engine.graph.BoundedSearch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Searches only loaded cells connected to the starting fluid; unrelated neighboring basins are
 * excluded.
 */
public final class PumpUtil {
  private static final ThreadLocal<BoundedSearch> SEARCH =
      ThreadLocal.withInitial(() -> new BoundedSearch(256));
  private static final Direction[] ORDER = {
    Direction.UP, Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN
  };

  private PumpUtil() {}

  public static BlockPos searchFluidSource(Level world, BlockPos start) {
    if (world == null || start == null || !world.hasChunkAt(start)) return null;
    var initial = world.getFluidState(start);
    if (initial.isEmpty()) return null;
    if (initial.isSource() && LiquidUtil.drainWorldFluidBlock(world, start, true) != null)
      return start.immutable();
    var fluid = initial.getType();
    var result =
        SEARCH
            .get()
            .find(
                start.asLong(),
                packed -> {
                  var position = BlockPos.of(packed);
                  if (!world.hasChunkAt(position)) return -1;
                  var state = world.getFluidState(position);
                  if (state.isEmpty() || !state.getType().isSame(fluid)) return -1;
                  return state.isSource()
                          && LiquidUtil.drainWorldFluidBlock(world, position, true) != null
                      ? 1
                      : 0;
                },
                (packed, adjacent) -> {
                  var position = BlockPos.of(packed);
                  for (int index = 0; index < ORDER.length; index++)
                    adjacent[index] = position.relative(ORDER[index]).asLong();
                  return ORDER.length;
                });
    return result.isPresent() ? BlockPos.of(result.getAsLong()) : null;
  }
}
