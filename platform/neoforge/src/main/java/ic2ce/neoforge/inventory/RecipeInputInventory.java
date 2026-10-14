package ic2ce.neoforge.inventory;

import ic2ce.api.recipe.IMachineRecipeManager;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.recipe.Recipes;
import ic2ce.core.block.IInventorySlotHolder;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/** Typed recipe query/adjustment adapter over native input storage. */
public abstract class RecipeInputInventory<RI, RO, I> extends ConsumableInventory {
  protected Recipes.IGetter<? extends IMachineRecipeManager<RI, RO, I>> recipeManager;

  public RecipeInputInventory(
      IInventorySlotHolder<?> holder,
      String name,
      int count,
      Recipes.IGetter<? extends IMachineRecipeManager<RI, RO, I>> manager) {
    super(holder, name, count);
    recipeManager = manager;
  }

  @Override
  public boolean accepts(ItemStack stack) {
    if (stack == null || stack.isEmpty() && !allowEmptyInput()) return false;
    return getOutputFor(
            getInput(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(Integer.MAX_VALUE)),
            true)
        != null;
  }

  public MachineRecipeResult<RI, RO, I> process() {
    ItemStack stack = get();
    return stack.isEmpty() && !allowEmptyInput() ? null : getOutputFor(getInput(stack), false);
  }

  public void consume(MachineRecipeResult<RI, RO, I> result) {
    Objects.requireNonNull(result);
    if (get().isEmpty() && !allowEmptyInput())
      throw new IllegalStateException("Recipe input disappeared before commit");
    setInput(result.getAdjustedInput());
  }

  public void setRecipeManager(
      Recipes.IGetter<? extends IMachineRecipeManager<RI, RO, I>> manager) {
    recipeManager = Objects.requireNonNull(manager);
  }

  protected boolean allowEmptyInput() {
    return false;
  }

  protected MachineRecipeResult<RI, RO, I> getOutputFor(I input, boolean acceptanceOnly) {
    return Objects.requireNonNull(recipeManager)
        .get(base.getParent().getLevel())
        .apply(input, acceptanceOnly);
  }

  protected abstract I getInput(ItemStack stack);

  protected abstract void setInput(I adjusted);
}
