package ic2ce.api.item;

import java.util.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

/**
 * Immutable adapter generations publish identity-registered extension callbacks without per-query
 * mutable buckets.
 */
public final class ItemWrapper {
  private static volatile Map<Item, List<IBoxable>> boxable = Map.of();
  private static volatile Map<Item, List<IMetalArmor>> metal = Map.of();

  private ItemWrapper() {}

  public static synchronized void registerBoxable(Item item, IBoxable callback) {
    boxable = append(boxable, item, callback);
  }

  public static synchronized void registerMetalArmor(Item item, IMetalArmor callback) {
    metal = append(metal, item, callback);
  }

  private static <T> Map<Item, List<T>> append(Map<Item, List<T>> prior, Item item, T callback) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(callback);
    var values = prior.getOrDefault(item, List.of());
    for (var existing : values) if (existing == callback) return prior;
    var updated = new ArrayList<>(values);
    updated.add(callback);
    var next = new IdentityHashMap<>(prior);
    next.put(item, List.copyOf(updated));
    return Collections.unmodifiableMap(next);
  }

  public static boolean canBeStoredInToolbox(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return false;
    for (var callback : boxable.getOrDefault(stack.getItem(), List.of()))
      if (callback.canBeStoredInToolbox(stack)) return true;
    return stack.getItem() instanceof IBoxable callback && callback.canBeStoredInToolbox(stack);
  }

  public static boolean isMetalArmor(ItemStack stack, Player owner) {
    if (stack == null || stack.isEmpty()) return false;
    for (var callback : metal.getOrDefault(stack.getItem(), List.of()))
      if (callback.isMetalArmor(stack, owner)) return true;
    return stack.getItem() instanceof IMetalArmor callback && callback.isMetalArmor(stack, owner);
  }
}
