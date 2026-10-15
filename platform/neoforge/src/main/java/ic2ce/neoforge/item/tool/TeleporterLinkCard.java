package ic2ce.neoforge.item.tool;

import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.runtime.SideServices;
import ic2ce.neoforge.utility.LinkedTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * One owned address selects a same-dimension pair; stale/unloaded targets never load arbitrary
 * chunks.
 */
public class TeleporterLinkCard extends Item {
  public TeleporterLinkCard(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult use(Level world, Player owner, InteractionHand hand) {
    var stack = owner.getItemInHand(hand);
    if (!stack.is(this)) return InteractionResult.PASS;
    if (!world.isClientSide()) {
      var data = StackTag.get(stack);
      if (data != null) {
        boolean justSet = data.getBooleanOr("targetJustSet", false);
        if (!justSet && data.getBooleanOr("targetSet", false)) {
          data.putBoolean("targetSet", false);
          SideServices.proxy().messagePlayer(owner, "ic2.frequency_transmitter.unlinked");
        }
        data.remove("targetJustSet");
        StackTag.set(stack, data);
      }
    }
    return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    var owner = context.getPlayer();
    var stack = context.getItemInHand();
    var world = context.getLevel();
    var pos = context.getClickedPos();
    if (owner == null
        || !stack.is(this)
        || !owner.mayBuild()
        || !owner.mayUseItemAt(pos, context.getClickedFace(), stack)
        || !(world.getBlockEntity(pos) instanceof LinkedTeleporter endpoint))
      return InteractionResult.PASS;
    if (world.isClientSide()) return InteractionResult.SUCCESS;
    var data = StackTag.getOrCreate(stack);
    boolean set = data.getBooleanOr("targetSet", false);
    boolean justSet = true;
    var target =
        new BlockPos(
            data.getIntOr("targetX", 0), data.getIntOr("targetY", 0), data.getIntOr("targetZ", 0));
    var dimension = world.dimension().identifier().toString();
    if (!set) {
      target = pos;
      set = true;
      SideServices.proxy().messagePlayer(owner, "ic2.frequency_transmitter.linked");
    } else if (!data.getStringOr("targetDimension", dimension).equals(dimension)
        || world.isOutsideBuildHeight(target)
        || !world.hasChunkAt(target)) {
      set = justSet = false;
    } else if (target.equals(pos))
      SideServices.proxy().messagePlayer(owner, "ic2.frequency_transmitter.self");
    else if (world.getBlockEntity(target) instanceof LinkedTeleporter other
        && !other.isRemoved()
        && owner.mayUseItemAt(target, context.getClickedFace(), stack)) {
      if (endpoint.hasTarget()
          && endpoint.getTarget().equals(target)
          && other.hasTarget()
          && other.getTarget().equals(pos))
        SideServices.proxy().messagePlayer(owner, "ic2.frequency_transmitter.unchanged");
      else {
        endpoint.setTarget(target);
        other.setTarget(pos);
        SideServices.proxy().messagePlayer(owner, "ic2.frequency_transmitter.established");
      }
    } else set = justSet = false;
    data.putBoolean("targetSet", set);
    data.putBoolean("targetJustSet", justSet);
    data.putInt("targetX", target.getX());
    data.putInt("targetY", target.getY());
    data.putInt("targetZ", target.getZ());
    data.putString("targetDimension", dimension);
    StackTag.set(stack, data);
    return InteractionResult.SUCCESS;
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      Item.TooltipContext context,
      TooltipDisplay display,
      java.util.function.Consumer<Component> output,
      TooltipFlag flags) {
    var data = StackTag.get(stack);
    output.accept(
        data != null && data.getBooleanOr("targetSet", false)
            ? Component.translatable(
                "ic2.frequency_transmitter.tooltip.target",
                data.getIntOr("targetX", 0),
                data.getIntOr("targetY", 0),
                data.getIntOr("targetZ", 0))
            : Component.translatable("ic2.frequency_transmitter.tooltip.blank"));
  }
}
