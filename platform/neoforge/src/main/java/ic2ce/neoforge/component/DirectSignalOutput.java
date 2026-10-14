package ic2ce.neoforge.component;

import ic2ce.neoforge.block.NativeBlockEntity;

/**
 * Direct redstone sends one server-owned neighbor notification for each actual level transition.
 */
public class DirectSignalOutput extends SignalOutput {
  public DirectSignalOutput(NativeBlockEntity parent) {
    super(parent);
  }

  @Override
  public void onChange() {
    var world = parent.getLevel();
    if (world != null && !world.isClientSide() && !parent.isRemoved())
      world.updateNeighborsAt(parent.getBlockPos(), parent.getBlockState().getBlock());
  }
}
