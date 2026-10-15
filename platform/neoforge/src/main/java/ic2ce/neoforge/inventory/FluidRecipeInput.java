package ic2ce.neoforge.inventory;

import ic2ce.api.recipe.ILiquidAcceptManager;
import java.util.Objects;
import net.minecraft.world.level.material.Fluid;

/** Live recipe-manager policy: datapack changes remain visible without rebuilding the slot. */
public class FluidRecipeInput extends FluidContainerInventory {
  private final ILiquidAcceptManager policy;

  public FluidRecipeInput(
      IInventorySlotHolder<?> holder, String name, int count, ILiquidAcceptManager policy) {
    this(holder, name, Access.I, count, InvSide.TOP, OpType.Drain, policy);
  }

  public FluidRecipeInput(
      IInventorySlotHolder<?> holder,
      String name,
      Access access,
      int count,
      InvSide side,
      OpType operation,
      ILiquidAcceptManager policy) {
    super(holder, name, access, count, side, operation);
    this.policy = Objects.requireNonNull(policy);
  }

  @Override
  protected boolean acceptsLiquid(Fluid fluid) {
    return fluid != null && policy.acceptsFluid(fluid);
  }

  @Override
  protected Iterable<Fluid> getPossibleFluids() {
    return policy.getAcceptedFluids();
  }
}
