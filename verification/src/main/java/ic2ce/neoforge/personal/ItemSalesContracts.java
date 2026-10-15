package ic2ce.neoforge.personal;

import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.storage.StorageBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Actual adjacent stock and payment plus independently restored intermediate native invoices. */
public final class ItemSalesContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native item sales: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ItemSalesContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8784, 100, 8784);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, PersonalBlocks.TRADE_O_MAT.defaultBlockState());
    world.setBlockAndUpdate(position.east(), StorageBlocks.IRON_STORAGE_BOX.defaultBlockState());
    var tile = (ItemTradeTerminal) world.getBlockEntity(position);
    var stock =
        ((ic2ce.neoforge.storage.box.TileEntityStorageBox) world.getBlockEntity(position.east()))
            .getInventorySlot("inventory");
    stock.put(new ItemStack(Items.GOLD_INGOT, 9));
    tile.demandSlot.put(new ItemStack(Items.IRON_INGOT, 2));
    tile.offerSlot.put(new ItemStack(Items.GOLD_INGOT, 3));
    tile.inputSlot.put(new ItemStack(Items.IRON_INGOT, 4));
    tile.updateEntityServer();
    tile.updateEntityServer();
    check(
        tile.outputSlot.get().is(Items.GOLD_INGOT)
            && tile.outputSlot.get().getCount() == 6
            && tile.inputSlot.isEmpty()
            && tile.totalTradeCount == 2,
        "two actual invoices emit six offered items and consume four payment items");
    check(
        count(stock, Items.GOLD_INGOT) == 3 && count(stock, Items.IRON_INGOT) == 4,
        "adjacent stock and income conserve every traded gold and iron");
    tile.outputSlot.put(new ItemStack(Items.BEDROCK, 64));
    tile.inputSlot.put(new ItemStack(Items.IRON_INGOT, 2));
    tile.updateEntityServer();
    check(
        tile.inputSlot.get().getCount() == 2
            && count(stock, Items.GOLD_INGOT) == 3
            && tile.totalTradeCount == 2,
        "blocked output reserves neither customer payment nor supply");
    tile.outputSlot.clear();
    tile.inputSlot.clear();
    stock.clear();
    var saved = new CompoundTag();
    tile.saveAdditional(saved);
    encode(world, saved, "pendingPayment", new ItemStack(Items.IRON_INGOT, 1));
    encode(world, saved, "pendingOffer", new ItemStack(Items.GOLD_INGOT, 3));
    encode(world, saved, "reservedOffer", new ItemStack(Items.GOLD_INGOT, 2));
    tile.load(saved);
    stock.put(new ItemStack(Items.GOLD_INGOT, 1));
    tile.updateEntityServer();
    check(
        tile.outputSlot.get().getCount() == 3
            && count(stock, Items.GOLD_INGOT) == 0
            && count(stock, Items.IRON_INGOT) == 1
            && tile.totalTradeCount == 3,
        "restored partial invoice fetches only its one missing supply item and delivers only its"
            + " remaining one payment item");
    var finished = new CompoundTag();
    tile.saveAdditional(finished);
    check(
        !finished.contains("pendingPayment")
            && !finished.contains("pendingOffer")
            && !finished.contains("reservedOffer"),
        "completed invoice erases all stale escrow persistence");
    tile.infinite = true;
    tile.saveAdditional(finished);
    tile.infinite = false;
    tile.saveAdditional(finished);
    tile.load(finished);
    check(
        !tile.infinite,
        "turning off administrative infinite supply persists an explicit false state");
    encode(world, finished, "pendingPayment", new ItemStack(Items.IRON_INGOT));
    tile.load(finished);
    tile.updateEntityServer();
    check(
        tile.getAuxDrops(0).stream()
                .filter(stack -> stack.is(Items.IRON_INGOT))
                .mapToInt(ItemStack::getCount)
                .sum()
            >= 1,
        "malformed invoice stays inert and preserves its actually owned payment for removal instead"
            + " of crashing world loading");
    check(
        tile.setWireless(true) && !tile.setWireless(true) && tile.setWireless(false),
        "actual wireless attachment is idempotent in both directions");
    tile.load(new CompoundTag());
    stock.clear();
    world.removeBlock(position, false);
    world.removeBlock(position.east(), false);
    return new Result(checks, List.copyOf(failures));
  }

  private static void encode(ServerLevel world, CompoundTag target, String field, ItemStack stack) {
    var data = new CompoundTag();
    StackTag.save(world.registryAccess(), stack, data);
    target.put(field, data);
  }

  private static int count(
      ic2ce.neoforge.inventory.InventorySlot slot, net.minecraft.world.item.Item item) {
    int count = 0;
    for (var stack : slot) if (stack.is(item)) count += stack.getCount();
    return count;
  }
}
