package ic2ce.api.energy.prefab;

import ic2ce.api.energy.tile.*;
import ic2ce.api.info.ILocatable;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Addon receiver admits only its finite free capacity and returns unaccepted packet energy. */
public class BasicSink extends BasicEnergyTile implements IEnergySink {
  protected int tier;

  public BasicSink(BlockEntity owner, double capacity, int tier) {
    super(owner, capacity);
    this.tier = tier(tier);
  }

  public BasicSink(ILocatable owner, double capacity, int tier) {
    super(owner, capacity);
    this.tier = tier(tier);
  }

  public BasicSink(Level world, BlockPos position, double capacity, int tier) {
    super(world, position, capacity);
    this.tier = tier(tier);
  }

  public void setSinkTier(int tier) {
    this.tier = tier(tier);
  }

  @Override
  public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction face) {
    return true;
  }

  @Override
  public double getDemandedEnergy() {
    return getFreeCapacity();
  }

  @Override
  public double injectEnergy(Direction face, double amount, double voltage) {
    if (!Double.isFinite(amount) || amount <= 0 || !Double.isFinite(voltage) || voltage < 0)
      return amount;
    return amount - addEnergy(amount);
  }

  @Override
  public int getSinkTier() {
    return tier;
  }

  @Override
  protected String getNbtTagName() {
    return "IC2BasicSink";
  }
}
