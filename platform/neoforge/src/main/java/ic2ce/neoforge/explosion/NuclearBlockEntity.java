package ic2ce.neoforge.explosion;

import ic2ce.api.entity.block.ExplosiveEntity;
import ic2ce.core.util.*;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Nuclear admission owns configuration, finite payload validation and actual server activity
 * attribution.
 */
public abstract class NuclearBlockEntity extends ExplosiveBlockEntity {
  protected NuclearBlockEntity(
      BlockEntityType<? extends NuclearBlockEntity> type, BlockPos position, BlockState state) {
    super(type, position, state);
  }

  public abstract float getNukeExplosivePower();

  public abstract int getRadiationRange();

  @Override
  protected ExplosiveEntity getEntity(LivingEntity igniter) {
    if (getLevel() == null
        || !NativeConfigValues.getBool(ProjectConfiguration.get(), "protection/enableNuke"))
      return null;
    float power = getNukeExplosivePower();
    int radiation = getRadiationRange();
    return Float.isFinite(power) && power >= 0 && radiation >= 0
        ? new PrimedNuclearCharge(
            getLevel(),
            worldPosition.getX() + .5,
            worldPosition.getY() + .5,
            worldPosition.getZ() + .5,
            power,
            radiation)
        : null;
  }

  @Override
  public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing) {
    super.onPlaced(stack, placer, facing);
    if (placer instanceof Player actor)
      ProjectRuntime.log.info(
          DiagnosticArea.PlayerActivity,
          "Nuclear device placed by %s/%s at %s",
          actor.getGameProfile().name(),
          actor.getGameProfile().id(),
          Util.formatPosition(this));
  }

  @Override
  protected void onIgnite(LivingEntity igniter) {
    ProjectRuntime.log.info(
        DiagnosticArea.PlayerActivity,
        "Nuclear device at %s primed by %s",
        Util.formatPosition(this),
        igniter == null ? "indirect signal" : igniter.getName().getString());
  }

  public static class ClassicNuclearCharge extends NuclearBlockEntity {
    public ClassicNuclearCharge(BlockPos position, BlockState state) {
      super(BlockEntityDefinitions.CLASSIC_NUKE, position, state);
    }

    @Override
    public float getNukeExplosivePower() {
      float limit =
          NativeConfigValues.getFloat(
              ProjectConfiguration.get(), "protection/nukeExplosionPowerLimit");
      return Float.isFinite(limit) && limit >= 0 ? Math.min(35, limit) : -1;
    }

    @Override
    public int getRadiationRange() {
      return 1;
    }
  }
}
