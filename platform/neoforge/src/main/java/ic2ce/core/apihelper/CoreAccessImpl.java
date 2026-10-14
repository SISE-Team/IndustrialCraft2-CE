package ic2ce.core.apihelper;

import ic2ce.api.info.IInfoProvider;
import ic2ce.api.network.INetworkManager;
import ic2ce.api.tile.IRotorProvider;
import ic2ce.api.util.CoreAccess;
import ic2ce.api.util.CoreAccessRef;
import ic2ce.core.util.ItemInfo;
import ic2ce.neoforge.damage.DamageProfiles;
import ic2ce.neoforge.damage.RadiationEffect;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Platform bindings for the loader-independent public API service registry. */
public final class CoreAccessImpl implements CoreAccess {
  private static final CoreAccessImpl INSTANCE = new CoreAccessImpl();
  private static final IInfoProvider ITEMS = new ItemInfo();

  private CoreAccessImpl() {}

  public static void init() {
    CoreAccessRef.install(INSTANCE);
    ic2ce.api.entity.block.ExplosionAccess.install(
        entity -> {
          var explosion =
              new ic2ce.neoforge.explosion.NativeBlast(
                  entity.level(),
                  entity,
                  entity.getX(),
                  entity.getY(),
                  entity.getZ(),
                  entity.explosivePower,
                  entity.dropRate,
                  entity.radiationRange > 0
                      ? ic2ce.neoforge.explosion.NativeBlast.Type.Nuclear
                      : ic2ce.neoforge.explosion.NativeBlast.Type.Normal,
                  entity.getCausingEntity(),
                  entity.radiationRange);
          explosion.doExplosion();
        });
  }

  @Override
  public boolean isCallingFromIc2() {
    return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
        .getCallerClass()
        .getPackageName()
        .startsWith("ic2ce.");
  }

  @Override
  public IInfoProvider getItemInfo() {
    return ITEMS;
  }

  @Override
  public INetworkManager getClientNetworkManager() {
    return ProjectRuntime.network.get(false);
  }

  @Override
  public INetworkManager getServerNetworkManager() {
    return ProjectRuntime.network.get(true);
  }

  @Override
  public DamageSource getElectricDamageSource() {
    return DamageProfiles.electricity();
  }

  @Override
  public DamageSource getNukeExplosionDamageSource() {
    return DamageProfiles.nuke();
  }

  @Override
  public DamageSource getRadiationDamageSource() {
    return DamageProfiles.radiation();
  }

  @Override
  public MobEffect getRadiationStatusEffect() {
    return RadiationEffect.radiation;
  }

  @Override
  public <T extends BlockEntity & IRotorProvider> void registerRotorProvider(
      BlockEntityType<T> type) {
    ProjectRuntime.sideProxy.registerRotorProvider(type);
  }
}
