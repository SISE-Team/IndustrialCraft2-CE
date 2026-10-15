package ic2ce.neoforge.inventory;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.util.FluidContainerOutputMode;
import ic2ce.core.block.IInventorySlotHolder;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.fluid.LiquidUtil;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.mutable.MutableObject;

/** Container/tank conversion with pure planning and quantity-checked commits. */
public class FluidContainerInventory extends ConsumableInventory {
  private OpType operation;

  public FluidContainerInventory(IInventorySlotHolder<?> holder, String name, int count) {
    this(holder, name, Access.I, count, InvSide.TOP, OpType.Drain);
  }

  public FluidContainerInventory(
      IInventorySlotHolder<?> holder,
      String name,
      Access access,
      int count,
      InvSide side,
      OpType operation) {
    super(holder, name, access, count, side);
    setOpType(operation);
  }

  private boolean drains() {
    return operation == OpType.Drain || operation == OpType.Both;
  }

  private boolean fills() {
    return operation == OpType.Fill || operation == OpType.Both;
  }

  @Override
  public boolean accepts(ItemStack stack) {
    if (stack == null || stack.isEmpty() || !LiquidUtil.isFluidContainer(stack)) return false;
    if (drains()) {
      Ic2FluidStack content = Ic2FluidStack.get(stack);
      if (content != null && !content.isEmpty() && acceptsLiquid(content.getFluid())) return true;
    }
    return fills() && LiquidUtil.isFillableFluidContainer(stack, getPossibleFluids());
  }

  private LiquidUtil.FluidOperationResult planDrain(Fluid fluid, int amount) {
    if (!drains() || amount <= 0 || get().isEmpty() || fluid != null && !acceptsLiquid(fluid))
      return null;
    var plan =
        LiquidUtil.drainContainer(get(), fluid, amount, FluidContainerOutputMode.EmptyFullToOutput);
    return plan == null || plan.fluidChange.isEmpty() || !acceptsLiquid(plan.fluidChange.getFluid())
        ? null
        : plan;
  }

  private LiquidUtil.FluidOperationResult planFill(Ic2FluidStack fluid) {
    if (!fills() || fluid == null || fluid.isEmpty() || get().isEmpty()) return null;
    return LiquidUtil.fillContainer(get(), fluid, FluidContainerOutputMode.EmptyFullToOutput);
  }

  private void apply(
      LiquidUtil.FluidOperationResult plan, MutableObject<ItemStack> output, boolean simulate) {
    output.setValue(plan.extraOutput);
    if (!simulate) put(plan.inPlaceOutput);
  }

  public Ic2FluidStack drain(
      Fluid fluid, int maximum, MutableObject<ItemStack> output, boolean simulate) {
    output.setValue(null);
    var plan = planDrain(fluid, maximum);
    if (plan == null) return null;
    apply(plan, output, simulate);
    return plan.fluidChange;
  }

  public int fill(Ic2FluidStack fluid, MutableObject<ItemStack> output, boolean simulate) {
    output.setValue(null);
    var plan = planFill(fluid);
    if (plan == null) return 0;
    apply(plan, output, simulate);
    return plan.fluidChange.getAmountMb();
  }

  public boolean transferToTank(
      Ic2FluidTank tank, MutableObject<ItemStack> output, boolean simulate) {
    output.setValue(null);
    Ic2FluidStack stored = tank.getFluidStack();
    Fluid required = stored == null || stored.isEmpty() ? null : stored.getFluid();
    var plan = planDrain(required, Math.max(0, tank.getCapacity() - tank.getFluidAmount()));
    if (plan == null) return false;
    int accepted = tank.fillMb(plan.fluidChange, true);
    if (accepted <= 0) return false;
    if (accepted != plan.fluidChange.getAmountMb()) {
      plan = planDrain(required, accepted);
      if (plan == null || tank.fillMb(plan.fluidChange, true) != plan.fluidChange.getAmountMb())
        return false;
    }
    if (!simulate && tank.fillMb(plan.fluidChange, false) != plan.fluidChange.getAmountMb())
      throw new IllegalStateException("Fluid target changed between planning and commit");
    apply(plan, output, simulate);
    return true;
  }

  public boolean transferFromTank(
      Ic2FluidTank tank, MutableObject<ItemStack> output, boolean simulate) {
    output.setValue(null);
    Ic2FluidStack available = tank.drainMb(tank.getFluidAmount(), true);
    var plan = planFill(available);
    if (plan == null || plan.fluidChange.isEmpty()) return false;
    int required = plan.fluidChange.getAmountMb();
    if (tank.drainMb(plan.fluidChange, true) != required) return false;
    if (!simulate && tank.drainMb(plan.fluidChange, false) != required)
      throw new IllegalStateException("Fluid source changed between planning and commit");
    apply(plan, output, simulate);
    return true;
  }

  public boolean processIntoTank(Ic2FluidTank tank, OutputInventory output) {
    return process(tank, output, true);
  }

  public boolean processFromTank(Ic2FluidTank tank, OutputInventory output) {
    return process(tank, output, false);
  }

  private boolean process(Ic2FluidTank tank, OutputInventory output, boolean into) {
    if (isEmpty()) return false;
    var remainder = new MutableObject<ItemStack>();
    boolean possible =
        into ? transferToTank(tank, remainder, true) : transferFromTank(tank, remainder, true);
    ItemStack extra = remainder.getValue();
    if (!possible || extra != null && !extra.isEmpty() && !output.canAdd(extra)) return false;
    boolean committed =
        into ? transferToTank(tank, remainder, false) : transferFromTank(tank, remainder, false);
    if (!committed) return false;
    extra = remainder.getValue();
    if (extra != null && !extra.isEmpty() && output.add(extra) != 0)
      throw new IllegalStateException("Container output capacity changed before commit");
    return true;
  }

  public void setOpType(OpType operation) {
    this.operation = Objects.requireNonNull(operation);
  }

  protected boolean acceptsLiquid(Fluid fluid) {
    return true;
  }

  protected Iterable<Fluid> getPossibleFluids() {
    return null;
  }

  public enum OpType {
    Drain,
    Fill,
    Both,
    None
  }
}
