package ic2ce.neoforge.inventory;

import ic2ce.core.block.IInventorySlotHolder;
import ic2ce.core.util.ItemComparableItemStack;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

/** Immutable normalized item keys; callers cannot change the accepted set through a prototype. */
public class StackWhitelistInput extends ConsumableInventory {
  private final Set<ItemComparableItemStack> accepted;

  public StackWhitelistInput(
      IInventorySlotHolder<?> holder, String name, int count, ItemStack... prototypes) {
    this(holder, name, Access.I, count, InvSide.TOP, prototypes);
  }

  public StackWhitelistInput(
      IInventorySlotHolder<?> holder,
      String name,
      Access access,
      int count,
      InvSide side,
      ItemStack... prototypes) {
    super(holder, name, access, count, side);
    var keys = new HashSet<ItemComparableItemStack>();
    for (var prototype : Objects.requireNonNull(prototypes)) {
      if (prototype == null || prototype.isEmpty())
        throw new IllegalArgumentException("Empty accepted item prototype");
      keys.add(new ItemComparableItemStack(prototype, true));
    }
    accepted = Set.copyOf(keys);
  }

  @Override
  public boolean accepts(ItemStack stack) {
    return stack != null
        && !stack.isEmpty()
        && accepted.contains(new ItemComparableItemStack(stack, false));
  }
}
