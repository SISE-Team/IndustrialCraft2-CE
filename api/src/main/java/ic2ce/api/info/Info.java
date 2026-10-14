package ic2ce.api.info;

import ic2ce.api.util.CoreAccessRef;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;

/**
 * World-bound damage sources follow current core services and are never retained across server
 * restarts.
 */
public final class Info {
  public static final String MOD_ID = "ic2";

  private Info() {}

  public static boolean isIc2Available() {
    return CoreAccessRef.exists();
  }

  public static IInfoProvider getItemInfo() {
    return CoreAccessRef.get().getItemInfo();
  }

  public static DamageSource getElectricDamageSource() {
    return CoreAccessRef.get().getElectricDamageSource();
  }

  public static DamageSource getNukeExplosionDamageSource() {
    return CoreAccessRef.get().getNukeExplosionDamageSource();
  }

  public static DamageSource getRadiationDamageSource() {
    return CoreAccessRef.get().getRadiationDamageSource();
  }

  public static MobEffect getRadiationStatusEffect() {
    return CoreAccessRef.get().getRadiationStatusEffect();
  }
}
