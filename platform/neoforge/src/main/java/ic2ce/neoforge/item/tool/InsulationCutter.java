package ic2ce.neoforge.item.tool;

import ic2ce.api.item.IEnhancedOverlayProvider;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.cable.BareCableBlock;
import ic2ce.neoforge.material.MaterialItems;
import ic2ce.neoforge.registry.SoundDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Insulation addition escrows one physical rubber; removal owns its own three-point wear protocol.
 */
public class InsulationCutter extends DurableCraftingTool implements IEnhancedOverlayProvider {
  public InsulationCutter(Properties properties) {
    super(properties);
  }

  private boolean usable(Player player, InteractionHand hand) {
    var held = player.getItemInHand(hand);
    return held.is(this) && (player.getAbilities().instabuild || getRemainingUses(held) > 0);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    var player = context.getPlayer();
    if (player == null || !usable(player, context.getHand())) return InteractionResult.PASS;
    var world = context.getLevel();
    var position = context.getClickedPos();
    var state = world.getBlockState(position);
    if (!(state.getBlock() instanceof BareCableBlock cable)
        || !player.mayUseItemAt(position, context.getClickedFace(), context.getItemInHand()))
      return InteractionResult.PASS;
    int rubberSlot = -1;
    var inventory = player.getInventory();
    boolean creative = player.getAbilities().instabuild;
    if (!creative)
      for (int slot = 0; slot < inventory.getContainerSize(); slot++)
        if (inventory.getItem(slot).is(MaterialItems.RUBBER)) {
          rubberSlot = slot;
          break;
        }
    if (!creative && rubberSlot < 0) return InteractionResult.PASS;
    if (world.isClientSide())
      return cable.tryAddInsulation(state, world, position)
          ? InteractionResult.SUCCESS
          : InteractionResult.PASS;
    if (!world.mayInteract(player, position)) return InteractionResult.PASS;
    var reserved = creative ? ItemStack.EMPTY : inventory.removeItem(rubberSlot, 1);
    if (!creative && reserved.isEmpty()) return InteractionResult.PASS;
    if (!cable.tryAddInsulation(state, world, position)) {
      refund(player, rubberSlot, reserved);
      return InteractionResult.PASS;
    }
    if (!creative) context.getItemInHand().hurtAndBreak(1, player, context.getHand());
    inventory.setChanged();
    return InteractionResult.SUCCESS;
  }

  private static void refund(Player player, int slot, ItemStack reserved) {
    if (reserved.isEmpty()) return;
    var inventory = player.getInventory();
    var current = inventory.getItem(slot);
    if (current.isEmpty()) inventory.setItem(slot, reserved);
    else if (ItemStack.isSameItemSameComponents(current, reserved)
        && current.getCount() + reserved.getCount() <= current.getMaxStackSize()) {
      current.grow(reserved.getCount());
      inventory.setChanged();
    } else if (!inventory.add(reserved)) player.drop(reserved, false);
  }

  public boolean removeInsulation(
      Player player, InteractionHand hand, BlockState state, Level world, BlockPos position) {
    if (player == null
        || !(state.getBlock() instanceof BareCableBlock cable)
        || !usable(player, hand)
        || !player.mayUseItemAt(position, Direction.UP, player.getItemInHand(hand))
        || !cable.tryRemoveInsulation(state, world, position, true)) return false;
    if (world.isClientSide()) {
      player.playSound(SoundDefinitions.ITEM_CUTTER_USE, 1, 1);
      return true;
    }
    if (!world.mayInteract(player, position)
        || !cable.tryRemoveInsulation(state, world, position, false)) return false;
    if (!player.getAbilities().instabuild) {
      player.getItemInHand(hand).hurtAndBreak(3, player, hand);
      StackUtil.dropAsEntity(world, position, new ItemStack(MaterialItems.RUBBER));
    }
    return true;
  }

  @Override
  public boolean providesEnhancedOverlay(
      Level world, BlockPos position, Direction side, Player player, ItemStack stack) {
    return false;
  }
}
