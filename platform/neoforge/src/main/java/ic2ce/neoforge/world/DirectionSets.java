package ic2ce.neoforge.world;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.Direction;

/** The six native directions have only sixty-four immutable combinations, shared by every tile. */
public final class DirectionSets {
  public static final Direction[] ALL_DIRS = Direction.values();
  public static final Direction[] HORIZONTAL_DIRS = {
    Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
  };
  /** Supported block facings; these exact instances are compared by registered block types. */
  public static final Set<Direction> noFacings = Set.of(),
      onlyNorth = Set.of(Direction.NORTH),
      horizontalFacings = Set.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST),
      verticalFacings = Set.of(Direction.DOWN, Direction.UP),
      downSideFacings =
          Set.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST),
      allFacings = Set.of(Direction.values());

  private static final Direction[] DIRECTIONS = Direction.values();
  private static final Set<Direction>[] SETS = create();

  private DirectionSets() {}

  @SuppressWarnings("unchecked")
  private static Set<Direction>[] create() {
    var result = (Set<Direction>[]) new Set<?>[1 << DIRECTIONS.length];
    result[0] = Set.of();
    for (int mask = 1; mask < result.length; mask++) {
      var set = EnumSet.noneOf(Direction.class);
      for (var direction : DIRECTIONS)
        if ((mask & 1 << direction.ordinal()) != 0) set.add(direction);
      result[mask] = Collections.unmodifiableSet(set);
    }
    return result;
  }

  public static int mask(Collection<Direction> directions) {
    int mask = 0;
    for (var direction : directions) mask |= 1 << direction.ordinal();
    return mask;
  }

  public static Set<Direction> ofMask(int mask) {
    if (mask < 0 || mask >= SETS.length)
      throw new IllegalArgumentException("Invalid direction mask");
    return SETS[mask];
  }

  public static Set<Direction> all() {
    return SETS[SETS.length - 1];
  }

  public static Set<Direction> except(Direction excluded) {
    return SETS[(SETS.length - 1) & ~(1 << excluded.ordinal())];
  }
}
