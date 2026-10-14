package ic2ce.core.block.transport.cover;

import ic2ce.core.util.StackTag;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.NativeComponent;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

/**
 * Six owned addon covers with checked admission, inert empty removal and complete reload
 * replacement.
 */
public class Covers extends NativeComponent {
  protected final ItemStack[] covers = new ItemStack[6];

  public Covers(NativeBlockEntity owner) {
    super(owner);
    Arrays.fill(covers, ItemStack.EMPTY);
  }

  private static int index(Direction side) {
    return Objects.requireNonNull(side).ordinal();
  }

  public void addCover(Direction side, ItemStack offered) {
    int index = index(side);
    if (offered == null
        || offered.isEmpty()
        || !covers[index].isEmpty()
        || !(offered.getItem() instanceof ICoverItem cover)) return;
    if (!(parent instanceof ICoverHolder holder)
        || !cover.isSuitableFor(offered, holder.getCoverProperties())) return;
    var owned = offered.copyWithCount(1);
    var tag = StackTag.getOrCreate(owned);
    tag.putByte("side", (byte) index);
    StackTag.set(owned, tag);
    covers[index] = owned;
    parent.setChanged();
  }

  public ItemStack removeCover(Direction side) {
    int index = index(side);
    var stored = covers[index];
    if (stored.isEmpty()) return ItemStack.EMPTY;
    covers[index] = ItemStack.EMPTY;
    var result = stored.copy();
    var tag = StackTag.get(result);
    if (tag != null) {
      tag.remove("side");
      StackTag.set(result, tag.isEmpty() ? null : tag);
    }
    parent.setChanged();
    return result;
  }

  public boolean hasCover(Direction side) {
    return !covers[index(side)].isEmpty();
  }

  public ICoverItem getCoverItem(Direction side) {
    var stack = covers[index(side)];
    return !stack.isEmpty() && stack.getItem() instanceof ICoverItem cover ? cover : null;
  }

  @Override
  public void readFromNbt(CompoundTag data) {
    Arrays.fill(covers, ItemStack.EMPTY);
    if (!(parent instanceof ICoverHolder holder)) return;
    var list = data.getListOrEmpty("covers");
    for (int row = 0; row < list.size(); row++) {
      var record = list.getCompound(row).orElse(null);
      if (record == null) continue;
      int index = record.getByteOr("facing", (byte) -1) & 255;
      if (index >= covers.length || !covers[index].isEmpty()) continue;
      var stack = StackTag.parseHolder(record);
      if (stack.isEmpty()
          || !(stack.getItem() instanceof ICoverItem cover)
          || !cover.isSuitableFor(stack, holder.getCoverProperties())) continue;
      var owned = stack.copyWithCount(1);
      var tag = StackTag.getOrCreate(owned);
      tag.putByte("side", (byte) index);
      StackTag.set(owned, tag);
      covers[index] = owned;
    }
  }

  @Override
  public CompoundTag writeToNbt() {
    var result = new CompoundTag();
    var list = new ListTag();
    var world = parent.getLevel();
    if (world != null)
      for (var side : Direction.values()) {
        var stack = covers[side.ordinal()];
        if (stack.isEmpty()) continue;
        var entry = new CompoundTag();
        entry.putByte("facing", (byte) side.ordinal());
        StackTag.save(world.registryAccess(), stack, entry);
        list.add(entry);
      }
    result.put("covers", list);
    return result;
  }

  @Override
  public boolean enableWorldTick() {
    return parent.getLevel() != null
        && !parent.getLevel().isClientSide()
        && parent instanceof ICoverHolder;
  }

  @Override
  public void onWorldTick() {
    if (parent.isRemoved()
        || parent.getLevel() == null
        || parent.getLevel().isClientSide()
        || !(parent instanceof ICoverHolder holder)) return;
    for (int index = 0; index < covers.length; index++) {
      var stack = covers[index];
      if (!stack.isEmpty()
          && stack.getItem() instanceof ICoverItem cover
          && cover.onTick(stack, holder)) parent.setChanged();
      if (covers[index].isEmpty()) covers[index] = ItemStack.EMPTY;
    }
  }
}
