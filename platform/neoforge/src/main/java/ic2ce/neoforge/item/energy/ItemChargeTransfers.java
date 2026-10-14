package ic2ce.neoforge.item.energy;

import ic2ce.api.item.ElectricItem;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

/**
 * Physical source/target transfer pays the observed debit first and returns any unaccepted credit.
 */
public final class ItemChargeTransfers {
  private static final ThreadLocal<Set<ItemStack>> ACTIVE =
      ThreadLocal.withInitial(() -> java.util.Collections.newSetFromMap(new IdentityHashMap<>()));

  private ItemChargeTransfers() {}

  public static double move(ItemStack source, ItemStack target, double maximum, int tier) {
    if (source == null
        || target == null
        || source == target
        || source.isEmpty()
        || target.isEmpty()
        || Double.isNaN(maximum)
        || maximum <= 0) return 0;
    var active = ACTIVE.get();
    if (active.contains(source) || active.contains(target)) return 0;
    active.add(source);
    active.add(target);
    try {
      double offered =
          ElectricItem.manager.discharge(source, maximum, Integer.MAX_VALUE, true, true, true);
      if (!(offered > 0)) return 0;
      double accepted =
          ElectricItem.manager.charge(target, Math.min(maximum, offered), tier, true, true);
      if (!(accepted > 0)) return 0;
      double paid =
          ElectricItem.manager.discharge(source, accepted, Integer.MAX_VALUE, true, true, false);
      if (!(paid > 0)) return 0;
      double injected = ElectricItem.manager.charge(target, paid, tier, true, false);
      if (paid > injected)
        ElectricItem.manager.charge(source, paid - injected, Integer.MAX_VALUE, true, false);
      return injected;
    } finally {
      active.remove(source);
      active.remove(target);
    }
  }
}
