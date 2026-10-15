package ic2ce.neoforge.component;

import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.crafting.CraftingBlockEntities;
import ic2ce.neoforge.crafting.CraftingBlocks;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;

/**
 * Addon component executes actual vanilla smelting and conserves paid energy through pause/reload
 * boundaries.
 */
public final class ProcessingComponentContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("addon processing: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ProcessingComponentContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var parent =
        new InventoryBlockEntity(
            CraftingBlockEntities.INDUSTRIAL_WORKBENCH,
            new BlockPos(1640, 110, 1310),
            CraftingBlocks.INDUSTRIAL_WORKBENCH.defaultBlockState()) {
          final ElectricalBuffer ledger = addComponent(ElectricalBuffer.asBasicSink(this, 300, 1));
          final AddonProcessor cycle = addComponent(AddonProcessor.asFurnace(this, 3, 100, 1, 0));
        };
    parent.setLevel(world);
    var energy = parent.ledger;
    var process = parent.cycle;
    process.setOverclockRates();
    check(
        process.energyConsume == 3
            && process.operationDuration == 100
            && process.operationsPerTick == 1,
        "no-upgrade component initializes explicit energy, duration and batch defaults");
    var input = parent.getInventorySlot("input");
    var output = parent.getInventorySlot("output");
    input.put(new ItemStack(Items.RAW_IRON, 2));
    energy.forceAddEnergy(297);
    for (int tick = 0; tick < 99; tick++) process.onWorldTick();
    check(
        process.getProgress() == 99
            && energy.getEnergy() == 0
            && input.get().getCount() == 2
            && output.isEmpty(),
        "actual smelting remains uncommitted until the final three-EU bill is paid");
    check(
        process.getProgressRatio() == .99,
        "actual addon progress is fractional rather than integer division");
    process.onWorldTick();
    check(
        process.getProgress() == 99 && input.get().getCount() == 2,
        "unpaid native tick preserves paused progress and both inputs");
    var saved = process.writeToNbt();
    process.readFromNbt(saved);
    check(
        process.getProgress() == 99 && saved.getIntOr("progress", 0) == 99,
        "component persistence uses the actual lifecycle NBT methods");
    energy.forceAddEnergy(3);
    process.onWorldTick();
    check(
        process.getProgress() == 0
            && input.get().getCount() == 1
            && output.get().is(Items.IRON_INGOT)
            && output.get().getCount() == 1
            && energy.getEnergy() == 0,
        "actual dynamic smelting recipe commits one output and one raw input after its exact last"
            + " payment");
    output.put(new ItemStack(Items.BEDROCK, 64));
    energy.forceAddEnergy(30);
    process.onWorldTick();
    check(
        energy.getEnergy() == 30 && input.get().getCount() == 1 && process.getProgress() == 0,
        "blocked output prevents progress and energy debit");
    var corrupt = new CompoundTag();
    corrupt.putInt("progress", Integer.MAX_VALUE);
    process.readFromNbt(corrupt);
    check(
        process.getProgress() == 99,
        "malformed addon saved progress is bounded to the actual cycle");
    return new Result(checks, List.copyOf(failures));
  }
}
