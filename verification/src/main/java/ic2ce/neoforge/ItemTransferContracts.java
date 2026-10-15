package ic2ce.neoforge;

import com.mojang.authlib.GameProfile;
import ic2ce.neoforge.personal.PersonalBlocks;
import ic2ce.neoforge.storage.StorageBlocks;
import ic2ce.neoforge.storage.box.TileEntityStorageBox;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.inventory.transfer.TransactionalItemAdapter;
import ic2ce.neoforge.personal.ItemTradeTerminal;
import ic2ce.neoforge.personal.PrivateChest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Real transactions cross native slot, ownership and nested-abort boundaries. */
public final class ItemTransferContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native item transfer: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ItemTransferContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8840, 100, 8840);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, StorageBlocks.IRON_STORAGE_BOX.defaultBlockState());
    world.setBlockAndUpdate(position.east(), StorageBlocks.IRON_STORAGE_BOX.defaultBlockState());
    var source = (TileEntityStorageBox) world.getBlockEntity(position);
    var destination = (TileEntityStorageBox) world.getBlockEntity(position.east());
    var input = source.getInventorySlot("inventory");
    var output = destination.getInventorySlot("inventory");
    for (int index = 0; index < output.size(); index++)
      output.put(index, new ItemStack(Items.BEDROCK, 64));
    output.put(0, new ItemStack(Items.GOLD_INGOT, 59));
    output.clear(1);
    input.put(new ItemStack(Items.GOLD_INGOT, 10));
    var target = StackUtil.ENV.getAdjacentInventory(source, Direction.EAST);
    var offered = new ItemStack(Items.GOLD_INGOT, 10);
    check(
        StackUtil.ENV.deposit(target, offered, true) == 10
            && output.get(0).getCount() == 59
            && output.isEmpty(1)
            && offered.getCount() == 10,
        "native simulated deposit spans multiple slots yet aborts every real target change");
    check(
        StackUtil.ENV.transfer(StackUtil.ENV.wrapInventory(source, Direction.EAST), target, 10)
                == 10
            && input.isEmpty()
            && output.get(0).getCount() == 64
            && output.get(1).getCount() == 5,
        "native committed move conserves exact source quantity across two destination slots");
    check(
        StackUtil.ENV.transfer(target, target, 10) == 0,
        "same physical native inventory cannot transfer items to itself");
    input.put(new ItemStack(Items.GOLD_INGOT, 10));
    try (var root = Transaction.openRoot()) {
      check(
          StackUtil.ENV.transfer(StackUtil.ENV.wrapInventory(source, Direction.EAST), target, 10)
              == 10,
          "item API composes inside an existing native transaction");
    }
    check(
        input.get().getCount() == 10 && output.get(1).getCount() == 5,
        "outer native abort rolls back an inner committed transfer on both endpoints");
    var probe = new PerformanceInventoryProbe(source);
    check(
        probe.verifyNestedTransactions().isEmpty(),
        "all four native parent/child commit combinations preserve exact slot quantities");
    check(
        PerformanceInventoryProbe.verifyCustomContainerFallback().isEmpty(),
        "unknown native containers retain cross-slot side effects in the full rollback snapshot");
    world.removeBlock(position, false);
    world.removeBlock(position.east(), false);

    world.setBlockAndUpdate(position, PersonalBlocks.TRADE_O_MAT.defaultBlockState());
    world.setBlockAndUpdate(position.east(), PersonalBlocks.PERSONAL_CHEST.defaultBlockState());
    var trader = (ItemTradeTerminal) world.getBlockEntity(position);
    var chest = (PrivateChest) world.getBlockEntity(position.east());
    var owner =
        new GameProfile(
            UUID.fromString("512c5c42-1f2e-4d14-8400-9f3846314c37"), "IC2TransferOwner");
    trader.setOwner(owner);
    chest.permitsAccess(owner);
    var privileged = chest.getPrivilegedInventory(owner);
    privileged.setItem(0, new ItemStack(Items.DIAMOND, 13));
    check(
        StackUtil.ENV.fetch(trader, new ItemStack(Items.DIAMOND, 5), true) == 5
            && privileged.getItem(0).getCount() == 13,
        "personal copied inventory view supports a completely aborted native extraction");
    check(
        StackUtil.ENV.fetch(trader, new ItemStack(Items.DIAMOND, 5), false) == 5
            && privileged.getItem(0).getCount() == 8,
        "partial real privileged extraction writes back its owned remainder instead of mutating a"
            + " borrowed copy");
    check(
        StackUtil.ENV.distribute(trader, new ItemStack(Items.DIAMOND, 4), false) == 4
            && privileged.getItem(0).getCount() == 12,
        "partial privileged insertion merges through an owned copy and persists all four actual"
            + " items");
    var journal = new TransactionalItemAdapter(chest.getPrivilegedInventory(owner), null);
    var diamond = ItemResource.of(new ItemStack(Items.DIAMOND));
    try (var root = Transaction.openRoot()) {
      journal.extract(0, diamond, 3, root);
    }
    check(
        privileged.getItem(0).getCount() == 12,
        "aborting a privileged native transaction restores all actual chest items");
    privileged.clearContent();
    world.removeBlock(position, false);
    world.removeBlock(position.east(), false);
    world.setBlockAndUpdate(position, StorageBlocks.IRON_STORAGE_BOX.defaultBlockState());
    world.setBlockAndUpdate(
        position.below(), net.minecraft.world.level.block.Blocks.COMPOSTER.defaultBlockState());
    source = (TileEntityStorageBox) world.getBlockEntity(position);
    var compost = StackUtil.ENV.getAdjacentInventory(source, Direction.DOWN);
    check(
        compost != null && world.getBlockEntity(position.below()) == null,
        "adjacent native item capabilities work on an actual composter without a block entity");
    var seeds = new ItemStack(Items.WHEAT_SEEDS, 10);
    int accepted = StackUtil.ENV.deposit(compost, seeds, true);
    check(
        accepted > 0
            && world
                    .getBlockState(position.below())
                    .getValue(net.minecraft.world.level.block.ComposterBlock.LEVEL)
                == 0
            && seeds.getCount() == 10,
        "composter simulation rolls back actual native block-state and random-journal changes");
    check(
        StackUtil.ENV.deposit(compost, seeds, false) == accepted
            && world
                    .getBlockState(position.below())
                    .getValue(net.minecraft.world.level.block.ComposterBlock.LEVEL)
                > 0,
        "composter commit takes the same quantity after simulation and publishes its real"
            + " block-state update");
    world.removeBlock(position.below(), false);
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
