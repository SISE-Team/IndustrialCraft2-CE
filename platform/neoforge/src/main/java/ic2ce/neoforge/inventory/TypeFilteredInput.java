package ic2ce.neoforge.inventory;

import java.util.Objects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class TypeFilteredInput extends ConsumableInventory {
  private final Class<?> acceptedType;

  public TypeFilteredInput(IInventorySlotHolder<?> holder, String name, int count, Class<?> type) {
    this(holder, name, Access.I, count, InvSide.TOP, type);
  }

  public TypeFilteredInput(
      IInventorySlotHolder<?> holder,
      String name,
      Access access,
      int count,
      InvSide side,
      Class<?> type) {
    super(holder, name, access, count, side);
    acceptedType = Objects.requireNonNull(type);
  }

  @Override
  public boolean accepts(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return false;
    Object value = stack.getItem() instanceof BlockItem item ? item.getBlock() : stack.getItem();
    return acceptedType.isInstance(value);
  }
}
