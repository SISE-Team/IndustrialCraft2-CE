package ic2ce.neoforge.inventory;

import ic2ce.api.recipe.IMachineRecipeManager;
import ic2ce.api.recipe.IRecipeInput;
import ic2ce.api.recipe.Recipes;
import ic2ce.core.block.IInventorySlotHolder;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/** Item-input/item-output processing binds the recipe manager to the current world. */
public class ItemRecipeInput
    extends RecipeInputInventory<IRecipeInput, Collection<ItemStack>, ItemStack> {
  public ItemRecipeInput(
      IInventorySlotHolder<?> holder,
      String name,
      int count,
      Recipes.IGetter<
              ? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>>
          manager) {
    super(holder, name, count, Objects.requireNonNull(manager));
  }

  @Override
  protected ItemStack getInput(ItemStack stack) {
    return stack;
  }

  @Override
  protected void setInput(ItemStack adjusted) {
    put(adjusted);
  }
}
