package ic2ce.api.upgrade;

import java.util.*;
import net.minecraft.world.item.ItemStack;

/**
 * Owned prototype generations cannot be changed by registration inputs or enumeration consumers.
 */
public final class UpgradeRegistry {
  private static volatile List<ItemStack> registered = List.of();

  private UpgradeRegistry() {}

  public static synchronized ItemStack register(ItemStack stack) {
    if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IUpgradeItem))
      throw new IllegalArgumentException("Upgrade must implement IUpgradeItem");
    for (var existing : registered)
      if (ItemStack.isSameItemSameComponents(existing, stack)) return stack;
    var next = new ArrayList<>(registered);
    next.add(stack.copy());
    registered = List.copyOf(next);
    return stack;
  }

  public static Iterable<ItemStack> getUpgrades() {
    var snapshot = registered;
    var result = new ArrayList<ItemStack>(snapshot.size());
    for (var item : snapshot) result.add(item.copy());
    return List.copyOf(result);
  }
}
