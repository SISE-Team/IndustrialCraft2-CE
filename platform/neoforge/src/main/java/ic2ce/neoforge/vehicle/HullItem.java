package ic2ce.neoforge.vehicle;

import java.util.Objects;
import net.minecraft.stats.Stats;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;

/**
 * Native boat factory creates the registered entity directly, validates collision and consumes only
 * successful placement.
 */
public class HullItem extends Item {
  private final EntityType<? extends Boat> type;

  public HullItem(
      Class<? extends Boat> ignored, EntityType<? extends Boat> type, Properties properties) {
    super(properties);
    this.type = Objects.requireNonNull(type);
  }

  @Override
  public InteractionResult use(Level world, Player owner, InteractionHand hand) {
    var stack = owner.getItemInHand(hand);
    if (!stack.is(this)) return InteractionResult.PASS;
    var hit = getPlayerPOVHitResult(world, owner, ClipContext.Fluid.ANY);
    if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
    var eye = owner.getEyePosition();
    for (var entity :
        world.getEntities(
            owner,
            owner.getBoundingBox().expandTowards(owner.getViewVector(1).scale(5)).inflate(1),
            EntitySelector.NO_SPECTATORS.and(Entity::isPickable)))
      if (entity.getBoundingBox().inflate(entity.getPickRadius()).contains(eye))
        return InteractionResult.PASS;
    if (!owner.mayUseItemAt(hit.getBlockPos(), hit.getDirection(), stack))
      return InteractionResult.PASS;
    var boat = type.create(world, EntitySpawnReason.SPAWN_ITEM_USE);
    if (boat == null) return InteractionResult.FAIL;
    var at = hit.getLocation();
    boat.setPos(at.x, at.y, at.z);
    boat.setYRot(owner.getYRot());
    if (!world.noCollision(boat, boat.getBoundingBox())) return InteractionResult.FAIL;
    if (!world.isClientSide()) {
      if (!world.addFreshEntity(boat)) return InteractionResult.FAIL;
      world.gameEvent(owner, GameEvent.ENTITY_PLACE, at);
      if (!owner.getAbilities().instabuild) stack.shrink(1);
      owner.awardStat(Stats.ITEM_USED.get(this));
    }
    return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
  }
}
