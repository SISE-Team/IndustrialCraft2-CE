package ic2ce.core.util;

import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Axis-projected face hit zones preserve corners/edges without world-coordinate float precision
 * loss.
 */
public final class RotationUtil {
  private RotationUtil() {}

  public static Direction rotateByRay(BlockHitResult hit) {
    Objects.requireNonNull(hit);
    var local =
        hit.getLocation()
            .subtract(hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ());
    return rotateByHit(hit.getDirection(), (float) local.x, (float) local.y, (float) local.z);
  }

  public static Direction rotateByHit(Direction face, float x, float y, float z) {
    Objects.requireNonNull(face);
    if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) return face;
    float horizontal = face.getAxis() == Direction.Axis.X ? z : x,
        vertical = face.getAxis() == Direction.Axis.Y ? z : y;
    int column = horizontal <= .25F ? -1 : horizontal >= .75F ? 1 : 0,
        row = vertical <= .25F ? -1 : vertical >= .75F ? 1 : 0;
    if (column != 0 && row != 0) return face.getOpposite();
    if (column != 0)
      return face.getAxis() == Direction.Axis.X
          ? (column < 0 ? Direction.NORTH : Direction.SOUTH)
          : (column < 0 ? Direction.WEST : Direction.EAST);
    if (row != 0)
      return face.getAxis() == Direction.Axis.Y
          ? (row < 0 ? Direction.NORTH : Direction.SOUTH)
          : (row < 0 ? Direction.DOWN : Direction.UP);
    return face;
  }

  public static int[] shuffledFacings() {
    int[] faces = {0, 1, 2, 3, 4, 5};
    for (int index = faces.length - 1; index > 0; index--) {
      int chosen = ProjectRuntime.random.nextInt(index + 1), previous = faces[index];
      faces[index] = faces[chosen];
      faces[chosen] = previous;
    }
    return faces;
  }
}
