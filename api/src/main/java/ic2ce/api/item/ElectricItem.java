package ic2ce.api.item;

import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/** Public electrical services and ordered immutable addon fallback generations. */
public final class ElectricItem {
  public static IElectricItemManager manager;
  public static IElectricItemManager rawManager;
  private static volatile List<IBackupElectricItemManager> backups = List.of();

  private ElectricItem() {}

  public static synchronized void registerBackupManager(IBackupElectricItemManager candidate) {
    Objects.requireNonNull(candidate);
    for (var existing : backups) if (existing == candidate) return;
    var next = new java.util.ArrayList<>(backups);
    next.add(candidate);
    backups = List.copyOf(next);
  }

  public static IBackupElectricItemManager getBackupManager(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return null;
    for (var candidate : backups) if (candidate.handles(stack)) return candidate;
    return null;
  }
}
