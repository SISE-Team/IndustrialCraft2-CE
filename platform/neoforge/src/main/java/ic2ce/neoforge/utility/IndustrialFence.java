package ic2ce.neoforge.utility;


import ic2ce.api.item.ItemWrapper;
import ic2ce.neoforge.runtime.SideServices;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;

/**
 * Wooden fences use native connectivity; isolated metal poles provide bounded, paid elevator
 * motion.
 */
public class IndustrialFence extends FenceBlock {
  private static final Direction[] SIDES = {
    Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
  };
  public static final Map<Direction, BooleanProperty> connectProperties =
      CrossCollisionBlock.PROPERTY_BY_DIRECTION;
  public final boolean canBoost;

  public IndustrialFence(Properties settings, boolean magnetic) {
    super(settings);
    canBoost = magnetic;
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    if (!canBoost) return super.getStateForPlacement(context);
    return getActualState(
        defaultBlockState()
            .setValue(
                WATERLOGGED,
                context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER)),
        context.getLevel(),
        context.getClickedPos());
  }

  @Override
  public BlockState updateShape(
      BlockState state,
      LevelReader world,
      ScheduledTickAccess ticks,
      BlockPos position,
      Direction direction,
      BlockPos neighborPosition,
      BlockState neighbor,
      RandomSource random) {
    if (!canBoost)
      return super.updateShape(
          state, world, ticks, position, direction, neighborPosition, neighbor, random);
    if (state.getValue(WATERLOGGED))
      ticks.scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickDelay(world));
    return getActualState(state, world, position);
  }

  private static boolean metalFence(BlockState state) {
    return state.is(BlockTags.FENCES) && !state.is(BlockTags.WOODEN_FENCES);
  }

  public BlockState getActualState(BlockState state, BlockGetter world, BlockPos position) {
    if (!canBoost) {
      for (var side : SIDES) {
        var adjacent = position.relative(side);
        var neighbor = world.getBlockState(adjacent);
        state =
            state.setValue(
                connectProperties.get(side),
                connectsTo(
                    neighbor, neighbor.isFaceSturdy(world, adjacent, side.getOpposite()), side));
      }
      return state;
    }
    boolean branch = false;
    for (var side : SIDES)
      if (metalFence(world.getBlockState(position.relative(side)))) {
        branch = true;
        break;
      }
    for (var side : SIDES) {
      var adjacent = position.relative(side);
      var neighbor = world.getBlockState(adjacent);
      boolean connected =
          metalFence(neighbor) || !branch && source(world, adjacent, side, false) != null;
      state = state.setValue(connectProperties.get(side), connected);
    }
    return state;
  }

  private static PoleDrive source(
      BlockGetter world, BlockPos position, Direction side, boolean powered) {
    if (!world.getBlockState(position).is(UtilityBlocks.MAGNETIZER)
        || !(world.getBlockEntity(position) instanceof PoleDrive machine)
        || machine.isRemoved()
        || side.getOpposite() != machine.getFacing()
        || powered && !machine.canBoost()) return null;
    return machine;
  }

  private static boolean poleAt(BlockGetter world, BlockPos position) {
    if (!(world.getBlockState(position).getBlock() instanceof IndustrialFence block)
        || !block.canBoost) return false;
    for (var side : SIDES)
      if (metalFence(world.getBlockState(position.relative(side)))) return false;
    return true;
  }

  private List<PoleDrive> atHeight(
      BlockGetter world, BlockPos position, int distance, boolean powered) {
    ArrayList<PoleDrive> sources = null;
    for (var side : SIDES) {
      var machine = source(world, position.relative(side), side, powered);
      if (machine != null && distance <= machine.getPoleRange()) {
        if (sources == null) sources = new ArrayList<>(2);
        sources.add(machine);
      }
    }
    return sources == null ? List.of() : sources;
  }

  private List<PoleDrive> getMagnetizers(BlockGetter world, BlockPos start, boolean powered) {
    if (!canBoost || !poleAt(world, start)) return List.of();
    var adjacent = atHeight(world, start, 0, powered);
    if (!adjacent.isEmpty()) return adjacent;
    boolean down = true, up = true;
    for (int distance = 1; distance <= 276 && (down || up); distance++) {
      List<PoleDrive> lower = List.of(), upper = List.of();
      if (down) {
        var position = start.below(distance);
        down = poleAt(world, position);
        if (down) lower = atHeight(world, position, distance, powered);
      }
      if (up) {
        var position = start.above(distance);
        up = poleAt(world, position);
        if (up) upper = atHeight(world, position, distance, powered);
      }
      if (lower.isEmpty()) {
        if (!upper.isEmpty()) return upper;
      } else {
        if (upper.isEmpty()) return lower;
        var combined = new ArrayList<PoleDrive>(lower.size() + upper.size());
        combined.addAll(lower);
        combined.addAll(upper);
        return combined;
      }
    }
    return List.of();
  }

  private boolean powered(Level world, BlockPos position) {
    var machines = getMagnetizers(world, position, true);
    if (machines.isEmpty()) return false;
    if (world.isClientSide()) return true;
    double share = 1.0 / machines.size();
    for (var machine : machines) if (!machine.tryBoost(share)) return false;
    for (var machine : machines)
      SideServices.network(true).updateTileEntityField(machine, "energy");
    return true;
  }

  @Override
  public void entityInside(
      BlockState state,
      Level world,
      BlockPos position,
      Entity entity,
      net.minecraft.world.entity.InsideBlockEffectApplier effects,
      boolean precise) {
    if (!canBoost
        || !(entity instanceof Player player)
        || !player.isAlive()
        || player.isSpectator()) return;
    boolean enabled = powered(world, position),
        shoes = hasMetalShoes(player),
        descending = player.isShiftKeyDown();
    var velocity = player.getDeltaMovement();
    boolean slow = velocity.y >= -0.25 && velocity.y < 1.6;
    if (slow) player.fallDistance = 0;
    if (descending) {
      if (!slow && (enabled || shoes))
        player.setDeltaMovement(velocity.multiply(1, enabled ? 0.8 : 0.9, 1));
      return;
    }
    if (!enabled) return;
    double vertical = velocity.y + 0.075;
    if (vertical > 0) vertical *= 1.03;
    double limit = SideServices.keyboard().isAltKeyDown(player) ? 0.1 : shoes ? 1.5 : 0.5;
    player.setDeltaMovement(velocity.x, Math.min(vertical, limit), velocity.z);
  }

  public static boolean hasMetalShoes(Player player) {
    var shoes = player.getItemBySlot(EquipmentSlot.FEET);
    return shoes.is(Items.IRON_BOOTS)
        || shoes.is(Items.GOLDEN_BOOTS)
        || shoes.is(Items.CHAINMAIL_BOOTS)
        || ItemWrapper.isMetalArmor(shoes, player);
  }
}
