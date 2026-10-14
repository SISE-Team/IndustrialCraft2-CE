package ic2ce.neoforge.world.block;

import ic2ce.engine.world.ScaffoldSupport;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.EntityGroups;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/**
 * Sparse physical support snapshots and scheduled collapse preserve distinct wooden/metal
 * reinforcement reach.
 */
public final class StructuralScaffold extends Block {
  @Override
  public int getFlammability(
      BlockState state, BlockGetter world, BlockPos position, Direction face) {
    return ic2ce.neoforge.EnvProxyNeoForge.flammability(state);
  }

  @Override
  public int getFireSpreadSpeed(
      BlockState state, BlockGetter world, BlockPos position, Direction face) {
    return ic2ce.neoforge.EnvProxyNeoForge.fireSpread(state);
  }

  private static final int MAX_COMPONENT = 32768;
  private static final Direction[] FACES = Direction.values(),
      LATERAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
  private static final VoxelShape COLLISION =
      Shapes.box(1d / 32, 0, 1d / 32, 31d / 32, 1, 31d / 32);
  private final int maxDistance;

  private record Snapshot(LongArrayList positions, int[] strength, boolean complete) {}

  public StructuralScaffold(Properties properties, int reach) {
    super(properties);
    if (reach < 0 || reach > 32) throw new IllegalArgumentException("Scaffold reach outside 0..32");
    maxDistance = reach;
  }

  @Override
  public VoxelShape getCollisionShape(
      BlockState state, BlockGetter world, BlockPos position, CollisionContext context) {
    return COLLISION;
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter world, BlockPos position, CollisionContext context) {
    return Shapes.block();
  }

