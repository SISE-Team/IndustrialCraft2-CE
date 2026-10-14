package ic2ce.api.energy.prefab;

import ic2ce.api.energy.tile.*;
import ic2ce.api.info.ILocatable;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Independent bidirectional addon buffer; subclasses specify their actual allowed faces. */
public abstract class BasicSinkSource extends BasicEnergyTile
    implements IEnergySink, IEnergySource {
  protected int sinkTier, sourceTier;

  public BasicSinkSource(BlockEntity owner, double capacity, int sinkTier, int sourceTier) {
    super(owner, capacity);
    setSinkTier(sinkTier);
    setSourceTier(sourceTier);
  }

  public BasicSinkSource(ILocatable owner, double capacity, int sinkTier, int sourceTier) {
    super(owner, capacity);
    setSinkTier(sinkTier);
    setSourceTier(sourceTier);
  }

  public BasicSinkSource(
      Level world, BlockPos position, double capacity, int sinkTier, int sourceTier) {
    super(world, position, capacity);
    setSinkTier(sinkTier);
    setSourceTier(sourceTier);
  }

  public void setSinkTier(int tier) {
    sinkTier = tier(tier);
  }

  public void setSourceTier(int tier) {
    sourceTier = tier(tier);
    reservePacketCapacity(tier);
  }

  @Override
  public int getSinkTier() {
    return sinkTier;
  }

  @Override
  public int getSourceTier() {
    return sourceTier;
  }

  @Override
  public double getDemandedEnergy() {
    return getFreeCapacity();
  }

  @Override
  public double getOfferedEnergy() {
    return getEnergyStored();
  }

  @Override
  public double injectEnergy(Direction face, double amount, double voltage) {
    if (!Double.isFinite(amount) || amount <= 0 || !Double.isFinite(voltage) || voltage < 0)
      return amount;
    return amount - addEnergy(amount);
  }

  @Override
  public void drawEnergy(double amount) {
    if (writable() && Double.isFinite(amount) && amount > 0)
      setEnergyStored(getEnergyStored() - Math.min(amount, getEnergyStored()));
  }

  @Override
  protected String getNbtTagName() {
    return "IC2BasicSinkSource";
  }
}
