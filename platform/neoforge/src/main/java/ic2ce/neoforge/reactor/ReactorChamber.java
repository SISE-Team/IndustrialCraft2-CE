package ic2ce.neoforge.reactor;

import ic2ce.api.energy.tile.*;
import ic2ce.api.reactor.IReactorChamber;
import ic2ce.core.util.StackUtil;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Chamber ownership is resolved from current native blocks; stale cores cannot expose inventory.
 */
public class ReactorChamber extends NativeBlockEntity
    implements ReactorInventoryView, IReactorChamber, IEnergyEmitter {
  public final SignalInput redstone = addComponent(new SignalInput(this));
  protected final FluidPorts fluids = addComponent(new FluidPorts(this));
  private NuclearCore reactor;
  private long resolvedTick = Long.MIN_VALUE;
  private List<FluidPorts.InternalFluidTank> sharedTanks = List.of();

  public ReactorChamber(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.REACTOR_CHAMBER, position, state);
    fluids.addUnmanagedTankHook(
        () -> {
          getReactorInstance();
          return sharedTanks;
        });
  }

  private boolean live(NuclearCore core) {
    return core != null
        && getLevel() != null
        && !core.isRemoved()
        && core.getLevel() == getLevel()
        && core.getBlockPos().distManhattan(worldPosition) == 1
        && getLevel().getBlockEntity(core.getBlockPos()) == core;
  }

  @Override
  public NuclearCore getReactorInstance() {
    var world = getLevel();
    if (world == null) return null;
    long tick = world.getGameTime();
    if (resolvedTick == tick && live(reactor)) return reactor;
    var previous = reactor;
    if (!live(reactor)) reactor = null;
    if (reactor == null)
      for (var face : Direction.values()) {
        var position = worldPosition.relative(face);
        if (world.hasChunkAt(position)
            && world.getBlockEntity(position) instanceof NuclearCore core
            && !core.isRemoved()) {
          reactor = core;
          break;
        }
      }
    resolvedTick = tick;
    if (reactor != previous || sharedTanks.isEmpty() && reactor != null)
      sharedTanks = reactor == null ? List.of() : List.of(reactor.inputTank, reactor.outputTank);
    return reactor;
  }

  private void updateLink() {
    if (getLevel() == null || getLevel().isClientSide()) return;
    var core = getReactorInstance();
    var receiver = core == null ? null : core.redstone;
    if (redstone.getLinkReceiver() == receiver) return;
    redstone.unlinkOutbound();
    if (receiver != null) redstone.linkTo(receiver);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    updateLink();
  }

  @Override
  protected void onUnloaded() {
    reactor = null;
    sharedTanks = List.of();
    resolvedTick = Long.MIN_VALUE;
    super.onUnloaded();
  }

  @Override
  protected void onNeighborChange(Block block) {
    super.onNeighborChange(block);
    resolvedTick = Long.MIN_VALUE;
    if (getLevel() != null && !getLevel().isClientSide()) {
      if (getReactorInstance() == null) destoryChamber(true);
      else updateLink();
    }
  }

  public void destoryChamber(boolean wrench) {
    var world = getLevel();
    if (world == null || world.isClientSide() || world.getBlockEntity(worldPosition) != this)
      return;
    var drops = getSelfDrops(0, wrench);
    world.removeBlock(worldPosition, false);
    for (var stack : drops)
      if (!StackUtil.isEmpty(stack)) StackUtil.dropAsEntity(world, worldPosition, stack);
  }

  @Override
  public void setChanged() {
    super.setChanged();
    var core = getReactorInstance();
    if (core != null) core.setChanged();
  }

  @Override
  public boolean emitsEnergyTo(IEnergyAcceptor target, Direction face) {
    var core = getReactorInstance();
    return core != null && !core.isFluidCooled();
  }

  @Override
  public boolean isWall() {
    return false;
  }

  @Override
  protected void updateEntityClient() {
    super.updateEntityClient();
    var core = getReactorInstance();
    if (core != null) NuclearCore.showHeatEffects(getLevel(), worldPosition, core.getHeat());
  }

  @Override
  protected InteractionResult onActivated(
      Player player, InteractionHand hand, Direction face, Vec3 hit) {
    var core = getReactorInstance();
    return core == null
        ? InteractionResult.PASS
        : core.getBlockType()
            .useWithoutItem(
                core.getBlockState(),
                getLevel(),
                core.getBlockPos(),
                player,
                new BlockHitResult(hit, face, core.getBlockPos(), false));
  }
}
