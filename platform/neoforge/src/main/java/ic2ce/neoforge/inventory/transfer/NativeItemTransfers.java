package ic2ce.neoforge.inventory.transfer;

import com.mojang.authlib.GameProfile;
import ic2ce.core.util.Util;
import ic2ce.neoforge.personal.OwnedDevice;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Native transactional item operations: simulation aborts the same path that a real transfer
 * commits.
 */
public final class NativeItemTransfers implements EnvItemHandler {
  private record Endpoint(Level world, BlockPos position) {}

  private record NativeInventoryHandle(
      ResourceHandler<ItemResource> handler, Direction side, Endpoint identity, boolean personal)
      implements AdjacentInventory {
    @Override
    public Direction getSide() {
      return side;
    }
  }

  private static final Comparator<NativeInventoryHandle> ORDER =
      Comparator.comparing(NativeInventoryHandle::personal)
          .thenComparing(
              Comparator.comparingInt((NativeInventoryHandle handle) -> handle.handler.size())
                  .reversed());
  private final ThreadLocal<HashSet<Endpoint>> busy = ThreadLocal.withInitial(HashSet::new);

  private static NativeInventoryHandle handle(AdjacentInventory inventory) {
    return inventory == null
        ? null
        : inventory instanceof NativeInventoryHandle nativeHandle ? nativeHandle : null;
  }

  private static NativeInventoryHandle storage(
      BlockEntity block, Direction outward, Direction access, GameProfile owner) {
    if (block == null || block.isRemoved() || block.getLevel() == null) return null;
    return storage(block.getLevel(), block.getBlockPos(), block, outward, access, owner);
  }

  private static NativeInventoryHandle storage(
      Level world,
      BlockPos position,
      BlockEntity block,
      Direction outward,
      Direction access,
      GameProfile owner) {
    if (block != null && block.isRemoved()) return null;
    ResourceHandler<ItemResource> handler;
    boolean personal = owner != null && block instanceof OwnedDevice;
    if (personal)
      handler =
          new TransactionalItemAdapter(((OwnedDevice) block).getPrivilegedInventory(owner), access);
    else {
      handler = world.getCapability(Capabilities.Item.BLOCK, position, access);
      if (handler == null && block instanceof Container container)
        handler = new TransactionalItemAdapter(container, access);
    }
    return handler == null || handler.size() == 0
        ? null
        : new NativeInventoryHandle(
            handler, outward, new Endpoint(world, position.immutable()), personal);
  }

  private static NativeInventoryHandle adjacent(BlockEntity source, Direction direction) {
    if (source == null || direction == null || source.getLevel() == null || source.isRemoved())
      return null;
    var world = source.getLevel();
    var target = source.getBlockPos().relative(direction);
    if (!world.isLoaded(target)) return null;
    var owner = source instanceof OwnedDevice owned ? owned.getOwner() : null;
    return storage(
        world, target, world.getBlockEntity(target), direction, direction.getOpposite(), owner);
  }

  @Override
  public AdjacentInventory getAdjacentInventory(BlockEntity source, Direction direction) {
    return adjacent(source, direction);
  }

  @Override
  public List<? extends AdjacentInventory> getAdjacentInventories(BlockEntity source) {
    var neighbors = new ArrayList<NativeInventoryHandle>(6);
    for (var side : Util.ALL_DIRS) {
      var handle = adjacent(source, side);
      if (handle != null) neighbors.add(handle);
    }
    neighbors.sort(ORDER);
    return neighbors;
  }

  @Override
  public AdjacentInventory wrapInventory(BlockEntity block, Direction side) {
    return storage(block, side, side, null);
  }

  private boolean enter(NativeInventoryHandle handle) {
    return busy.get().add(handle.identity);
  }

  private void leave(NativeInventoryHandle handle) {
    busy.get().remove(handle.identity);
  }

  private static int bounded(int accepted, int offered) {
    if (accepted < 0 || accepted > offered)
      throw new IllegalStateException("Native item handler exceeded reserved quantity");
    return accepted;
  }

  @Override
  public int deposit(
      BlockEntity target, Direction side, ItemStack stack, GameProfile owner, boolean simulate) {
    return deposit(storage(target, side, side, owner), stack, simulate);
  }

  @Override
  public int deposit(AdjacentInventory inventory, ItemStack stack, boolean simulate) {
    var target = handle(inventory);
    if (target == null || stack == null || stack.isEmpty() || !enter(target)) return 0;
    try (var transaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
      int inserted =
          bounded(
              target.handler.insert(ItemResource.of(stack), stack.getCount(), transaction),
              stack.getCount());
      if (!simulate) transaction.commit();
      return inserted;
    } finally {
      leave(target);
    }
  }

  @Override
  public int fetch(BlockEntity source, ItemStack requested, boolean simulate) {
    if (requested == null || requested.isEmpty()) return 0;
    var resource = ItemResource.of(requested);
    int obtained = 0;
    try (var transaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
      for (var inventory : getAdjacentInventories(source)) {
        var handle = handle(inventory);
        if (!enter(handle)) continue;
        try {
          int wanted = requested.getCount() - obtained;
          obtained += bounded(handle.handler.extract(resource, wanted, transaction), wanted);
        } finally {
          leave(handle);
        }
        if (obtained == requested.getCount()) break;
      }
      if (!simulate) transaction.commit();
      return obtained;
    }
  }

  @Override
  public int distribute(BlockEntity source, ItemStack offered, boolean simulate) {
    if (offered == null || offered.isEmpty()) return 0;
    var resource = ItemResource.of(offered);
    int delivered = 0;
    try (var transaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
      for (var inventory : getAdjacentInventories(source)) {
        var target = handle(inventory);
        if (!enter(target)) continue;
        try {
          int wanted = offered.getCount() - delivered;
          delivered += bounded(target.handler.insert(resource, wanted, transaction), wanted);
        } finally {
          leave(target);
        }
        if (delivered == offered.getCount()) break;
      }
      if (!simulate) transaction.commit();
      return delivered;
    }
  }

  @Override
  public int transfer(AdjacentInventory from, AdjacentInventory to, int limit) {
    return transfer(from, to, limit, _ -> true);
  }

  @Override
  public int transfer(
      AdjacentInventory from, AdjacentInventory to, int limit, Predicate<ItemStack> filter) {
    Objects.requireNonNull(filter);
    var source = handle(from);
    var target = handle(to);
    if (source == null
        || target == null
        || limit <= 0
        || source.identity.equals(target.identity)
        || !enter(source)) return 0;
    if (!enter(target)) {
      leave(source);
      return 0;
    }
    try (var transaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
      int moved =
          bounded(
              ResourceHandlerUtil.move(
                  source.handler,
                  target.handler,
                  resource -> filter.test(resource.toStack()),
                  limit,
                  transaction),
              limit);
      transaction.commit();
      return moved;
    } finally {
      leave(target);
      leave(source);
    }
  }
}
