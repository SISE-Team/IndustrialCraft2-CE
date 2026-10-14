package ic2ce.neoforge.recipe.processing;

import ic2ce.api.recipe.IRecipeInput;
import ic2ce.engine.recipe.OrderedRecipeIndex;
import ic2ce.neoforge.recipe.input.ExactStackInput;
import java.util.*;
import java.util.function.*;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * A whole immutable native recipe generation is published once; certified item buckets preserve
 * original row order.
 */
final class CanningRecipeCatalog<Q, R> {
  private record Generation<Q, R>(List<R> rows, OrderedRecipeIndex<Q, R> index) {}

  private final ToIntFunction<Q> key;
  private final BiPredicate<R, Q> matches;
  private final Function<R, IRecipeInput> indexedInput;
  private volatile Generation<Q, R> generation;

  CanningRecipeCatalog(
      ToIntFunction<Q> key, BiPredicate<R, Q> matches, Function<R, IRecipeInput> indexedInput) {
    this.key = key;
    this.matches = matches;
    this.indexedInput = indexedInput;
    generation = compile(List.of());
  }

  private Generation<Q, R> compile(List<R> rows) {
    var index =
        new OrderedRecipeIndex<Q, R>(
            Math.max(1, BuiltInRegistries.ITEM.size()), key, (row, query) -> true, matches, false);
    for (var row : rows) {
      var input = indexedInput.apply(row);
      int[] keys =
          input instanceof ExactStackInput
              ? input.getInputs().stream()
                  .mapToInt(stack -> BuiltInRegistries.ITEM.getId(stack.getItem()))
                  .distinct()
                  .toArray()
              : null;
      index.add(row, keys);
    }
    return new Generation<>(List.copyOf(rows), index);
  }

  synchronized boolean add(R row, Predicate<R> conflicts, boolean replace) {
    var next = new ArrayList<R>();
    for (var previous : generation.rows) {
      if (conflicts.test(previous)) {
        if (!replace) return false;
      } else next.add(previous);
    }
    next.add(row);
    generation = compile(next);
    return true;
  }

  synchronized void remove(R row) {
    var next = new ArrayList<>(generation.rows);
    if (next.remove(row)) generation = compile(next);
  }

  List<R> rows() {
    return generation.rows;
  }

  R find(Q query) {
    var state = generation;
    return state.index.find(query, OrderedMachineRecipes.tagGeneration());
  }

  static int quantity(IRecipeInput input) {
    Objects.requireNonNull(input);
    int amount = input.getAmount();
    if (amount <= 0) throw new IllegalArgumentException("Canning bill must be positive");
    return amount;
  }
}
