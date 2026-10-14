package ic2ce.neoforge.power;

import ic2ce.api.energy.tile.IHeatSource;
import ic2ce.core.profile.NotClassic;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Heat conversion follows the currently facing source rather than a stale previous neighbor. */
@NotClassic
public class ThermalConverter extends DemandDrivenConverter {
  private final double perHeat =
      0.5
          * NativeConfigValues.getDouble(
              ProjectConfiguration.get(), "balance/energy/generator/Stirling");
  protected IHeatSource source;
  private BlockEntity sourceEntity;

  public ThermalConverter(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.STIRLING_GENERATOR, position, block);
  }

  protected void updateSource() {
    var world = getLevel();
    sourceEntity = world == null ? null : world.getBlockEntity(worldPosition.relative(getFacing()));
    source = sourceEntity instanceof IHeatSource heat && !sourceEntity.isRemoved() ? heat : null;
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
        ? Math.max(0, source.drawHeat(side, Math.max(0, source.getConnectionBandwidth(side)), true))
        : 0;
  }

  @Override
  protected void drawEnergyAvailable(int amount) {
    if (validSource()) source.drawHeat(getFacing().getOpposite(), amount, false);
  }

  @Override
  protected double getMultiplier() {
    return perHeat;
  }
}
