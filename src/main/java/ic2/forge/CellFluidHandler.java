package ic2.forge;

import ic2.core.fluid.Ic2FluidItem;
import ic2.core.fluid.Ic2FluidStack;
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
import org.jetbrains.annotations.Nullable;

/** Fluid cell operations preserve item identity, requested quantities and transaction rollback. */
final class CellFluidHandler implements ResourceHandler<FluidResource> {
   private ItemStack stack;
   @Nullable private final ItemAccess access;
   private final SnapshotJournal<ItemStack> journal = new SnapshotJournal<>() {
      @Override protected ItemStack createSnapshot() { return stack.copy(); }
      @Override protected void revertToSnapshot(ItemStack snapshot) {
         if (access == null) restoreStack(stack, snapshot);
         else stack = snapshot.copy();
      }
   };

   CellFluidHandler(ItemStack stack, @Nullable ItemAccess access) {
      this.stack = stack;
      this.access = access;
   }

   @Nullable private Ic2FluidItem parent() {
      return this.stack.getItem() instanceof Ic2FluidItem item ? item : null;
   }

   private static void restoreStack(ItemStack target, ItemStack snapshot) {
      target.setCount(snapshot.getCount());
      for (var type : Set.copyOf(target.getComponents().keySet())) {
         if (!snapshot.has(type)) target.remove(type);
      }
      target.applyComponents(snapshot.getComponents());
   }

   private boolean store(ItemStack result, TransactionContext transaction) {
      if (result.isEmpty()) return false;
      if (this.access != null) {
         if (this.access.exchange(ItemResource.of(result), 1, transaction) != 1) return false;
         this.stack = result;
      } else {
         // An ItemAccess is required when the container changes its item type.
         if (this.stack.getItem() != result.getItem()) return false;
         restoreStack(this.stack, result);
      }
      return true;
   }

   @Override public int size() { return 1; }

   @Override public FluidResource getResource(int index) {
      var parent = this.parent();
      Ic2FluidStack content = parent == null || index != 0 ? null : parent.getFluidStack(this.stack);
      return content == null || content.isEmpty() ? FluidResource.EMPTY : FluidResource.of(EnvFluidHandlerForge.getForgeFs(content));
   }

   @Override public long getAmountAsLong(int index) {
      var parent = this.parent();
      Ic2FluidStack content = parent == null || index != 0 ? null : parent.getFluidStack(this.stack);
      return content == null || content.isEmpty() ? 0 : content.getAmountMb();
   }

   @Override public long getCapacityAsLong(int index, FluidResource resource) {
      var parent = this.parent();
      return parent == null || index != 0 ? 0 : parent.getCapacityMb(this.stack);
   }

   @Override public boolean isValid(int index, FluidResource resource) {
      return index == 0 && !resource.isEmpty() && this.parent() != null && this.parent().getCapacityMb(this.stack) > 0;
   }

   @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
      var parent = this.parent();
      if (index != 0 || resource.isEmpty() || amount <= 0 || this.stack.getCount() != 1 || parent == null) return 0;
      ItemStack working = this.stack.copy();
      int simulated = parent.fillMb(working, toIc2(resource, amount), true, null);
      if (simulated <= 0) return 0;
      try (var nested = Transaction.open(transaction)) {
         this.journal.updateSnapshots(nested);
         var container = new MutableObject<>(working);
         int inserted = parent.fillMb(working, toIc2(resource, Math.min(amount, simulated)), false, container);
         if (inserted <= 0 || !this.store(container.getValue(), nested)) return 0;
         nested.commit();
         return inserted;
      }
   }

   @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
      var parent = this.parent();
      if (index != 0 || resource.isEmpty() || amount <= 0 || this.stack.getCount() != 1 || parent == null) return 0;
      ItemStack working = this.stack.copy();
      Ic2FluidStack simulated = parent.drainMb(working, amount, true, null);
      if (simulated == null || simulated.isEmpty() || !FluidResource.of(EnvFluidHandlerForge.getForgeFs(simulated)).equals(resource)) return 0;
      try (var nested = Transaction.open(transaction)) {
         this.journal.updateSnapshots(nested);
         var container = new MutableObject<>(working);
         Ic2FluidStack drained = parent.drainMb(working, Math.min(amount, simulated.getAmountMb()), false, container);
         if (drained == null || drained.isEmpty() || !this.store(container.getValue(), nested)) return 0;
         nested.commit();
         return drained.getAmountMb();
      }
   }

   private static Ic2FluidStack toIc2(FluidResource resource, int amount) {
      return new Ic2FluidStackImpl(resource.toStack(amount));
   }
}
