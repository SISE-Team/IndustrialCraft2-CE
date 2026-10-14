package ic2ce.neoforge.world.block;

import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.Objects;
import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.*;

/**
 * Resin drag, rubber rebound/anchoring and one-way wool passage are separate physical surface
 * policies.
 */
public class ReactiveSheet extends Block {
  public enum Surface {
    RESIN,
    RUBBER,
    WOOL
  }

  private static final VoxelShape PLATE = Shapes.box(0, 0, 0, 1, .125, 1);
  private final Surface surface;

  public ReactiveSheet(Properties properties, Surface surface) {
    super(properties);
    this.surface = Objects.requireNonNull(surface);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    var state = defaultBlockState();
    return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
  }

  private static boolean solid(BlockGetter world, BlockPos position) {
    return world.getBlockState(position).isCollisionShapeFullBlock(world, position);
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader world, BlockPos position) {
    return switch (surface) {
      case WOOL -> true;
      case RESIN -> solid(world, position.below());
      case RUBBER -> {
        boolean support = solid(world, position.below());
        if (!support)
          for (var direction : Direction.Plane.HORIZONTAL) {
            var adjacent = position.relative(direction);
            if (world.getBlockState(adjacent).is(this) || solid(world, adjacent)) {
              support = true;
              break;
            }
          }
        yield support;
      }
    };
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter world, BlockPos position, CollisionContext context) {
    return PLATE;
  }

  @Override
  public VoxelShape getCollisionShape(
      BlockState state, BlockGetter world, BlockPos position, CollisionContext context) {
    if (surface == Surface.RESIN) return Shapes.empty();
    if (surface == Surface.WOOL
        && context instanceof EntityCollisionContext entityContext
        && entityContext.getEntity() instanceof Player actor
        && (actor.isShiftKeyDown() || actor.getY() < position.getY() + .125 - actor.maxUpStep()))
      return Shapes.empty();
    return PLATE;
  }

  @Override
  public void neighborChanged(
      BlockState state,
      Level world,
      BlockPos position,
      Block changed,
      Orientation orientation,
      boolean notify) {
    if (!world.isClientSide() && !canSurvive(state, world, position)) {
      Block.dropResources(state, world, position, null);
      world.removeBlock(position, false);
    }
  }

  @Override
  public void entityInside(
      BlockState state,
      Level world,
      BlockPos position,
      Entity entity,
      InsideBlockEffectApplier effects,
      boolean precise) {
    var velocity = entity.getDeltaMovement();
    switch (surface) {
      case RESIN -> {
        entity.fallDistance *= .75;
        entity.setDeltaMovement(velocity.multiply(.6, .85, .6));
      }
      case WOOL -> entity.fallDistance *= .95;
      case RUBBER -> {
        if (!world.isEmptyBlock(position.below())) return;
        if (entity instanceof LivingEntity && !supportedAcross(world, position)) {
          if (!world.isClientSide()) {
            world.levelEvent(2001, position, Block.getId(state));
            world.removeBlock(position, false);
          }
          return;
        }
        if (velocity.y <= -.4) {
          double rebound =
              entity instanceof Player actor && ProjectRuntime.keyboard.isJumpKeyDown(actor)
                  ? 1.3
                  : entity instanceof Player actor && actor.isShiftKeyDown() ? .1 : .8;
          entity.fallDistance = 0;
          entity.setDeltaMovement(velocity.x * 1.1, -velocity.y * rebound, velocity.z * 1.1);
        }
      }
    }
  }

  private boolean supportedAcross(LevelReader world, BlockPos position) {
    return anchored(world, position, Direction.EAST) && anchored(world, position, Direction.WEST)
        || anchored(world, position, Direction.NORTH) && anchored(world, position, Direction.SOUTH);
  }

  private boolean anchored(LevelReader world, BlockPos origin, Direction direction) {
    var cursor = origin.mutable();
    for (int length = 0; length < 16; length++) {
      cursor.move(direction);
      if (solid(world, cursor)) return true;
      if (!world.getBlockState(cursor).is(this)) return false;
      var below = cursor.below();
      if (solid(world, below)) return true;
    }
    return false;
  }
}
