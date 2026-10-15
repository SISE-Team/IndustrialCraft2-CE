package ic2ce.neoforge.fluid;

import ic2ce.api.fluid.Ic2FluidStack;

/** A side-masked ownership snapshot never exposes the mutable native tank content. */
public final class FluidTankInfo {
  public static final int ALL_SIDES = 63;
  private final int drainSideMask, fillSideMask, capacity;
  private final Ic2FluidStack content;

  public FluidTankInfo(int drainMask, int fillMask, int capacity, Ic2FluidStack content) {
    if (capacity < 0) throw new IllegalArgumentException("Negative fluid view capacity");
    drainSideMask = drainMask & ALL_SIDES;
    fillSideMask = fillMask & ALL_SIDES;
    this.capacity = capacity;
    this.content = content == null || content.isEmpty() ? null : content.copy();
  }

  public int getDrainSideMask() {
    return drainSideMask;
  }

  public int getFillSideMask() {
    return fillSideMask;
  }

  public int getCapacity() {
    return capacity;
  }

  public Ic2FluidStack getContent() {
    return content == null ? null : content.copy();
  }
}
