package ic2ce.neoforge.recipe.processing;

import ic2ce.api.recipe.*;
import ic2ce.engine.recipe.OrderedRecipeIndex;
import ic2ce.neoforge.recipe.codec.DeferredInput;
import ic2ce.neoforge.recipe.input.ExactStackInput;
import ic2ce.neoforge.recipe.input.MaterialTagIngredient;
import ic2ce.neoforge.recipe.input.NativeIngredientInput;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Minecraft stack/remainder adapter for the independent recipe index. */
public abstract class OrderedMachineRecipes<RI, RO>
    implements IMachineRecipeManager<RI, RO, ItemStack> {
  protected final Map<RI, MachineRecipe<RI, RO>> recipes = new HashMap<>();
  private static volatile Object tagEpoch = new Object();
  private final OrderedRecipeIndex<ItemStack, MachineRecipe<RI, RO>> index =
      new OrderedRecipeIndex<>(
          Math.max(1, BuiltInRegistries.ITEM.size()),
          stack -> BuiltInRegistries.ITEM.getId(stack.getItem()),
          this::mayMatchItem,
          (recipe, stack) -> getForRecipe(recipe).matches(stack));

  public static void invalidateTagCandidates() {
    tagEpoch = new Object();
  }

  public static Object tagGeneration() {
    return tagEpoch;
  }

  protected abstract IRecipeInput getForInput(RI input);

  protected IRecipeInput getForRecipe(MachineRecipe<RI, RO> recipe) {
    return getForInput(recipe.getInput());
  }

  protected boolean consumeContainer(
      ItemStack input, ItemStack remainder, MachineRecipe<RI, RO> recipe) {
    return false;
  }

  @Override
  public MachineRecipeResult<RI, RO, ItemStack> apply(ItemStack input, boolean test) {
    MachineRecipe<RI, RO> recipe = getRecipe(input);
    if (recipe == null) return null;
    int amount = getForRecipe(recipe).getAmount();
    if (input.getCount() < amount) return null;
    ItemStack remainder =
        ProjectRuntime.envProxy.hasRecipeRemainder(input)
            ? ProjectRuntime.envProxy.getRecipeRemainder(input)
            : ItemStack.EMPTY;
    ItemStack adjusted;
    if (!test && !remainder.isEmpty() && !consumeContainer(input, remainder, recipe)) {
      if (input.getCount() != amount) return null;
      adjusted = remainder.copy();
    } else adjusted = input.copyWithCount(input.getCount() - amount);
    return recipe.getResult(adjusted);
  }

  protected MachineRecipe<RI, RO> getRecipe(ItemStack input) {
    return input == null || input.isEmpty() ? null : index.find(input, tagEpoch);
  }

  protected void addToCache(MachineRecipe<RI, RO> recipe) {
    IRecipeInput input = getForRecipe(recipe);
    int[] keys = null;
    if (input instanceof ExactStackInput
        || input instanceof NativeIngredientInput nativeInput
            && !nativeInput.getIngredient().isCustom()
            && nativeInput.getIngredient().getValues() instanceof HolderSet.Direct<?>)
      keys =
          input.getInputs().stream()
              .mapToInt(stack -> BuiltInRegistries.ITEM.getId(stack.getItem()))
              .distinct()
              .toArray();
    index.removeIf(previous -> previous.getInput().equals(recipe.getInput()));
    index.add(recipe, keys);
  }

  protected void removeCachedRecipes(RI input) {
    index.removeIf(recipe -> input.equals(recipe.getInput()));
  }

  protected void removeStoredRecipe(MachineRecipe<RI, RO> recipe) {
    recipes.remove(recipe.getInput());
    removeCachedRecipes(recipe.getInput());
  }

  protected void storeRecipe(MachineRecipe<RI, RO> recipe) {
    recipes.put(recipe.getInput(), recipe);
    addToCache(recipe);
  }

  private boolean mayMatchItem(MachineRecipe<RI, RO> recipe, ItemStack stack) {
    IRecipeInput input = getForRecipe(recipe);
    if (input instanceof DeferredInput lazy) input = lazy.resolvedInput();
    if (input == null || input.getClass() != NativeIngredientInput.class) return true;
    Ingredient ingredient = input.getIngredient();
    if (ingredient.getCustomIngredient() instanceof MaterialTagIngredient material)
      return material.test(stack);
    if (ingredient.isCustom()) return true;
    Class<?> membership = ingredient.getValues().getClass();
    if (membership != HolderSet.Direct.class && membership != HolderSet.Named.class) return true;
    return input.matches(stack);
  }

  @Override
  public Iterable<? extends MachineRecipe<RI, RO>> getRecipes() {
    return () ->
        new Iterator<>() {
          private final Iterator<MachineRecipe<RI, RO>> entries = recipes.values().iterator();
          private MachineRecipe<RI, RO> last;

          @Override
          public boolean hasNext() {
            return entries.hasNext();
          }

          @Override
          public MachineRecipe<RI, RO> next() {
            return last = entries.next();
          }

          @Override
          public void remove() {
            entries.remove();
            removeCachedRecipes(last.getInput());
          }
        };
  }

  @Override
  public boolean isIterable() {
    return true;
  }
}
