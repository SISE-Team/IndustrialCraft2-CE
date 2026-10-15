package ic2ce.neoforge.transport;

import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.fluid.LiquidUtil;
import ic2ce.neoforge.block.NativeBlockEntity;
import net.minecraft.core.Direction;

/**
 * Quantity-reserved native tank transfer. Controllers retain their own routing and payment
 * policies.
 */
public final class FluidOutlet {
  private FluidOutlet() {}

  public static int send(
      NativeBlockEntity sender, Ic2FluidTank source, Direction direction, int maximum) {
    var world = sender.getLevel();
    if (world == null || world.isClientSide() || maximum <= 0 || source.isEmpty()) return 0;
    var position = sender.getBlockPos().relative(direction);
    if (!world.hasChunkAt(position)) return 0;
    var target = world.getBlockEntity(position);
    if (target == null) return 0;
    var side = direction.getOpposite();
    var offered = source.drainMbUnchecked(maximum, true);
    if (offered == null || offered.isEmpty()) return 0;
    int accepted = LiquidUtil.fillTile(target, side, offered.copy(), true);
    if (accepted < 0 || accepted > offered.getAmountMb())
      throw new IllegalStateException("Fluid target violated its preview budget");
    if (accepted == 0) return 0;
    var reserved = source.drainMbUnchecked(accepted, false);
    if (reserved == null || reserved.getAmountMb() != accepted || !reserved.hasExactFluid(offered))
      throw new IllegalStateException("Fluid source changed during reservation");
    int delivered;
    try {
      delivered = LiquidUtil.fillTile(target, side, reserved.copy(), false);
    } catch (RuntimeException | Error failure) {
      if (source.fillMbUnchecked(reserved, false) != accepted)
        failure.addSuppressed(new IllegalStateException("Fluid reservation refund failed"));
      throw failure;
    }
    if (delivered < 0 || delivered > accepted)
      throw new IllegalStateException("Fluid target exceeded its reserved quantity");
    if (delivered < accepted
        && source.fillMbUnchecked(reserved.copyWithAmountMb(accepted - delivered), false)
            != accepted - delivered)
      throw new IllegalStateException("Unused fluid reservation could not be returned");
    if (delivered > 0) sender.setChanged();
    return delivered;
  }
}
