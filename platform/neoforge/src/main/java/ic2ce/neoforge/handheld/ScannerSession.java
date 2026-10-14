package ic2ce.neoforge.handheld;

import ic2ce.core.network.GrowingBuffer;
import ic2ce.neoforge.handheld.menu.ScannerMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;

/**
 * Read-only ore report carries the actual scanner session and no duplicated player/stack fields.
 */
public class ScannerSession extends HandheldStorage {
  public ScannerSession(Player owner, InteractionHand hand, ItemStack stack) {
    super(owner, hand, stack, 0);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player actor) {
    return new ScannerMenu(id, actor.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new ScannerMenu(id, inventory, this);
  }
}
