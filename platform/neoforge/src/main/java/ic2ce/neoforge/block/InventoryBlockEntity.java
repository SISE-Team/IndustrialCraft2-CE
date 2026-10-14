package ic2ce.neoforge.block;

import ic2ce.core.block.IInventorySlotHolder;
import ic2ce.neoforge.component.AnalogSignalOutput;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.UpgradeInventory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Indexed native slot groups use compact owned arrays and allocate no lookup/iteration wrappers.
 */
public abstract class InventoryBlockEntity extends NativeBlockEntity
    implements WorldlyContainer, IInventorySlotHolder<InventoryBlockEntity> {
  private InventorySlot[] groups;
  private int groupCount;
  protected final AnalogSignalOutput comparator = addComponent(new AnalogSignalOutput(this));

  public InventoryBlockEntity(
      BlockEntityType<? extends InventoryBlockEntity> type, BlockPos position, BlockState state) {
    super(type, position, state);
    comparator.setUpdate(this::calcRedstoneFromInvSlots);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    var slots = data.getCompoundOrEmpty("InvSlots");
    for (int index = 0; index < groupCount; index++) {
      var group = groups[index];
      group.readFromNbt(slots.getCompoundOrEmpty(group.name));
    }
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    var slots = new CompoundTag();
    for (int index = 0; index < groupCount; index++) {
      var group = groups[index];
      var saved = new CompoundTag();
      group.writeToNbt(saved);
      slots.put(group.name, saved);
    }
    data.put("InvSlots", slots);
  }

  @Override
  public int getContainerSize() {
    int total = 0;
    for (int index = 0; index < groupCount; index++)
      total = Math.addExact(total, groups[index].size());
    return total;
  }

  @Override
  public boolean isEmpty() {
    for (int index = 0; index < groupCount; index++) if (!groups[index].isEmpty()) return false;
    return true;
  }

  private long locate(int index) {
    if (index < 0) return -1;
    for (int group = 0; group < groupCount; group++) {
      int count = groups[group].size();
      if (index < count) return (long) group << 32 | index;
      index -= count;
    }
    return -1;
  }

  private InventorySlot group(long location) {
    return groups[(int) (location >>> 32)];
  }

  private static int offset(long location) {
    return (int) location;
  }

  @Override
  public ItemStack getItem(int index) {
    long location = locate(index);
    return location < 0 ? ItemStack.EMPTY : group(location).get(offset(location));
  }

  private void dirty() {
    if (getLevel() != null) getLevel().blockEntityChanged(worldPosition);
  }

  private void put(long location, ItemStack stack) {
    group(location).put(offset(location), stack == null ? ItemStack.EMPTY : stack);
    dirty();
  }

  @Override
  public ItemStack removeItem(int index, int requested) {
    if (requested <= 0) return ItemStack.EMPTY;
    long location = locate(index);
    if (location < 0) return ItemStack.EMPTY;
    var stored = group(location).get(offset(location));
    if (stored.isEmpty()) return ItemStack.EMPTY;
    int amount = Math.min(requested, stored.getCount());
    var removed = stored.copyWithCount(amount);
    put(location, stored.copyWithCount(stored.getCount() - amount));
    return removed;
  }

  @Override
  public ItemStack removeItemNoUpdate(int index) {
    long location = locate(index);
    if (location < 0) return ItemStack.EMPTY;
    var result = group(location).get(offset(location));
    if (!result.isEmpty()) put(location, ItemStack.EMPTY);
    return result;
  }

  @Override
  public void setItem(int index, ItemStack stack) {
    long location = locate(index);
    if (location >= 0) put(location, stack);
  }

  @Override
  public void setItem(int index, ItemStack stack, boolean insideTransaction) {
    if (!insideTransaction) {
      setItem(index, stack);
      return;
    }
    long location = locate(index);
    if (location >= 0) group(location).putInTransaction(offset(location), stack);
  }

  @Override
  public void setChanged() {
    dirty();
    for (int index = 0; index < groupCount; index++) groups[index].onChanged();
  }

  @Override
  public int getMaxStackSize() {
    int maximum = 0;
    for (int index = 0; index < groupCount; index++)
      maximum = Math.max(maximum, groups[index].getStackSizeLimit());
    return maximum;
  }

  public int getSlotLimit(int index) {
    long location = locate(index);
    return location < 0 ? 0 : Math.max(0, group(location).getStackSizeLimit());
  }

  @Override
  public boolean stillValid(Player player) {
    return !isRemoved()
        && player.distanceToSqr(
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5)
            <= 64;
  }

  @Override
  public int[] getSlotsForFace(Direction side) {
    int size = getContainerSize();
    var indices = new int[size];
    for (int index = 0; index < size; index++) indices[index] = index;
    return indices;
  }

  @Override
  public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction side) {
    long location = locate(index);
    if (location < 0 || stack == null || stack.isEmpty() || side == null) return false;
    var target = group(location);
    if (!target.canInput() || !target.accepts(stack)) return false;
    if (target.preferredSide != InventorySlot.InvSide.ANY && target.preferredSide.matches(side))
      return true;
    for (int candidate = 0; candidate < groupCount; candidate++) {
      var other = groups[candidate];
      if (other != target
          && other.preferredSide != InventorySlot.InvSide.ANY
          && other.preferredSide.matches(side)
          && other.canInput()
          && other.accepts(stack)) return false;
    }
    return true;
  }

  @Override
  public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction side) {
    long location = locate(index);
    if (location < 0 || side == null) return false;
    var target = group(location);
    if (!target.canOutput()) return false;
    boolean preferred = target.preferredSide.matches(side);
    if (target.preferredSide != InventorySlot.InvSide.ANY && preferred) return true;
    for (int candidate = 0; candidate < groupCount; candidate++) {
      var other = groups[candidate];
      if (other != target
          && (other.preferredSide != InventorySlot.InvSide.ANY || !preferred)
          && other.preferredSide.matches(side)
          && other.canOutput()
          && !other.isEmpty()) return false;
    }
    return true;
  }

  @Override
  public void clearContent() {
    for (int index = 0; index < groupCount; index++) groups[index].clear();
    dirty();
  }

  @Override
  public int getBaseIndex(InventorySlot target) {
    int first = 0;
    for (int index = 0; index < groupCount; index++) {
      if (groups[index] == target) return first;
      first += groups[index].size();
    }
    return -1;
  }

  @Override
  public InventoryBlockEntity getParent() {
    return this;
  }

  @Override
  public InventorySlot getInventorySlot(String name) {
    for (int index = 0; index < groupCount; index++)
      if (groups[index].name.equals(name)) return groups[index];
    return null;
  }

  @Override
  public void addInventorySlot(InventorySlot slot) {
    if (slot == null
        || slot.base == null
        || slot.base.getParent() != this
        || getInventorySlot(slot.name) != null)
      throw new IllegalArgumentException("Invalid or duplicate inventory group");
    if (groups == null) groups = new InventorySlot[4];
    else if (groupCount == groups.length)
      groups = Arrays.copyOf(groups, Math.multiplyExact(groupCount, 2));
    groups[groupCount++] = slot;
  }

  @Override
  protected List<ItemStack> getAuxDrops(int fortune) {
    var drops = new ArrayList<>(super.getAuxDrops(fortune));
    for (int index = 0; index < groupCount; index++) {
      var group = groups[index];
      for (int slot = 0; slot < group.size(); slot++)
        if (!group.get(slot).isEmpty()) drops.add(group.get(slot).copy());
    }
    return drops;
  }

  protected static int calcRedstoneFromInvSlots(InventorySlot... groups) {
    return calculate(groups, groups.length);
  }

  protected int calcRedstoneFromInvSlots() {
    return calculate(groups, groupCount);
  }

  private static int signal(long used, long space) {
    return used == 0 || space == 0 ? 0 : (int) Math.clamp(1 + used * 14 / space, 0, 15);
  }

  private static int calculate(InventorySlot[] groups, int count) {
    long space = 0, used = 0;
    for (int index = 0; index < count; index++) {
      var group = groups[index];
      if (group instanceof UpgradeInventory) continue;
      int limit = Math.max(0, group.getStackSizeLimit());
      space += (long) group.size() * limit;
      for (int slot = 0; slot < group.size(); slot++) {
        var stack = group.get(slot);
        if (!stack.isEmpty()) {
          int itemLimit = Math.max(1, stack.getMaxStackSize());
          used += (long) Math.min(itemLimit, stack.getCount()) * limit / itemLimit;
        }
      }
    }
    return signal(used, space);
  }

  protected static int calcRedstoneFromInvSlots(Iterable<InventorySlot> groups) {
    long space = 0, used = 0;
    for (var group : groups)
      if (!(group instanceof UpgradeInventory)) {
        int limit = Math.max(0, group.getStackSizeLimit());
        space += (long) group.size() * limit;
        for (int slot = 0; slot < group.size(); slot++) {
          var stack = group.get(slot);
          if (!stack.isEmpty()) {
            int itemLimit = Math.max(1, stack.getMaxStackSize());
            used += (long) Math.min(itemLimit, stack.getCount()) * limit / itemLimit;
          }
        }
      }
    return signal(used, space);
  }
}
