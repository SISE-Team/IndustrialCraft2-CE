package ic2ce.neoforge.utility.client;

import java.util.*;

/**
 * Real client chunk snapshots are cached within one native world tick and never allocate missing
 * chunks.
 */
public final class ChunkMapContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run(ChunkAnchorScreen screen) {
    var failures = new ArrayList<String>();
    screen.updateMap(true);
    long before = screen.sampledColumns();
    if (before < 256 || before > 81L * 256)
      failures.add(
          "chunk map: actual loaded voxel snapshot count is outside the bounded visible map");
    for (int frame = 0; frame < 200; frame++) screen.updateMap(true);
    if (screen.sampledColumns() != before)
      failures.add("chunk map: repeated frames resample unchanged native world columns");
    return new Result(2, List.copyOf(failures));
  }
}
