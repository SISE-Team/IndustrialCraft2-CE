package ic2.core.recipe.v2;

import ic2.api.recipe.Recipes;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;

/** Uses authoritative server recipes or the current connection's synced recipes. */
public class RecipeManagerGetter<T> implements Recipes.IGetter<T> {
   private static volatile RecipeMap clientRecipes = RecipeMap.EMPTY;
   private final Function<RecipeMap, T> factory;
   private final Map<RecipeMap, T> cache = new WeakHashMap<>();
   public RecipeManagerGetter(Function<RecipeMap, T> factory) { this.factory = factory; }
   public static void setClientRecipes(RecipeMap recipes) { clientRecipes = recipes; }
   public static RecipeMap clientRecipes() { return clientRecipes; }
   @Override public T get(Level world) {
      RecipeMap recipes = world instanceof ServerLevel serverLevel
         ? serverLevel.recipeAccess().recipeMap() : clientRecipes;
      synchronized (cache) { return cache.computeIfAbsent(recipes, factory); }
   }
}
