package ic2ce.neoforge.personal;

import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Real paid EU debit, fractional credit persistence, payment delivery and reentrant market
 * notifications.
 */
public final class EnergySalesContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native energy sales: " + label);
  }

  public static Result run(ServerLevel world) {
    return new EnergySalesContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8752, 100, 8752);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, BlockDefinitions.ENERGY_O_MAT.defaultBlockState());
    var vendor = (EnergyTradeTerminal) world.getBlockEntity(position);
    vendor.euBuffer = 100;
    check(
        vendor.useEnergy(25)
            && vendor.euBuffer == 75
            && !vendor.useEnergy(76)
            && vendor.euBuffer == 75,
        "useEnergy actually debits sold energy and rejects an excessive request");
    check(
        !vendor.useEnergy(Double.NaN) && !vendor.useEnergy(-1) && vendor.euBuffer == 75,
        "invalid work cannot create buffer EU");
    vendor.euBuffer = 0;
    vendor.paidFor = 1;
    var input = vendor.getFacing().getOpposite();
    check(
        vendor.injectEnergy(input, .25, 1) == 0
            && vendor.euBuffer == .25
            && vendor.getDemandedEnergy() == .75,
        "fractional input consumes only its own fraction of the one paid EU");
    var saved = new CompoundTag();
    vendor.saveAdditional(saved);
    vendor.load(saved);
    check(
        vendor.getDemandedEnergy() == .75 && vendor.euBuffer == .25,
        "fractional paid credit survives native save and load");
    check(
        vendor.injectEnergy(input, 2, 1) == 1.25
            && vendor.euBuffer == 1
            && vendor.getDemandedEnergy() == 0
            && vendor.paidFor == 0,
        "later injection accepts the remaining paid fraction and returns every unpaid EU");
    check(
        vendor.injectEnergy(input, -10, 1) == -10 && vendor.euBuffer == 1,
        "negative injection cannot increase paid credit or invert energy flow");
    vendor.drawEnergy(20);
    check(vendor.euBuffer == 0, "source debit is bounded by the real sold buffer");
    vendor.load(new CompoundTag());
    check(
        vendor.euOffer == 1000 && vendor.paidFor == 0 && vendor.euBuffer == 0,
        "empty persistence restores the sensible offer and clears stale ledger values");
    world.setBlockAndUpdate(position.east(), BlockDefinitions.IRON_STORAGE_BOX.defaultBlockState());
    var income =
        ((ic2ce.core.block.storage.box.TileEntityStorageBox) world.getBlockEntity(position.east()))
            .getInventorySlot("inventory");
    vendor.demandSlot.put(new ItemStack(Items.IRON_INGOT, 2));
    vendor.inputSlot.put(new ItemStack(Items.IRON_INGOT, 4));
    vendor.updateEntityServer();
    check(
        income.get().getCount() == 2
            && vendor.inputSlot.get().getCount() == 2
            && vendor.paidFor == 1000,
        "one completed native two-item invoice buys exactly one configured energy offer");
    vendor.updateEntityServer();
    check(
        income.get().getCount() == 4 && vendor.inputSlot.isEmpty() && vendor.paidFor == 2000,
        "second invoice conserves all delivered payment and cumulative credit");
    world.removeBlock(position, false);
    world.removeBlock(position.east(), false);
    world.setBlockAndUpdate(position, BlockDefinitions.TRADE_O_MAT.defaultBlockState());
    var trader = (ItemTradeTerminal) world.getBlockEntity(position);
    var market = new WirelessTradeMarket(world);
    var notices = new int[2];
    WirelessTradeMarket.MarketWatcher first =
        new WirelessTradeMarket.MarketWatcher() {
          @Override
          public void onAdd(BlockPos ignored) {
            notices[0]++;
            market.unregisterWatcher(this);
          }

          @Override
          public void onRemove(BlockPos ignored) {
            notices[0]++;
          }
        };
    var second =
        new WirelessTradeMarket.MarketWatcher() {
          @Override
          public void onAdd(BlockPos ignored) {
            notices[1]++;
          }

          @Override
          public void onRemove(BlockPos ignored) {
            notices[1]++;
          }
        };
    market.registerWatcher(first);
    market.registerWatcher(second);
    market.registerTradeOMat(trader);
    market.registerTradeOMat(trader);
    check(
        notices[0] == 1
            && notices[1] == 1
            && market.tradersAround(position, Integer.MAX_VALUE).count() == 1,
        "reentrant watcher removal and duplicate registration are stable; wide query does not"
            + " overflow");
    market.unregisterTradeOMat(trader);
    market.unregisterTradeOMat(trader);
    check(
        notices[0] == 1 && notices[1] == 2 && market.tradersAround(position, 0).findAny().isEmpty(),
        "idempotent market removal publishes only one actual removal");
    try {
      market.tradersAround(position, -1);
      check(false, "negative market radius rejected");
    } catch (IllegalArgumentException expected) {
      check(true, "negative market radius rejected");
    }
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
