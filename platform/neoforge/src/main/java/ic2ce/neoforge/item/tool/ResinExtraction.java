package ic2ce.neoforge.item.tool;

import ic2ce.neoforge.material.MaterialItems;
import ic2ce.neoforge.registry.GameSignals;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.world.WorldBlocks;
import ic2ce.neoforge.world.block.ResinLog;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Native wet/dry resin protocols produce one owned quantity in either a collection or the world.
 */
public final class ResinExtraction {
  private ResinExtraction() {}

  public static boolean extract(
      Player player,
      Level world,
      BlockPos position,
      Direction face,
      BlockState expected,
      List<ItemStack> collection,
      boolean electric) {
    return extract(
        player,
        world,
        position,
        face,
        expected,
        collection,
        electric,
        player == null ? ItemStack.EMPTY : player.getMainHandItem());
  }

  public static boolean extract(
      Player player,
      Level world,
      BlockPos position,
      Direction face,
      BlockState expected,
      List<ItemStack> collection,
      boolean electric,
      ItemStack permissionStack) {
    if (world == null
        || face == null
        || !expected.is(WorldBlocks.RUBBER_LOG)
        || world.getBlockState(position) != expected) return false;
    var wood = expected.getValue(ResinLog.stateProperty);
    if (wood.isPlain() || wood.facing != face) return false;
    if (player != null
        && (!world.mayInteract(player, position)
            || !player.mayUseItemAt(position, face, permissionStack))) return false;
    if (world.isClientSide()) return wood.wet;
    boolean deplete = !wood.wet && world.getRandom().nextInt(5) == 0;
    int quantity =
        wood.wet ? world.getRandom().nextInt(3) + 1 : world.getRandom().nextInt(5) == 0 ? 1 : 0;
    var next =
        wood.wet
            ? expected.setValue(ResinLog.stateProperty, wood.getDry())
            : deplete
                ? expected.setValue(ResinLog.stateProperty, ResinLog.RubberWoodState.plain)
                : expected;
    if (quantity == 0 && next == expected || world.getBlockState(position) != expected)
      return false;
    if (next != expected && !world.setBlockAndUpdate(position, next)) return false;
    if (quantity > 0) {
      var output = new ItemStack(MaterialItems.RESIN, quantity);
      if (collection != null) collection.add(output);
      else {
        var entity =
            new ItemEntity(
                world,
                position.getX() + .5 + face.getStepX() * .3,
                position.getY() + .5 + face.getStepY() * .3,
                position.getZ() + .5 + face.getStepZ() * .3,
                output);
        entity.setDefaultPickUpDelay();
        world.addFreshEntity(entity);
      }
    }
    world.playSound(
        null,
        position,
        electric ? SoundDefinitions.ITEM_TREETAP_ELECTRIC_USE : SoundDefinitions.ITEM_TREETAP_USE,
        SoundSource.PLAYERS,
        1,
        1);
    world.gameEvent(GameEvent.BLOCK_CHANGE, position, GameEvent.Context.of(player, expected));
    world.gameEvent(
        net.minecraft.core.Holder.direct(GameSignals.TOOL_USE),
        position,
        GameEvent.Context.of(player));
    return true;
  }
}
