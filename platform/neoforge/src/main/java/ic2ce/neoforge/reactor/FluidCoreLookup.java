package ic2ce.neoforge.reactor;

import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.NativeComponent;
import net.minecraft.core.BlockPos;

/** Live world-thread shell membership; interior blocks and removed cores never qualify. */
public class FluidCoreLookup extends NativeComponent {
  private NuclearCore reactor;
  private long searchedTick = Long.MIN_VALUE;

  public FluidCoreLookup(NativeBlockEntity owner) {
    super(owner);
  }

  private boolean valid(NuclearCore core) {
    var world = parent.getLevel();
    return core != null
        && world != null
        && core.getLevel() == world
        && !core.isRemoved()
        && core.isFluidCooled()
        && ReactorStructure.onShell(core.getBlockPos(), parent.getBlockPos())
        && world.hasChunkAt(core.getBlockPos())
        && world.getBlockEntity(core.getBlockPos()) == core;
  }

  public NuclearCore getReactor() {
    var world = parent.getLevel();
    if (world == null) return null;
    if (valid(reactor)) return reactor;
    reactor = null;
    long tick = world.getGameTime();
    if (searchedTick == tick) return null;
    searchedTick = tick;
    var position = parent.getBlockPos();
    var cursor = new BlockPos.MutableBlockPos();
    for (int y = -2; y <= 2; y++)
      for (int z = -2; z <= 2; z++)
        for (int x = -2; x <= 2; x++) {
          if (Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z))) != 2) continue;
          cursor.set(position.getX() + x, position.getY() + y, position.getZ() + z);
          if (world.hasChunkAt(cursor)
              && world.getBlockEntity(cursor) instanceof NuclearCore core
              && valid(core)) {
            reactor = core;
            return core;
          }
        }
    return null;
  }

  @Override
  public void onUnloaded() {
    reactor = null;
    searchedTick = Long.MIN_VALUE;
  }

  @Override
  public void onNeighborChange(net.minecraft.world.level.block.Block changed) {
    searchedTick = Long.MIN_VALUE;
    reactor = null;
  }
}
