package ic2ce.neoforge.menu.slot;

import ic2ce.neoforge.inventory.InventorySlot;
import java.util.Objects;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Native slot adapter owns physical extraction and reports the actual taken quantity once. */
public class MachineSlot extends Slot {
  public final InventorySlot invSlot;
  public final int index;

  private static Container owner(InventorySlot slots) {
    Objects.requireNonNull(slots);
    return (Container) Objects.requireNonNull(slots.base).getParent();
  }

  public MachineSlot(InventorySlot slots, int index, int x, int y) {
    super(owner(slots), slots.base.getBaseIndex(slots) + index, x, y);
    if (index < 0 || index >= slots.size()) throw new IndexOutOfBoundsException(index);
    invSlot = slots;
    this.index = index;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return stack != null && !stack.isEmpty() && invSlot.accepts(stack);
  }

  @Override
  public ItemStack getItem() {
    return invSlot.get(index);
  }

  @Override
  public void set(ItemStack stack) {
    invSlot.put(index, Objects.requireNonNull(stack));
    setChanged();
  }

  @Override
  public ItemStack remove(int amount) {
    var stored = getItem();
    if (amount <= 0 || stored.isEmpty()) return ItemStack.EMPTY;
    int taken = Math.min(amount, stored.getCount());
    var result = stored.copyWithCount(taken);
    invSlot.put(index, stored.copyWithCount(stored.getCount() - taken));
    setChanged();
    return result;
  }

  @Override
  public int getMaxStackSize() {
    return invSlot.getStackSizeLimit();
  }

  @Override
  public void onTake(Player player, ItemStack taken) {
    super.onTake(player, taken);
    invSlot.onPickupFromSlot(player, taken.copy());
  }
}
