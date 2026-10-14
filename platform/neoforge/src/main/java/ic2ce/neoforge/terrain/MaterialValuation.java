package ic2ce.neoforge.terrain;

import ic2ce.core.util.ItemComparableItemStack;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.ItemStack;

/**
 * Native material values are monotone per owned item key and saturate aggregate arithmetic instead
 * of wrapping negative.
 */
public final class MaterialValuation {
  private static final Map<ItemComparableItemStack, Integer> VALUES = new ConcurrentHashMap<>();

  private MaterialValuation() {}

  public static void add(ItemStack material, int value) {
    if (material == null || material.isEmpty() || value <= 0)
      throw new IllegalArgumentException("Positive nonempty material value required");
    VALUES.merge(new ItemComparableItemStack(material, true), value, Math::max);
  }

  public static int get(ItemStack material) {
    if (material == null || material.isEmpty()) return 0;
    long value =
        (long) VALUES.getOrDefault(new ItemComparableItemStack(material, false), 0)
            * material.getCount();
    return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value));
  }

  public static int get(List<ItemStack> materials) {
    long total = 0;
    for (var stack : materials) {
      total += get(stack);
      if (total >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
    }
    return (int) total;
  }
}
