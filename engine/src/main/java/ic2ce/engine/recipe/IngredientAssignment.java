package ic2ce.engine.recipe;

import java.util.Arrays;

/**
 * Augmenting-path matching resolves ambiguous small crafting grids without factorial permutations.
 */
public final class IngredientAssignment {
  private IngredientAssignment() {}

  public static boolean matches(int[] candidateMasks, int size) {
    if (size < 0 || size > 9 || candidateMasks.length < size)
      throw new IllegalArgumentException("Crafting assignment size outside 0..9");
    int[] owners = new int[size];
    Arrays.fill(owners, -1);
    int allowed = (1 << size) - 1;
    int[] visited = new int[size];
    for (int ingredient = 0; ingredient < size; ingredient++)
      if ((candidateMasks[ingredient] & allowed) == 0
          || !augment(ingredient, candidateMasks, owners, visited, ingredient + 1, allowed))
        return false;
    return true;
  }

  private static boolean augment(
      int ingredient, int[] candidates, int[] owners, int[] visited, int visit, int allowed) {
    int options = candidates[ingredient] & allowed;
    while (options != 0) {
      int slot = Integer.numberOfTrailingZeros(options);
      options &= options - 1;
      if (visited[slot] == visit) continue;
      visited[slot] = visit;
      if (owners[slot] < 0 || augment(owners[slot], candidates, owners, visited, visit, allowed)) {
        owners[slot] = ingredient;
        return true;
      }
    }
    return false;
  }
}
