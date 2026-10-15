package ic2ce.neoforge.item.container;

import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.handheld.IHandHeldInventory;
import ic2ce.neoforge.handheld.ToolboxStorage;
import ic2ce.neoforge.handheld.menu.ToolboxMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * Physical portable toolbox opens a nine-slot tool inventory and saves the actual dropped stack.
 */
public class PortableToolbox extends Item implements IHandHeldInventory {
  public PortableToolbox(Properties properties) {
    super(properties.rarity(Rarity.UNCOMMON).stacksTo(1));
  }

  @Override
  public InteractionResult use(Level world, Player owner, InteractionHand hand) {
    var stack = owner.getItemInHand(hand);
    if (!stack.is(this)) return InteractionResult.PASS;
    if (!world.isClientSide()
        && !getInventory(owner, hand, stack).openManagedItem(owner, hand, null))
      return InteractionResult.FAIL;
    return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
  }

  public boolean onDroppedByPlayer(ItemStack stack, Player owner) {
    if (owner instanceof ServerPlayer actor
        && owner.containerMenu instanceof ToolboxMenu menu
        && menu.base.isThisContainer(stack)) {
      menu.base.saveAsThrown(stack);
      actor.closeContainer();
    }
    return true;
  }

  public Rarity getRarity(ItemStack stack) {
    return Rarity.UNCOMMON;
  }

  @Override
  public IHasGui getInventory(Player owner, InteractionHand hand, ItemStack stack) {
    return new ToolboxStorage(owner, hand, stack, 9);
  }
}
