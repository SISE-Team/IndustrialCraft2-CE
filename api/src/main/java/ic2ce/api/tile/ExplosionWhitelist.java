package ic2ce.api.tile;

import java.util.*;
import net.minecraft.world.level.block.Block;

/**
 * Rare addon whitelist changes publish immutable identity generations to every explosion worker.
 */
public final class ExplosionWhitelist {
  private static volatile Set<Block> blocks =
      Collections.unmodifiableSet(Collections.newSetFromMap(new IdentityHashMap<>()));

  private ExplosionWhitelist() {}

  public static synchronized void addWhitelistedBlock(Block block) {
    Objects.requireNonNull(block);
    if (blocks.contains(block)) return;
    var next = Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());
    next.addAll(blocks);
    next.add(block);
    blocks = Collections.unmodifiableSet(next);
  }

  public static synchronized void removeWhitelistedBlock(Block block) {
    Objects.requireNonNull(block);
    if (!blocks.contains(block)) return;
    var next = Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());
    next.addAll(blocks);
    next.remove(block);
    blocks = Collections.unmodifiableSet(next);
  }

  public static boolean isBlockWhitelisted(Block block) {
    return block != null && blocks.contains(block);
  }
}