  @Override
  public VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos position) {
    return Shapes.block();
  }

  @Override
  public void entityInside(
      BlockState state,
      Level world,
      BlockPos position,
      Entity entity,
      InsideBlockEffectApplier effects,
      boolean precise) {
    if (!(entity instanceof LivingEntity living)) return;
    living.fallDistance = 0;
    var motion = living.getDeltaMovement();
    double x = Math.clamp(motion.x, -.15, .15), z = Math.clamp(motion.z, -.15, .15);
    double y;
    if (living instanceof Player && living.isShiftKeyDown()) y = living.isInWater() ? .02 : .08;
    else if (living.horizontalCollision
        && living.getType().builtInRegistryHolder().is(EntityGroups.SCAFFOLD_CLIMBABLE)) y = .2;
    else y = -.07;
    living.setDeltaMovement(x, y, z);
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader world, BlockPos position) {
    var plan = snapshot(world, position, this);
    return !plan.complete || plan.strength[0] >= 0;
  }

  @Override
  public void neighborChanged(
      BlockState state,
      Level world,
      BlockPos position,
      Block changed,
      Orientation orientation,
      boolean notify) {
    if (!world.isClientSide()) world.scheduleTick(position, this, 1);
  }

  @Override
  public void tick(BlockState state, ServerLevel world, BlockPos position, RandomSource random) {
    collapse(world, position);
  }

  @Override
  public void randomTick(
      BlockState state, ServerLevel world, BlockPos position, RandomSource random) {
    if (random.nextInt(8) == 0) collapse(world, position);
  }

  private void collapse(ServerLevel world, BlockPos position) {
    if (!world.getBlockState(position).is(this)) return;
    var plan = snapshot(world, position, this);
    if (!plan.complete) return;
    for (int node = 0; node < plan.strength.length; node++)
      if (plan.strength[node] < 0) {
        var target = BlockPos.of(plan.positions.getLong(node));
        if (world.getBlockState(target).getBlock() instanceof StructuralScaffold)
          world.destroyBlock(target, true);
      }
  }

  private static boolean loaded(BlockGetter world, BlockPos position) {
    return !(world instanceof LevelReader reader) || reader.hasChunkAt(position);
  }

  private static Snapshot snapshot(
      BlockGetter world, BlockPos start, StructuralScaffold virtualBlock) {
    var positions = new LongArrayList();
    var blocks = new ArrayList<StructuralScaffold>();
    var indices = new Long2IntOpenHashMap();
    indices.defaultReturnValue(-1);
    positions.add(start.asLong());
    blocks.add(virtualBlock);
    indices.put(start.asLong(), 0);
    boolean complete = true;
    var cursor = new BlockPos.MutableBlockPos();
    for (int node = 0; node < positions.size(); node++) {
      var position = BlockPos.of(positions.getLong(node));
      for (var face : FACES) {
        cursor.setWithOffset(position, face);
        if (!loaded(world, cursor)) {
          complete = false;
          continue;
        }
        long key = cursor.asLong();
        if (indices.containsKey(key)) continue;
        var state = world.getBlockState(cursor);
        if (!(state.getBlock() instanceof StructuralScaffold block)) continue;
        if (positions.size() >= MAX_COMPONENT) {
          complete = false;
          continue;
        }
        indices.put(key, positions.size());
        positions.add(key);
        blocks.add(block);
      }
    }
    int size = positions.size();
    var reach = new int[size];
    var above = new int[size];
    var offsets = new int[size + 1];
    var edges = new IntArrayList();
    var foundation = new boolean[size];
    for (int node = 0; node < size; node++) {
      var position = BlockPos.of(positions.getLong(node));
      reach[node] = blocks.get(node).maxDistance;
      above[node] = indices.get(position.above().asLong());
      var below = position.below();
      if (loaded(world, below)) {
        var state = world.getBlockState(below);
        foundation[node] =
            !(state.getBlock() instanceof StructuralScaffold)
                && state.isFaceSturdy(world, below, Direction.UP, SupportType.FULL);
      } else complete = false;
      offsets[node] = edges.size();
      for (var face : LATERAL) {
        int neighbor = indices.get(position.relative(face).asLong());
        if (neighbor >= 0) edges.add(neighbor);
      }
    }
    offsets[size] = edges.size();
    return new Snapshot(
        positions,
        ScaffoldSupport.solve(reach, above, offsets, edges.toIntArray(), foundation),
        complete);
  }

  private boolean pillar(Level world, BlockPos position) {
    var cursor = position.mutable();
    int minimum = world.getMinY();
    while (cursor.getY() >= minimum) {
      if (!world.hasChunkAt(cursor)) return false;
      var state = world.getBlockState(cursor);
      if (!(state.getBlock() instanceof StructuralScaffold))
        return state.isFaceSturdy(world, cursor, Direction.UP, SupportType.FULL);
      cursor.move(Direction.DOWN);
    }
    return false;
  }

  @Override
  public InteractionResult useItemOn(
      ItemStack stack,
      BlockState state,
      Level world,
      BlockPos position,
      Player owner,
      InteractionHand hand,
      BlockHitResult hit) {
    if (owner.isShiftKeyDown()) return InteractionResult.TRY_WITH_EMPTY_HAND;
    Block replacement;
    int count;
    if (this == BlockDefinitions.WOODEN_SCAFFOLD && stack.is(Items.STICK)) {
      replacement = BlockDefinitions.REINFORCED_WOODEN_SCAFFOLD;
      count = 2;
    } else if (this == BlockDefinitions.IRON_SCAFFOLD
        && stack.is(BlockDefinitions.IRON_FENCE.asItem())) {
      replacement = BlockDefinitions.REINFORCED_IRON_SCAFFOLD;
      count = 1;
    } else return InteractionResult.TRY_WITH_EMPTY_HAND;
    if (stack.isEmpty()
        || !owner.getAbilities().instabuild && stack.getCount() < count
        || !owner.getAbilities().mayBuild
        || !world.mayInteract(owner, position)
        || !owner.mayUseItemAt(position, hit.getDirection(), stack)
        || !pillar(world, position)) return InteractionResult.FAIL;
    if (world.isClientSide()) return InteractionResult.SUCCESS;
    if (world.getBlockState(position) != state
        || !world.setBlockAndUpdate(position, replacement.defaultBlockState()))
      return InteractionResult.FAIL;
    if (!owner.getAbilities().instabuild) stack.shrink(count);
    return InteractionResult.SUCCESS;
  }

  @Override
  public void attack(BlockState state, Level world, BlockPos position, Player owner) {
    if (world.isClientSide() || !owner.getAbilities().mayBuild) return;
    var stack = owner.getMainHandItem();
    if (!stack.is(asItem()) || !world.mayInteract(owner, position)) return;
    var cursor = position.mutable();
    int top = world.getMaxY();
    while (cursor.getY() < top && world.getBlockState(cursor).is(this)) cursor.move(Direction.UP);
    if (cursor.getY() >= top
        || !world.hasChunkAt(cursor)
        || !owner.mayUseItemAt(cursor, Direction.UP, stack)
        || !canSurvive(defaultBlockState(), world, cursor)) return;
    var target = cursor.immutable();
    stack.useOn(
        new BlockPlaceContext(
            owner,
            InteractionHand.MAIN_HAND,
            stack,
            new BlockHitResult(
                Vec3.atCenterOf(target.below()).add(0, .5, 0),
                Direction.UP,
                target.below(),
                false)));
  }
}
