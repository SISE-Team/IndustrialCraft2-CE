package ic2ce.neoforge.cable;

import ic2ce.api.energy.EnergyNet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Samples delivered conductor traffic, independently of splitter gating or stored machine energy.
 */
public abstract class TrafficSensorBlock extends ConductorBlock {
  public static final BooleanProperty active = BooleanProperty.create("active");
  private static final int SAMPLE_INTERVAL = 32;

  protected TrafficSensorBlock(Properties settings) {
    super(settings, ConductorMaterial.detector, 0);
    registerDefaultState(defaultBlockState().setValue(active, false));
  }

  @Override
  protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(active);
  }

  @Override
  public void onPlace(
      BlockState state, Level world, BlockPos position, BlockState former, boolean moved) {
    super.onPlace(state, world, position, former, moved);
    if (!world.isClientSide() && !former.is(this))
      world.scheduleTick(position, this, 1 + (int) (position.asLong() & 31));
  }

  @Override
  public void onLoad(BlockState state, Level world, BlockPos position) {
    super.onLoad(state, world, position);
    if (!world.isClientSide()) world.scheduleTick(position, this, SAMPLE_INTERVAL);
  }

  @Override
  public void tick(BlockState state, ServerLevel world, BlockPos position, RandomSource random) {
    if (!world.getBlockState(position).is(this)) return;
    world.scheduleTick(position, this, SAMPLE_INTERVAL);
    boolean flowing = traffic(world, position) > 0;
    if (state.getValue(active) != flowing)
      world.setBlockAndUpdate(position, state.setValue(active, flowing));
    else if (flowing) world.updateNeighbourForOutputSignal(position, this);
  }

  private double traffic(Level world, BlockPos position) {
    if (world.isClientSide()) return 0;
    var tile = EnergyNet.instance.getTile(world, position);
    if (tile == null) return 0;
    var stats = EnergyNet.instance.getNodeStats(tile);
    double amount = stats == null ? 0 : stats.getEnergyIn();
    return Double.isFinite(amount) ? Math.max(0, amount) : 0;
  }

  @Override
  public boolean isSignalSource(BlockState state) {
    return true;
  }

  @Override
  public int getSignal(
      BlockState state, BlockGetter world, BlockPos position, Direction direction) {
    return state.getValue(active) ? 15 : 0;
  }

  @Override
  public boolean hasAnalogOutputSignal(BlockState state) {
    return true;
  }

  @Override
  public int getAnalogOutputSignal(
      BlockState state, Level world, BlockPos position, Direction direction) {
    double amount = state.getValue(active) ? traffic(world, position) : 0;
    return Math.clamp((int) (Math.min(1, amount / type.capacity) * 15), 0, 15);
  }
}
