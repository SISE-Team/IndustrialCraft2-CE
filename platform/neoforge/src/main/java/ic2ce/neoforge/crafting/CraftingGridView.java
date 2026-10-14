package ic2ce.neoforge.crafting;


import ic2ce.neoforge.inventory.InventorySlot;
import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

/** Checked rectangular native grid with a stable live list and explicit quantity writes. */
public abstract class CraftingGridView implements CraftingContainer {
  private final int width, height, size;
  private final List<ItemStack> view =
      new AbstractList<>() {
        @Override
        public ItemStack get(int index) {
          Objects.checkIndex(index, size);
          return getItem(index);
        }

        @Override
        public int size() {
          return size;
        }
      };

  protected CraftingGridView(int width, int height) {
    if (width <= 0 || height <= 0)
      throw new IllegalArgumentException("Invalid crafting dimensions");
    this.width = width;
    this.height = height;
    size = Math.multiplyExact(width, height);
  }

  protected abstract ItemStack get(int index);

  protected abstract void set(int index, ItemStack stack);

  private boolean index(int index) {
    return index >= 0 && index < size;
  }

  @Override
  public int getContainerSize() {
    return size;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public List<ItemStack> getItems() {
    return view;
  }

  @Override
  public ItemStack getItem(int index) {
    if (!index(index)) return ItemStack.EMPTY;
    var stack = get(index);
    return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
  }

  @Override
  public void setItem(int index, ItemStack stack) {
    if (index(index)) {
      set(index, stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack);
      setChanged();
    }
  }

  @Override
  public ItemStack removeItem(int index, int requested) {
    var stored = getItem(index);
    if (requested <= 0 || stored.isEmpty()) return ItemStack.EMPTY;
    int count = Math.min(requested, stored.getCount());
    var result = stored.copyWithCount(count);
    setItem(index, stored.copyWithCount(stored.getCount() - count));
    return result;
  }

  @Override
  public ItemStack removeItemNoUpdate(int index) {
    var current = getItem(index);
    if (!current.isEmpty()) setItem(index, ItemStack.EMPTY);
    return current;
  }

  @Override
  public boolean isEmpty() {
    for (int index = 0; index < size; index++) if (!getItem(index).isEmpty()) return false;
    return true;
  }

  @Override
  public void clearContent() {
    for (int index = 0; index < size; index++) set(index, ItemStack.EMPTY);
    setChanged();
  }

  @Override
  public void setChanged() {}

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public void fillStackedContents(net.minecraft.world.entity.player.StackedItemContents contents) {
    for (int index = 0; index < size; index++) contents.accountStack(getItem(index), 1);
  }

  public static class ArrayGrid extends CraftingGridView {
    private final ItemStack[] contents;

    public ArrayGrid(ItemStack[] contents, int width) {
      super(width, rows(contents.length, width));
      this.contents = contents;
    }

    private static int rows(int length, int width) {
      if (width <= 0 || length <= 0 || length % width != 0)
        throw new IllegalArgumentException("Nonrectangular crafting array");
      return length / width;
    }

    @Override
    protected ItemStack get(int index) {
      return contents[index];
    }

    @Override
    protected void set(int index, ItemStack value) {
      contents[index] = value;
    }
  }

  public static class InventoryGrid extends CraftingGridView {
    private final InventorySlot slots;

    public InventoryGrid(InventorySlot slots, int width) {
      super(width, ArrayGrid.rows(slots.size(), width));
      this.slots = slots;
    }

    @Override
    protected ItemStack get(int index) {
      return slots.get(index);
    }

    @Override
    protected void set(int index, ItemStack value) {
      slots.put(index, value);
    }
  }
}
