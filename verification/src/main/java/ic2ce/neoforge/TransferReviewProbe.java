package ic2ce.neoforge;

import ic2ce.api.fluid.Ic2FluidItem;
import ic2ce.neoforge.inventory.transfer.NativeItemTransfers;
import ic2ce.neoforge.inventory.transfer.TransactionalItemAdapter;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Exercises the real IC2 adapters against placed inventories and owned item transactions. */
public final class TransferReviewProbe {
  private int checks;
  private final List<String> failures = new ArrayList<>();
  private final List<Map<String, Object>> cases = new ArrayList<>();

  private void check(boolean passed, String name) {
    checks++;
    if (!passed) failures.add(name);
  }

  private ItemAccess owned(ItemStack stack) {
    var storage = new ItemStacksResourceHandler(1);
    try (var root = Transaction.openRoot()) {
      storage.insert(0, ItemResource.of(stack), stack.getCount(), root);
      root.commit();
    }
    return ItemAccess.forHandlerIndex(storage, 0);
  }

  public static Map<String, Object> run(ServerLevel world) {
    var probe = new TransferReviewProbe();
    probe.items(world);
    probe.slotLimits(world);
    probe.fluids();
    return Map.of("checks", probe.checks, "failures", probe.failures, "cases", probe.cases);
  }

  private void items(ServerLevel world) {
    var origin = new BlockPos(400, 120, 400);
    world.setBlock(origin, Blocks.CHEST.defaultBlockState(), 3);
    world.setBlock(origin.east(2), Blocks.CHEST.defaultBlockState(), 3);
    var source = (ChestBlockEntity) world.getBlockEntity(origin);
    var target = (ChestBlockEntity) world.getBlockEntity(origin.east(2));
    var adapter = new NativeItemTransfers();
    var from = adapter.wrapInventory(source, Direction.NORTH);
    var to = adapter.wrapInventory(target, Direction.SOUTH);
    for (boolean filtered : List.of(false, true))
      for (int space : List.of(0, 1, 7, 64)) {
        source.clearContent();
        target.clearContent();
        source.setItem(0, new ItemStack(Items.IRON_INGOT, 64));
        for (int slot = 0; slot < target.getContainerSize(); slot++)
          target.setItem(slot, new ItemStack(Items.IRON_INGOT, 64));
        target.setItem(
            0, space == 64 ? ItemStack.EMPTY : new ItemStack(Items.IRON_INGOT, 64 - space));
        int moved =
            filtered
                ? adapter.transfer(from, to, 64, stack -> stack.is(Items.IRON_INGOT))
                : adapter.transfer(from, to, 64);
        check(
            moved == space,
            "item transfer reports accepted amount " + space + " filtered=" + filtered);
        check(
            source.getItem(0).getCount() == 64 - space,
            "source retains unaccepted items " + space + " filtered=" + filtered);
        check(target.getItem(0).getCount() == 64, "target fills only its available space " + space);
        cases.add(
            Map.of(
                "kind",
                "partial_item_transfer",
                "filtered",
                filtered,
                "destinationSpace",
                space,
                "moved",
                moved,
                "sourceRemaining",
                source.getItem(0).getCount()));
      }
    source.clearContent();
    source.setItem(0, new ItemStack(Items.COAL, 16));
    source.setItem(1, new ItemStack(Items.IRON_INGOT, 16));
    target.setItem(0, new ItemStack(Items.IRON_INGOT, 63));
    int moved = adapter.transfer(from, to, 64);
    check(
        moved == 1 && source.getItem(0).getCount() == 16 && source.getItem(1).getCount() == 15,
        "blocked first source item does not hide a compatible later slot");
    check(
        adapter.transfer(from, to, 0) == 0 && adapter.transfer(from, to, -1) == 0,
        "nonpositive transfer requests make no changes");
    check(adapter.transfer(from, from, 64) == 0, "same inventory transfer is a no-op");
    for (boolean filtered : List.of(false, true)) {
      source.clearContent();
      target.clearContent();
      source.setItem(0, new ItemStack(Items.IRON_INGOT, 7));
      source.setItem(1, new ItemStack(Items.IRON_INGOT, 9));
      source.setItem(2, new ItemStack(Items.COAL, 12));
      int batch =
          filtered
              ? adapter.transfer(from, to, 20, stack -> stack.is(Items.IRON_INGOT))
              : adapter.transfer(from, to, 20);
      check(batch == (filtered ? 16 : 20), "multi-slot transfer respects total maximum and filter");
      check(
          source.getItem(0).isEmpty()
              && source.getItem(1).isEmpty()
              && source.getItem(2).getCount() == (filtered ? 12 : 8),
          "multi-slot transfer retains every unaccepted source item");
    }
    world.setBlock(origin.below(), Blocks.FURNACE.defaultBlockState(), 3);
    var below = adapter.getAdjacentInventory(source, Direction.DOWN);
    check(
        below != null && below.getSide() == Direction.DOWN,
        "adjacent inventory retains its location direction");
    check(
        adapter.deposit(below, new ItemStack(Items.IRON_ORE), false) == 1,
        "neighbor below accepts ore through its opposite upper face");
    var lowerFace = adapter.wrapInventory(world.getBlockEntity(origin.below()), Direction.DOWN);
    check(
        adapter.deposit(lowerFace, new ItemStack(Items.IRON_ORE), false) == 0,
        "unexposed furnace slots are inaccessible from below");
  }

