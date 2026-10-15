package ic2ce.neoforge.item.tool;

import ic2ce.api.tile.IWrenchable;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * Facing geometry is shared; mechanical wear and electrical reservation remain separate payment
 * policies.
 */
public final class WrenchActions {
  public interface Payment {
    boolean reserve(int units);

    void refund(int units);
  }

  public enum Action {
    NONE(0),
    ROTATE(1),
    REMOVE(10);
    public final int units;

    Action(int units) {
      this.units = units;
    }
  }

  private WrenchActions() {}

  private static Direction requested(Direction current, Direction hit, Player player) {
    if (!ProjectRuntime.keyboard.isAltKeyDown(player))
      return player.isShiftKeyDown() ? hit.getOpposite() : hit;
    boolean clockwise =
        (hit.getAxisDirection() == Direction.AxisDirection.POSITIVE) != player.isShiftKeyDown();
    return clockwise
        ? current.getClockWise(hit.getAxis())
        : current.getCounterClockWise(hit.getAxis());
  }

  @SuppressWarnings("unchecked")
  private static BlockState rotate(
      BlockState state, Level world, BlockPos position, Direction hit, Player player) {
    Rotation rotation = null;
    if (ProjectRuntime.keyboard.isAltKeyDown(player))
      rotation =
          (hit.getAxisDirection() == Direction.AxisDirection.POSITIVE) != player.isShiftKeyDown()
              ? Rotation.CLOCKWISE_90
              : Rotation.COUNTERCLOCKWISE_90;
    else if (hit.getAxis().isHorizontal()) {
      var raw = state.getBlock().getStateDefinition().getProperty("facing");
      if (raw != null && raw.getValueClass() == Direction.class) {
        var property = (Property<Direction>) raw;
        var before = state.getValue(property);
        var after = requested(before, hit, player);
        if (before.getAxis().isHorizontal()
            && after != before
            && property.getPossibleValues().contains(after))
          rotation =
              after == before.getOpposite()
                  ? Rotation.CLOCKWISE_180
                  : after == before.getClockWise()
                      ? Rotation.CLOCKWISE_90
                      : Rotation.COUNTERCLOCKWISE_90;
      }
    }
    return rotation == null
        ? state
        : ProjectRuntime.envProxy.rotate(state, world, position, rotation);
  }

  public static Action execute(
      Level world,
      BlockPos position,
      Direction hit,
      Player player,
      boolean allowRemoval,
      Payment payment) {
    if (world == null
        || player == null
        || hit == null
        || !world.isLoaded(position)
        || !world.mayInteract(player, position)) return Action.NONE;
    var state = world.getBlockState(position);
    if (state.isAir()) return Action.NONE;
    var block = state.getBlock();
    if (block instanceof IWrenchable wrenchable) {
      var before = wrenchable.getFacing(world, position);
      if (before == null) return Action.NONE;
      var after = requested(before, hit, player);
      if (after != before && wrenchable.canSetFacing(world, position, after, player)) {
        if (world.isClientSide()) return Action.ROTATE;
        if (!payment.reserve(1)) return Action.NONE;
        if (world.getBlockState(position) == state
            && wrenchable.setFacing(world, position, after, player)) return Action.ROTATE;
        payment.refund(1);
        return Action.NONE;
      }
      if (!allowRemoval || !wrenchable.wrenchCanRemove(world, position, player)) return Action.NONE;
      if (world.isClientSide()) return Action.REMOVE;
      if (!(world instanceof ServerLevel server)
          || !(player instanceof ServerPlayer actor)
          || player.blockActionRestricted(world, position, actor.gameMode.getGameModeForPlayer())
          || NeoForge.EVENT_BUS
              .post(new BreakBlockEvent(world, position, state, player))
              .isCanceled()
          || world.getBlockState(position) != state
          || !payment.reserve(10)) return Action.NONE;
      boolean committed = false;
      try {
        var entity = world.getBlockEntity(position);
        var supplied = wrenchable.getWrenchDrops(world, position, state, entity, player, 0);
        var drops = new ArrayList<ItemStack>();
        if (supplied != null)
          for (var drop : supplied) if (drop != null && !drop.isEmpty()) drops.add(drop.copy());
        if (world.getBlockState(position) != state || !world.removeBlock(position, false))
          return Action.NONE;
        committed = true;
        block.destroy(world, position, state);
        for (var drop : drops) StackUtil.dropAsEntity(world, position, drop);
        if (ic2ce.neoforge.config.NativeConfigValues.getBool(
            ic2ce.neoforge.config.ProjectConfiguration.get(), "protection/wrenchLogging"))
          ProjectRuntime.log.info(
              ic2ce.neoforge.diagnostics.DiagnosticArea.PlayerActivity,
              "Player %s/%s removed %s with a wrench at %s in %s.",
              player.getGameProfile().name(),
              player.getGameProfile().id(),
              state,
              position,
              world.dimension().identifier());
        if (drops.isEmpty()
            && ic2ce.neoforge.config.NativeConfigValues.getBool(
                ic2ce.neoforge.config.ProjectConfiguration.get(), "debug/logEmptyWrenchDrops"))
          ProjectRuntime.log.warn(
              ic2ce.neoforge.diagnostics.DiagnosticArea.General,
              "Wrench removal of %s at %s produced no block drops.",
              state,
              position);
        if (!player.getAbilities().instabuild)
          state.spawnAfterBreak(server, position, player.getMainHandItem(), false);
        return Action.REMOVE;
      } finally {
        if (!committed) payment.refund(10);
      }
    }
    var rotated = rotate(state, world, position, hit, player);
    if (rotated == state) return Action.NONE;
    if (world.isClientSide()) return Action.ROTATE;
    if (!payment.reserve(1)) return Action.NONE;
    if (world.getBlockState(position) == state && world.setBlockAndUpdate(position, rotated))
      return Action.ROTATE;
    payment.refund(1);
    return Action.NONE;
  }
}
