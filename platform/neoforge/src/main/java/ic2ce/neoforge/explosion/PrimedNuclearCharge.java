package ic2ce.neoforge.explosion;

import ic2ce.api.entity.block.ExplosiveEntity;
import ic2ce.neoforge.item.tool.MechanicalWrench;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.EntityDefinitions;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Nuclear payload preserves its distinct 300-tick fuse and can be defused by one paid physical
 * wrench use.
 */
public class PrimedNuclearCharge extends ExplosiveEntity {
  private static final int FUSE_TICKS = 300;

  public PrimedNuclearCharge(
      Level world, double x, double y, double z, float power, int radiation) {
    super(
        EntityDefinitions.NUKE,
        world,
        x,
        y,
        z,
        FUSE_TICKS,
        validPower(power),
        .05F,
        1.5F,
        BlockDefinitions.NUKE.defaultBlockState(),
        Math.max(0, radiation));
  }

  public PrimedNuclearCharge(EntityType<? extends PrimedNuclearCharge> type, Level world) {
    super(type, world);
    setFuse(FUSE_TICKS);
    explosivePower = 1;
    dropRate = .05F;
    damageVsEntities = 1.5F;
    radiationRange = 1;
    renderBlockState = BlockDefinitions.NUKE.defaultBlockState();
  }

  private static float validPower(float power) {
    if (!Float.isFinite(power) || power < 0)
      throw new IllegalArgumentException("Invalid nuclear explosive power");
    return power;
  }

  @Override
  public InteractionResult interact(Player actor, InteractionHand hand, Vec3 hit) {
    if (actor == null
        || hand == null
        || actor.level() != level()
        || isRemoved()
        || actor.distanceToSqr(this) > 64
        || actor.isSpectator()) return InteractionResult.PASS;
    var stack = actor.getItemInHand(hand);
    if (!(stack.getItem() instanceof MechanicalWrench wrench) || !wrench.canTakeDamage(stack, 1))
      return InteractionResult.PASS;
    if (level().isClientSide()) return InteractionResult.SUCCESS;
    if (!(level() instanceof net.minecraft.server.level.ServerLevel server)
        || !actor.mayInteract(server, blockPosition())) return InteractionResult.PASS;
    wrench.damage(stack, 1, actor, hand);
    discard();
    return InteractionResult.CONSUME;
  }
}
