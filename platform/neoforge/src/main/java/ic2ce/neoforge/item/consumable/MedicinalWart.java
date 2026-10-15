package ic2ce.neoforge.item.consumable;

import ic2ce.neoforge.damage.RadiationEffect;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * Native medicinal food removes its defined ailments and reduces actual radiation by six hundred
 * ticks.
 */
public class MedicinalWart extends Item {
  public MedicinalWart(Properties properties) {
    super(properties);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity owner) {
    if (!world.isClientSide() && stack.is(this)) {
      owner.removeEffect(MobEffects.NAUSEA);
      owner.removeEffect(MobEffects.MINING_FATIGUE);
      owner.removeEffect(MobEffects.HUNGER);
      owner.removeEffect(MobEffects.SLOWNESS);
      owner.removeEffect(MobEffects.WEAKNESS);
      owner.removeEffect(MobEffects.BLINDNESS);
      owner.removeEffect(MobEffects.POISON);
      owner.removeEffect(MobEffects.WITHER);
      var radiation = owner.getEffect(RadiationEffect.radiationHolder());
      if (radiation != null) {
        owner.removeEffect(RadiationEffect.radiationHolder());
        if (radiation.getDuration() > 600)
          RadiationEffect.radiation.applyTo(
              owner, radiation.getDuration() - 600, radiation.getAmplifier());
      }
    }
    return super.finishUsingItem(stack, world, owner);
  }
}
