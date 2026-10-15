package ic2ce.neoforge.diagnostics;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Log presentation of block positions; -Dic2.debug.includeworldhash adds the world identity. */
public final class Positions {
  private static final boolean WORLD_HASH = System.getProperty("ic2.debug.includeworldhash") != null;

  private Positions() {}

  public static String format(BlockEntity tile) {
    if (tile == null) return "(null)";
    var world = tile.getLevel();
    var position = tile.getBlockPos();
    String text =
        "dim "
            + (world instanceof Level level ? level.dimension().identifier() : null)
            + ": "
            + position.getX()
            + "/"
            + position.getY()
            + "/"
            + position.getZ();
    return WORLD_HASH ? text + " (@" + Integer.toHexString(System.identityHashCode(world)) + ")" : text;
  }
}
