package ic2ce.neoforge.recipe.processing;

import ic2ce.api.recipe.*;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ListedMachineInputs extends OrderedMachineRecipes<IRecipeInput, Object>
    implements IListRecipeManager {
  private static final Object PRESENT = new Object();

  @Override
  protected IRecipeInput getForInput(IRecipeInput input) {
    return input;
  }

  @Override
  protected boolean consumeContainer(
      ItemStack input, ItemStack remainder, MachineRecipe<IRecipeInput, Object> recipe) {
    return true;
  }

  @Override
  public void add(IRecipeInput input) {
    addRecipe(Objects.requireNonNull(input), PRESENT, null, false);
  }

  @Override
  public boolean contains(ItemStack stack) {
    return getRecipe(stack) != null;
  }

  @Override
  public boolean isEmpty() {
    return recipes.isEmpty();
  }

  @Override
  public List<IRecipeInput> getInputs() {
    return new ArrayList<>(recipes.keySet());
  }

  @Override
  public Iterator<IRecipeInput> iterator() {
    Iterator<? extends MachineRecipe<IRecipeInput, Object>> entries = getRecipes().iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return entries.hasNext();
      }

      @Override
      public IRecipeInput next() {
        return entries.next().getInput();
      }

      @Override
      public void remove() {
        entries.remove();
      }
    };
  }

  public boolean addRecipe(
      IRecipeInput input, Object output, CompoundTag metadata, boolean replace) {
    Objects.requireNonNull(input);
    for (ItemStack example : input.getInputs()) {
      var existing = getRecipe(example);
      if (existing != null && !replace) return false;
      while (existing != null) {
        removeStoredRecipe(existing);
        existing = getRecipe(example);
      }
    }
    storeRecipe(new MachineRecipe<>(input, output, metadata));
    // This concrete legacy extension historically returns false even after insertion.
    return false;
  }
}
