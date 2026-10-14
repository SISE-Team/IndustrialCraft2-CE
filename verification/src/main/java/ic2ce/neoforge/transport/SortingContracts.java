package ic2ce.neoforge.transport;

import ic2ce.core.util.StackTag;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native finite batches, paid fallback, malformed filter saves and bank work-budget boundaries. */
public final class SortingContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native sorting: " + label);
  }

  public static Result run(ServerLevel world) {
    return new SortingContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8680, 100, 8680);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, BlockDefinitions.SORTING_MACHINE.defaultBlockState());
    world.setBlockAndUpdate(position.east(), BlockDefinitions.IRON_STORAGE_BOX.defaultBlockState());
    world.setBlockAndUpdate(
        position.below(), BlockDefinitions.IRON_STORAGE_BOX.defaultBlockState());
    var sorter = (BatchItemSorter) world.getBlockEntity(position);
    var first =
        ((ic2ce.core.block.storage.box.TileEntityStorageBox) world.getBlockEntity(position.east()))
            .getInventorySlot("inventory");
    var fallback =
        ((ic2ce.core.block.storage.box.TileEntityStorageBox) world.getBlockEntity(position.below()))
            .getInventorySlot("inventory");
    var energy = sorter.getComponent(ElectricalBuffer.class);
    sorter.getFilterSlots(Direction.EAST)[0] = new ItemStack(Items.IRON_INGOT, 3);
    sorter.buffer.put(new ItemStack(Items.IRON_INGOT, 2));
    energy.forceAddEnergy(100);
    sorter.updateEntityServer();
    check(
        first.isEmpty()
            && fallback.isEmpty()
            && sorter.buffer.get().getCount() == 2
            && energy.getEnergy() == 100,
        "incomplete matching batch cannot leak into the default route or consume energy");
    sorter.buffer.put(new ItemStack(Items.IRON_INGOT, 5));
    sorter.updateEntityServer();
    check(
        first.get().getCount() == 3
            && fallback.isEmpty()
            && sorter.buffer.get().getCount() == 2
            && energy.getEnergy() == 40,
        "matching native route takes its exact three-item batch and pays sixty EU");
    sorter.buffer.put(new ItemStack(Items.GOLD_INGOT, 2));
    sorter.updateEntityServer();
    check(
        fallback.get().is(Items.GOLD_INGOT)
            && fallback.get().getCount() == 1
            && sorter.buffer.get().getCount() == 1
            && energy.getEnergy() == 20,
        "globally unmatched item uses one paid fallback transfer");
    var malformed = new CompoundTag();
    var list = new ListTag();
    var row = new CompoundTag();
    row.putByte("index", (byte) 255);
    StackTag.save(world.registryAccess(), new ItemStack(Items.DIAMOND), row);
    list.add(row);
    malformed.put("filters", list);
    malformed.putByte("defaultroute", (byte) 127);
    sorter.load(malformed);
    check(
        sorter.defaultRoute == Direction.DOWN && sorter.getFilterSlots(Direction.EAST)[0].isEmpty(),
        "out-of-range persisted filter is ignored and a repeated load clears prior filters");
    sorter.getFilterSlots(Direction.SOUTH)[0] = new ItemStack(Items.DIAMOND, 2);
    sorter.onNetworkEvent(null, 5);
    var saved = new CompoundTag();
    sorter.saveAdditional(saved);
    sorter.load(saved);
    check(
        sorter.defaultRoute == Direction.EAST
            && sorter.getFilterSlots(Direction.SOUTH)[0].is(Items.DIAMOND)
            && sorter.getFilterSlots(Direction.SOUTH)[0].getCount() == 2,
        "valid route and native batch filter survive a real save round trip");
    sorter.onNetworkEvent(null, Integer.MAX_VALUE);
    check(
        sorter.defaultRoute == Direction.EAST, "invalid GUI route does not corrupt routing state");
    world.removeBlock(position, false);
    world.removeBlock(position.east(), false);
    world.removeBlock(position.below(), false);
    world.setBlockAndUpdate(position, BlockDefinitions.ITEM_BUFFER.defaultBlockState());
    var banks = (DualItemBuffer) world.getBlockEntity(position);
    check(
        banks.useEnergy(25)
            && !banks.useEnergy(16)
            && banks.useEnergy(15)
            && banks.getEnergy() == 0,
        "one dual-bank upgrade turn shares its complete forty-unit virtual work budget");
    check(
        !banks.useEnergy(Double.NaN) && !banks.useEnergy(-1),
        "invalid virtual work cannot manufacture budget");
    banks.updateEntityServer();
    check(banks.getEnergy() == 40, "a new native turn replenishes the virtual budget exactly once");
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
