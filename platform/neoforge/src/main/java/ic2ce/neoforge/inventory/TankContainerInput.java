package ic2ce.neoforge.inventory;

import ic2ce.core.block.IInventorySlotHolder;
import ic2ce.core.fluid.Ic2FluidTank;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.level.material.Fluid;

/** The current tank content determines compatibility, including after a tank swap. */
public class TankContainerInput extends FluidContainerInventory {
  public final Ic2FluidTank tank;

  public TankContainerInput(
      IInventorySlotHolder<?> holder,
      String name,
      Access access,
      int count,
      InvSide side,
      OpType operation,
      Ic2FluidTank tank) {
    super(holder, name, access, count, side, operation);
    this.tank = Objects.requireNonNull(tank);
  }

  @Override
  protected boolean acceptsLiquid(Fluid fluid) {
    return fluid != null && (tank.isEmpty() || tank.hasExactFluid(fluid));
  }

  @Override
  protected Iterable<Fluid> getPossibleFluids() {
    var content = tank.getFluidStack();
    return content == null || content.isEmpty() ? null : List.of(content.getFluid());
  }
}
