package ic2ce.neoforge.transport;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.core.item.EnvItemHandler;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.profile.NotClassic;
import ic2ce.core.util.StackTag;
import ic2ce.core.util.StackUtil;
import ic2ce.core.util.Util;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.transport.menu.BatchItemMenu;
import java.util.Arrays;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Exact filtered batches take precedence; only globally unfiltered items use the one-item fallback.
 */
@NotClassic
public class BatchItemSorter extends PoweredBlockEntity
    implements IHasGui, INetworkClientTileEntityEventListener, IUpgradableBlock {
  public static final int defaultTier = 2;
  public final UpgradeInventory upgradeSlot = new UpgradeInventory(this, "upgrade", 3);
  public final InventorySlot buffer =
      new InventorySlot(this, "Buffer", InventorySlot.Access.IO, 11);
  private final ItemStack[][] filters = new ItemStack[6][7];
  public Direction defaultRoute = Direction.DOWN;

  public BatchItemSorter(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.SORTING_MACHINE, position, state, 15000, defaultTier, false);
    clearFilters();
  }

  private void clearFilters() {
    for (var side : filters) Arrays.fill(side, ItemStack.EMPTY);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    clearFilters();
    var saved = data.getListOrEmpty("filters");
    for (int entry = 0; entry < Math.min(saved.size(), 42); entry++) {
      var row = saved.getCompound(entry).orElse(null);
      if (row == null) continue;
      int index = row.getByteOr("index", (byte) -1) & 255;
      if (index >= 42) continue;
      var stack = StackTag.parseHolder(row);
      if (!stack.isEmpty())
        filters[index / 7][index % 7] = stack.copyWithCount(Math.clamp(stack.getCount(), 1, 64));
    }
    int direction = data.getByteOr("defaultroute", (byte) 0);
    defaultRoute = direction >= 0 && direction < 6 ? Util.ALL_DIRS[direction] : Direction.DOWN;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    var saved = new ListTag();
    for (int index = 0; index < 42; index++) {
      var stack = filters[index / 7][index % 7];
      if (stack == null || stack.isEmpty()) continue;
      var row = new CompoundTag();
      row.putByte("index", (byte) index);
      StackTag.save(getLevel().registryAccess(), stack, row);
      saved.add(row);
    }
    data.put("filters", saved);
    data.putByte("defaultroute", (byte) (defaultRoute == null ? 0 : defaultRoute.ordinal()));
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    setUpgradableBlock();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (energy.getEnergy() < 20 || buffer.isEmpty()) return;
    var neighbors = StackUtil.ENV.getAdjacentInventories(this);
    var fallback = defaultRoute == null ? Direction.DOWN : defaultRoute;
    boolean changed = false;
    for (int slot = 0; slot < buffer.size() && energy.getEnergy() >= 20; slot++) {
      var stack = buffer.get(slot);
      if (stack.isEmpty()) continue;
      boolean filtered = matchesAny(stack);
      for (var neighbor : neighbors) {
        if (neighbor.getSide() == fallback) continue;
        for (var filter : filters[neighbor.getSide().ordinal()]) {
          if (filter == null || filter.isEmpty() || !StackUtil.checkItemEquality(filter, stack))
            continue;
          int batch = filter.getCount();
          if (batch > 0 && batch <= stack.getCount() && energy.canUseEnergy(batch * 20.0))
            changed |= transfer(slot, neighbor, batch);
          break;
        }
        stack = buffer.get(slot);
        if (stack.isEmpty()) break;
      }
      if (!filtered && !buffer.isEmpty(slot) && energy.getEnergy() >= 20)
        for (var neighbor : neighbors)
          if (neighbor.getSide() == fallback) {
            changed |= transfer(slot, neighbor, 1);
            break;
          }
    }
    if (changed) super.setChanged();
  }

  private boolean matchesAny(ItemStack stack) {
    for (var side : filters)
      for (var filter : side)
        if (filter != null && !filter.isEmpty() && StackUtil.checkItemEquality(filter, stack))
          return true;
    return false;
  }

  private boolean transfer(int slot, EnvItemHandler.AdjacentInventory destination, int count) {
    var original = buffer.get(slot).copy();
    var offered = original.copyWithCount(count);
    if (StackUtil.ENV.deposit(destination, offered.copy(), true) != count
        || !energy.useEnergy(count * 20.0)) return false;
    buffer.put(
        slot,
        count == original.getCount()
            ? ItemStack.EMPTY
            : original.copyWithCount(original.getCount() - count));
    int delivered;
    try {
      delivered = StackUtil.ENV.deposit(destination, offered, false);
    } catch (RuntimeException | Error error) {
      restore(slot, original, count);
      energy.forceAddEnergy(count * 20.0);
      throw error;
    }
    if (delivered < 0 || delivered > count)
      throw new IllegalStateException("Item target returned an invalid transferred count");
    restore(slot, original, count - delivered);
    energy.forceAddEnergy((count - delivered) * 20.0);
    return delivered > 0;
  }

  private void restore(int slot, ItemStack original, int count) {
    if (count <= 0) return;
    var current = buffer.get(slot);
    if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, original))
      throw new IllegalStateException("Reserved sorting slot changed during transfer");
    buffer.put(slot, original.copyWithCount(current.getCount() + count));
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event >= 0 && event < 6) {
      defaultRoute = Util.ALL_DIRS[event];
      super.setChanged();
    }
  }

  public ItemStack[] getFilterSlots(Direction side) {
    return filters[side.ordinal()];
  }

  @Override
  public void setChanged() {
    super.setChanged();
    var world = getLevel();
    if (world != null && !world.isClientSide()) setUpgradableBlock();
  }

  public void setUpgradableBlock() {
    energy.setSinkTier(upgradeSlot.getTier(defaultTier));
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return Set.of(UpgradableProperty.Transformer);
  }

  @Override
  public double getEnergy() {
    return energy.getEnergy();
  }

  @Override
  public boolean useEnergy(double amount) {
    return energy.useEnergy(amount);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return new BatchItemMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new BatchItemMenu(id, inventory, this);
  }
}
