package ic2ce.neoforge.recipe.processing;

import ic2ce.api.recipe.*;
import ic2ce.api.util.FluidContainerOutputMode;
import ic2ce.core.util.LiquidUtil;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/**
 * Native draining owns the candidate item and returns the actual drained bill without mutating
 * recipe callers.
 */
public final class ContainerDrainingRecipes implements IEmptyFluidContainerRecipeManager {
  public boolean addRecipe(Void input, Output output, CompoundTag metadata, boolean replace) {
    return false;
  }

  @Override
  public MachineRecipeResult<Void, Output, ItemStack> apply(
      ItemStack input, boolean acceptanceOnly) {
    return apply(input, null, FluidContainerOutputMode.AnyToOutput, acceptanceOnly);
  }

  @Override
  public MachineRecipeResult<Void, Output, ItemStack> apply(
      ItemStack input, Fluid required, FluidContainerOutputMode mode, boolean acceptanceOnly) {
    Objects.requireNonNull(mode);
    if (input == null || input.isEmpty()) return null;
    var drained = LiquidUtil.drainContainer(input.copy(), required, Integer.MAX_VALUE, mode);
    if (drained == null || drained.fluidChange == null || drained.fluidChange.isEmpty())
      return null;
    var remainder =
        drained.extraOutput == null || drained.extraOutput.isEmpty()
            ? List.<ItemStack>of()
            : List.of(drained.extraOutput.copy());
    var output = new Output(remainder, drained.fluidChange);
    return new MachineRecipe<Void, Output>(null, output).getResult(drained.inPlaceOutput.copy());
  }

  @Override
  public boolean isIterable() {
    return false;
  }

  @Override
  public Iterable<? extends MachineRecipe<Void, Output>> getRecipes() {
    throw new UnsupportedOperationException(
        "Fluid draining follows actual native item capabilities");
  }
}
