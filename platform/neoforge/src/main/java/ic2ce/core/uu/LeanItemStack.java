package ic2ce.core.uu;

import ic2ce.core.util.StackTag;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Owned immutable material identity; quantity is separate from recipe graph equality. */
final class LeanItemStack {
  private final Item item;
  private final CompoundTag data;
  private final int size;
  private final int hash;

  public LeanItemStack(Item item) {
    this(item, 1);
  }

  public LeanItemStack(Item item, int size) {
    this(item, null, size);
  }

  public LeanItemStack(ItemStack stack) {
    this(stack, stack.getCount());
  }

  public LeanItemStack(ItemStack stack, int size) {
    this(stack.getItem(), StackTag.get(stack), size);
  }

  public LeanItemStack(Item item, CompoundTag tag, int size) {
    this.item = Objects.requireNonNull(item);
    this.size = size;
    data = tag == null || tag.isEmpty() ? null : tag.copy();
    hash = 31 * System.identityHashCode(item) + Objects.hashCode(data);
  }

  public Item getItem() {
    return item;
  }

  public CompoundTag getNbt() {
    return data == null ? null : data.copy();
  }

  public int getSize() {
    return size;
  }

  public boolean hasSameItem(LeanItemStack other) {
    return other != null && item == other.item && Objects.equals(data, other.data);
  }

  public LeanItemStack copy() {
    return this;
  }

  public LeanItemStack copyWithSize(int quantity) {
    return quantity == size ? this : new LeanItemStack(item, data, quantity);
  }

  public ItemStack toMcStack() {
    if (size <= 0) return ItemStack.EMPTY;
    var result = new ItemStack(item, size);
    if (data != null) StackTag.set(result, data.copy());
    return result;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof LeanItemStack material && hasSameItem(material);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    return size + "x" + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
  }
}
