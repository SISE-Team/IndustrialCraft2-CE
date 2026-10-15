package ic2ce.neoforge.vehicle;

import ic2ce.api.entity.boat.*;
import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.registry.BoatDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.SideServices;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Electrical hull spends four observed EU from real outward equipment before applying its own
 * propulsion.
 */
public class ElectricHull extends AbstractBoatEntity {
  private static final EquipmentSlot[] SOURCES = {
    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };
  private boolean accelerated;

  public ElectricHull(
      net.minecraft.world.entity.EntityType<? extends AbstractBoatEntity> type, Level world) {
    super(type, world, () -> ItemDefinitions.ELECTRIC_BOAT);
  }

  public ElectricHull(
      net.minecraft.world.entity.EntityType<? extends AbstractBoatEntity> type,
      Level world,
      double x,
      double y,
      double z) {
    this(type, world);
    setPos(x, y, z);
  }

  @Override
  public BoatType getOverrideBoatType() {
    return BoatDefinitions.ELECTRIC;
  }

  @Override
  public boolean brokenByFalling() {
    return false;
  }

  @Override
  public boolean fireImmune() {
    return true;
  }

  @Override
  public boolean isOnFire() {
    return false;
  }

  protected double getAccelerationFactor() {
    return accelerated ? 1.5 : .25;
  }

  @Override
  public void tick() {
    accelerated = false;
    var driver = getControllingPassenger();
    if (driver instanceof Player owner && SideServices.keyboard().isForwardKeyDown(owner)) {
      for (var slot : SOURCES) {
        var source = owner.getItemBySlot(slot);
        if (source.isEmpty()
            || ElectricItem.manager.discharge(source, 4, Integer.MAX_VALUE, true, true, true) < 4)
          continue;
        if (level().isClientSide()) {
          accelerated = true;
          break;
        }
        double paid =
            ElectricItem.manager.discharge(source, 4, Integer.MAX_VALUE, true, true, false);
        if (paid < 4) {
          if (paid > 0) ElectricItem.manager.charge(source, paid, Integer.MAX_VALUE, true, false);
          continue;
        }
        accelerated = true;
        break;
      }
    }
    super.tick();
    if (accelerated && driver != null) {
      double radians = Math.toRadians(getYRot());
      var motion = getDeltaMovement();
      setDeltaMovement(
          motion.x - Math.sin(radians) * .04, motion.y, motion.z + Math.cos(radians) * .04);
    }
  }
}
