package ic2ce.neoforge.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;

/** A redstone-controlled conductor attaches only while enabled, including chunk restoration. */
public abstract class RedstoneGateBlock extends ConductorBlock {
  public static final BooleanProperty active = BooleanProperty.create("active");

  protected RedstoneGateBlock(Properties settings, ConductorMaterial type, int insulation) {
    super(settings, type, insulation);
    registerDefaultState(defaultBlockState().setValue(active, false));
  }

  @Override
  protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(active);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return super.getStateForPlacement(context)
        .setValue(active, context.getLevel().hasNeighborSignal(context.getClickedPos()));
  }

  @Override
  public void onLoad(BlockState state, Level world, BlockPos position) {
    reconcile(state, world, position);
  }

  @Override
  public void neighborChanged(
      BlockState state,
      Level world,
      BlockPos position,
      Block neighbor,
      Orientation orientation,
      boolean moved) {
    reconcile(state, world, position);
  }

  private void reconcile(BlockState state, Level world, BlockPos position) {
    if (world.isClientSide() || !world.getBlockState(position).is(this)) return;
    boolean powered = world.hasNeighborSignal(position);
    if (state.getValue(active) != powered) {
      state = state.setValue(active, powered);
      world.setBlockAndUpdate(position, state);
    }
    if (powered) addToEnet(state, world, position, true);
    else removeFromEnet(state, world, position);
  }

  @Override
  protected void addToEnet(
      BlockState state, Level world, BlockPos position, boolean checkConflicting) {
    if (state.getValue(active)) super.addToEnet(state, world, position, checkConflicting);
  }
}
