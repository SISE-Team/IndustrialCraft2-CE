package ic2ce.neoforge.inventory;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.ICannerBottleRecipeManager;
import ic2ce.api.recipe.ICannerEnrichRecipeManager;
import ic2ce.api.recipe.IFillFluidContainerRecipeManager;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.util.FluidContainerOutputMode;
import ic2ce.neoforge.processing.fluid.MultiPolicyCanner;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/** Explicit input/commit protocols for solid, enrichment, filling and emptying modes. */
public class CanningInput extends RecipeInputInventory<Object, Object, Object> {
  private final MultiPolicyCanner machine;

  public CanningInput(MultiPolicyCanner machine, String name, int count) {
    super(machine, name, count, null);
    this.machine = machine;
  }

  @Override
  public boolean accepts(ItemStack additive) {
    return switch (machine.getMode()) {
      case BottleSolid, EnrichLiquid -> super.accepts(additive);
      case BottleLiquid, EmptyLiquid -> false;
    };
  }

  @Override
  protected boolean allowEmptyInput() {
    return true;
  }

  @Override
  protected Object getInput(ItemStack additive) {
    var container = machine.canInputSlot.get();
    var fluid = machine.inputTank.getFluidStack();
    return switch (machine.getMode()) {
      case BottleSolid -> new ICannerBottleRecipeManager.RawInput(container, additive);
      case EnrichLiquid -> new ICannerEnrichRecipeManager.RawInput(fluid, additive);
      case BottleLiquid -> new IFillFluidContainerRecipeManager.Input(container, fluid);
      case EmptyLiquid -> container;
    };
  }

  @Override
  protected void setInput(Object adjusted) {
    Objects.requireNonNull(adjusted);
    switch (machine.getMode()) {
      case BottleSolid -> {
        var input = (ICannerBottleRecipeManager.RawInput) adjusted;
        machine.canInputSlot.put(input.container);
        put(input.fill);
      }
      case EnrichLiquid -> {
        var input = (ICannerEnrichRecipeManager.RawInput) adjusted;
        drainInput(input.fluid);
        put(input.additive);
      }
      case BottleLiquid -> {
        var input = (IFillFluidContainerRecipeManager.Input) adjusted;
        drainInput(input.fluid);
        machine.canInputSlot.put(input.container);
      }
      case EmptyLiquid -> machine.canInputSlot.put((ItemStack) adjusted);
    }
  }

  private void drainInput(Ic2FluidStack remaining) {
    var tank = machine.inputTank;
    int before = tank.getFluidAmount();
    int after = remaining == null || remaining.isEmpty() ? 0 : remaining.getAmountMb();
    if (after > before || after > 0 && !tank.getFluidStack().hasExactFluid(remaining))
      throw new IllegalStateException("Canning recipe changed input fluid instead of consuming it");
    tank.drainMbUnchecked(before - after, false);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected MachineRecipeResult<Object, Object, Object> getOutputFor(
      Object input, boolean acceptanceOnly) {
    return (MachineRecipeResult<Object, Object, Object>)
        (MachineRecipeResult<?, ?, ?>) getOutput(input, acceptanceOnly);
  }

  protected MachineRecipeResult<?, ?, ?> getOutput(Object input, boolean acceptanceOnly) {
    var world = machine.getLevel();
    return switch (machine.getMode()) {
      case BottleSolid ->
          Recipes.cannerBottle
              .get(world)
              .apply((ICannerBottleRecipeManager.RawInput) input, acceptanceOnly);
      case EnrichLiquid ->
          Recipes.cannerEnrich
              .get(world)
              .apply((ICannerEnrichRecipeManager.RawInput) input, acceptanceOnly);
      case BottleLiquid ->
          Recipes.fillFluidContainer.apply(
              (IFillFluidContainerRecipeManager.Input) input,
              FluidContainerOutputMode.EmptyFullToOutput,
              acceptanceOnly);
      case EmptyLiquid -> {
        var fluid = machine.inputTank.getFluidStack();
        yield Recipes.emptyFluidContainer.apply(
            (ItemStack) input,
            fluid == null || fluid.isEmpty() ? null : fluid.getFluid(),
            FluidContainerOutputMode.EmptyFullToOutput,
            acceptanceOnly);
      }
    };
  }
}
