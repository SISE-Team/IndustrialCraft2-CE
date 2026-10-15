package ic2ce.neoforge.power;

import ic2ce.api.energy.tile.IKineticSource;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Kinetic conversion negotiates the current shaft's bandwidth and extraction side. */
@NotClassic
public class KineticConverter extends DemandDrivenConverter {
  private final double perKinetic =
      0.25
          * NativeConfigValues.getDouble(
              ProjectConfiguration.get(), "balance/energy/generator/Kinetic");
  protected IKineticSource source;
  private BlockEntity sourceEntity;

  public KineticConverter(BlockPos position, BlockState block) {
    super(PowerBlockEntities.KINETIC_GENERATOR, position, block);
  }

  protected void updateSource() {
    var world = getLevel();
    sourceEntity = world == null ? null : world.getBlockEntity(worldPosition.relative(getFacing()));
    source =
        sourceEntity instanceof IKineticSource kinetic && !sourceEntity.isRemoved()
            ? kinetic
            : null;
  }

  private boolean validSource() {
    if (sourceEntity == null || sourceEntity.isRemoved()) updateSource();
    return source != null;
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    updateSource();
  }

  @Override
  protected void setFacing(Level world, Direction direction) {
    super.setFacing(world, direction);
    updateSource();
  }

  @Override
  protected void onNeighborChange(Block block) {
    super.onNeighborChange(block);
    updateSource();
  }

  @Override
  protected int getEnergyAvailable() {
    var side = getFacing().getOpposite();
    return validSource()
        ? Math.max(
            0,
            source.drawKineticEnergy(side, Math.max(0, source.getConnectionBandwidth(side)), true))
        : 0;
  }

  @Override
  protected void drawEnergyAvailable(int amount) {
    if (validSource()) source.drawKineticEnergy(getFacing().getOpposite(), amount, false);
  }

  @Override
  protected double getMultiplier() {
    return perKinetic;
  }
}
