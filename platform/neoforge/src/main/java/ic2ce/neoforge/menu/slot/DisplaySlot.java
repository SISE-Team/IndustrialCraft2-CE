package ic2ce.neoforge.menu.slot;

import ic2ce.neoforge.inventory.InventorySlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** A visible owned inventory slot has no physical click/extraction path. */
public final class DisplaySlot extends MachineSlot {
  public DisplaySlot(InventorySlot slots, int index, int x, int y) {
    super(slots, index, x, y);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return false;
  }

  @Override
  public boolean mayPickup(Player player) {
    return false;
  }

  @Override
  public ItemStack remove(int amount) {
    return ItemStack.EMPTY;
  }

  @Override
  public void onTake(Player player, ItemStack stack) {}
}
