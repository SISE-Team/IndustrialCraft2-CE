package ic2ce.neoforge.client.widget;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.Objects;

/** A fluid-slot projection reads its actual native reservoir live. */
public class ReservoirSlot extends FluidWidget {
  private final Ic2FluidTank tank;

  public static ReservoirSlot createFluidSlot(
      MachineScreen<?> gui, int x, int y, Ic2FluidTank tank) {
    return new ReservoirSlot(gui, x, y, 18, 18, tank);
  }

  protected ReservoirSlot(
      MachineScreen<?> gui, int x, int y, int width, int height, Ic2FluidTank tank) {
    super(gui, x, y, width, height);
    this.tank = Objects.requireNonNull(tank);
  }

  @Override
  protected Ic2FluidStack getFluidStack() {
    return tank.getFluidStack();
  }
}
