package ic2.forge;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ic2.core.block.storage.box.*;

/**
 * Exposes a vanilla {@link Container} (IC2 machines implement Container and
 * WorldlyContainer) as a {@link ResourceHandler} for the 26.1.2 item
 * capability, honouring the sided restrictions of WorldlyContainer.
 * Replaces the old IItemHandler capability wrappers.
 */
final class ContainerItemHandler implements ResourceHandler<ItemResource> {
   private static final Set<Class<?>> independentStorageTypes = Set.of(TileEntityWoodenStorageBox.class,
      TileEntityIronStorageBox.class, TileEntityBronzeStorageBox.class, TileEntitySteelStorageBox.class, TileEntityIridiumStorageBox.class);
   private final Container container;
   private final SlotJournal[] slotJournals;
   @Nullable
   private final Direction side;

   private final SnapshotJournal<List<ItemStack>> journal = new SnapshotJournal<>() {
      @Override
      protected List<ItemStack> createSnapshot() {
         List<ItemStack> snapshot = new ArrayList<>(ContainerItemHandler.this.container.getContainerSize());
         for (int i = 0; i < ContainerItemHandler.this.container.getContainerSize(); i++) {
            snapshot.add(ContainerItemHandler.this.container.getItem(i).copy());
         }
         return snapshot;
      }

      @Override
      protected void revertToSnapshot(List<ItemStack> snapshot) {
         for (int i = 0; i < Math.min(snapshot.size(), ContainerItemHandler.this.container.getContainerSize()); i++) {
            ContainerItemHandler.this.container.setItem(i, snapshot.get(i));
         }
      }
   };

   ContainerItemHandler(Container container, @Nullable Direction side) {
      this.container = container;
      this.side = side;
      this.slotJournals = independentStorageTypes.contains(container.getClass()) ? new SlotJournal[container.getContainerSize()] : null;
   }

   private void updateSnapshot(int index, TransactionContext transaction) {
      if (this.slotJournals == null) {
         // Unknown/custom containers may have cross-slot side effects.
         this.journal.updateSnapshots(transaction);
      } else {
         SlotJournal slot = this.slotJournals[index];
         if (slot == null) this.slotJournals[index] = slot = new SlotJournal(index);
         slot.updateSnapshots(transaction);
      }
   }

   private final class SlotJournal extends SnapshotJournal<ItemStack> {
      private final int index;

      SlotJournal(int index) { this.index = index; }

      @Override
      protected ItemStack createSnapshot() { return container.getItem(this.index).copy(); }

      @Override
      protected void revertToSnapshot(ItemStack snapshot) { container.setItem(this.index, snapshot); }
   }

   private boolean accessible(int slot) {
      if (!(this.container instanceof WorldlyContainer sided) || this.side == null) return true;
      for (int exposed : sided.getSlotsForFace(this.side)) if (exposed == slot) return true;
      return false;
   }

   private boolean canInsert(int slot, ItemStack stack) {
      return this.accessible(slot) && !(this.container instanceof WorldlyContainer sided && this.side != null
         && !sided.canPlaceItemThroughFace(slot, stack, this.side))
         && this.container.canPlaceItem(slot, stack);
   }

   private boolean canExtract(int slot, ItemStack stack) {
      return this.accessible(slot) && !(this.container instanceof WorldlyContainer sided && this.side != null
         && !sided.canTakeItemThroughFace(slot, stack, this.side));
   }

   @Override
   public int size() {
      return this.container.getContainerSize();
   }

   @Override
   public ItemResource getResource(int index) {
      return ItemResource.of(this.container.getItem(index));
   }

   @Override
   public long getAmountAsLong(int index) {
      return this.container.getItem(index).getCount();
   }

   @Override
   public long getCapacityAsLong(int index, ItemResource resource) {
      int limit = this.container instanceof ic2.core.block.tileentity.TileEntityInventory inventory
         ? inventory.getSlotLimit(index) : this.container.getMaxStackSize();
      return Math.min(limit, resource.getMaxStackSize());
   }

   @Override
   public boolean isValid(int index, ItemResource resource) {
      ItemStack stack = resource.toStack();
      return !resource.isEmpty() && this.container.canPlaceItem(index, stack) && this.canInsert(index, stack);
   }

   @Override
   public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
      if (amount <= 0 || resource.isEmpty()) {
         return 0;
      }
      ItemStack current = this.container.getItem(index);
      int max = (int)this.getCapacityAsLong(index, resource);
      if (!current.isEmpty()) {
         if (!ItemResource.of(current).equals(resource)) {
            return 0;
         }
         max = Math.min(max, current.getMaxStackSize());
      }
      int space = max - current.getCount();
      int toInsert = Math.min(amount, space);
      if (toInsert <= 0 || !this.canInsert(index, resource.toStack())) {
         return 0;
      }
      this.updateSnapshot(index, transaction);
      if (current.isEmpty()) {
         ItemStack inserted = resource.toStack();
         inserted.setCount(toInsert);
         this.container.setItem(index, inserted);
      } else {
         current.setCount(current.getCount() + toInsert);
         this.container.setChanged();
      }
      return toInsert;
   }

   @Override
   public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
      if (amount <= 0 || resource.isEmpty()) {
         return 0;
      }
      ItemStack current = this.container.getItem(index);
      if (current.isEmpty() || !ItemResource.of(current).equals(resource)) {
         return 0;
      }
      int toExtract = Math.min(amount, current.getCount());
      if (toExtract <= 0 || !this.canExtract(index, current)) {
         return 0;
      }
      this.updateSnapshot(index, transaction);
      current.setCount(current.getCount() - toExtract);
      if (current.getCount() <= 0) {
         this.container.setItem(index, ItemStack.EMPTY);
      } else {
         this.container.setChanged();
      }
      return toExtract;
   }
}
