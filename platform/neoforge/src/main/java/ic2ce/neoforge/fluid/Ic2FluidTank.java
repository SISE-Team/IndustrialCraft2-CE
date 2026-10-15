package ic2ce.neoforge.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.engine.fluid.FluidAmounts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

/** Component-sensitive fluid buffer; simulation never edits storage or request values. */
public class Ic2FluidTank {
  private int capacity;
  private Ic2FluidStack contents;

  public Ic2FluidTank(int capacity) {
    setCapacity(capacity);
  }

  public final int getCapacity() {
    return capacity;
  }

  public final void setCapacity(int capacity) {
    if (capacity < 0) throw new IllegalArgumentException("Negative fluid capacity");
    this.capacity = capacity;
  }

  public final Ic2FluidStack getFluidStack() {
    return contents;
  }

  public final void setFluidStack(Ic2FluidStack value) {
    contents = value == null || value.isEmpty() ? null : value;
  }

  public final int getFluidAmount() {
    return contents == null ? 0 : contents.getAmountMb();
  }

  public final boolean isEmpty() {
    return contents == null || contents.isEmpty();
  }

  public final boolean hasExactFluid(Fluid fluid) {
    return contents != null && contents.hasExactFluid(fluid);
  }

  public final void fromNbt(CompoundTag data) {
    setFluidStack(data.contains("Empty") ? null : Ic2FluidStack.read(data));
  }

  public final CompoundTag toNbt(CompoundTag data) {
    if (isEmpty()) {
      data.remove("FluidName");
      data.remove("Amount");
      data.remove("Tag");
      data.remove("Components");
      data.putString("Empty", "");
    } else {
      data.remove("Empty");
      contents.toNbt(data);
    }
    return data;
  }

  public final Ic2FluidStack drainMb(int amount, boolean simulate) {
    return drain(amount, simulate, true);
  }

  public final Ic2FluidStack drainMbUnchecked(int amount, boolean simulate) {
    return drain(amount, simulate, false);
  }

  private Ic2FluidStack drain(int requested, boolean simulate, boolean external) {
    if (isEmpty() || external && !canDrain()) return Ic2FluidStack.EMPTY;
    int amount = FluidAmounts.drain(getFluidAmount(), requested);
    if (amount == 0) return Ic2FluidStack.EMPTY;
    if (!simulate && amount == getFluidAmount()) {
      Ic2FluidStack result = contents;
      contents = null;
      return result;
    }
    Ic2FluidStack result = contents.copyWithAmountMb(amount);
    if (!simulate) contents.decreaseMb(amount);
    return result;
  }

  public final int drainMb(Ic2FluidStack request, boolean simulate) {
    return drainMbLimited(request, request == null ? 0 : request.getAmountMb(), simulate);
  }

  public final int drainMbUnchecked(Ic2FluidStack request, boolean simulate) {
    return drain(request, request == null ? 0 : request.getAmountMb(), simulate, false);
  }

  public final int drainMbLimited(Ic2FluidStack request, int maximum, boolean simulate) {
    return drain(request, maximum, simulate, true);
  }

  private int drain(Ic2FluidStack request, int maximum, boolean simulate, boolean external) {
    if (request == null
        || request.isEmpty()
        || isEmpty()
        || !contents.hasExactFluid(request)
        || external && !canDrain()) return 0;
    int amount = FluidAmounts.drain(getFluidAmount(), Math.min(maximum, request.getAmountMb()));
    if (!simulate && amount > 0) {
      if (amount == getFluidAmount()) contents = null;
      else contents.decreaseMb(amount);
    }
    return amount;
  }

  public final int fillMb(Ic2FluidStack request, boolean simulate) {
    return fillMbLimited(request, request == null ? 0 : request.getAmountMb(), simulate);
  }

  public final int fillMbUnchecked(Ic2FluidStack request, boolean simulate) {
    return fill(request, request == null ? 0 : request.getAmountMb(), simulate, false);
  }

  public final int fillMbLimited(Ic2FluidStack request, int maximum, boolean simulate) {
    return fill(request, maximum, simulate, true);
  }

  private int fill(Ic2FluidStack request, int maximum, boolean simulate, boolean external) {
    if (request == null
        || request.isEmpty()
        || !isEmpty() && !contents.hasExactFluid(request)
        || external && !canFill(request.getFluid())) return 0;
    int amount =
        FluidAmounts.fill(capacity, getFluidAmount(), Math.min(maximum, request.getAmountMb()));
    if (!simulate && amount > 0) {
      if (isEmpty()) contents = request.copyWithAmountMb(amount);
      else contents.increaseMb(amount);
    }
    return amount;
  }

  protected boolean canDrain() {
    return true;
  }

  protected boolean canFill(Fluid fluid) {
    return true;
  }
}
