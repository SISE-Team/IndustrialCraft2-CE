package ic2ce.neoforge.inventory;

import ic2ce.api.energy.tile.IEnergySink;
import ic2ce.api.energy.tile.IEnergySource;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.processing.solid.OreGrinder;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native inventory callbacks, stack limits, serialization and machine role changes. */
public final class InventoryContracts {
  public record Result(int checks, List<String> failures) {}

  private final List<String> failures = new ArrayList<>();
  private int checks;

  private void check(boolean success, String label) {
    checks++;
    if (!success) failures.add("native inventory: " + label);
  }

  public static Result run(ServerLevel world) {
    return new InventoryContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var parent =
        new OreGrinder(
            new BlockPos(8196, 100, 8196), BlockDefinitions.MACERATOR.defaultBlockState());
    parent.setLevel(world);
    var filter = new InventorySlot(parent, "contract_filter", null, 1);
    check(
        !filter.canInput() && !filter.canOutput(),
        "manual filter slot normalizes absent automation policy");
    var notifications = new int[1];
    var output =
        new OutputInventory(parent, "contract_output", 3) {
          @Override
          public void onChanged() {
            notifications[0]++;
          }
        };
    output.put(0, new ItemStack(Items.IRON_INGOT, 63));
    notifications[0] = 0;
    check(
        output.canAdd(
            List.of(new ItemStack(Items.IRON_INGOT, 3), new ItemStack(Items.GOLD_INGOT, 2))),
        "valid batch has capacity");
    check(
        notifications[0] == 0 && output.get(0).getCount() == 63 && output.get(1).isEmpty(),
        "simulation produces no mutation or callback");
    check(
        output.add(List.of(new ItemStack(Items.IRON_INGOT, 3), new ItemStack(Items.GOLD_INGOT, 2)))
            == 0,
        "batch insertion consumes all requested output");
    check(
        output.get(0).getCount() == 64
            && output.get(1).getCount() == 2
            && output.get(2).getCount() == 2,
        "batch first merges then uses empty slots");
    check(notifications[0] == 1, "batch insertion emits one committed notification");
    output.clear();
    notifications[0] = 0;
    check(
        output.add(new ItemStack(Items.IRON_PICKAXE, 4)) == 1,
        "unstackable items respect the item maximum");
    check(
        output.get(0).getCount() == 1
            && output.get(1).getCount() == 1
            && output.get(2).getCount() == 1,
        "unstackable outputs occupy separate slots");
    var organized = new InventorySlot(parent, "contract_sort", InventorySlot.Access.IO, 4);
    organized.setStackSizeLimit(16);
    organized.put(0, new ItemStack(Items.IRON_INGOT, 10));
    organized.put(2, new ItemStack(Items.IRON_INGOT, 10));
    organized.organize();
    check(
        organized.get(0).getCount() == 16
            && organized.get(1).getCount() == 4
            && organized.get(2).isEmpty(),
        "compaction observes the slot maximum without losing items");
    var wide = new InventorySlot(parent, "contract_wide", InventorySlot.Access.IO, 300);
    wide.put(299, new ItemStack(Items.DIAMOND, 7));
    var saved = new CompoundTag();
    wide.writeToNbt(saved);
    wide.clear();
    wide.readFromNbt(saved);
    check(
        wide.get(299).getCount() == 7 && wide.get(43).isEmpty(),
        "slot index above 255 survives native item codec persistence");
    var consumable =
        new ConsumableInventory(parent, "contract_consume", 2) {
          @Override
          public boolean accepts(ItemStack stack) {
            return !stack.isEmpty();
          }
        };
    consumable.put(0, new ItemStack(Items.COAL, 3));
    consumable.put(1, new ItemStack(Items.COAL, 4));
    check(
        consumable.consume(5, true, false).getCount() == 5
            && consumable.get(0).getCount() == 3
            && consumable.get(1).getCount() == 4,
        "consumption simulation retains both live stacks");
    check(
        consumable.consume(5).getCount() == 5
            && consumable.get(0).isEmpty()
            && consumable.get(1).getCount() == 2,
        "committed consumption conserves quantity");
    var tool = new ItemStack(Items.IRON_PICKAXE);
    tool.setDamageValue(tool.getMaxDamage() - 5);
    consumable.put(0, tool);
    consumable.clear(1);
    check(
        consumable.damage(8, true) == 5
            && consumable.get(0).getDamageValue() == tool.getMaxDamage() - 5,
        "wear prediction has no callbacks, randomness or mutation");
    check(
        consumable.damage(8, false) == 5 && consumable.isEmpty(),
        "machine wear consumes a broken tool exactly once");
    consumable.put(0, new ItemStack(Items.LAVA_BUCKET));
    check(
        consumable.consume(1).getItem() == Items.LAVA_BUCKET
            && consumable.get(0).getItem() == Items.BUCKET,
        "consumption retains a bucket remainder");
    var energy = ElectricalBuffer.asBasicSource(parent, 100);
    energy.onLoaded();
    check(
        energy.getDelegate() instanceof IEnergySource
            && !(energy.getDelegate() instanceof IEnergySink),
        "source-only registration exposes source role");
    energy.setDirections(Set.of(Direction.WEST), Set.of(Direction.EAST));
    check(
        energy.getDelegate() instanceof IEnergySink
            && energy.getDelegate() instanceof IEnergySource,
        "role change rebuilds source/sink API identity");
    var sink = (IEnergySink) energy.getDelegate();
    check(
        sink.injectEnergy(Direction.EAST, 80, 32) == 80 && energy.getEnergy() == 0,
        "wrong-side energy injection is rejected");
    check(
        sink.injectEnergy(Direction.WEST, 120, 32) == 20 && energy.getEnergy() == 100,
        "energy injection returns capacity overflow");
    energy.forceAddEnergy(10);
    check(
        energy.addEnergy(1) == 0 && energy.getEnergy() == 110,
        "adding to an overcapacity machine preserves stored work");
    energy.onUnloaded();
    var reactor =
        new ic2ce.neoforge.reactor.NuclearCore(
            new BlockPos(8200, 100, 8200), BlockDefinitions.NUCLEAR_REACTOR.defaultBlockState());
    reactor.setLevel(world);
    var core = (ReactorInventory) reactor.getInventorySlot("reactor");
    core.put(3, new ItemStack(Items.DIAMOND));
    check(
        core.get(0, 1).getItem() == Items.DIAMOND && core.get(3).getItem() == Items.DIAMOND,
        "reactor logical write maps to a physical position exactly once");
    var coreData = new CompoundTag();
    core.writeToNbt(coreData);
    core.clear();
    core.readFromNbt(coreData);
    check(
        core.get(0, 1).getItem() == Items.DIAMOND && core.get(0, 3).isEmpty(),
        "partially expanded reactor restores physical save positions");
    return new Result(checks, List.copyOf(failures));
  }
}
