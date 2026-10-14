package ic2ce.api.recipe;

import ic2ce.api.fluid.Ic2FluidStack;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/** Enrichment owns a fluid descriptor and a separate solid additive bill. */
public interface ICannerEnrichRecipeManager
    extends IMachineRecipeManager<
        ICannerEnrichRecipeManager.Input, Ic2FluidStack, ICannerEnrichRecipeManager.RawInput> {
  @Deprecated
  RecipeOutput getOutputFor(
      Ic2FluidStack fluid, ItemStack additive, boolean adjustFluid, boolean adjustAdditive);

  final class Input {
    public final Ic2FluidStack fluid;
    public final IRecipeInput additive;
    private final Ic2FluidStack fluidIdentity;

    public Input(Ic2FluidStack fluid, IRecipeInput additive) {
      fluidIdentity = Objects.requireNonNull(fluid).copy();
      if (fluidIdentity.isEmpty())
        throw new IllegalArgumentException("Enrichment fluid bill must be positive");
      this.fluid = fluidIdentity.copy();
      this.additive = Objects.requireNonNull(additive);
    }

    public boolean matches(Ic2FluidStack fluid, ItemStack additive) {
      return fluid != null
          && additive != null
          && !additive.isEmpty()
          && fluidIdentity.hasExactFluid(fluid)
          && this.additive.matches(additive);
    }
  }

  final class RawInput {
    public final Ic2FluidStack fluid;
    public final ItemStack additive;

    public RawInput(Ic2FluidStack fluid, ItemStack additive) {
      this.fluid = fluid == null ? null : fluid.copy();
      this.additive = additive == null ? ItemStack.EMPTY : additive.copy();
    }
  }
}
