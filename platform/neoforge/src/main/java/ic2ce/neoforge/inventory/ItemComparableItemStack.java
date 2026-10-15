package ic2ce.neoforge.inventory;

import ic2ce.core.util.StackTag;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Immutable legacy UU/item key: registry identity and normalized custom data. */
public final class ItemComparableItemStack {
  private final Item item;
  private final CompoundTag data;
  private final int hash;

  public ItemComparableItemStack(ItemStack stack, boolean copyNbt) {
    Objects.requireNonNull(stack);
    item = stack.getItem();
    CompoundTag tag = StackTag.get(stack); // The bridge already returns an owned copy.
    if (tag != null) for (String ignored : StackUtil.ignoredNbtKeys) tag.remove(ignored);
    data = tag == null || tag.isEmpty() ? null : tag;
    hash =
        data == null
            ? System.identityHashCode(item)
            : 31 * System.identityHashCode(item) + data.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof ItemComparableItemStack key
        && item == key.item
        && Objects.equals(data, key.data);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  public ItemComparableItemStack copy() {
    return this;
  }

  public ItemStack toStack() {
    return toStack(1);
  }

  public ItemStack toStack(int count) {
    if (item == null) return null;
    var result = new ItemStack(item, count);
    if (data != null) StackTag.set(result, data.copy());
    return result;
  }
}
