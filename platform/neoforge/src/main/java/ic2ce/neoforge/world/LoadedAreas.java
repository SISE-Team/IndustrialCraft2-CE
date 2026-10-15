package ic2ce.neoforge.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

/** Square chunk-readiness checks that reject radii overflowing native block coordinates. */
public final class LoadedAreas {
  private LoadedAreas() {}

  public static boolean isLoaded(LevelReader world, BlockPos center, int radius) {
    if (radius < 0) return false;
    long left = (long) center.getX() - radius,
        right = (long) center.getX() + radius,
        top = (long) center.getZ() - radius,
        bottom = (long) center.getZ() + radius;
    return left >= Integer.MIN_VALUE
        && right <= Integer.MAX_VALUE
        && top >= Integer.MIN_VALUE
        && bottom <= Integer.MAX_VALUE
        && world.hasChunksAt((int) left, (int) top, (int) right, (int) bottom);
  }
}
