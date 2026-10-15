package ic2ce.neoforge.vehicle;

import ic2ce.api.entity.boat.*;
import ic2ce.neoforge.registry.BoatDefinitions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Flexible rubber hull retains ordinary fall vulnerability and its distinct damaged-hull material.
 */
public class RubberHull extends AbstractBoatEntity {
  public RubberHull(EntityType<? extends AbstractBoatEntity> type, Level world) {
    super(type, world, () -> VehicleItems.RUBBER_BOAT);
  }

  public RubberHull(
      EntityType<? extends AbstractBoatEntity> type, Level world, double x, double y, double z) {
    this(type, world);
    setPos(x, y, z);
  }

  @Override
  public ItemStack getExtraDropItemStack() {
    return new ItemStack(VehicleItems.BROKEN_RUBBER_BOAT);
  }

  @Override
  public BoatType getOverrideBoatType() {
    return BoatDefinitions.RUBBER;
  }
}