  private void slotLimits(ServerLevel world) {
    record LimitedSlot(
        net.minecraft.world.level.block.Block block,
        String slot,
        net.minecraft.world.item.Item item) {}
    var fixtures =
        List.of(
            new LimitedSlot(
                ic2ce.neoforge.registry.BlockDefinitions.ELECTRIC_HEAT_GENERATOR,
                "CoilSlot",
                ItemDefinitions.COIL),
            new LimitedSlot(
                ic2ce.neoforge.registry.BlockDefinitions.ELECTRIC_KINETIC_GENERATOR,
                "slotMotor",
                ItemDefinitions.ELECTRIC_MOTOR),
            new LimitedSlot(
                ic2ce.neoforge.registry.BlockDefinitions.LIQUID_HEAT_EXCHANGER,
                "heatExchanger",
                ItemDefinitions.HEAT_CONDUCTOR));
    int next = 0;
    var adapter = new NativeItemTransfers();
    for (var fixture : fixtures) {
      var origin = new BlockPos(430 + (next++) * 16, 120, 430);
      world.setBlock(origin, fixture.block.defaultBlockState(), 3);
      var machine = (ic2ce.neoforge.block.InventoryBlockEntity) world.getBlockEntity(origin);
      var components = machine.getInventorySlot(fixture.slot);
      var target = adapter.wrapInventory(machine, Direction.UP);
      var resource = ItemResource.of(new ItemStack(fixture.item));
      var capability = new TransactionalItemAdapter(machine, Direction.UP);
      int index = 0;
      while (index < machine.getContainerSize()
          && !machine.canPlaceItem(index, new ItemStack(fixture.item))) index++;
      check(
          index < machine.getContainerSize()
              && components.getStackSizeLimit() == 1
              && machine.getMaxStackSize() == 64
              && fixture.item.getDefaultInstance().getMaxStackSize() == 64,
          "fixture has a genuinely limited component slot in a larger inventory: " + fixture.slot);
      check(
          capability.getCapacityAsLong(index, resource) == 1,
          "native capability reports the component slot's one-item capacity: " + fixture.slot);
      for (boolean commit : List.of(false, true)) {
        components.clear();
        int inserted;
        try (var root = Transaction.openRoot()) {
          inserted = capability.insert(index, resource, 64, root);
          if (commit) root.commit();
        }
        check(
            inserted == 1,
            "native capability accepts one component rather than sixty-four: "
                + fixture.slot
                + " commit="
                + commit);
        check(
            components.get().getCount() == (commit ? 1 : 0),
            "component commit/rollback respects its actual slot limit: " + fixture.slot);
        cases.add(
            Map.of(
                "kind",
                "placed_component_slot_capacity",
                "slot",
                fixture.slot,
                "commit",
                commit,
                "accepted",
                inserted,
                "stored",
                components.get().getCount(),
                "limit",
                1));
      }
      for (int i = 0; i < components.size(); i++) components.put(i, new ItemStack(fixture.item));
      components.clear(0);
      int deposited = adapter.deposit(target, new ItemStack(fixture.item, 64), false);
      check(
          deposited == 1 && components.get().getCount() == 1,
          "adjacent item adapter fills only one available component slot: " + fixture.slot);
      cases.add(
          Map.of(
              "kind",
              "placed_component_slot_deposit",
              "slot",
              fixture.slot,
              "accepted",
              deposited,
              "stored",
              components.get().getCount(),
              "limit",
              1));
      components.clear(0);
      var sourcePos = origin.north(2);
      world.setBlock(sourcePos, Blocks.CHEST.defaultBlockState(), 3);
      var source = (ChestBlockEntity) world.getBlockEntity(sourcePos);
      source.setItem(0, new ItemStack(fixture.item, 64));
      var from = adapter.wrapInventory(source, Direction.UP);
      int transferred = adapter.transfer(from, target, 64);
      check(
          transferred == 1
              && source.getItem(0).getCount() == 63
              && components.get().getCount() == 1,
          "component transfer preserves all sixty-four source items: " + fixture.slot);
      cases.add(
          Map.of(
              "kind",
              "placed_component_slot_transfer",
              "slot",
              fixture.slot,
              "accepted",
              transferred,
              "sourceRemaining",
              source.getItem(0).getCount(),
              "stored",
              components.get().getCount()));
      components.clear();
      source.setItem(0, new ItemStack(fixture.item, 64));
      transferred = 0;
      for (int step = 0; step < components.size() + 1; step++) {
        int moved = adapter.transfer(from, target, 64);
        if (moved == 0) break;
        transferred += moved;
      }
      cases.add(
          Map.of(
              "kind",
              "repeated_component_slot_transfer",
              "slot",
              fixture.slot,
              "accepted",
              transferred,
              "sourceRemaining",
              source.getItem(0).getCount(),
              "stored",
              java.util.stream.IntStream.range(0, components.size())
                  .map(i -> components.get(i).getCount())
                  .sum()));
      check(
          transferred == components.size()
              && source.getItem(0).getCount() == 64 - components.size(),
          "multi-slot component transfer fills every one-item slot exactly once: " + fixture.slot);
      check(
          java.util.stream.IntStream.range(0, components.size())
              .allMatch(i -> components.get(i).getCount() <= 1),
          "every actual component slot remains within its limit: " + fixture.slot);
      boolean rejectedIndex = false;
      try {
        capability.getCapacityAsLong(machine.getContainerSize(), resource);
      } catch (IndexOutOfBoundsException expected) {
        rejectedIndex = true;
      }
      check(
          rejectedIndex,
          "invalid native resource-handler slot fails its required index bounds: " + fixture.slot);
    }
  }

