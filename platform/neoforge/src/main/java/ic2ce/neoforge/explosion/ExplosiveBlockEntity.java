package ic2ce.neoforge.explosion;

import ic2ce.api.entity.block.ExplosiveEntity;
import ic2ce.core.util.StackUtil;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.SignalInput;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Priming is a server-owned one-shot transition; physical ignition costs follow accepted entity
 * admission.
 */
public abstract class ExplosiveBlockEntity extends InventoryBlockEntity
    implements SignalInput.IRedstoneChangeHandler {
  protected final SignalInput redstone = addComponent(new SignalInput(this));
  private boolean primed;

  protected ExplosiveBlockEntity(
      BlockEntityType<? extends ExplosiveBlockEntity> type, BlockPos position, BlockState state) {
    super(type, position, state);
    redstone.subscribe(this);
  }

  @Override
  public void onRedstoneChange(int signal) {
    if (signal > 0) explode(null, false);
  }

  private boolean actorAllowed(Player actor) {
    return actor != null
        && !actor.isSpectator()
        && actor.level() == getLevel()
        && actor.distanceToSqr(
                worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5)
            <= 64
        && getLevel() instanceof ServerLevel world
        && actor.mayInteract(world, worldPosition);
  }

  @Override
  protected InteractionResult onActivated(
      Player actor, InteractionHand hand, Direction side, Vec3 hit) {
    if (actor == null || hand == null) return InteractionResult.PASS;
    var held = actor.getItemInHand(hand);
    boolean fireCharge = held.is(Items.FIRE_CHARGE), flint = held.is(Items.FLINT_AND_STEEL);
    if (!fireCharge && !flint) return super.onActivated(actor, hand, side, hit);
    if (getLevel() != null && getLevel().isClientSide())
      return actor.isSpectator() ? InteractionResult.PASS : InteractionResult.SUCCESS;
    if (!actorAllowed(actor) || !explode(actor, false)) return InteractionResult.PASS;
    if (fireCharge) StackUtil.consume(actor, hand, StackUtil.sameItem(Items.FIRE_CHARGE), 1);
    else StackUtil.damage(actor, hand, StackUtil.sameItem(Items.FLINT_AND_STEEL), 1);
    return InteractionResult.CONSUME;
  }

  @Override
  protected void onExploded(Explosion explosion) {
    explode(explosion.getIndirectSourceEntity(), true);
  }

  @Override
  protected boolean onRemovedByPlayer(Player actor, boolean harvest) {
    if (!explodeOnRemoval()) return super.onRemovedByPlayer(actor, harvest);
    if (getLevel() != null && getLevel().isClientSide()) return true;
    return actorAllowed(actor) && explode(actor, false);
  }

  @Override
  protected void onEntityCollision(Entity entity) {
    if (entity instanceof Projectile projectile
        && entity.isOnFire()
        && getLevel() instanceof ServerLevel) {
      var owner = projectile.getOwner();
      explode(owner instanceof LivingEntity living ? living : null, false);
    }
  }

  @Override
  public ItemStack adjustDrop(ItemStack stack, boolean wrench) {
    return primed ? null : super.adjustDrop(stack, wrench);
  }

  @Override
  protected List<ItemStack> getAuxDrops(int fortune) {
    return primed ? List.of() : super.getAuxDrops(fortune);
  }

  protected boolean explode(LivingEntity igniter, boolean shortened) {
    if (primed
        || isRemoved()
        || !(getLevel() instanceof ServerLevel world)
        || world.getBlockEntity(worldPosition) != this) return false;
    var entity = getEntity(igniter);
    if (entity == null) return false;
    entity.setCausingEntity(igniter);
    if (shortened) {
      int fuse = entity.getFuse();
      entity.setFuse(world.getRandom().nextInt(Math.max(1, fuse / 4)) + fuse / 8);
    }
    primed = true;
    if (!world.addFreshEntity(entity)) {
      primed = false;
      return false;
    }
    if (!world.removeBlock(worldPosition, false)) {
      entity.discard();
      primed = false;
      return false;
    }
    onIgnite(igniter);
    world.playSound(
        null,
        entity.getX(),
        entity.getY(),
        entity.getZ(),
        SoundEvents.TNT_PRIMED,
        SoundSource.BLOCKS,
        1,
        1);
    return true;
  }

  protected boolean explodeOnRemoval() {
    return false;
  }

  protected abstract ExplosiveEntity getEntity(LivingEntity igniter);

  protected void onIgnite(LivingEntity igniter) {}
}
