package ic2ce.neoforge.inventory;

import ic2ce.api.recipe.ICannerBottleRecipeManager;
import ic2ce.api.recipe.Recipes;
import ic2ce.neoforge.processing.solid.SolidPackager;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/** Two independent consumed inputs for solid canning. */
public class SolidCanningInput
    extends RecipeInputInventory<
        ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> {
  private final SolidPackager machine;

  public SolidCanningInput(SolidPackager machine, String name, int count) {
    super(machine, name, count, Recipes.cannerBottle);
    this.machine = machine;
  }

  @Override
  protected ICannerBottleRecipeManager.RawInput getInput(ItemStack additive) {
    return new ICannerBottleRecipeManager.RawInput(machine.canInputSlot.get(), additive);
  }

  @Override
  protected void setInput(ICannerBottleRecipeManager.RawInput adjusted) {
    Objects.requireNonNull(adjusted);
    machine.canInputSlot.put(adjusted.container);
    put(adjusted.fill);
  }
}
