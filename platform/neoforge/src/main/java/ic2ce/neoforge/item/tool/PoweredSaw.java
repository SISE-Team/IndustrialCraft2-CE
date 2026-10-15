package ic2ce.neoforge.item.tool;

import ic2ce.api.item.*;
import ic2ce.core.IHitSoundOverride;
import ic2ce.core.util.StackTag;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.core.util.Util;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.registry.ToolProfiles;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * Powered axe and entity/block shearing keep separate native payment, cancellation and output
 * policies.
 */
public class PoweredSaw extends PoweredTool
    implements IHitSoundOverride, BlockBreakableItem, IEntityAttackableItem {
  public PoweredSaw(Properties properties) {
    super(properties, 100, ToolProfiles.CHAINSAW, List.of(BlockTags.MINEABLE_WITH_AXE));
    maxCharge = 30000;
    transferLimit = 100;
    tier = 1;
  }

  private static boolean shearing(ItemStack stack) {
    var data = StackTag.get(stack);
    return data == null || !data.getBooleanOr("disableShear", false);
  }

  @Override
  public InteractionResult use(Level world, Player player, InteractionHand hand) {
    var held = player.getItemInHand(hand);
    if (!world.isClientSide()
        && held.is(this)
        && ProjectRuntime.keyboard.isModeSwitchKeyDown(player)) {
      boolean enabled = !shearing(held);
      StackTag.update(
          held,
          data -> {
            data.putBoolean("disableShear", !enabled);
            return data;
          });
      ProjectRuntime.sideProxy.messagePlayer(
          player,
          "ic2.tooltip.mode",
          enabled ? "ic2.tooltip.mode.normal" : "ic2.tooltip.mode.noShear");
      return InteractionResult.SUCCESS.heldItemTransformedTo(held);
    }
    return super.use(world, player, hand);
  }

  @Override
  public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
    return super.isCorrectToolForDrops(stack, state)
        || state.is(Blocks.COBWEB)
        || Util.canShear(state);
  }

  @Override
  public float getDestroySpeed(ItemStack stack, BlockState state) {
    return canUse(stack)
            && (state.is(BlockTags.MINEABLE_WITH_AXE)
                || state.is(Blocks.COBWEB)
                || Util.canShear(state))
        ? speed
        : 1;
  }

  @Override
  public boolean onAttackEntity(Player player, Entity target) {
    if (!player.level().isClientSide()
        && consumeEnergy(player.getMainHandItem(), operationEnergyCost, player))
      playUsingSound(player);
    return true;
  }

  @Override
  public InteractionResult onBlockStartBreak(
      Player player, Level world, InteractionHand hand, BlockPos position, Direction direction) {
    var held = player.getItemInHand(hand);
    var state = world.getBlockState(position);
    if (!held.is(this)
        || !shearing(held)
        || !Util.canShear(state)
        || !canUse(held)
        || !player.mayUseItemAt(position, direction, held)) return InteractionResult.PASS;
    if (world.isClientSide()) return InteractionResult.SUCCESS;
    if (!(world instanceof ServerLevel server)
        || !world.mayInteract(player, position)
        || NeoForge.EVENT_BUS.post(new BreakBlockEvent(world, position, state, player)).isCanceled()
        || world.getBlockState(position) != state) return InteractionResult.PASS;
    double paid =
        ElectricItem.manager.discharge(held, operationEnergyCost, tier, true, false, false);
    if (paid + 1e-8 < operationEnergyCost) {
      if (paid > 0) ElectricItem.manager.charge(held, paid, tier, true, false);
      return InteractionResult.PASS;
    }
    if (!world.removeBlock(position, false)) {
      ElectricItem.manager.charge(held, paid, tier, true, false);
      return InteractionResult.PASS;
    }
    StackUtil.dropAsEntity(world, position, new ItemStack(state.getBlock().asItem()));
    world.levelEvent(player, 2001, position, Block.getId(state));
    if (state.is(BlockTags.GUARDED_BY_PIGLINS)) PiglinAi.angerNearbyPiglins(server, player, false);
    world.gameEvent(GameEvent.BLOCK_DESTROY, position, GameEvent.Context.of(player, state));
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResult interactLivingEntity(
      ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
    if (!stack.is(this)
        || !shearing(stack)
        || !(entity instanceof Shearable shearable)
        || !entity.isAlive()
        || !shearable.readyForShearing()
        || !canUse(stack)) return InteractionResult.PASS;
    if (player.level().isClientSide()) {
      playUsingSound(player);
      return InteractionResult.SUCCESS;
    }
    if (!(player.level() instanceof ServerLevel world)
        || !consumeEnergy(stack, operationEnergyCost, player)) return InteractionResult.PASS;
    shearable.shear(world, SoundSource.PLAYERS, stack);
    return InteractionResult.SUCCESS;
  }

  public void playUsingSound(LivingEntity owner) {
    if (owner.level().isClientSide()) owner.playSound(getToolUsingSound(), 1, 1);
  }

  public SoundEvent getToolUsingSound() {
    return ProjectRuntime.random.nextBoolean()
        ? SoundDefinitions.ITEM_CHAINSAW_USE1
        : SoundDefinitions.ITEM_CHAINSAW_USE2;
  }

  @Override
  public SoundEvent getHitSoundForBlock(
      LocalPlayer player, Level world, BlockPos position, ItemStack stack) {
    return getToolUsingSound();
  }

  @Override
  public SoundEvent getBreakSoundForBlock(
      LocalPlayer player, Level world, BlockPos position, ItemStack stack) {
    return null;
  }

  @Override
  protected SoundEvent getIdleSound(LivingEntity owner, ItemStack stack) {
    return SoundDefinitions.ITEM_CHAINSAW_IDLE;
  }

  @Override
  protected SoundEvent getStopSound(LivingEntity owner, ItemStack stack) {
    return SoundDefinitions.ITEM_CHAINSAW_STOP;
  }

  @Override
  public boolean beforeBlockBreak(
      Level world, Player player, BlockPos position, BlockState state, BlockEntity entity) {
    return true;
  }

  @Override
  public void afterBlockBreak(
      Level world, Player player, BlockPos position, BlockState state, BlockEntity entity) {}
}
