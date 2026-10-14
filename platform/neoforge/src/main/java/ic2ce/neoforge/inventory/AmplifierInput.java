package ic2ce.neoforge.inventory;

import ic2ce.api.recipe.*;
import ic2ce.core.block.IInventorySlotHolder;
import net.minecraft.world.item.ItemStack;

/** Amplifier stack adaptation is shared; fabrication policies own consumption and output rules. */
public final class AmplifierInput extends RecipeInputInventory<IRecipeInput, Integer, ItemStack> {
  public AmplifierInput(
      IInventorySlotHolder<?> owner,
      Recipes.IGetter<? extends IMachineRecipeManager<IRecipeInput, Integer, ItemStack>> recipes) {
    super(owner, "scrap", 1, recipes);
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
