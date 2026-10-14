package ic2ce.neoforge.explosion;

import ic2ce.api.entity.block.ExplosiveEntity;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.EntityDefinitions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Industrial demolition charge has its own fuse, loot retention and entity damage profile. */
public class PrimedDemolitionCharge extends ExplosiveEntity {
  private static final int FUSE_TICKS = 60;
  private static final float POWER = 5.5F, DROPS = .9F, ENTITY_DAMAGE = .3F;

  public PrimedDemolitionCharge(Level world, double x, double y, double z) {
    super(
        EntityDefinitions.ITNT,
        world,
        x,
        y,
        z,
        FUSE_TICKS,
        POWER,
        DROPS,
        ENTITY_DAMAGE,
        BlockDefinitions.ITNT.defaultBlockState(),
        0);
  }

  public PrimedDemolitionCharge(EntityType<? extends PrimedDemolitionCharge> type, Level world) {
    super(type, world);
    setFuse(FUSE_TICKS);
    explosivePower = POWER;
    dropRate = DROPS;
    damageVsEntities = ENTITY_DAMAGE;
    renderBlockState = BlockDefinitions.ITNT.defaultBlockState();
  }
}
