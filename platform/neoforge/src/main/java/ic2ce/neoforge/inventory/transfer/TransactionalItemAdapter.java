package ic2ce.neoforge.inventory.transfer;

import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.storage.box.TileEntityBronzeStorageBox;
import ic2ce.neoforge.storage.box.TileEntityIridiumStorageBox;
import ic2ce.neoforge.storage.box.TileEntityIronStorageBox;
import ic2ce.neoforge.storage.box.TileEntitySteelStorageBox;
import ic2ce.neoforge.storage.box.TileEntityWoodenStorageBox;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Native component-aware transactions with per-slot journals only for exact independent storage
 * types.
 */
public final class TransactionalItemAdapter implements ResourceHandler<ItemResource> {
  private static final Set<Class<?>> INDEPENDENT =
      Set.of(
          TileEntityWoodenStorageBox.class,
          TileEntityIronStorageBox.class,
          TileEntityBronzeStorageBox.class,
          TileEntitySteelStorageBox.class,
          TileEntityIridiumStorageBox.class);
  private final Container container;
  private final Direction side;
  private SlotJournal[] slots;
  private final RootCommitJournal notifications;
  private final SnapshotJournal<ItemStack[]> coupled =
      new SnapshotJournal<>() {
        @Override
        protected ItemStack[] createSnapshot() {
          var snapshot = new ItemStack[size()];
          for (int index = 0; index < snapshot.length; index++)
            snapshot[index] = container.getItem(index).copy();
          return snapshot;
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
          for (int index = 0; index < Math.min(snapshot.length, size()); index++)
            container.setItem(index, snapshot[index].copy(), true);
        }
      };

  public TransactionalItemAdapter(Container container, Direction side) {
    this.container = Objects.requireNonNull(container);
    this.side = side;
    if (INDEPENDENT.contains(container.getClass()))
      slots = new SlotJournal[container.getContainerSize()];
    notifications = new RootCommitJournal(container::setChanged);
  }

  private final class SlotJournal extends SnapshotJournal<ItemStack> {
    private final int index;

    SlotJournal(int index) {
      this.index = index;
    }

    @Override
    protected ItemStack createSnapshot() {
      return container.getItem(index).copy();
    }

    @Override
    protected void revertToSnapshot(ItemStack snapshot) {
      container.setItem(index, snapshot.copy(), true);
    }
  }

  private void capture(int index, TransactionContext transaction) {
    Objects.requireNonNull(transaction, "Item changes require a native transaction");
    if (slots == null) coupled.updateSnapshots(transaction);
    else {
      if (slots.length != size()) slots = java.util.Arrays.copyOf(slots, size());
      var journal = slots[index];
      if (journal == null) slots[index] = journal = new SlotJournal(index);
      journal.updateSnapshots(transaction);
    }
    notifications.updateSnapshots(transaction);
  }

  private void index(int index) {
    Objects.checkIndex(index, size());
  }

  private boolean exposed(int index) {
    if (!(container instanceof WorldlyContainer worldly) || side == null) return true;
    for (int slot : worldly.getSlotsForFace(side)) if (slot == index) return true;
    return false;
  }

  private boolean insertable(int index, ItemStack stack) {
    return exposed(index)
        && container.canPlaceItem(index, stack)
        && (!(container instanceof WorldlyContainer worldly)
            || side == null
            || worldly.canPlaceItemThroughFace(index, stack, side));
  }

  private boolean extractable(int index, ItemStack stack) {
    return exposed(index)
        && (!(container instanceof WorldlyContainer worldly)
            || side == null
            || worldly.canTakeItemThroughFace(index, stack, side));
  }

  @Override
  public int size() {
    return container.getContainerSize();
  }

  @Override
  public ItemResource getResource(int index) {
    index(index);
    return ItemResource.of(container.getItem(index));
  }

  @Override
  public long getAmountAsLong(int index) {
    index(index);
    return Math.max(0, container.getItem(index).getCount());
  }

  @Override
  public long getCapacityAsLong(int index, ItemResource resource) {
    index(index);
    int bound =
        container instanceof InventoryBlockEntity machine
            ? machine.getSlotLimit(index)
            : container.getMaxStackSize(resource.toStack());
    return Math.max(0, Math.min(bound, resource.getMaxStackSize()));
  }

  @Override
  public boolean isValid(int index, ItemResource resource) {
    index(index);
    return !resource.isEmpty() && insertable(index, resource.toStack());
  }

  @Override
  public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
    index(index);
    if (amount <= 0 || resource.isEmpty()) return 0;
    var before = container.getItem(index);
    if (!before.isEmpty() && !ItemResource.of(before).equals(resource)
        || !insertable(index, resource.toStack())) return 0;
    int accepted =
        (int) Math.min(amount, Math.max(0, getCapacityAsLong(index, resource) - before.getCount()));
    if (accepted <= 0) return 0;
    capture(index, transaction);
    if (slots != null && !before.isEmpty()) before.grow(accepted);
    else container.setItem(index, resource.toStack(before.getCount() + accepted), true);
    container.onTransfer(index, accepted, transaction);
    return accepted;
  }

  @Override
  public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
    index(index);
    if (amount <= 0 || resource.isEmpty()) return 0;
    var before = container.getItem(index);
    if (before.isEmpty()
        || !ItemResource.of(before).equals(resource)
        || !extractable(index, before)) return 0;
    int accepted = Math.min(amount, before.getCount());
    capture(index, transaction);
    if (slots != null && accepted < before.getCount()) before.shrink(accepted);
    else
      container.setItem(
          index,
          accepted == before.getCount()
              ? ItemStack.EMPTY
              : before.copyWithCount(before.getCount() - accepted),
          true);
    container.onTransfer(index, -accepted, transaction);
    return accepted;
  }
}
