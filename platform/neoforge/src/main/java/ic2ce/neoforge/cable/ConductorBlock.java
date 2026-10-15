package ic2ce.neoforge.cable;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.*;
import ic2ce.api.info.ILocatable;
import ic2ce.neoforge.world.ChunkLoadAwareBlock;
import ic2ce.neoforge.item.tool.InsulationCutter;
import ic2ce.neoforge.registry.BlockGroups;
import ic2ce.neoforge.registry.FluidDefinitions;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.*;

/** Native cable geometry, variant-preserving insulation and position-owned conductor lifetimes. */
public abstract class ConductorBlock extends PipeBlock
    implements ChunkLoadAwareBlock, SimpleWaterloggedBlock {
  public static final DyeColor DEFAULT_COLOR = DyeColor.BLACK;
  public static final EnumProperty<DyeColor> colorProperty =
      EnumProperty.create("color", DyeColor.class);
  public static final EnumProperty<FoamFinish> foamProperty =
      EnumProperty.create("foam", FoamFinish.class);
  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  public static final BooleanProperty UP = BlockStateProperties.UP,
      DOWN = BlockStateProperties.DOWN,
      NORTH = BlockStateProperties.NORTH,
      EAST = BlockStateProperties.EAST,
      SOUTH = BlockStateProperties.SOUTH,
      WEST = BlockStateProperties.WEST;
  private static final Direction[] DIRECTIONS = Direction.values();
  private static final ThreadLocal<Boolean> CONSTRUCTION_COLOR = new ThreadLocal<>();
  private static final ConductorBlock[][][] VARIANTS =
      new ConductorBlock[ConductorMaterial.values().length][2][];
  private boolean shapesReady;
  final ConductorMaterial type;
  final int insulation;

  public abstract boolean isFoam();

  public abstract boolean isHardFoam(BlockState state);

  protected static <T extends ConductorBlock> T construct(
      ConductorMaterial type, int insulation, java.util.function.Supplier<T> factory) {
    java.util.Objects.requireNonNull(type);
    if (insulation < 0 || insulation > type.maxInsulation)
      throw new IllegalArgumentException("Invalid cable insulation");
    CONSTRUCTION_COLOR.set(insulation >= type.minColoredInsulation);
    try {
      return factory.get();
    } finally {
      CONSTRUCTION_COLOR.remove();
    }
  }

  protected ConductorBlock(Properties settings, ConductorMaterial type, int insulation) {
    super(type.getThickness(insulation) * 16, settings);
    CONSTRUCTION_COLOR.remove();
    shapesReady = true;
    if (insulation < 0 || insulation > type.maxInsulation)
      throw new IllegalArgumentException("Invalid cable insulation");
    this.type = type;
    this.insulation = insulation;
    initializeState(stateDefinition.any());
    int family = isFoam() ? 1 : 0;
    var row = VARIANTS[type.ordinal()][family];
    if (row == null)
      VARIANTS[type.ordinal()][family] = row = new ConductorBlock[type.maxInsulation + 1];
    if (row[insulation] != null)
      throw new IllegalStateException(
          "Cable variant already registered: " + type + "/" + family + "/" + insulation);
    row[insulation] = this;
  }

  @Override
  protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
    if (isFoam()) builder.add(foamProperty);
    else builder.add(WATERLOGGED);
    if (Boolean.TRUE.equals(CONSTRUCTION_COLOR.get())) builder.add(colorProperty);
  }

  public void initializeState(BlockState state) {
    for (var direction : DIRECTIONS)
      state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), false);
    state =
        isFoam()
            ? state.setValue(foamProperty, FoamFinish.SOFT)
            : state.setValue(WATERLOGGED, false);
    if (state.hasProperty(colorProperty)) state = state.setValue(colorProperty, DEFAULT_COLOR);
    registerDefaultState(state);
  }

  @Override
  protected Function<BlockState, VoxelShape> getShapeForEachState(
      Function<BlockState, VoxelShape> calculator) {
    if (shapesReady) return super.getShapeForEachState(calculator);
    var connectionShapes = new VoxelShape[64];
    return super.getShapeForEachState(
        state -> {
          int mask = 0;
          for (var direction : DIRECTIONS)
            if (state.getValue(PROPERTY_BY_DIRECTION.get(direction)))
              mask |= 1 << direction.ordinal();
          var shape = connectionShapes[mask];
          if (shape == null) connectionShapes[mask] = shape = calculator.apply(state);
          return shape;
        });
  }

  protected BlockState copyState(BlockState from, ConductorBlock destination) {
    var result = destination.defaultBlockState();
    for (var property : from.getProperties())
      if (result.hasProperty(property)) result = copyProperty(from, result, property);
    return result;
  }

  private static <T extends Comparable<T>> BlockState copyProperty(
      BlockState source, BlockState target, Property<T> property) {
    return target.setValue(property, source.getValue(property));
  }

  public boolean hasColor() {
    return insulation >= type.minColoredInsulation;
  }

  DyeColor getColor(BlockState state) {
    return getColor(state, type, insulation);
  }

  public static DyeColor getColor(BlockState state, ConductorMaterial type, int insulation) {
    return insulation >= type.minColoredInsulation && state.hasProperty(colorProperty)
        ? state.getValue(colorProperty)
        : DEFAULT_COLOR;
  }

  public BlockState withConnectionStates(BlockState state, Level world, BlockPos position) {
    for (var direction : DIRECTIONS) {
      var neighbor = position.relative(direction);
      state =
          state.setValue(
              PROPERTY_BY_DIRECTION.get(direction),
              isConnectedWith(state, world.getBlockState(neighbor), world, neighbor));
    }
    return state;
  }

  public boolean isConnectedWith(
      BlockState state, BlockState neighborState, Level world, BlockPos neighborPosition) {
    if (neighborState == null) neighborState = world.getBlockState(neighborPosition);
    if (!(neighborState.getBlock() instanceof ConductorBlock cable))
      return neighborState.is(BlockGroups.CABLE_CONNECTABLE);
    var ours = getColor(state);
    var theirs = cable.getColor(neighborState);
    return ours == DEFAULT_COLOR || theirs == DEFAULT_COLOR || ours == theirs;
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    var state = defaultBlockState();
    var fluid = context.getLevel().getFluidState(context.getClickedPos()).getType();
    if (isFoam())
      return fluid == FluidDefinitions.CONSTRUCTION_FOAM.still
          ? state.setValue(foamProperty, FoamFinish.SOFT)
          : state;
    return withConnectionStates(
        state.setValue(WATERLOGGED, fluid == Fluids.WATER),
        context.getLevel(),
        context.getClickedPos());
  }

  @Override
  public VoxelShape getOcclusionShape(BlockState state) {
    return isFoam() && state.getValue(foamProperty).isSoft()
        ? Shapes.empty()
        : super.getOcclusionShape(state);
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter world, BlockPos position, CollisionContext context) {
    return isFoam() ? Shapes.block() : super.getShape(state, world, position, context);
  }

  @Override
  public FluidState getFluidState(BlockState state) {
    return !isFoam() && state.getValue(WATERLOGGED)
        ? Fluids.WATER.getSource(false)
        : Fluids.EMPTY.defaultFluidState();
  }

  @Override
  public BlockState updateShape(
      BlockState state,
      LevelReader world,
      ScheduledTickAccess scheduler,
      BlockPos position,
      Direction direction,
      BlockPos neighborPosition,
      BlockState neighborState,
      RandomSource random) {
    if (isFoam()) return state;
    if (state.getValue(WATERLOGGED))
      scheduler.scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickDelay(world));
    boolean connect =
        neighborState.getBlock() instanceof ConductorBlock cable
            ? getColor(state) == DEFAULT_COLOR
                || cable.getColor(neighborState) == DEFAULT_COLOR
                || getColor(state) == cable.getColor(neighborState)
            : neighborState.is(BlockGroups.CABLE_CONNECTABLE);
    return state.setValue(PROPERTY_BY_DIRECTION.get(direction), connect);
  }

  @Override
  public boolean canPlaceLiquid(
      LivingEntity user, BlockGetter world, BlockPos position, BlockState state, Fluid fluid) {
    return !isFoam() && !state.getValue(WATERLOGGED) && fluid == Fluids.WATER;
  }

  @Override
  public boolean placeLiquid(
      LevelAccessor world, BlockPos position, BlockState state, FluidState fluid) {
    if (!canPlaceLiquid(null, world, position, state, fluid.getType())) return false;
    if (!world.isClientSide()) {
      world.setBlock(position, state.setValue(WATERLOGGED, true), 3);
      world.scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickDelay(world));
    }
    return true;
  }

  @Override
  public ItemStack pickupBlock(
      LivingEntity user, LevelAccessor world, BlockPos position, BlockState state) {
    if (isFoam() || !state.getValue(WATERLOGGED)) return ItemStack.EMPTY;
    if (!world.isClientSide()) world.setBlock(position, state.setValue(WATERLOGGED, false), 3);
    return new ItemStack(Items.WATER_BUCKET);
  }

  @Override
  public void attack(BlockState state, Level world, BlockPos position, Player player) {
    if (isFoam() || isHardFoam(state)) return;
    if (player.getMainHandItem().getItem() instanceof InsulationCutter cutter)
      cutter.removeInsulation(
          player, net.minecraft.world.InteractionHand.MAIN_HAND, state, world, position);
  }

  private ConductorBlock variant(int depth) {
    var row = VARIANTS[type.ordinal()][isFoam() ? 1 : 0];
    return row != null && depth >= 0 && depth < row.length ? row[depth] : null;
  }

  public boolean tryAddInsulation(BlockState state, Level world, BlockPos position) {
    return changeInsulation(state, world, position, insulation + 1, false);
  }

  public boolean tryRemoveInsulation(
      BlockState state, Level world, BlockPos position, boolean simulate) {
    return changeInsulation(state, world, position, insulation - 1, simulate);
  }

  private boolean changeInsulation(
      BlockState state, Level world, BlockPos position, int depth, boolean simulate) {
    var target = variant(depth);
    if (target == null || isHardFoam(state) || world.getBlockState(position).getBlock() != this)
      return false;
    if (simulate || world.isClientSide()) return true;
    return world.setBlockAndUpdate(position, copyState(state, target));
  }

  /** Painting must rebuild topology as well as update the visible block state. */
  public boolean recolor(BlockState state, Level world, BlockPos position, DyeColor color) {
    if (!hasColor() || getColor(state) == color || world.getBlockState(position).getBlock() != this)
      return false;
    if (world.isClientSide()) return true;
    removeFromEnet(state, world, position);
    var updated = withConnectionStates(state.setValue(colorProperty, color), world, position);
    boolean changed = world.setBlockAndUpdate(position, updated);
    addToEnet(world.getBlockState(position), world, position, true);
    return changed;
  }

  @Override
  protected void affectNeighborsAfterRemoval(
      BlockState state, ServerLevel world, BlockPos position, boolean moved) {
    removeFromEnet(state, world, position);
    super.affectNeighborsAfterRemoval(state, world, position, moved);
  }

  @Override
  public void onPlace(
      BlockState state, Level world, BlockPos position, BlockState former, boolean moved) {
    addToEnet(state, world, position, true);
  }

  @Override
  public void onLoad(BlockState state, Level world, BlockPos position) {
    addToEnet(state, world, position, true);
  }

  @Override
  public void onUnload(BlockState state, Level world, BlockPos position) {
    removeFromEnet(state, world, position);
  }

  protected void addToEnet(
      BlockState state, Level world, BlockPos position, boolean checkConflicting) {
    if (world.isClientSide() || state.getBlock() != this) return;
    var previous = EnergyNet.instance.getTile(world, position);
    if (previous instanceof ConductorBlock.Conductor conductor && conductor.owner != this)
      EnergyNet.instance.removeTile(previous);
    if (EnergyNet.instance.getTile(world, position) == null)
      EnergyNet.instance.addLocatableTile(new Conductor(world, position));
  }

  protected void removeFromEnet(BlockState state, Level world, BlockPos position) {
    if (world.isClientSide()) return;
    var endpoint = EnergyNet.instance.getTile(world, position);
    if (endpoint instanceof ConductorBlock.Conductor conductor && conductor.owner == this)
      EnergyNet.instance.removeTile(endpoint);
  }

  private final class Conductor implements ILocatable, IColoredEnergyTile, IEnergyConductor {
    private final ConductorBlock owner = ConductorBlock.this;
    private final Level world;
    private final BlockPos position;

    Conductor(Level world, BlockPos position) {
      this.world = world;
      this.position = position.immutable();
    }

    @Override
    public Level getWorldObj() {
      return world;
    }

    @Override
    public BlockPos getPosition() {
      return position;
    }

    @Override
    public DyeColor getColor(Direction side) {
      var state = world.getBlockState(position);
      var color = state.getBlock() == owner ? owner.getColor(state) : DEFAULT_COLOR;
      return color == DEFAULT_COLOR ? null : color;
    }

    @Override
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction side) {
      return canInteractWith(emitter, side);
    }

    @Override
    public boolean emitsEnergyTo(IEnergyAcceptor acceptor, Direction side) {
      return canInteractWith(acceptor, side);
    }

    @Override
    public double getConductionLoss() {
      return type.loss;
    }

    @Override
    public double getInsulationEnergyAbsorption() {
      return type.maxInsulation == 0
          ? Integer.MAX_VALUE
          : EnergyNet.instance.getPowerFromTier(insulation + (type.capacity < 128 ? 0 : 1));
    }

    @Override
    public double getInsulationBreakdownEnergy() {
      return 9001;
    }

    @Override
    public double getConductorBreakdownEnergy() {
      return (double) type.capacity + 1;
    }

    @Override
    public void removeInsulation() {
      var state = world.getBlockState(position);
      if (state.getBlock() == owner) tryRemoveInsulation(state, world, position, false);
    }

    @Override
    public void removeConductor() {
      if (world.getBlockState(position).getBlock() == owner) world.removeBlock(position, false);
    }

    @Override
    public void onConnectionChange() {}
  }
}
