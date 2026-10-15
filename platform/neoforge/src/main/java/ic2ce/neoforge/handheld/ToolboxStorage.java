package ic2ce.neoforge.handheld;

import ic2ce.api.item.ItemWrapper;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.handheld.menu.ToolboxMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;

/** Native toolbox stores only accepted physical tools in owned bounded slots. */
public class ToolboxStorage extends HandheldStorage {
  public ToolboxStorage(Player owner, InteractionHand hand, ItemStack stack, int size) {
    super(owner, hand, stack, size);
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    return slot >= 0
        && slot < getContainerSize()
        && !isThisContainer(stack)
        && ItemWrapper.canBeStoredInToolbox(stack);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player actor) {
    return new ToolboxMenu(id, this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new ToolboxMenu(id, this);
  }
}