  private void fluids() {
    var water = FluidResource.of(Fluids.WATER);
    for (boolean commit : List.of(false, true)) {
      var owned = owned(new ItemStack(ItemDefinitions.EMPTY_CELL));
      var handler = new CellFluidHandler(owned.getResource().toStack(1), owned);
      int filled;
      try (var root = Transaction.openRoot()) {
        filled = handler.insert(0, water, 1000, root);
        if (commit) root.commit();
      }
      check(filled == 1000, "classic cell accepts an entire bucket commit=" + commit);
      check(
          owned.getResource().getItem()
              == (commit ? ItemDefinitions.WATER_CELL : ItemDefinitions.EMPTY_CELL),
          "classic fill changes or restores its owned item type commit=" + commit);
      check(
          handler.getAmountAsLong(0) == (commit ? 1000 : 0),
          "cached classic handler follows conversion/rollback commit=" + commit);
      cases.add(
          Map.of(
              "kind",
              "classic_cell_fill",
              "commit",
              commit,
              "accepted",
              filled,
              "cachedAmount",
              handler.getAmountAsLong(0)));
    }
    for (boolean commit : List.of(false, true)) {
      var owned = owned(new ItemStack(ItemDefinitions.WATER_CELL));
      var handler = new CellFluidHandler(owned.getResource().toStack(1), owned);
      int drained;
      try (var root = Transaction.openRoot()) {
        drained = handler.extract(0, water, 1000, root);
        if (commit) root.commit();
      }
      check(drained == 1000, "classic cell drains a bucket commit=" + commit);
      check(
          owned.getResource().getItem()
              == (commit ? ItemDefinitions.EMPTY_CELL : ItemDefinitions.WATER_CELL),
          "classic drain preserves container count/type commit=" + commit);
      check(owned.getAmount() == 1, "classic operation retains one container");
      check(
          handler.getAmountAsLong(0) == (commit ? 0 : 1000),
          "drain rollback restores cached fluid and item type");
    }
    var fluidItem = (Ic2FluidItem) ItemDefinitions.CF_PACK;
    var empty = new ItemStack(ItemDefinitions.CF_PACK);
    var foam = ic2ce.neoforge.registry.FluidDefinitions.CONSTRUCTION_FOAM.still;
    var fluid = FluidResource.of(foam);
    for (boolean ownedMode : List.of(false, true))
      for (boolean commit : List.of(false, true))
        for (int requested : List.of(1, 17, 500, 1000)) {
          var original = empty.copy();
          var owner = ownedMode ? owned(original) : null;
          var handler =
              new CellFluidHandler(ownedMode ? owner.getResource().toStack(1) : original, owner);
          int inserted;
          try (var root = Transaction.openRoot()) {
            inserted = handler.insert(0, fluid, requested, root);
            if (commit) root.commit();
          }
          ItemStack result = ownedMode ? owner.getResource().toStack(owner.getAmount()) : original;
          int actual = fluidItem.getFluidStack(result).getAmountMb();
          check(
              inserted == requested,
              "standard container respects request=" + requested + " owned=" + ownedMode);
          check(
              actual == (commit ? requested : 0),
              "standard container persists or fully rolls back components");
          check(
              handler.getAmountAsLong(0) == actual,
              "handler and owned item agree after transaction");
          cases.add(
              Map.of(
                  "kind",
                  "standard_fluid_fill",
                  "owned",
                  ownedMode,
                  "commit",
                  commit,
                  "requested",
                  requested,
                  "accepted",
                  inserted,
                  "retained",
                  actual));
        }
    var original = empty.copy();
    var handler = new CellFluidHandler(original, null);
    try (var root = Transaction.openRoot()) {
      check(
          handler.insert(1, fluid, 100, root) == 0 && handler.insert(0, fluid, -1, root) == 0,
          "invalid slot/quantity does not mutate fluid");
      root.commit();
    }
    check(
        fluidItem.getFluidStack(original).isEmpty(),
        "rejected fluid operations preserve empty data");
    var classic = new ItemStack(ItemDefinitions.EMPTY_CELL);
    handler = new CellFluidHandler(classic, null);
    try (var root = Transaction.openRoot()) {
      check(handler.insert(0, water, 1000, root) == 0, "type replacement requires an item owner");
      root.commit();
    }
    check(
        classic.is(ItemDefinitions.EMPTY_CELL) && classic.getCount() == 1,
        "ownerless rejected conversion retains its cell");
  }
}
