package ic2ce.neoforge.block;

import ic2ce.api.block.BreakableBlock;
import ic2ce.api.crops.CropSoilType;
import ic2ce.api.tile.*;
import ic2ce.core.util.Util;
import ic2ce.engine.recipe.DeferredValue;
import ic2ce.neoforge.agriculture.*;
import ic2ce.neoforge.world.surface.SurfaceMap;
import java.lang.invoke.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/**
 * Native state, lifetime, interaction and drop projection; individual machines own their resource
 * and shape policies.
 */
public final class MachineBlockType extends Block
    implements EntityBlock, IWrenchable, BreakableBlock, RetexturableBlock {
  public static final Property<Direction> anyFacingProperty =
      EnumProperty.create("facing", Direction.class, List.of(Direction.values()));
  public static final Property<Direction> horizontalFacingProperty =
      EnumProperty.create(
          "facing",
          Direction.class,
          List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
  public static final Property<Direction> verticalFacingProperty =
      EnumProperty.create("facing", Direction.class, List.of(Direction.DOWN, Direction.UP));
  public static final BooleanProperty CROSSING_BASE = BooleanProperty.create("crossing_base"),
      ACTIVE = BooleanProperty.create("active");
  private static final Map<Integer, IntegerProperty> AGES = new ConcurrentHashMap<>();
  private static final ClassValue<MethodHandle> CONSTRUCTORS =
      new ClassValue<>() {
        @Override
        protected MethodHandle computeValue(Class<?> type) {
          try {
            return MethodHandles.publicLookup()
                .findConstructor(
                    type, MethodType.methodType(void.class, BlockPos.class, BlockState.class))
                .asType(
                    MethodType.methodType(
                        NativeBlockEntity.class, BlockPos.class, BlockState.class));
          } catch (ReflectiveOperationException error) {
            throw new IllegalArgumentException(
                "Native block entity lacks a public position/state constructor: " + type.getName(),
                error);
          }
        }
      };
  private final Class<? extends NativeBlockEntity> entityClass;
  private final boolean activeState, rotatable;
  private final DefaultDrop drops;
  private final Set<Direction> directions;
  private final Cultivar crop;
  private final int maximumAge;
  private final DeferredValue<NativeBlockEntity> prototype;
  public final Property<Direction> facingProperty;

  public static MachineBlockType create(
      Properties properties,
      Class<? extends NativeBlockEntity> type,
      boolean active,
      DefaultDrop drop,
      Set<Direction> directions,
      boolean rotate) {
    return new MachineBlockType(properties, type, active, drop, directions, rotate, null);
  }

  public static MachineBlockType create(
      Properties properties,
      Class<? extends NativeBlockEntity> type,
      boolean active,
      DefaultDrop drop,
      Set<Direction> directions,
      boolean rotate,
      Cultivar crop) {
    return new MachineBlockType(
        properties, type, active, drop, directions, rotate, Objects.requireNonNull(crop));
  }

  private MachineBlockType(
      Properties properties,
      Class<? extends NativeBlockEntity> type,
      boolean active,
      DefaultDrop drop,
      Set<Direction> supported,
      boolean rotate,
      Cultivar cultivar) {
    // Native Block invokes the state-definition callback from its constructor.
    var nativeCrop = type == CropPlot.class ? cultivar : null;
    directions =
        supported.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(supported));
    activeState = active;
    crop = nativeCrop;
    maximumAge = nativeCrop == null ? -1 : nativeCrop.getMaxAge();
    super(properties);
    entityClass = Objects.requireNonNull(type);
    drops = Objects.requireNonNull(drop);
    rotatable = rotate;
    facingProperty =
        directions.size() > 1 ? (Property<Direction>) stateDefinition.getProperty("facing") : null;
    prototype = new DeferredValue<>(() -> createBlockEntity(BlockPos.ZERO, defaultBlockState()));
  }

  private static IntegerProperty age(int maximum) {
    if (maximum < 1 || maximum > 255)
      throw new IllegalArgumentException("Invalid native crop age bound");
    return AGES.computeIfAbsent(maximum, value -> IntegerProperty.create("age", 0, value));
  }

  public IntegerProperty getAgeProperty() {
    return maximumAge < 1 ? null : age(maximumAge);
  }

  public int getCropMaxAge() {
    return maximumAge;
  }

  public Cultivar getCropType() {
    return crop;
  }

  public Class<? extends NativeBlockEntity> getTeClass() {
    return entityClass;
  }

  public boolean canActive() {
    return activeState;
  }

  public DefaultDrop getDefaultDrop() {
    return drops;
  }

  public boolean allowWrenchRotating() {
    return rotatable;
  }

  public Set<Direction> getSupportedFacings() {
    return directions;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return createBlockEntity(pos, state);
  }

  public NativeBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    try {
      return (NativeBlockEntity) CONSTRUCTORS.get(entityClass).invokeExact(pos, state);
    } catch (Throwable error) {
      if (error instanceof Error fatal) throw fatal;
      throw new IllegalStateException(
          "Unable to construct native entity " + entityClass.getName() + " at " + pos, error);
    }
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level world, BlockState state, BlockEntityType<T> type) {
    if (!type.isValid(state)) return null;
    return (level, pos, current, entity) -> {
      if (entity instanceof NativeBlockEntity nativeEntity
          && !nativeEntity.isRemoved()
          && nativeEntity.getLevel() == level) nativeEntity.tick();
    };
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> definition) {
    if (directions.size() > 1) {
      Property<Direction> facing =
          directions.size() == 6
              ? anyFacingProperty
              : directions.equals(Util.horizontalFacings)
                  ? horizontalFacingProperty
                  : directions.equals(Util.verticalFacings)
                      ? verticalFacingProperty
                      : EnumProperty.create("facing", Direction.class, List.copyOf(directions));
      definition.add(facing);
    }
    if (activeState) definition.add(ACTIVE);
    if (crop != null) definition.add(crop == Cultivar.NONE ? CROSSING_BASE : age(maximumAge));
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    var state = defaultBlockState();
    if (facingProperty != null)
      state =
          state.setValue(
              facingProperty,
              placementFacing(context.getPlayer(), context.getNearestLookingDirection()));
    if (activeState) state = state.setValue(ACTIVE, false);
    if (crop != null)
      state =
          crop == Cultivar.NONE
              ? state.setValue(CROSSING_BASE, false)
              : state.setValue(getAgeProperty(), 0);
    return state;
  }

  private Direction placementFacing(LivingEntity placer, Direction fallback) {
    if (directions.isEmpty()) return Direction.NORTH;
    if (placer == null)
      return fallback != null && directions.contains(fallback.getOpposite())
          ? fallback.getOpposite()
          : directions.iterator().next();
    var look = placer.getLookAngle();
    Direction selected = directions.iterator().next();
    double best = Double.NEGATIVE_INFINITY;
    for (var facing : directions) {
      double match =
          -(look.x * facing.getStepX() + look.y * facing.getStepY() + look.z * facing.getStepZ());
      if (match > best) {
        best = match;
        selected = facing;
      }
    }
    return selected;
  }

  private NativeBlockEntity tile(BlockGetter world, BlockPos pos) {
    return world.getBlockEntity(pos) instanceof NativeBlockEntity entity
            && entity.getBlockState().is(this)
        ? entity
        : null;
  }

  public void setActive(Level world, BlockPos pos, BlockState state, boolean value) {
    if (!activeState || world.isClientSide() || !state.is(this)) return;
    var tile = tile(world, pos);
    if (tile != null) tile.setActive(value);
    if (state.getValue(ACTIVE) != value)
      world.setBlockAndUpdate(pos, state.setValue(ACTIVE, value));
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
    return (crop == null || CropSoilType.contains(world.getBlockState(pos.below()).getBlock()))
        && super.canSurvive(state, world, pos);
  }

  @Override
  public BlockState updateShape(
      BlockState state,
      LevelReader world,
      ScheduledTickAccess ticks,
      BlockPos pos,
      Direction side,
      BlockPos neighbor,
      BlockState neighborState,
      net.minecraft.util.RandomSource random) {
    return crop != null && !state.canSurvive(world, pos)
        ? Blocks.AIR.defaultBlockState()
        : super.updateShape(state, world, ticks, pos, side, neighbor, neighborState, random);
  }

  @Override
  public void neighborChanged(
      BlockState state,
      Level world,
      BlockPos pos,
      Block source,
      @org.jetbrains.annotations.Nullable
          net.minecraft.world.level.redstone.Orientation orientation,
      boolean moved) {
    super.neighborChanged(state, world, pos, source, orientation, moved);
    var entity = tile(world, pos);
    if (entity != null) entity.onNeighborChange(source);
  }

  @Override
  public void entityInside(
      BlockState state,
      Level world,
      BlockPos pos,
      Entity actor,
      InsideBlockEffectApplier effects,
      boolean precise) {
    var entity = tile(world, pos);
    if (entity != null) entity.onEntityCollision(actor);
    if (crop != null
        && actor instanceof Ravager
        && world instanceof net.minecraft.server.level.ServerLevel server
        && server.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING))
      world.destroyBlock(pos, true, actor);
    super.entityInside(state, world, pos, actor, effects, precise);
  }

  @Override
  public void setPlacedBy(
      Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
    var entity = tile(world, pos);
    if (entity != null)
      entity.onPlaced(
          stack, placer, facingProperty == null ? Direction.NORTH : state.getValue(facingProperty));
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    var entity = tile(world, pos);
    return entity == null
        ? (hasCollision ? Shapes.block() : Shapes.empty())
        : entity.getOutlineShape(context);
  }

  @Override
  public VoxelShape getCollisionShape(
      BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    var entity = tile(world, pos);
    return entity == null
        ? (hasCollision ? Shapes.block() : Shapes.empty())
        : entity.getCollisionShape(context);
  }

  @Override
  public VoxelShape getOcclusionShape(BlockState state) {
    return hasCollision ? Shapes.block() : Shapes.empty();
  }

  @Override
  public InteractionResult useWithoutItem(
      BlockState state, Level world, BlockPos pos, Player actor, BlockHitResult hit) {
    if (actor.isShiftKeyDown()) return InteractionResult.PASS;
    var entity = tile(world, pos);
    return entity == null
        ? InteractionResult.PASS
        : entity.onActivated(
            actor, InteractionHand.MAIN_HAND, hit.getDirection(), hit.getLocation());
  }

  @Override
  protected InteractionResult useItemOn(
      ItemStack stack,
      BlockState state,
      Level world,
      BlockPos pos,
      Player actor,
      InteractionHand hand,
      BlockHitResult hit) {
    var entity = tile(world, pos);
    if (entity instanceof ic2ce.neoforge.explosion.ExplosiveBlockEntity
        && (stack.is(net.minecraft.world.item.Items.FIRE_CHARGE)
            || stack.is(net.minecraft.world.item.Items.FLINT_AND_STEEL)))
      return entity.onActivated(actor, hand, hit.getDirection(), hit.getLocation());
    return super.useItemOn(stack, state, world, pos, actor, hand, hit);
  }

  @Override
  protected void onExplosionHit(
      BlockState state,
      net.minecraft.server.level.ServerLevel world,
      BlockPos pos,
      Explosion explosion,
      java.util.function.BiConsumer<ItemStack, BlockPos> output) {
    if (explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK) {
      super.onExplosionHit(state, world, pos, explosion, output);
      return;
    }
    var entity = tile(world, pos);
    if (entity != null) entity.onExploded(explosion);
    if (world.getBlockState(pos).is(this)
        && (entity == null || world.getBlockEntity(pos) == entity))
      super.onExplosionHit(state, world, pos, explosion, output);
  }

  @Override
  public InteractionResult startBreak(
      Player actor,
      Level world,
      InteractionHand hand,
      BlockPos pos,
      BlockState state,
      Direction side) {
    var entity = tile(world, pos);
    return entity == null ? InteractionResult.PASS : entity.onClicked(actor);
  }

  @Override
  protected List<ItemStack> getDrops(
      BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder context) {
    return ic2ce.neoforge.world.BlockDrops.retain(
        context.getOptionalParameter(
            net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY),
        asItem(),
        super.getDrops(state, context));
  }

  @Override
  public Direction getFacing(Level world, BlockPos pos) {
    var entity = tile(world, pos);
    return entity == null ? Direction.DOWN : entity.getFacing();
  }

  @Override
  public boolean canSetFacing(Level world, BlockPos pos, Direction side, Player actor) {
    var entity = tile(world, pos);
    return entity != null && side != null && entity.canSetFacingWrench(side, actor);
  }

  @Override
  public boolean setFacing(Level world, BlockPos pos, Direction side, Player actor) {
    var entity = tile(world, pos);
    return !world.isClientSide()
        && entity != null
        && side != null
        && entity.setFacingWrench(world, side, actor);
  }

  @Override
  public boolean wrenchCanRemove(Level world, BlockPos pos, Player actor) {
    var entity = tile(world, pos);
    return entity != null && entity.wrenchCanRemove(actor);
  }

  @Override
  public List<ItemStack> getWrenchDrops(
      Level world, BlockPos pos, BlockState state, BlockEntity entity, Player actor, int fortune) {
    var stack = new ItemStack(asItem());
    if (entity instanceof NativeBlockEntity nativeEntity)
      stack = nativeEntity.adjustDrop(stack, true);
    return stack == null || stack.isEmpty() ? List.of() : List.of(stack.copy());
  }

  @Override
  public boolean retexture(
      BlockState state,
      Level world,
      BlockPos pos,
      Direction side,
      Player actor,
      BlockState reference,
      String variant,
      Direction referenceSide,
      int[] tints) {
    if (world.isClientSide()
        || !state.is(this)
        || side == null
        || reference == null
        || variant == null
        || referenceSide == null
        || tints == null
        || tints.length == 0
        || tints.length > 64) return false;
    var entity = tile(world, pos);
    if (entity == null || !entity.hasComponent(SurfaceMap.class)) return false;
    return entity
        .getComponent(SurfaceMap.class)
        .applyObscuration(
            side, new SurfaceMap.ObscurationData(reference, variant, referenceSide, tints));
  }

  public NativeBlockEntity getDummyTe() {
    return prototype.get();
  }

  public enum DefaultDrop {
    Self,
    None,
    Generator,
    Machine,
    AdvMachine
  }
}
