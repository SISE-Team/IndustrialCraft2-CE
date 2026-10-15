package ic2ce.neoforge.runtime;

import net.minecraft.util.RandomSource;

/**
 * The project-wide random source for draws that are not tied to a world. It is a single shared
 * instance so the sequence of draws across callers is unchanged; it is not thread-safe.
 */
public final class SharedRandom {
  public static final RandomSource INSTANCE = RandomSource.create();

  private SharedRandom() {}
}
