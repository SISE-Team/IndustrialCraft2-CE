package ic2ce.api.recipe;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.util.FluidContainerOutputMode;
import java.util.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/** Container draining separates actual item remainders from an owned fluid output bill. */
public interface IEmptyFluidContainerRecipeManager
    extends IMachineRecipeManager<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack> {
  MachineRecipeResult<Void, Output, ItemStack> apply(
      ItemStack container, Fluid required, FluidContainerOutputMode mode, boolean acceptanceOnly);

  final class Output {
    public final Collection<ItemStack> container;
    public final Ic2FluidStack fluid;

    public Output(Collection<ItemStack> containers, Ic2FluidStack fluid) {
      var owned = new ArrayList<ItemStack>();
      for (var stack : Objects.requireNonNull(containers))
        owned.add(Objects.requireNonNull(stack).copy());
      container = List.copyOf(owned);
      this.fluid = fluid == null || fluid.isEmpty() ? Ic2FluidStack.EMPTY : fluid.copy();
    }
  }
}
