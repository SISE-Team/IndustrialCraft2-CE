package ic2.core;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class Ic2Potion extends MobEffect {
   public static Ic2Potion radiation;

   public Ic2Potion(MobEffectCategory type, int liquidColor) {
      super(type, liquidColor);
   }

   @Override
   public boolean applyEffectTick(net.minecraft.server.level.ServerLevel world, LivingEntity entity, int amplifier) {
      if (this == radiation) {
         entity.hurtServer(world, Ic2DamageSource.radiation(), amplifier / 100 + 0.5F);
      }
      return true;
   }

   @Override
   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      if (this == radiation) {
         int rate = amplifier >= 5 ? 0 : 25 >> Math.max(0, amplifier);
         return rate > 0 ? duration % rate == 0 : true;
      } else {
         return false;
      }
   }

   /** 26.1.2: effect APIs want Holder<MobEffect>; resolved from the registry after registration. */
   public static net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> radiationHolder() {
      return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(radiation);
   }

   public void applyTo(LivingEntity entity, int duration, int amplifier) {
      MobEffectInstance effect = new MobEffectInstance(
            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(radiation), duration, amplifier);
      entity.addEffect(effect);
   }
}
