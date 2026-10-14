package ic2ce.api.entity.boat;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;

/**
 * Native boat physics owns passengers/collisions; hull policies add distinct fall and one-shot drop
 * behavior.
 */
public abstract class AbstractBoatEntity extends Boat {
  private boolean destructionPaid;

  public AbstractBoatEntity(EntityType<? extends Boat> type, Level world, Supplier<Item> drop) {
    super(type, world, drop);
  }

  public AbstractBoatEntity(
      EntityType<? extends Boat> type,
      Level world,
      Supplier<Item> drop,
      double x,
      double y,
      double z) {
    this(type, world, drop);
    setPos(x, y, z);
    xo = x;
    yo = y;
    zo = z;
  }

  public ItemStack getExtraDropItemStack() {
    return ItemStack.EMPTY;
  }

  public abstract BoatType getOverrideBoatType();

  public boolean brokenByFalling() {
    return true;
  }

  public boolean canFloatOn(FluidState fluid) {
    return fluid.is(FluidTags.WATER);
  }

  @Override
  protected SoundEvent getPaddleSound() {
    return SoundEvents.BOAT_PADDLE_WATER;
  }

  @Override
  protected void checkFallDamage(
      double vertical, boolean grounded, BlockState floor, BlockPos pos) {
    if (grounded
        && !isPassenger()
        && brokenByFalling()
        && fallDistance > 3
        && level() instanceof ServerLevel server) {
      destroy(server, getDropItem());
      return;
    }
    super.checkFallDamage(vertical, grounded, floor, pos);
  }

  @Override
  public void destroy(ServerLevel world, Item drop) {
    if (destructionPaid) return;
    destructionPaid = true;
    var extra = getExtraDropItemStack();
    if (!extra.isEmpty() && world.getGameRules().get(GameRules.ENTITY_DROPS))
      spawnAtLocation(world, extra.copy());
    super.destroy(world, drop);
  }
}
