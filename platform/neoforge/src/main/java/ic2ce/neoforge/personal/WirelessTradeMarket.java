package ic2ce.neoforge.personal;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** World-owned registrations with idempotent changes and stable watcher snapshots. */
public class WirelessTradeMarket {
  private final Level world;
  private final Set<BlockPos> traders = new HashSet<>();
  private final Set<MarketWatcher> watchers = new LinkedHashSet<>();

  public WirelessTradeMarket(Level world) {
    this.world = Objects.requireNonNull(world);
  }

  private void ownerThread() {
    if (world.isClientSide() || world.getServer() != null && !world.getServer().isSameThread())
      throw new IllegalStateException("Market mutation requires the native server thread");
  }

  public void registerTradeOMat(ItemTradeTerminal trader) {
    ownerThread();
    if (trader.getLevel() != world || trader.isRemoved())
      throw new IllegalArgumentException("Foreign or removed market trader");
    var position = trader.getBlockPos().immutable();
    if (!traders.add(position)) return;
    for (var watcher : watchers.toArray(MarketWatcher[]::new)) watcher.onAdd(position);
  }

  public void unregisterTradeOMat(ItemTradeTerminal trader) {
    ownerThread();
    var position = trader.getBlockPos();
    if (!traders.remove(position)) return;
    for (var watcher : watchers.toArray(MarketWatcher[]::new)) watcher.onRemove(position);
  }

  public void registerWatcher(MarketWatcher watcher) {
    ownerThread();
    watchers.add(Objects.requireNonNull(watcher));
  }

  public void unregisterWatcher(MarketWatcher watcher) {
    ownerThread();
    watchers.remove(watcher);
  }

  public Stream<BlockPos> tradersAround(BlockPos origin, int radius) {
    ownerThread();
    Objects.requireNonNull(origin);
    if (radius < 0) throw new IllegalArgumentException("Negative market radius");
    long squared = (long) radius * radius;
    var center = origin.immutable();
    return traders.stream()
        .filter(position -> center.distSqr(position) <= squared)
        .toList()
        .stream();
  }

  public interface MarketWatcher {
    void onAdd(BlockPos position);

    void onRemove(BlockPos position);
  }
}
