package ic2ce.neoforge.world.block;

import ic2ce.neoforge.material.MaterialItems;
import ic2ce.neoforge.world.WorldBlocks;
import java.util.Objects;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.*;

/** Resin regeneration and physical native axe stripping are separate server-owned log protocols. */
public final class ResinLog extends RotatedPillarBlock {
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

  public static final EnumProperty<RubberWoodState> stateProperty =
      EnumProperty.create("state", RubberWoodState.class);

  public ResinLog(Properties properties) {
    super(properties);
    registerDefaultState(defaultBlockState().setValue(stateProperty, RubberWoodState.plain));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(stateProperty);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState()
        .setValue(AXIS, context.getClickedFace().getAxis())
        .setValue(stateProperty, RubberWoodState.plain);
  }

  @Override
  public void randomTick(
      BlockState state, ServerLevel world, BlockPos position, RandomSource random) {
    var resin = state.getValue(stateProperty);
    if (resin.canRegenerate() && random.nextInt(7) == 0 && world.getBlockState(position) == state)
      world.setBlockAndUpdate(position, state.setValue(stateProperty, resin.getWet()));
  }

  @Override
  public BlockState getToolModifiedState(
      BlockState state, UseOnContext context, ItemAbility ability, boolean simulate) {
    return ability == ItemAbilities.AXE_STRIP && context.getItemInHand().canPerformAction(ability)
        ? WorldBlocks.STRIPPED_RUBBER_LOG
            .defaultBlockState()
            .setValue(AXIS, state.getValue(AXIS))
        : null;
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
    if (!(stack.getItem() instanceof AxeItem)) return InteractionResult.TRY_WITH_EMPTY_HAND;
    if (!owner.getAbilities().mayBuild
        || !world.mayInteract(owner, position)
        || !owner.mayUseItemAt(position, hit.getDirection(), stack)) return InteractionResult.FAIL;
    var resin = state.getValue(stateProperty);
    var result = stack.getItem().useOn(new UseOnContext(owner, hand, hit));
    if (!world.isClientSide()
        && result.consumesAction()
        && resin.wet
        && world.getBlockState(position).is(WorldBlocks.STRIPPED_RUBBER_LOG))
      Block.popResourceFromFace(
          world,
          position,
          hit.getDirection(),
          new ItemStack(MaterialItems.RESIN, world.getRandom().nextInt(2) + 1));
    return result;
  }

  public enum RubberWoodState implements StringRepresentable {
    plain(null, false),
    dry_north(Direction.NORTH, false),
    dry_south(Direction.SOUTH, false),
    dry_west(Direction.WEST, false),
    dry_east(Direction.EAST, false),
    wet_north(Direction.NORTH, true),
    wet_south(Direction.SOUTH, true),
    wet_west(Direction.WEST, true),
    wet_east(Direction.EAST, true);
    public final Direction facing;
    public final boolean wet;

    RubberWoodState(Direction face, boolean wet) {
      facing = face;
      this.wet = wet;
    }

    @Override
    public String getSerializedName() {
      return name();
    }

    public boolean isPlain() {
      return facing == null;
    }

    public boolean canRegenerate() {
      return facing != null && !wet;
    }

    public RubberWoodState getWet() {
      return facing == null ? null : getWet(facing);
    }

    public RubberWoodState getDry() {
      return facing == null
          ? plain
          : switch (facing) {
            case NORTH -> dry_north;
            case SOUTH -> dry_south;
            case WEST -> dry_west;
            case EAST -> dry_east;
            default -> throw new IllegalStateException("Vertical resin hole");
          };
    }

    public static RubberWoodState getWet(Direction face) {
      return switch (Objects.requireNonNull(face)) {
        case NORTH -> wet_north;
        case SOUTH -> wet_south;
        case WEST -> wet_west;
        case EAST -> wet_east;
        default -> throw new IllegalArgumentException("Resin holes require horizontal faces");
      };
    }
  }
}
