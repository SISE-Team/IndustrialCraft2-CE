package ic2ce.neoforge.vehicle;

import ic2ce.api.entity.boat.*;
import ic2ce.neoforge.registry.BoatDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Carbon hull absorbs falls independently of the flexible rubber and electrical propulsion
 * policies.
 */
public class CarbonHull extends AbstractBoatEntity {
  public CarbonHull(EntityType<? extends AbstractBoatEntity> type, Level world) {
    super(type, world, () -> ItemDefinitions.CARBON_BOAT);
  }

  public CarbonHull(
      EntityType<? extends AbstractBoatEntity> type, Level world, double x, double y, double z) {
    this(type, world);
    setPos(x, y, z);
  }

  @Override
  public boolean brokenByFalling() {
    return false;
  }

  @Override
  public BoatType getOverrideBoatType() {
    return BoatDefinitions.CARBON;
  }
}
