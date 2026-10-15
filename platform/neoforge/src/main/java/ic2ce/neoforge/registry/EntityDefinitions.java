package ic2ce.neoforge.registry;


import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.projectile.MiningBeam;
import ic2ce.neoforge.explosion.PrimedDemolitionCharge;
import ic2ce.neoforge.explosion.PrimedNuclearCharge;
import ic2ce.neoforge.vehicle.CarbonHull;
import ic2ce.neoforge.vehicle.ElectricHull;
import ic2ce.neoforge.vehicle.RubberHull;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public final class EntityDefinitions {
   public static final EntityType<PrimedDemolitionCharge> ITNT = register(
      "itnt", Builder.<PrimedDemolitionCharge>of(PrimedDemolitionCharge::new, MobCategory.MISC).fireImmune().sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(10)
   );
   public static final EntityType<PrimedNuclearCharge> NUKE = register(
      "nuke", Builder.<PrimedNuclearCharge>of(PrimedNuclearCharge::new, MobCategory.MISC).fireImmune().sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(10)
   );
   public static final EntityType<MiningBeam> LASER_BULLET = register(
      "laser_bullet", Builder.<MiningBeam>of(MiningBeam::new, MobCategory.MISC).fireImmune().sized(0.8F, 0.8F).clientTrackingRange(8).updateInterval(8)
   );
   public static final EntityType<RubberHull> RUBBER_BOAT = register(
      "rubber_boat", Builder.<RubberHull>of(RubberHull::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10)
   );
   public static final EntityType<ElectricHull> ELECTRIC_BOAT = register(
      "electric_boat", Builder.<ElectricHull>of(ElectricHull::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10)
   );
   public static final EntityType<CarbonHull> CARBON_BOAT = register(
      "carbon_boat", Builder.<CarbonHull>of(CarbonHull::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10)
   );

   public static void init() {
   }

   private static <T extends Entity> EntityType<T> register(String name, Builder<T> builder) {
      net.minecraft.resources.ResourceKey<EntityType<?>> key = net.minecraft.resources.ResourceKey.create(
         net.minecraft.core.registries.Registries.ENTITY_TYPE, ProjectIds.id(name)
      );
      EntityType<T> ret = builder.build(key);
      EnvProxy.get().registerEntity(ProjectIds.id(name), ret);
      return ret;
   }
}
