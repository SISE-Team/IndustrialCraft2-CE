package ic2ce.neoforge.item.tool;

import ic2ce.api.item.IBoxable;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Mechanical tapping applies finite native wear only after real extraction or scar depletion. */
public class ManualResinTap extends Item implements IBoxable {
  public ManualResinTap(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    var world = context.getLevel();
    var position = context.getClickedPos();
    var state = world.getBlockState(position);
    if (!state.is(BlockDefinitions.RUBBER_LOG)) return InteractionResult.PASS;
    var player = context.getPlayer();
    var held = context.getItemInHand();
    if (!held.is(this)
        || held.isEmpty()
        || player != null
            && !player.getAbilities().instabuild
            && held.getDamageValue() >= held.getMaxDamage()) return InteractionResult.FAIL;
    if (!ResinExtraction.extract(
        player, world, position, context.getClickedFace(), state, null, false, held))
      return InteractionResult.FAIL;
    if (!world.isClientSide() && player != null && !player.getAbilities().instabuild)
      held.hurtAndBreak(1, player, context.getHand());
    return InteractionResult.SUCCESS;
  }

  public static boolean attemptExtract(
      Player player,
      Level world,
      BlockPos position,
      Direction face,
      BlockState state,
      List<ItemStack> collection,
      boolean electric) {
    return ResinExtraction.extract(player, world, position, face, state, collection, electric);
  }

  @Override
  public boolean canBeStoredInToolbox(ItemStack stack) {
    return stack != null && !stack.isEmpty();
  }

  public static SoundEvent getToolUseSound(boolean electric) {
    return electric
        ? SoundDefinitions.ITEM_TREETAP_ELECTRIC_USE
        : SoundDefinitions.ITEM_TREETAP_USE;
  }
}
