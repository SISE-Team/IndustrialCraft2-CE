package ic2ce.api.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Legacy public output shape; construction owns the list, stacks and metadata. */
@Deprecated
public final class RecipeOutput {
  public final List<ItemStack> items;
  public final CompoundTag metadata;

  public RecipeOutput(CompoundTag metadata, List<ItemStack> items) {
    this.metadata = metadata == null ? null : metadata.copy();
    this.items =
        Objects.requireNonNull(items).stream()
            .map(stack -> Objects.requireNonNull(stack).copy())
            .toList();
  }

  public RecipeOutput(CompoundTag metadata, ItemStack... items) {
    this(metadata, Arrays.asList(items));
  }

  @Override
  public boolean equals(Object value) {
    if (this == value) return true;
    if (!(value instanceof RecipeOutput other)
        || !Objects.equals(metadata, other.metadata)
        || items.size() != other.items.size()) return false;
    for (int index = 0; index < items.size(); index++)
      if (!ItemStack.matches(items.get(index), other.items.get(index))) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int hash = Objects.hashCode(metadata);
    for (var stack : items)
      hash = 31 * hash + 31 * ItemStack.hashItemAndComponents(stack) + stack.getCount();
    return hash;
  }

  @Override
  public String toString() {
    return "RecipeOutput[" + items + ", " + metadata + "]";
  }
}
