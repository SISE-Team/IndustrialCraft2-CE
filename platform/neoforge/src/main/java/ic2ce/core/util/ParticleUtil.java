package ic2ce.core.util;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

/**
 * Client combustion particles sample one native front-face point at the reference one-in-eight
 * cadence.
 */
public final class ParticleUtil {
  private ParticleUtil() {}

  public static void showFurnaceFlames(Level world, BlockPos pos, Direction front) {
    if (world == null || !world.isClientSide() || front == null) return;
    var random = world.getRandom();
    if (random.nextInt(8) != 0) return;
    double x = pos.getX() + .5 + front.getStepX() * .52,
        y = pos.getY() + random.nextFloat() * .375,
        z = pos.getZ() + .5 + front.getStepZ() * .52;
    double lateral = (random.nextFloat() - .5) * .625;
    if (front.getAxis() == Direction.Axis.X) z += lateral;
    else x += lateral;
    world.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
    world.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
  }
}
