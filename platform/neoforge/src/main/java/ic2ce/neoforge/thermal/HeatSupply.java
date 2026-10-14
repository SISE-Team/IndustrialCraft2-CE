package ic2ce.neoforge.thermal;

import ic2ce.api.energy.tile.IHeatSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/** Native heat endpoint adapter; every simulated/committed draw is bounded by remaining demand. */
public final class HeatSupply {
  private HeatSupply() {}

  public static int fromSide(Level world, BlockPos owner, Direction facing, int maximum) {
    if (world == null || world.isClientSide() || maximum <= 0) return 0;
    var position = owner.relative(facing);
    if (!world.hasChunkAt(position)
        || !(world.getBlockEntity(position) instanceof IHeatSource source)) return 0;
    var contact = facing.getOpposite();
    int offered = Math.clamp(source.drawHeat(contact, maximum, true), 0, maximum);
    return offered == 0 ? 0 : Math.clamp(source.drawHeat(contact, offered, false), 0, offered);
  }

  public static int collect(Level world, BlockPos owner, int maximum) {
    int remaining = Math.max(0, maximum);
    for (var direction : Direction.values()) {
      remaining -= fromSide(world, owner, direction, remaining);
      if (remaining == 0) break;
    }
    return Math.max(0, maximum) - remaining;
  }
}
