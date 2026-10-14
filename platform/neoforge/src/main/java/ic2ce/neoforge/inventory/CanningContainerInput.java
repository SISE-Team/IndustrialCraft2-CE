package ic2ce.neoforge.inventory;

import ic2ce.api.recipe.ICannerBottleRecipeManager;
import ic2ce.api.recipe.Recipes;
import ic2ce.neoforge.processing.fluid.MultiPolicyCanner;
import net.minecraft.world.item.ItemStack;

/** The container policy follows the selected canning mode. */
public class CanningContainerInput extends FluidContainerInventory {
  private final MultiPolicyCanner machine;

  public CanningContainerInput(MultiPolicyCanner machine, String name, int count) {
    super(machine, name, count);
    this.machine = machine;
  }

  @Override
  public boolean accepts(ItemStack container) {
    if (container == null || container.isEmpty()) return false;
    return switch (machine.getMode()) {
      case BottleSolid ->
          Recipes.cannerBottle
                  .get(machine.getLevel())
                  .apply(
                      new ICannerBottleRecipeManager.RawInput(container, machine.inputSlot.get()),
                      true)
              != null;
      case BottleLiquid, EmptyLiquid, EnrichLiquid -> super.accepts(container);
    };
  }
}
