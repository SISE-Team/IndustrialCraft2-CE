package ic2.core;

import ic2.core.IC2;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

/**
 * 26.1.2 damage sources reference registry entries ({@code ic2:electricity},
 * {@code ic2:nuke}, {@code ic2:radiation}); the JSON definitions live in
 * data/ic2/damage_type/. Instances are built lazily so they resolve after
 * registry freeze.
 */
public final class Ic2DamageSource {
   public static final ResourceKey<DamageType> ELECTRICITY_KEY = ResourceKey.create(Registries.DAMAGE_TYPE, IC2.getIdentifier("electricity"));
   public static final ResourceKey<DamageType> NUKE_KEY = ResourceKey.create(Registries.DAMAGE_TYPE, IC2.getIdentifier("nuke"));
   public static final ResourceKey<DamageType> RADIATION_KEY = ResourceKey.create(Registries.DAMAGE_TYPE, IC2.getIdentifier("radiation"));

   private static volatile DamageSource electricitySrc;
   private static volatile DamageSource nukeSrc;
   private static volatile DamageSource radiationSrc;

   private Ic2DamageSource() {}

   // 26.1.2: damage types are data-driven per-world, so resolve lazily from the
   // running server (cached to keep reference equality for armor checks).
   private static Holder<DamageType> holder(ResourceKey<DamageType> key) {
      net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
      if (server == null) {
         throw new IllegalStateException("Damage type " + key + " requested before server start");
      }
      return server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key);
   }

   public static DamageSource electricity() {
      if (electricitySrc == null) {
         electricitySrc = new DamageSource(holder(ELECTRICITY_KEY));
      }
      return electricitySrc;
   }

   public static DamageSource nuke() {
      if (nukeSrc == null) {
         nukeSrc = new DamageSource(holder(NUKE_KEY));
      }
      return nukeSrc;
   }

   public static DamageSource radiation() {
      if (radiationSrc == null) {
         radiationSrc = new DamageSource(holder(RADIATION_KEY));
      }
      return radiationSrc;
   }

   public static DamageSource getNukeSource(LivingEntity igniter) {
      return igniter != null ? new DamageSource(holder(NUKE_KEY), igniter, igniter) : nuke();
   }
}
