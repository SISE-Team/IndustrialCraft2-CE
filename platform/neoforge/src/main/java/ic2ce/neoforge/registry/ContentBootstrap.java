package ic2ce.neoforge.registry;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.damage.RadiationEffect;
import ic2ce.neoforge.runtime.EnvProxy;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Registry dependency order is explicit: effect/fluid definitions precede blocks, their entities,
 * item forms and native menus.
 */
public final class ContentBootstrap {
  private ContentBootstrap() {}

  public static void init() {
    var radiation = new RadiationEffect(MobEffectCategory.HARMFUL, 5149489);
    RadiationEffect.radiation = radiation;
    EnvProxy.get().registerStatusEffect(
        ProjectIds.id("radiation"), radiation);
    FluidDefinitions.init();
    BlockDefinitions.init();
    BlockEntityDefinitions.init();
    ItemDefinitions.init();
    MenuDefinitions.init();
  }
}
