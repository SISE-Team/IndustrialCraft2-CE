package ic2ce.neoforge.recipe.processing;

import ic2ce.api.recipe.*;
import ic2ce.api.util.FluidContainerOutputMode;
import ic2ce.neoforge.fluid.LiquidUtil;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Native filling separately validates acceptance hints, reserves actual fluid and owns all
 * replacement containers.
 */
public final class ContainerFillingRecipes implements IFillFluidContainerRecipeManager {
  public boolean addRecipe(
      Void input, Collection<ItemStack> output, CompoundTag metadata, boolean replace) {
    return false;
  }

  @Override
  public MachineRecipeResult<Void, Collection<ItemStack>, Input> apply(
      Input input, boolean acceptanceOnly) {
    return apply(input, FluidContainerOutputMode.AnyToOutput, acceptanceOnly);
  }

  @Override
  public MachineRecipeResult<Void, Collection<ItemStack>, Input> apply(
      Input input, FluidContainerOutputMode mode, boolean acceptanceOnly) {
    Objects.requireNonNull(mode);
    if (input == null) return null;
    if (input.fluid != null && input.fluid.isEmpty()) return null;
    boolean container = input.container != null && !input.container.isEmpty(),
        fluid = input.fluid != null && !input.fluid.isEmpty();
    if (!container || !fluid) {
      if (!acceptanceOnly
          || !container && input.fluid == null
          || container && !LiquidUtil.isFillableFluidContainer(input.container)) return null;
      return new MachineRecipe<Void, Collection<ItemStack>>(null, List.of())
          .getResult(new Input(input.container, input.fluid));
    }
    var filled = LiquidUtil.fillContainer(input.container.copy(), input.fluid.copy(), mode);
    if (filled == null || filled.fluidChange == null) return null;
    int paid = filled.fluidChange.getAmountMb();
    if (paid <= 0
        || paid > input.fluid.getAmountMb()
        || !filled.fluidChange.hasExactFluid(input.fluid)) return null;
    List<ItemStack> products =
        filled.extraOutput == null || filled.extraOutput.isEmpty()
            ? List.of()
            : List.of(filled.extraOutput.copy());
    var remaining =
        paid == input.fluid.getAmountMb()
            ? null
            : input.fluid.copyWithAmountMb(input.fluid.getAmountMb() - paid);
    return new MachineRecipe<Void, Collection<ItemStack>>(null, products)
        .getResult(new Input(filled.inPlaceOutput, remaining));
  }

  @Override
  public boolean isIterable() {
    return false;
  }

  @Override
  public Iterable<? extends MachineRecipe<Void, Collection<ItemStack>>> getRecipes() {
    throw new UnsupportedOperationException(
        "Fluid filling follows actual native item capabilities");
  }
}
