package ic2ce.neoforge.item.tool;

import ic2ce.api.item.IMiningDrill;
import ic2ce.core.IHitSoundOverride;
import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mining policy is owned by the drill; player-dependent speed uses the native break-speed event
 * instead of searching all players.
 */
public class MiningDrill extends PoweredTool implements IMiningDrill, IHitSoundOverride {
  private record MiningPolicy(int runningCost, int duration, int blockCost) {}

  private final MiningPolicy mining;
  private final float speedMultiplier;

  public MiningDrill(
      Properties properties,
      int cost,
      ToolMaterial material,
      int capacity,
      int transfer,
      int tier,
      float miningSpeed) {
    super(
        properties,
        cost,
        material,
        List.of(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_SHOVEL));
    if (!Float.isFinite(miningSpeed) || miningSpeed <= 0 || material.speed() <= 0)
      throw new IllegalArgumentException("Invalid drill speed");
    maxCharge = capacity;
    transferLimit = transfer;
    this.tier = tier;
    speedMultiplier = miningSpeed / material.speed();
    mining =
        switch (cost) {
          case 50 -> new MiningPolicy(6, 200, 50);
          case 80 -> new MiningPolicy(20, 50, 80);
          case 800 -> new MiningPolicy(200, 20, 800);
          default -> new MiningPolicy(Math.max(1, cost / 4), 50, cost);
        };
  }

  @Override
  public float getDestroySpeed(ItemStack stack, BlockState state) {
    float base = super.getDestroySpeed(stack, state);
    return base == 1 ? 1 : base * speedMultiplier;
  }

  private void requireOwn(ItemStack stack) {
    if (stack == null || !stack.is(this))
      throw new IllegalArgumentException("Drill operation requires its own physical stack");
  }

  @Override
  public int energyUse(ItemStack stack, Level world, BlockPos position, BlockState state) {
    requireOwn(stack);
    return mining.runningCost;
  }

  @Override
  public int breakTime(ItemStack stack, Level world, BlockPos position, BlockState state) {
    requireOwn(stack);
    return mining.duration;
  }

  @Override
  public boolean breakBlock(ItemStack stack, Level world, BlockPos position, BlockState state) {
    requireOwn(stack);
    return !world.isClientSide() && consumeEnergy(stack, mining.blockCost, null);
  }

  @Override
  protected SoundEvent getIdleSound(LivingEntity actor, ItemStack stack) {
    return SoundDefinitions.ITEM_DRILL_IDLE;
  }

  @Override
  public SoundEvent getHitSoundForBlock(
      LocalPlayer player, Level world, BlockPos position, ItemStack stack) {
    return null;
  }

  @Override
  public SoundEvent getBreakSoundForBlock(
      LocalPlayer player, Level world, BlockPos position, ItemStack stack) {
    return world.getBlockState(position).getDestroySpeed(world, position) >= 3
        ? SoundDefinitions.ITEM_DRILL_HARD
        : SoundDefinitions.ITEM_DRILL_SOFT;
  }
}
