package ic2ce.neoforge.world.block;

import ic2ce.neoforge.world.WorldBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.*;

/**
 * All-bark wood delegates correct hand wear and native modification events without resin
 * generation.
 */
public final class BarkWood extends Block {
  public BarkWood(Properties properties) {
    super(properties);
  }

  @Override
  public BlockState getToolModifiedState(
      BlockState state, UseOnContext context, ItemAbility ability, boolean simulate) {
    return ability == ItemAbilities.AXE_STRIP && context.getItemInHand().canPerformAction(ability)
        ? WorldBlocks.STRIPPED_RUBBER_WOOD.defaultBlockState()
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
    return stack.getItem().useOn(new UseOnContext(owner, hand, hit));
  }
}
