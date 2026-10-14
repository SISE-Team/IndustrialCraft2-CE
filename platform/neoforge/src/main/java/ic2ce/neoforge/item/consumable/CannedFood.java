package ic2ce.neoforge.item.consumable;

import ic2ce.core.item.*;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * Instant preserved food pays one actual can per food point and returns the exact used containers.
 */
public class CannedFood extends Item {
  public CannedFood(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult use(Level world, Player owner, InteractionHand hand) {
    var stack = owner.getItemInHand(hand);
    if (!stack.is(this) || !owner.getFoodData().needsFood()) return InteractionResult.PASS;
    return world.isClientSide() ? InteractionResult.SUCCESS : onEaten(owner, stack);
  }

  public InteractionResult onEaten(Player owner, ItemStack stack) {
    if (owner.level().isClientSide() || stack == null || !stack.is(this))
      return InteractionResult.PASS;
    int used = Math.min(stack.getCount(), Math.max(0, 20 - owner.getFoodData().getFoodLevel()));
    if (used <= 0) return InteractionResult.PASS;
    owner.getFoodData().eat(used, used);
    if (!owner.getAbilities().instabuild) {
      stack.shrink(used);
      var empties = new ItemStack(ItemDefinitions.TIN_CAN, used);
      if (!owner.getInventory().add(empties)) owner.drop(empties, false);
    }
    return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
  }
}
