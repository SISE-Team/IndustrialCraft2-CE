package ic2ce.neoforge.inventory;

import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Stable named inventory group with explicit mutation and persistence boundaries. */
public class InventorySlot implements Iterable<ItemStack> {
  public final IInventorySlotHolder<?> base;
  public final String name;
  public final InvSide preferredSide;
  protected final Access access;
  private final ItemStack[] contents;
  private int stackSizeLimit = 64;

  public InventorySlot(IInventorySlotHolder<?> base, String name, Access access, int count) {
    this(base, name, access, count, InvSide.ANY);
  }

  public InventorySlot(
      IInventorySlotHolder<?> base, String name, Access access, int count, InvSide side) {
    if (count <= 0) throw new IllegalArgumentException("Invalid inventory group size");
    this.base = Objects.requireNonNull(base);
    this.name = Objects.requireNonNull(name);
    this.access = access == null ? Access.NONE : access;
    preferredSide = Objects.requireNonNull(side);
    contents = new ItemStack[count];
    Arrays.fill(contents, ItemStack.EMPTY);
    base.addInventorySlot(this);
  }

  public InventorySlot(int count) {
    if (count < 0) throw new IllegalArgumentException("Negative inventory group size");
    base = null;
    name = null;
    access = Access.NONE;
    preferredSide = InvSide.ANY;
    contents = new ItemStack[count];
    Arrays.fill(contents, ItemStack.EMPTY);
  }

  public int size() {
    return contents.length;
  }

  public ItemStack get() {
    return get(0);
  }

  public ItemStack get(int index) {
    return contents[index];
  }

  public boolean isEmpty(int index) {
    return get(index).isEmpty();
  }

  public boolean isEmpty() {
    for (ItemStack content : contents) if (!content.isEmpty()) return false;
    return true;
  }

  public void put(ItemStack value) {
    put(0, value);
  }

  public void put(int index, ItemStack value) {
    contents[index] = value == null || value.isEmpty() ? ItemStack.EMPTY : value;
    onChanged();
  }

  protected void putFromNBT(int index, ItemStack value) {
    contents[index] = value == null || value.isEmpty() ? ItemStack.EMPTY : value;
  }

  /** Transaction adapters defer change hooks until the native root transaction commits. */
  public void putInTransaction(int index, ItemStack value) {
    putFromNBT(index, value);
  }

  public void clear(int index) {
    put(index, ItemStack.EMPTY);
  }

  public void clear() {
    Arrays.fill(contents, ItemStack.EMPTY);
    onChanged();
  }

  public void onChanged() {}

  public boolean accepts(ItemStack value) {
    return true;
  }

  public boolean canInput() {
    return access.isInput();
  }

  public boolean canOutput() {
    return access.isOutput();
  }

  public int getStackSizeLimit() {
    return stackSizeLimit;
  }

  public void setStackSizeLimit(int limit) {
    if (limit < 1) throw new IllegalArgumentException("Invalid stack limit");
    stackSizeLimit = limit;
  }

  private HolderLookup.Provider lookup() {
    var lookup =
        base == null
            ? ProjectRuntime.envProxy.getRegistryLookup()
            : base.getParent().getSerializationLookup();
    return Objects.requireNonNull(lookup, "Inventory serialization requires a registry context");
  }

  public void readFromNbt(CompoundTag data) {
    Arrays.fill(contents, ItemStack.EMPTY);
    ListTag entries = data.getListOrEmpty("Contents");
    var lookup = lookup();
    for (int i = 0; i < entries.size(); i++) {
      CompoundTag entry = entries.getCompound(i).orElse(null);
      if (entry == null) continue;
      int index = entry.getIntOr("SlotIndex", entry.getByteOr("Index", (byte) 0) & 255);
      if (index < 0 || index >= contents.length) {
        ProjectRuntime.log.warn(
            DiagnosticArea.Block, "Ignoring invalid inventory index %d in %s", index, name);
        continue;
      }
      putFromNBT(index, StackTag.parse(lookup, entry));
    }
    onChanged();
  }

  public void writeToNbt(CompoundTag data) {
    var entries = new ListTag();
    var lookup = lookup();
    for (int i = 0; i < contents.length; i++)
      if (!contents[i].isEmpty()) {
        var entry = new CompoundTag();
        entry.putByte("Index", (byte) i);
        if (i > 255) entry.putInt("SlotIndex", i);
        StackTag.save(lookup, contents[i], entry);
        entries.add(entry);
      }
    data.put("Contents", entries);
  }

  public void organize() {
    boolean changed = false;
    for (int destination = 0; destination < contents.length; destination++) {
      for (int source = destination + 1; source < contents.length; source++) {
        ItemStack incoming = contents[source], held = contents[destination];
        if (incoming.isEmpty()
            || !held.isEmpty() && !ItemStack.isSameItemSameComponents(incoming, held)) continue;
        int limit = Math.min(stackSizeLimit, incoming.getMaxStackSize());
        int accepted = Math.min(incoming.getCount(), Math.max(0, limit - held.getCount()));
        if (accepted == 0) continue;
        contents[destination] =
            (held.isEmpty() ? incoming : held).copyWithCount(held.getCount() + accepted);
        contents[source] =
            incoming.getCount() == accepted
                ? ItemStack.EMPTY
                : incoming.copyWithCount(incoming.getCount() - accepted);
        changed = true;
      }
    }
    if (changed) onChanged();
  }

  protected ItemStack[] backup() {
    ItemStack[] copy = new ItemStack[contents.length];
    for (int i = 0; i < contents.length; i++) copy[i] = contents[i].copy();
    return copy;
  }

  protected void restore(ItemStack[] snapshot) {
    if (snapshot.length != contents.length)
      throw new IllegalArgumentException("Inventory snapshot size mismatch");
    for (int i = 0; i < contents.length; i++) putFromNBT(i, snapshot[i]);
  }

  public void onPickupFromSlot(Player player, ItemStack value) {}

  @Override
  public Iterator<ItemStack> iterator() {
    return new Iterator<>() {
      private int cursor;

      public boolean hasNext() {
        return cursor < contents.length;
      }

      public ItemStack next() {
        if (!hasNext()) throw new NoSuchElementException();
        return contents[cursor++];
      }
    };
  }

  @Override
  public String toString() {
    return name + Arrays.toString(contents);
  }

  public enum Access {
    NONE(false, false),
    I(true, false),
    O(false, true),
    IO(true, true);
    private final boolean input, output;

    Access(boolean input, boolean output) {
      this.input = input;
      this.output = output;
    }

    public boolean isInput() {
      return input;
    }

    public boolean isOutput() {
      return output;
    }
  }

  public enum InvSide {
    ANY(Direction.values()),
    TOP(Direction.UP),
    BOTTOM(Direction.DOWN),
    SIDE(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST),
    NOTSIDE();
    private final Set<Direction> directions;

    InvSide(Direction... values) {
      var set = EnumSet.noneOf(Direction.class);
      Collections.addAll(set, values);
      directions = Collections.unmodifiableSet(set);
    }

    public boolean matches(Direction direction) {
      return directions.contains(direction);
    }

    public Set<Direction> getAcceptedSides() {
      return directions;
    }
  }
}
