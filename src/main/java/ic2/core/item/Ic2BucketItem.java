package ic2.core.item;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public abstract class Ic2BucketItem extends BucketItem {
   protected Fluid fluid;
   private final List<Fluid> drainableFluidList;

   public Ic2BucketItem(Fluid fluid, Properties settings) {
      super(fluid == Fluids.EMPTY ? Fluids.EMPTY : fluid, settings);
      this.fluid = fluid;
      this.drainableFluidList = this.getDrainableFluidList();
   }

   @Override
   public InteractionResult use(Level world, Player user, InteractionHand hand) {
      ItemStack itemStack = user.getItemInHand(hand);
      BlockHitResult blockHitResult = getPlayerPOVHitResult(
         world,
         user,
         this.fluid == Fluids.EMPTY ? net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY : net.minecraft.world.level.ClipContext.Fluid.NONE
      );
      if (blockHitResult.getType() == HitResult.Type.MISS) {
         return InteractionResult.PASS;
      } else if (blockHitResult.getType() != HitResult.Type.BLOCK) {
         return InteractionResult.PASS;
      } else {
         BlockPos blockPos = blockHitResult.getBlockPos();
         Direction direction = blockHitResult.getDirection();
         BlockPos blockPos1 = blockPos.relative(direction);
         if (!world.mayInteract(user, blockPos) || !user.mayUseItemAt(blockPos1, direction, itemStack)) {
            return InteractionResult.FAIL;
         } else if (this.fluid == Fluids.EMPTY) {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock() instanceof BucketPickup bucketPickUpFluid) {
               ItemStack afterDrainingItemStack = this.tryDrainFluid(world, blockPos, blockState);
               if (!afterDrainingItemStack.isEmpty()) {
                  user.awardStat(Stats.ITEM_USED.get(this));
                  bucketPickUpFluid.getPickupSound(blockState).ifPresent(p_150709_ -> user.playSound(p_150709_, 1.0F, 1.0F));
                  world.gameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
                  ItemStack exchangedStack = ItemUtils.createFilledResult(itemStack, user, afterDrainingItemStack);
                  if (!world.isClientSide()) {
                     CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)user, afterDrainingItemStack);
                  }

                  return InteractionResult.SUCCESS.heldItemTransformedTo(exchangedStack);
               }
            }

            return InteractionResult.FAIL;
         } else {
            BlockState blockState = world.getBlockState(blockPos);
            BlockPos fluidDrainablePos = this.canBlockContainFluid(world, blockPos, blockState) ? blockPos : blockPos1;
            if (this.bucketUseOnBlock(new UseOnContext(user, hand, blockHitResult))) {
               return InteractionResult.PASS;
            } else if (this.emptyContents(user, world, fluidDrainablePos, blockHitResult)) {
               this.checkExtraContent(user, world, itemStack, fluidDrainablePos);
               if (user instanceof ServerPlayer) {
                  CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)user, fluidDrainablePos, itemStack);
               }

               user.awardStat(Stats.ITEM_USED.get(this));
               ItemStack emptiedBucketExchangedItemStack = ItemUtils.createFilledResult(itemStack, user, this.getEmptiedBucketStack(itemStack, user));
               return InteractionResult.SUCCESS.heldItemTransformedTo(emptiedBucketExchangedItemStack);
            } else {
               return InteractionResult.FAIL;
            }
         }
      }
   }

   private ItemStack getEmptiedBucketStack(ItemStack stack, Player player) {
      return !player.getAbilities().instabuild ? new ItemStack(this.getEmptiedBucketItem()) : stack;
   }

   public ItemStack tryDrainFluid(LevelAccessor world, BlockPos pos, BlockState state) {
      if (state.getBlock() instanceof LiquidBlock) {
         Fluid fluid = state.getFluidState().getType();
         if (this.drainableFluidList.contains(fluid)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            return new ItemStack(this.getBucketItem(fluid));
         }
      }

      if (state.getBlock() instanceof SimpleWaterloggedBlock && state.getValue(BlockStateProperties.WATERLOGGED)) {
         world.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), 3);
         if (!state.canSurvive(world, pos)) {
            world.destroyBlock(pos, true);
         }

         return new ItemStack(this.getBucketItem(Fluids.WATER));
      } else {
         return this.tryDrain(world, pos, state);
      }
   }

   public abstract Item getEmptiedBucketItem();

   public abstract List<Fluid> getDrainableFluidList();

   public abstract Item getBucketItem(Fluid var1);

   public ItemStack tryDrain(LevelAccessor world, BlockPos pos, BlockState state) {
      return ItemStack.EMPTY;
   }

   public boolean bucketUseOnBlock(UseOnContext context) {
      return false;
   }

   public boolean emptyContents(@Nullable LivingEntity user, Level world, BlockPos pos, @Nullable BlockHitResult hitResult) {
      if (!(this.fluid instanceof FlowingFluid)) {
         return false;
      } else {
         BlockState blockState = world.getBlockState(pos);
         Block block = blockState.getBlock();
         boolean replaceable = blockState.canBeReplaced(this.fluid);
         boolean placeable = blockState.isAir()
            || replaceable
            || block instanceof LiquidBlockContainer && ((LiquidBlockContainer)block).canPlaceLiquid(user, world, pos, blockState, this.fluid);
         if (!placeable) {
            return hitResult != null && this.emptyContents(user, world, hitResult.getBlockPos().relative(hitResult.getDirection()), null);
         } else if (world.dimension() == Level.NETHER && this.fluid.is(FluidTags.WATER)) {
            world.playSound(user instanceof Player player ? player : null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.8F);
            return true;
         } else if (block instanceof LiquidBlockContainer && this.fluid == Fluids.WATER) {
            ((LiquidBlockContainer)block).placeLiquid(world, pos, blockState, ((FlowingFluid)this.fluid).getSource(false));
            this.playEmptySound(user, world, pos);
            return true;
         } else {
            if (!world.isClientSide() && replaceable && blockState.getFluidState().isEmpty()) {
               world.destroyBlock(pos, true);
            }

            if (!world.setBlock(pos, this.fluid.defaultFluidState().createLegacyBlock(), 11) && !blockState.getFluidState().isSource()) {
               return false;
            } else {
               this.playEmptySound(user, world, pos);
               return true;
            }
         }
      }
   }

   private boolean canBlockContainFluid(Level worldIn, BlockPos posIn, BlockState blockstate) {
      return blockstate.getBlock() instanceof LiquidBlockContainer
         && ((LiquidBlockContainer)blockstate.getBlock()).canPlaceLiquid(null, worldIn, posIn, blockstate, this.fluid);
   }

   protected void playEmptySound(@Nullable LivingEntity user, LevelAccessor world, BlockPos pos) {
      net.minecraft.sounds.SoundEvent soundEvent = this.fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
      world.playSound(user instanceof Player player ? player : null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
      world.gameEvent(user, GameEvent.FLUID_PLACE, pos);
   }
}
