package ic2ce.neoforge.component;

import ic2ce.neoforge.block.NativeBlockEntity;

/** Comparator output refreshes only the native analog circuit, independently of direct redstone. */
public class AnalogSignalOutput extends SignalOutput {
  public AnalogSignalOutput(NativeBlockEntity parent) {
    super(parent);
  }

  @Override
  public void onChange() {
    var world = parent.getLevel();
    if (world != null && !world.isClientSide() && !parent.isRemoved())
      world.updateNeighbourForOutputSignal(parent.getBlockPos(), parent.getBlockState().getBlock());
  }
}
