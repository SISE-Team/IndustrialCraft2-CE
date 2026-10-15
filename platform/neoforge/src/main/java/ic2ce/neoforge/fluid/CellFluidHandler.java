package ic2ce.neoforge.fluid;

import ic2ce.api.fluid.Ic2FluidItem;
import ic2ce.api.fluid.Ic2FluidStack;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

/**
 * One native owned cell admits only bounded component-sensitive transfers inside nested
 * transactions.
 */
public final class CellFluidHandler implements ResourceHandler<FluidResource> {
  private ItemStack stack;
  private final @Nullable ItemAccess access;
  private final SnapshotJournal<ItemStack> journal =
      new SnapshotJournal<>() {
        @Override
        protected ItemStack createSnapshot() {
          return stack.copy();
        }

        @Override
        protected void revertToSnapshot(ItemStack previous) {
          if (access == null) copyComponents(stack, previous);
          else stack = previous.copy();
        }
      };

  public CellFluidHandler(ItemStack stack, @Nullable ItemAccess access) {
    this.stack = Objects.requireNonNull(stack);
    this.access = access;
  }

  private @Nullable Ic2FluidItem item() {
    return stack.getCount() == 1 && stack.getItem() instanceof Ic2FluidItem cell ? cell : null;
  }

  private @Nullable Ic2FluidStack content() {
    var cell = item();
    return cell == null ? null : cell.getFluidStack(stack);
  }

  private static FluidResource resource(@Nullable Ic2FluidStack content) {
    return content == null || content.isEmpty()
        ? FluidResource.EMPTY
        : FluidResource.of(EnvFluidHandlerNeoForge.getForgeFs(content));
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public FluidResource getResource(int index) {
    return index == 0 ? resource(content()) : FluidResource.EMPTY;
  }

  @Override
  public long getAmountAsLong(int index) {
    var contents = index == 0 ? content() : null;
    return contents == null || contents.isEmpty() ? 0 : Math.max(0, contents.getAmountMb());
  }

  @Override
  public long getCapacityAsLong(int index, FluidResource resource) {
    var cell = item();
    return index == 0 && cell != null ? Math.max(0, cell.getCapacityMb(stack)) : 0;
  }

  @Override
  public boolean isValid(int index, FluidResource offered) {
    var cell = item();
    if (index != 0 || cell == null || offered.isEmpty()) return false;
    return cell.fillMb(stack.copy(), new Ic2FluidStackImpl(offered.toStack(1)), true, null) > 0;
  }

  @Override
  public int insert(int index, FluidResource offered, int maximum, TransactionContext transaction) {
    var cell = item();
    if (index != 0 || cell == null || offered.isEmpty() || maximum <= 0) return 0;
    var working = stack.copy();
    var request = new Ic2FluidStackImpl(offered.toStack(maximum));
    int predicted = cell.fillMb(working, request, true, null);
    if (predicted <= 0 || predicted > maximum) return 0;
    try (var nested = Transaction.open(transaction)) {
      journal.updateSnapshots(nested);
      var container = new MutableObject<>(working);
      int inserted =
          cell.fillMb(working, new Ic2FluidStackImpl(offered.toStack(predicted)), false, container);
      if (inserted <= 0 || inserted > predicted || !store(container.getValue(), nested)) return 0;
      nested.commit();
      return inserted;
    }
  }

  @Override
  public int extract(
      int index, FluidResource requested, int maximum, TransactionContext transaction) {
    var cell = item();
    if (index != 0 || cell == null || requested.isEmpty() || maximum <= 0) return 0;
    var working = stack.copy();
    var predicted = cell.drainMb(working, maximum, true, null);
    if (predicted == null
        || predicted.isEmpty()
        || predicted.getAmountMb() > maximum
        || !resource(predicted).equals(requested)) return 0;
    try (var nested = Transaction.open(transaction)) {
      journal.updateSnapshots(nested);
      var container = new MutableObject<>(working);
      var drained = cell.drainMb(working, predicted.getAmountMb(), false, container);
      if (drained == null
          || drained.isEmpty()
          || drained.getAmountMb() > predicted.getAmountMb()
          || !resource(drained).equals(requested)
          || !store(container.getValue(), nested)) return 0;
      nested.commit();
      return drained.getAmountMb();
    }
  }

  private boolean store(@Nullable ItemStack replacement, TransactionContext transaction) {
    if (replacement == null || replacement.isEmpty() || replacement.getCount() != 1) return false;
    if (access != null) {
      if (access.exchange(ItemResource.of(replacement), 1, transaction) != 1) return false;
      stack = replacement.copy();
      return true;
    }
    if (!stack.is(replacement.getItem())) return false;
    copyComponents(stack, replacement);
    return true;
  }

  private static void copyComponents(ItemStack target, ItemStack source) {
    target.setCount(source.getCount());
    for (var type : Set.copyOf(target.getComponents().keySet()))
      if (!source.has(type)) target.remove(type);
    target.applyComponents(source.getComponents());
  }
}
