package ic2.forge;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Uses the actual production adapter and native transaction implementation. */
public final class PerformanceInventoryProbe {
    private final Container inventory;
    private final ResourceHandler<ItemResource> handler;
    private final ItemResource iron = ItemResource.of(new ItemStack(Items.IRON_INGOT));
    private long operations;

    public PerformanceInventoryProbe(Container inventory) {
        this.inventory = inventory;
        this.handler = new ContainerItemHandler(inventory, null);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            inventory.setItem(i, new ItemStack(Items.IRON_INGOT, 32));
        }
    }

    /** One complete committed transaction, changing only one real occupied slot. */
    public long cycle(int repetitions) {
        long accepted = 0;
        for (int i = 0; i < repetitions; i++) {
            int slot = i % inventory.getContainerSize();
            try (var root = Transaction.openRoot()) {
                accepted += handler.extract(slot, iron, 1, root);
                accepted += handler.insert(slot, iron, 1, root);
                root.commit();
            }
        }
        operations += repetitions;
        return accepted;
    }

    public List<String> verifyNestedTransactions() {
        var failures = new ArrayList<String>();
        for (boolean rootCommit : List.of(false, true)) {
            for (boolean nestedCommit : List.of(false, true)) {
                inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 32));
                inventory.setItem(1, new ItemStack(Items.IRON_INGOT, 32));
                try (var root = Transaction.openRoot()) {
                    handler.extract(0, iron, 3, root);
                    try (var nested = Transaction.open(root)) {
                        handler.extract(0, iron, 5, nested);
                        handler.extract(1, iron, 7, nested);
                        if (nestedCommit) nested.commit();
                    }
                    if (rootCommit) root.commit();
                }
                int first = rootCommit ? (nestedCommit ? 24 : 29) : 32;
                int second = rootCommit && nestedCommit ? 25 : 32;
                if (inventory.getItem(0).getCount() != first || inventory.getItem(1).getCount() != second) {
                    failures.add("nested rollback root=" + rootCommit + " nested=" + nestedCommit);
                }
            }
        }
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 32));
        inventory.setItem(1, new ItemStack(Items.IRON_INGOT, 32));
        return failures;
    }

    public boolean conserved() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).is(Items.IRON_INGOT) || inventory.getItem(i).getCount() != 32) return false;
        }
        return true;
    }

    public int slots() { return inventory.getContainerSize(); }
    public long operations() { return operations; }

    public static List<String> verifyCustomContainerFallback() {
        class CoupledContainer extends SimpleContainer {
            CoupledContainer() { super(2); }
            @Override public void setItem(int slot, ItemStack stack) {
                super.setItem(slot, stack);
                if (slot == 0) super.setItem(1, new ItemStack(Items.COAL));
            }
        }
        var container = new CoupledContainer();
        container.setItem(0, new ItemStack(Items.IRON_INGOT, 32));
        container.setItem(1, new ItemStack(Items.DIAMOND, 13));
        var adapter = new ContainerItemHandler(container, null);
        try (var root = Transaction.openRoot()) {
            adapter.extract(0, ItemResource.of(new ItemStack(Items.IRON_INGOT)), 32, root);
        }
        return container.getItem(0).is(Items.IRON_INGOT) && container.getItem(0).getCount() == 32
            && container.getItem(1).is(Items.DIAMOND) && container.getItem(1).getCount() == 13
            ? List.of() : List.of("custom container cross-slot rollback");
    }
}
