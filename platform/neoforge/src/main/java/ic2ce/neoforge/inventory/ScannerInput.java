package ic2ce.neoforge.inventory;

import ic2ce.neoforge.network.IRpcProvider;
import ic2ce.neoforge.network.Rpc;
import ic2ce.neoforge.network.RpcHandler;
import ic2ce.neoforge.processing.uu.UuGraph;
import ic2ce.neoforge.processing.uu.UuIndex;
import ic2ce.neoforge.runtime.SideServices;
import java.util.LinkedHashMap;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative scanning; client slot hints never wait on their own tick thread. */
public class ScannerInput extends ConsumableInventory {
  private record Query(Rpc<Boolean> result, long submitted) {}

  private final LinkedHashMap<ItemComparableItemStack, Query> queries =
      new LinkedHashMap<>(16, 0.75F, true);

  public ScannerInput(IInventorySlotHolder<?> holder, String name, int count) {
    super(holder, name, count);
    setStackSizeLimit(1);
  }

  @Override
  public boolean accepts(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return false;
    if (SideServices.proxy().isSimulating()) return valid(stack);
    long now = System.nanoTime();
    var key = new ItemComparableItemStack(stack, true);
    Query query = queries.get(key);
    if (query == null || now - query.submitted() > TimeUnitNanos.CACHE) {
      if (queries.size() >= 64) {
        var first = queries.entrySet().iterator();
        var stale = first.next();
        first.remove();
        stale.getValue().result().cancel(false);
      }
      query = new Query(RpcHandler.run(ServerScannableCheck.class, stack.copyWithCount(1)), now);
      queries.put(key, query);
    }
    if (!query.result().isDone()) return false;
    try {
      return Boolean.TRUE.equals(query.result().completion().getNow(false));
    } catch (java.util.concurrent.CompletionException
        | java.util.concurrent.CancellationException error) {
      if (now - query.submitted() > TimeUnitNanos.RETRY) queries.remove(key);
      return false;
    }
  }

  private static boolean valid(ItemStack stack) {
    ItemStack normalized = UuGraph.find(stack);
    return normalized != null
        && !normalized.isEmpty()
        && Double.isFinite(UuIndex.instance.get(normalized));
  }

  static {
    RpcHandler.registerProvider(new ServerScannableCheck());
  }

  public static final class ServerScannableCheck implements IRpcProvider<Boolean> {
    @Override
    public Boolean executeRpc(Object... arguments) {
      return arguments.length == 1
          && arguments[0] instanceof ItemStack stack
          && !stack.isEmpty()
          && valid(stack);
    }
  }

  private static final class TimeUnitNanos {
    static final long RETRY = 1_000_000_000L, CACHE = 30_000_000_000L;
  }
}
