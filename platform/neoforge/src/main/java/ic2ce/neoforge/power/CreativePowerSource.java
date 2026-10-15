package ic2ce.neoforge.power;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.IEnergyAcceptor;
import ic2ce.api.energy.tile.IMultiEnergySource;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Explicit unlimited creative source with a fixed ten-packet low-voltage output policy. */
@NotClassic
public class CreativePowerSource extends NativeBlockEntity implements IMultiEnergySource {
  private boolean attached;

  public CreativePowerSource(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.CREATIVE_GENERATOR, position, block);
  }

  @Override
  public double getOfferedEnergy() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public void drawEnergy(double amount) {}

  @Override
  public int getSourceTier() {
    return 1;
  }

  @Override
  public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side) {
    return true;
  }

  @Override
  public boolean sendMultipleEnergyPackets() {
    return true;
  }

  @Override
  public int getMultipleEnergyPacketAmount() {
    return 10;
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (!getLevel().isClientSide() && !attached) {
      EnergyNet.instance.addBlockEntityTile(this);
      attached = true;
    }
  }

  @Override
  protected void onUnloaded() {
    if (attached) {
      EnergyNet.instance.removeTile(this);
      attached = false;
    }
    super.onUnloaded();
  }
}
