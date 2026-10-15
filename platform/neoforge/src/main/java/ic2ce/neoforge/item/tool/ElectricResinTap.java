package ic2ce.neoforge.item.tool;

import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.world.WorldBlocks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

/** Electric tapping reserves its fifty-EU action before touching a real wet/dry resin scar. */
public class ElectricResinTap extends PoweredTool {
  public ElectricResinTap(Properties properties) {
    super(properties, 50);
    maxCharge = 10000;
    transferLimit = 100;
    tier = 1;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    var world = context.getLevel();
    var position = context.getClickedPos();
    var state = world.getBlockState(position);
    var held = context.getItemInHand();
    var player = context.getPlayer();
    if (!state.is(WorldBlocks.RUBBER_LOG) || player == null || !held.is(this))
      return InteractionResult.PASS;
    boolean creative = player.getAbilities().instabuild;
    if (!creative && !canUse(held)) return InteractionResult.PASS;
    if (world.isClientSide())
      return ResinExtraction.extract(
              player, world, position, context.getClickedFace(), state, null, true, held)
          ? InteractionResult.SUCCESS
          : InteractionResult.PASS;
    double paid =
        creative ? 50 : ElectricItem.manager.discharge(held, 50, tier, true, false, false);
    if (paid < 50) {
      if (!creative && paid > 0) ElectricItem.manager.charge(held, paid, tier, true, false);
      return InteractionResult.PASS;
    }
    if (ResinExtraction.extract(
        player, world, position, context.getClickedFace(), state, null, true, held))
      return InteractionResult.SUCCESS;
    if (!creative) ElectricItem.manager.charge(held, paid, tier, true, false);
    return InteractionResult.PASS;
  }
}
