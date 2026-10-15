package ic2ce.neoforge.explosion;

import ic2ce.api.entity.block.ExplosiveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Industrial demolition charges also prime on authorized physical mining. */
public class DemolitionBlockEntity extends ExplosiveBlockEntity {
  public DemolitionBlockEntity(BlockPos position, BlockState state) {
    super(ExplosionBlockEntities.ITNT, position, state);
  }

  @Override
  protected boolean explodeOnRemoval() {
    return true;
  }

  @Override
  protected ExplosiveEntity getEntity(LivingEntity igniter) {
    return getLevel() == null
        ? null
        : new PrimedDemolitionCharge(
            getLevel(),
            worldPosition.getX() + .5,
            worldPosition.getY() + .5,
            worldPosition.getZ() + .5);
  }
}
