package ic2ce.api.recipe;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.util.FluidContainerOutputMode;
import java.util.Collection;
import net.minecraft.world.item.ItemStack;

/**
 * Filled container proposals own the physical stack and preserve the nullable exhausted fluid
 * sentinel.
 */
public interface IFillFluidContainerRecipeManager
    extends IMachineRecipeManager<
        Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input> {
  MachineRecipeResult<Void, Collection<ItemStack>, Input> apply(
      Input input, FluidContainerOutputMode mode, boolean acceptanceOnly);

  final class Input {
    public final ItemStack container;
    public final Ic2FluidStack fluid;

    public Input(ItemStack container, Ic2FluidStack fluid) {
      this.container =
          container == null || container.isEmpty() ? ItemStack.EMPTY : container.copy();
      this.fluid = fluid == null ? null : fluid.isEmpty() ? Ic2FluidStack.EMPTY : fluid.copy();
    }
  }
}
