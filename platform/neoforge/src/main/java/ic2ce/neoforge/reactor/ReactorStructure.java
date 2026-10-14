package ic2ce.neoforge.reactor;

import ic2ce.api.reactor.IReactorChamber;
import ic2ce.neoforge.registry.BlockDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Bounded native vessel geometry. Only the 98 shell cells belong to a fluid reactor. */
public final class ReactorStructure {
  private ReactorStructure() {}

  public static boolean onShell(BlockPos center, BlockPos position) {
    int x = Math.abs(position.getX() - center.getX()),
        y = Math.abs(position.getY() - center.getY()),
        z = Math.abs(position.getZ() - center.getZ());
    return x <= 2 && y <= 2 && z <= 2 && Math.max(x, Math.max(y, z)) == 2;
  }

  public static boolean hasShell(Level world, BlockPos center) {
    var cursor = new BlockPos.MutableBlockPos();
    for (int y = -2; y <= 2; y++)
      for (int z = -2; z <= 2; z++)
        for (int x = -2; x <= 2; x++) {
          if (Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z))) != 2) continue;
          cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
          if (!world.hasChunkAt(cursor)) return false;
          if (world.getBlockState(cursor).is(BlockDefinitions.REACTOR_VESSEL)) continue;
          if (!(world.getBlockEntity(cursor) instanceof IReactorChamber part) || !part.isWall())
            return false;
        }
    return true;
  }

  public static boolean isFluidReactor(NuclearCore core) {
    var world = core.getLevel();
    var center = core.getBlockPos();
    if (world == null || core.getReactorSize() != 9 || !hasShell(world, center)) return false;
    var cursor = new BlockPos.MutableBlockPos();
    for (int y = -4; y <= 4; y++)
      for (int z = -4; z <= 4; z++)
        for (int x = -4; x <= 4; x++) {
          if (x == 0 && y == 0 && z == 0) continue;
          cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
          if (!world.hasChunkAt(cursor)) continue;
          if (world.getBlockEntity(cursor) instanceof NuclearCore other
              && !other.isRemoved()
              && other.getReactorSize() == 9
              && hasShell(world, cursor)) return false;
        }
    return true;
  }
}
