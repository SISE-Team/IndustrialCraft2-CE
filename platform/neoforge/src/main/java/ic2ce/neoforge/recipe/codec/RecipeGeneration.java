package ic2ce.neoforge.recipe.codec;

import ic2ce.api.recipe.Recipes;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;

/**
 * Current server or connection recipe generations own one hot manager; obsolete maps cannot form
 * cache retention cycles.
 */
public final class RecipeGeneration<T> implements Recipes.IGetter<T> {
  private record Active<T>(RecipeMap recipes, T manager) {}

  private static volatile RecipeMap clientRecipes = RecipeMap.EMPTY;
  private final Function<RecipeMap, T> factory;
  private final Map<RecipeMap, WeakReference<T>> previous = new WeakHashMap<>();
  private volatile Active<T> active;

  public RecipeGeneration(Function<RecipeMap, T> factory) {
    this.factory = Objects.requireNonNull(factory);
  }

  public static void setClientRecipes(RecipeMap recipes) {
    clientRecipes = Objects.requireNonNull(recipes);
  }

  public static RecipeMap clientRecipes() {
    return clientRecipes;
  }

  @Override
  public T get(Level world) {
    var recipes =
        world instanceof ServerLevel server ? server.recipeAccess().recipeMap() : clientRecipes;
    var current = active;
    if (current != null && current.recipes == recipes) return current.manager;
    synchronized (previous) {
      current = active;
      if (current != null && current.recipes == recipes) return current.manager;
      var reference = previous.get(recipes);
      T manager = reference == null ? null : reference.get();
      if (manager == null) {
        manager =
            Objects.requireNonNull(factory.apply(recipes), "Recipe manager factory returned null");
        previous.put(recipes, new WeakReference<>(manager));
      }
      active = new Active<>(recipes, manager);
      return manager;
    }
  }
}
