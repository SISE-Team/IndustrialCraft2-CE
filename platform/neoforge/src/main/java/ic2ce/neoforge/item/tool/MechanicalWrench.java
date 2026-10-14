package ic2ce.neoforge.item.tool;

import ic2ce.api.item.IBoxable;
import ic2ce.core.item.PriorityUsableItem;
import ic2ce.neoforge.registry.ItemGroups;
import ic2ce.neoforge.registry.SoundDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Mechanical wrench retains its native durability protocol and protects failed/cancelled world
 * operations.
 */
public class MechanicalWrench extends Item implements PriorityUsableItem, IBoxable {
  public static final int wrenchUseFailed = -1, wrenchUsePass = -2;

  public enum WrenchResult {
    Rotated,
    Removed,
    Nothing
  }

  public MechanicalWrench(Properties properties) {
    super(properties.repairable(ItemGroups.BRONZE_INGOTS));
  }

  public boolean canTakeDamage(ItemStack stack, int amount) {
    return amount > 0
        && stack != null
        && stack.is(this)
        && !stack.isEmpty()
        && stack.getDamageValue() < stack.getMaxDamage();
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    var player = context.getPlayer();
    if (player == null) return InteractionResult.PASS;
    if (!canTakeDamage(stack, 1)) return InteractionResult.FAIL;
    int result = onWrenchUse(player, stack, context, canTakeDamage(stack, 10));
    if (result < 0)
      return result == wrenchUsePass ? InteractionResult.PASS : InteractionResult.FAIL;
    if (!context.getLevel().isClientSide()) damage(stack, result, player, context.getHand());
    return InteractionResult.SUCCESS;
  }

  private static final WrenchActions.Payment MECHANICAL =
      new WrenchActions.Payment() {
        public boolean reserve(int units) {
          return true;
        }

        public void refund(int units) {}
      };

  public static int onWrenchUse(
      Player player, ItemStack stack, UseOnContext context, boolean allowRemoval) {
    var action =
        WrenchActions.execute(
            context.getLevel(),
            context.getClickedPos(),
            context.getClickedFace(),
            player,
            allowRemoval,
            MECHANICAL);
    if (action == WrenchActions.Action.NONE) return wrenchUseFailed;
    if (context.getLevel().isClientSide()) {
      player.playSound(SoundDefinitions.ITEM_WRENCH_USE, 1, 1);
      return wrenchUsePass;
    }
    return action.units;
  }

  public static WrenchResult wrenchBlock(
      Level world, BlockPos position, Direction hit, Player player, boolean allowRemoval) {
    return switch (WrenchActions.execute(world, position, hit, player, allowRemoval, MECHANICAL)) {
      case NONE -> WrenchResult.Nothing;
      case ROTATE -> WrenchResult.Rotated;
      case REMOVE -> WrenchResult.Removed;
    };
  }

  public void damage(ItemStack stack, int amount, Player player, InteractionHand hand) {
    if (amount > 0 && !player.getAbilities().instabuild && !player.level().isClientSide())
      stack.hurtAndBreak(amount, player, hand);
  }

  @Override
  public boolean canBeStoredInToolbox(ItemStack stack) {
    return stack != null && !stack.isEmpty();
  }

  public boolean isValidRepairItem(ItemStack target, ItemStack material) {
    return material != null
        && ic2ce.neoforge.material.CommonMaterialTags.matches(material, ItemGroups.BRONZE_INGOTS);
  }
}
