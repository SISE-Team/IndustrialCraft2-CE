package ic2ce.api.event;

import java.util.Objects;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * Cancellable native blast admission with owned immutable scalar geometry and live source
 * attribution.
 */
public final class ExplosionEvent extends LevelEvent
    implements net.neoforged.bus.api.ICancellableEvent {
  public final Entity entity;
  public final Vec3 pos;
  public final double power, rangeLimit;
  public final LivingEntity igniter;
  public final int radiationRange;

  public ExplosionEvent(
      Level world,
      Entity source,
      Vec3 position,
      double power,
      LivingEntity igniter,
      int radiation,
      double reach) {
    Objects.requireNonNull(position);
    if (!Double.isFinite(position.x + position.y + position.z + power + reach)
        || power < 0
        || reach < 0
        || radiation < 0) throw new IllegalArgumentException("Invalid native blast event geometry");
    super(Objects.requireNonNull(world));
    entity = source;
    pos = position;
    this.power = power;
    this.igniter = igniter;
    radiationRange = radiation;
    rangeLimit = reach;
  }
}
