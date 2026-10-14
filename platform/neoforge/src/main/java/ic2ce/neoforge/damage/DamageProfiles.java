package ic2ce.neoforge.damage;

import ic2ce.core.*;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.LivingEntity;

/**
 * Damage holders belong to the live registry generation; restarting a server cannot reuse a retired
 * world's sources.
 */
public final class DamageProfiles {
  public static final ResourceKey<DamageType> ELECTRICITY_KEY =
      ResourceKey.create(Registries.DAMAGE_TYPE, ProjectRuntime.getIdentifier("electricity"));
  public static final ResourceKey<DamageType> NUKE_KEY =
      ResourceKey.create(Registries.DAMAGE_TYPE, ProjectRuntime.getIdentifier("nuke"));
  public static final ResourceKey<DamageType> RADIATION_KEY =
      ResourceKey.create(Registries.DAMAGE_TYPE, ProjectRuntime.getIdentifier("radiation"));

  private record Snapshot(
      HolderLookup.Provider registry,
      DamageSource electricity,
      DamageSource nuke,
      DamageSource radiation) {}

  private static volatile Snapshot snapshot;

  private DamageProfiles() {}

  private static Snapshot current() {
    var registry = ProjectRuntime.envProxy.getRegistryLookup();
    if (registry == null)
      throw new IllegalStateException("Native damage registry is not available");
    var known = snapshot;
    return known != null && known.registry == registry ? known : resolve(registry);
  }

  private static synchronized Snapshot resolve(HolderLookup.Provider registry) {
    var known = snapshot;
    if (known != null && known.registry == registry) return known;
    var types = registry.lookupOrThrow(Registries.DAMAGE_TYPE);
    return snapshot =
        new Snapshot(
            registry,
            new DamageSource(types.getOrThrow(ELECTRICITY_KEY)),
            new DamageSource(types.getOrThrow(NUKE_KEY)),
            new DamageSource(types.getOrThrow(RADIATION_KEY)));
  }

  public static DamageSource electricity() {
    return current().electricity;
  }

  public static DamageSource nuke() {
    return current().nuke;
  }

  public static DamageSource radiation() {
    return current().radiation;
  }

  public static DamageSource getNukeSource(LivingEntity igniter) {
    return igniter == null
        ? nuke()
        : new DamageSource(
            igniter
                .level()
                .registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(NUKE_KEY),
            igniter,
            igniter);
  }
}
