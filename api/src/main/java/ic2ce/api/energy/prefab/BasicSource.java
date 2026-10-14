package ic2ce.api.energy.prefab;

import ic2ce.api.energy.tile.*;
import ic2ce.api.info.ILocatable;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Addon source reserves a complete voltage packet but can never draw into negative stored energy.
 */
public class BasicSource extends BasicEnergyTile implements IEnergySource {
  protected int tier;

  public BasicSource(BlockEntity owner, double capacity, int tier) {
    super(owner, capacity);
    setSourceTier(tier);
  }

  public BasicSource(ILocatable owner, double capacity, int tier) {
    super(owner, capacity);
    setSourceTier(tier);
  }

  public BasicSource(Level world, BlockPos position, double capacity, int tier) {
    super(world, position, capacity);
    setSourceTier(tier);
  }

  public void setSourceTier(int tier) {
    this.tier = tier(tier);
    reservePacketCapacity(tier);
  }

  @Override
  public boolean emitsEnergyTo(IEnergyAcceptor acceptor, Direction face) {
    return true;
  }

  @Override
  public double getOfferedEnergy() {
    return getEnergyStored();
  }

  @Override
  public void drawEnergy(double amount) {
    if (writable() && Double.isFinite(amount) && amount > 0)
      setEnergyStored(getEnergyStored() - Math.min(amount, getEnergyStored()));
  }

  @Override
  public int getSourceTier() {
    return tier;
  }

  @Override
  protected String getNbtTagName() {
    return "IC2BasicSource";
  }
}
