package ic2ce.neoforge.item.container;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.stats.Stats;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.*;

/**
 * Source-only native cell pickup and owned container exchanges; special crop-care cells are a
 * separate protocol.
 */
public abstract class WorldFluidCell extends BucketItem {
  protected final Fluid fluid;

  public WorldFluidCell(Fluid fluid, Properties properties) {
    super(fluid == null ? Fluids.EMPTY : fluid, properties);
    this.fluid = fluid;
  }

  @Override
  public InteractionResult use(Level world, Player owner, InteractionHand hand) {
    var stack = owner.getItemInHand(hand);
    if (!stack.is(this)) return InteractionResult.PASS;
    var hit =
        getPlayerPOVHitResult(
            world,
            owner,
            fluid == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
    if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
    var pos = hit.getBlockPos();
    var face = hit.getDirection();
    if (!world.mayInteract(owner, pos) || !owner.mayUseItemAt(pos, face, stack))
      return InteractionResult.FAIL;
    var context = new UseOnContext(owner, hand, hit);
    if (bucketUseOnBlock(context)) return InteractionResult.SUCCESS;
    if (fluid == null) return InteractionResult.PASS;
    if (fluid == Fluids.EMPTY) {
      var state = world.getBlockState(pos);
      if (world.isClientSide())
        return canDrain(state) ? InteractionResult.SUCCESS : InteractionResult.PASS;
      var output = tryDrainFluid(world, pos, state);
      if (output.isEmpty()) return InteractionResult.FAIL;
      owner.awardStat(Stats.ITEM_USED.get(this));
      world.gameEvent(owner, GameEvent.FLUID_PICKUP, pos);
      if (owner instanceof ServerPlayer server)
        CriteriaTriggers.FILLED_BUCKET.trigger(server, output);
      return InteractionResult.SUCCESS.heldItemTransformedTo(
          ItemUtils.createFilledResult(stack, owner, output));
    }
    var state = world.getBlockState(pos);
    var destination =
        state.getBlock() instanceof LiquidBlockContainer container
                && container.canPlaceLiquid(owner, world, pos, state, fluid)
            ? pos
            : pos.relative(face);
    if (!owner.mayUseItemAt(destination, face, stack)) return InteractionResult.FAIL;
    if (!emptyContents(owner, world, destination, hit)) return InteractionResult.FAIL;
    if (world.isClientSide()) return InteractionResult.SUCCESS;
    checkExtraContent(owner, world, stack, destination);
    if (owner instanceof ServerPlayer server)
      CriteriaTriggers.PLACED_BLOCK.trigger(server, destination, stack);
    owner.awardStat(Stats.ITEM_USED.get(this));
    return InteractionResult.SUCCESS.heldItemTransformedTo(
        ItemUtils.createFilledResult(stack, owner, new ItemStack(getEmptiedBucketItem())));
  }

  private boolean canDrain(BlockState state) {
    return state.getFluidState().isSource()
        && getDrainableFluidList().contains(state.getFluidState().getType());
  }

  public ItemStack tryDrainFluid(LevelAccessor world, BlockPos pos, BlockState state) {
    if (!canDrain(state)) return tryDrain(world, pos, state);
    var source = state.getFluidState().getType();
    var item = getBucketItem(source);
    if (item == null) return ItemStack.EMPTY;
    if (state.getBlock() instanceof LiquidBlock) {
      if (!world.setBlock(pos, Blocks.AIR.defaultBlockState(), 11)) return ItemStack.EMPTY;
      return new ItemStack(item);
    }
    if (state.getBlock() instanceof SimpleWaterloggedBlock
        && state.hasProperty(BlockStateProperties.WATERLOGGED)
        && state.getValue(BlockStateProperties.WATERLOGGED)) {
      if (!world.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), 3))
        return ItemStack.EMPTY;
      return new ItemStack(item);
    }
    return tryDrain(world, pos, state);
  }

  public abstract Item getEmptiedBucketItem();

  public abstract List<Fluid> getDrainableFluidList();

  public abstract Item getBucketItem(Fluid fluid);

  public ItemStack tryDrain(LevelAccessor world, BlockPos pos, BlockState state) {
    return ItemStack.EMPTY;
  }

  public boolean bucketUseOnBlock(UseOnContext context) {
    return false;
  }

  @Override
  public boolean emptyContents(LivingEntity owner, Level world, BlockPos pos, BlockHitResult hit) {
    if (!(fluid instanceof FlowingFluid flow)) return false;
    var state = world.getBlockState(pos);
    var block = state.getBlock();
    boolean accepts =
        block instanceof LiquidBlockContainer container
            && container.canPlaceLiquid(owner, world, pos, state, fluid);
    boolean replaceable = state.canBeReplaced(fluid);
    if (!state.isAir() && !replaceable && !accepts) return false;
    if (world.isClientSide()) return true;
    if (world
            .environmentAttributes()
            .getValue(net.minecraft.world.attribute.EnvironmentAttributes.WATER_EVAPORATES, pos)
        && fluid.is(net.minecraft.tags.FluidTags.WATER)) {
      world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, .5F, 2.6F);
      return true;
    }
    if (accepts) {
      if (!((LiquidBlockContainer) block).placeLiquid(world, pos, state, flow.getSource(false)))
        return false;
    } else {
      if (replaceable && state.getFluidState().isEmpty()) world.destroyBlock(pos, true);
      if (!world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 11)) return false;
    }
    playEmptySound(owner, world, pos);
    return true;
  }

  @Override
  protected void playEmptySound(LivingEntity owner, LevelAccessor world, BlockPos pos) {
    world.playSound(
        owner instanceof Player player ? player : null,
        pos,
        fluid.is(net.minecraft.tags.FluidTags.LAVA)
            ? SoundEvents.BUCKET_EMPTY_LAVA
            : SoundEvents.BUCKET_EMPTY,
        SoundSource.BLOCKS,
        1,
        1);
    world.gameEvent(owner, GameEvent.FLUID_PLACE, pos);
  }
}
