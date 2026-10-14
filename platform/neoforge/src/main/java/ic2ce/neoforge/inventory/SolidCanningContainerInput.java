package ic2ce.neoforge.inventory;

import ic2ce.api.recipe.ICannerBottleRecipeManager;
import ic2ce.api.recipe.Recipes;
import ic2ce.neoforge.processing.solid.SolidPackager;
import net.minecraft.world.item.ItemStack;

/** Solid containers are selected by the bottle recipe, independently of fluid capabilities. */
public class SolidCanningContainerInput extends FluidContainerInventory {
  private final SolidPackager machine;

  public SolidCanningContainerInput(SolidPackager machine, String name, int count) {
    super(machine, name, count);
    this.machine = machine;
  }

  @Override
  public boolean accepts(ItemStack container) {
    return container != null
        && !container.isEmpty()
        && Recipes.cannerBottle
                .get(machine.getLevel())
                .apply(
                    new ICannerBottleRecipeManager.RawInput(container, machine.inputSlot.get()),
                    true)
            != null;
  }
}
