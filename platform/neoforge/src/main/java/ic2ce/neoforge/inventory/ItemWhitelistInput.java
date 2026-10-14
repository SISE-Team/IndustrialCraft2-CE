package ic2ce.neoforge.inventory;

import ic2ce.core.block.IInventorySlotHolder;
import java.util.Objects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemWhitelistInput extends ConsumableInventory {
  private final Item[] allowed;

  public ItemWhitelistInput(
      IInventorySlotHolder<?> holder, String name, int count, Item... allowed) {
    this(holder, name, Access.I, count, InvSide.TOP, allowed);
  }

  public ItemWhitelistInput(
      IInventorySlotHolder<?> holder,
      String name,
      Access access,
      int count,
      InvSide side,
      Item... allowed) {
    super(holder, name, access, count, side);
    this.allowed = allowed.clone();
    for (Item item : this.allowed) Objects.requireNonNull(item);
  }

  @Override
  public boolean accepts(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return false;
    Item item = stack.getItem();
    for (int i = 0; i < allowed.length; i++) if (allowed[i] == item) return true;
    return false;
  }
}
