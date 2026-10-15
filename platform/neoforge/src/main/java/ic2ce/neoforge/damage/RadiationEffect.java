package ic2ce.neoforge.damage;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;

/**
 * Radiation has a bounded native damage pulse schedule and uses the target world's current damage
 * holders.
 */
public class RadiationEffect extends MobEffect {
  public static RadiationEffect radiation;

  public RadiationEffect(MobEffectCategory category, int color) {
    super(category, color);
  }

  @Override
  public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
    if (this == radiation && entity != null && entity.level() == world) {
      float damage = Math.max(0, amplifier) / 100 + .5F;
      entity.hurtServer(
          world,
          new net.minecraft.world.damagesource.DamageSource(
              world
                  .registryAccess()
                  .lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                  .getOrThrow(DamageProfiles.RADIATION_KEY)),
          damage);
    }
    return true;
  }

  @Override
  public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
    if (this != radiation || duration <= 0) return false;
    int interval = 25 >> Math.clamp(amplifier, 0, 5);
    return interval == 0 || duration % interval == 0;
  }

  public static Holder<MobEffect> radiationHolder() {
    if (radiation == null)
      throw new IllegalStateException("Native radiation effect is not registered");
    return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(radiation);
  }

  public void applyTo(LivingEntity entity, int duration, int amplifier) {
    if (entity != null && duration > 0)
      entity.addEffect(
          new MobEffectInstance(
              BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this), duration, Math.max(0, amplifier)));
  }
}
