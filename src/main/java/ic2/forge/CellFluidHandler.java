package ic2.forge;

import ic2.core.fluid.Ic2FluidItem;
import ic2.core.fluid.Ic2FluidStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes an {@link ic2.core.fluid.Ic2FluidItem} (fluid cell, tin can, ...) as a
 * {@link net.neoforged.neoforge.transfer.ResourceHandler} for the 26.1.2 capability
 * system, replacing the removed IFluidHandlerItem capability provider.
 *
 * <p>Cell contents live in the stack's custom data; transactional rollback is
 * implemented by snapshotting the full component map of the stack.</p>
 */
final class CellFluidHandler implements net.neoforged.neoforge.transfer.ResourceHandler<FluidResource> {
   private final Ic2FluidItem parent;
   private ItemStack stack;
   @Nullable
   private final ItemAccess access;

   private final SnapshotJournal<ItemStack> journal = new SnapshotJournal<>() {
      @Override
      protected ItemStack createSnapshot() {
         return CellFluidHandler.this.stack.copy();
      }

      @Override
      protected void revertToSnapshot(ItemStack snapshot) {
         CellFluidHandler.this.applySnapshot(snapshot);
      }
   };

   CellFluidHandler(ItemStack stack, @Nullable ItemAccess access) {
      this.parent = (Ic2FluidItem)stack.getItem();
      this.stack = stack;
      this.access = access;
   }

   private void applySnapshot(ItemStack snapshot) {
      if (this.access != null && !this.access.getResource().equals(ItemResource.of(snapshot))) {
         return; // container item was swapped out; nothing to restore on this stack
      }
      this.stack.setCount(snapshot.getCount());
      this.stack.applyComponents(snapshot.getComponents());
   }

   private Mutable<ItemStack> containerSink(TransactionContext transaction) {
      return new Mutable<>() {
         @Override
         public ItemStack getValue() {
            return CellFluidHandler.this.stack;
         }

         @Override
         public void setValue(ItemStack value) {
            if (value == CellFluidHandler.this.stack || ItemStack.isSameItemSameComponents(value, CellFluidHandler.this.stack)) {
               return;
            }
            // the cell turned into a different item (e.g. filled -> empty cell): swap via the access
            if (CellFluidHandler.this.access != null) {
               CellFluidHandler.this.access.exchange(ItemResource.of(value), 1, transaction);
            }
         }
      };
   }

   @Override
   public int size() {
      return 1;
   }

   @Override
   public FluidResource getResource(int index) {
      Ic2FluidStack content = this.parent.getFluidStack(this.stack);
      return content == null || content.isEmpty()
         ? FluidResource.EMPTY
         : FluidResource.of(EnvFluidHandlerForge.getForgeFs(content));
   }

   @Override
   public long getAmountAsLong(int index) {
      Ic2FluidStack content = this.parent.getFluidStack(this.stack);
      return content == null || content.isEmpty() ? 0 : content.getAmountMb();
   }

   @Override
   public long getCapacityAsLong(int index, FluidResource resource) {
      return this.parent.getCapacityMb(this.stack);
   }

   @Override
   public boolean isValid(int index, FluidResource resource) {
      return index == 0;
   }

   @Override
   public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
      if (amount <= 0 || this.stack.getCount() != 1) {
         return 0;
      }
      int simulated = this.parent.fillMb(this.stack, toIc2(resource), true, null);
      if (simulated <= 0) {
         return 0;
      }
      int toInsert = Math.min(amount, simulated);
      this.journal.updateSnapshots(transaction);
      return this.parent.fillMb(this.stack, toIc2(resource), false, this.containerSink(transaction));
   }

   @Override
   public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
      if (amount <= 0 || this.stack.getCount() != 1) {
         return 0;
      }
      Ic2FluidStack drainedSim = this.parent.drainMb(this.stack, amount, true, null);
      if (drainedSim == null || drainedSim.isEmpty()) {
         return 0;
      }
      if (!FluidResource.of(EnvFluidHandlerForge.getForgeFs(drainedSim)).equals(resource)) {
         drainedSim = this.parent.getFluidStack(this.stack);
         if (drainedSim == null || !FluidResource.of(EnvFluidHandlerForge.getForgeFs(drainedSim)).equals(resource)) {
            return 0;
         }
      }
      this.journal.updateSnapshots(transaction);
      Ic2FluidStack drained = this.parent.drainMb(this.stack, amount, false, this.containerSink(transaction));
      return drained == null ? 0 : drained.getAmountMb();
   }

   private static ic2.core.fluid.Ic2FluidStack toIc2(FluidResource resource) {
      return resource.isEmpty()
         ? ic2.core.fluid.Ic2FluidStack.EMPTY
         : new Ic2FluidStackImpl(resource.toStack(1));
   }
}
