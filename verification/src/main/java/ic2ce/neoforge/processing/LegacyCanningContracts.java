package ic2ce.neoforge.processing;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.core.util.StackTag;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.item.armor.WearableFluidReservoir;
import ic2ce.neoforge.processing.fluid.LegacyCellCanner;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;

/**
 * Native legacy machine passes blocked outputs, exact energy boundaries, persistent fuel stages and
 * genuine foam tanks.
 */
public final class LegacyCanningContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("legacy canning: " + label);
  }

  public static Result run(ServerLevel world) {
    return new LegacyCanningContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var pos = new BlockPos(1650, 110, 1300);
    world.setBlockAndUpdate(pos, BlockDefinitions.CLASSIC_CANNER.defaultBlockState());
    var machine = (LegacyCellCanner) world.getBlockEntity(pos);
    var energy = machine.getComponent(ElectricalBuffer.class);
    machine.inputSlot.put(new ItemStack(ItemDefinitions.TIN_CAN, 10));
    machine.resInputSlot.put(new ItemStack(Items.BREAD));
    int cans = machine.getFoodValue(new ItemStack(Items.BREAD));
    check(
        cans > 0 && machine.canOperate(),
        "actual native bread recipe admits its declared container bill");
    energy.forceAddEnergy(cans * 50);
    for (int tick = 0; tick < cans * 50 - 1; tick++) MachineFixtureAccess.tick(machine);
    check(
        machine.outputSlot.isEmpty()
            && machine.resInputSlot.get().getCount() == 1
            && energy.getEnergy() == 1,
        "food resource remains owned until the final paid tick");
    check(
        machine.getGuiValue("progress") > 0 && machine.getGuiValue("progress") < 1,
        "actual food gauge has a fractional native progress value");
    MachineFixtureAccess.tick(machine);
    check(
        machine.outputSlot.get().is(ItemDefinitions.FILLED_TIN_CAN)
            && machine.outputSlot.get().getCount() == cans
            && machine.inputSlot.get().getCount() == 10 - cans
            && machine.resInputSlot.isEmpty()
            && energy.getEnergy() == 0,
        "final food tick pays one resource and the exact cans with no remaining energy");
    machine.outputSlot.put(new ItemStack(Items.BEDROCK, 64));
    machine.inputSlot.put(new ItemStack(ItemDefinitions.TIN_CAN, 10));
    machine.resInputSlot.put(new ItemStack(Items.BREAD));
    energy.forceAddEnergy(5);
    MachineFixtureAccess.tick(machine);
    check(
        energy.getEnergy() == 5
            && machine.inputSlot.get().getCount() == 10
            && machine.resInputSlot.get().getCount() == 1,
        "blocked food output cannot debit energy or either physical input");
    machine.outputSlot.clear();
    machine.inputSlot.put(new ItemStack(ItemDefinitions.EMPTY_FUEL_CAN));
    machine.resInputSlot.put(new ItemStack(ItemDefinitions.COALFUEL_CELL, 6));
    energy.forceAddEnergy(595);
    for (int tick = 0; tick < 100; tick++) MachineFixtureAccess.tick(machine);
    check(
        machine.resInputSlot.get().getCount() == 5
            && machine.outputSlot.isEmpty()
            && machine.progress == 100,
        "fuel circuit commits one resource only at each hundred paid ticks");
    var saved = new CompoundTag();
    machine.saveAdditional(saved);
    var restored = new LegacyCellCanner(pos, machine.getBlockState());
    restored.setLevel(world);
    restored.inputSlot.put(machine.inputSlot.get().copy());
    restored.resInputSlot.put(machine.resInputSlot.get().copy());
    restored.load(saved);
    check(
        restored.progress == 100 && saved.getIntOr("fuelQuality", 0) == 2548,
        "paid fuel quality and stage progress survive a native saved-state roundtrip");
    for (int tick = 100; tick < 600; tick++) MachineFixtureAccess.tick(machine);
    check(
        machine.outputSlot.get().is(ItemDefinitions.FILLED_FUEL_CAN)
            && StackTag.get(machine.outputSlot.get()).getIntOr("value", 0) == 15288
            && machine.inputSlot.isEmpty()
            && machine.resInputSlot.isEmpty(),
        "six coal fuel stages pay exactly six cells for their saved fuel value");
    machine.outputSlot.clear();
    machine.inputSlot.put(new ItemStack(ItemDefinitions.CF_PACK));
    machine.resInputSlot.put(new ItemStack(ItemDefinitions.PELLET, 2));
    energy.forceAddEnergy(100);
    for (int tick = 0; tick < 50; tick++) MachineFixtureAccess.tick(machine);
    check(
        machine.inputSlot.get().is(ItemDefinitions.CF_PACK)
            && Ic2FluidStack.get(machine.inputSlot.get()).getAmountMb() == 2000
            && machine.resInputSlot.get().getCount() == 1,
        "first foam pellet fills two real buckets while retaining the physical wearable tank");
    for (int tick = 0; tick < 50; tick++) MachineFixtureAccess.tick(machine);
    check(
        machine.outputSlot.get().is(ItemDefinitions.CF_PACK)
            && Ic2FluidStack.get(machine.outputSlot.get()).getAmountMb() == 4000
            && machine.outputSlot.get().getDamageValue() == 0
            && machine.inputSlot.isEmpty()
            && machine.resInputSlot.isEmpty(),
        "two foam pellets produce a real four-bucket reservoir without fake durability changes");
    machine.outputSlot.clear();
    var full = new ItemStack(ItemDefinitions.CF_PACK);
    ((WearableFluidReservoir) full.getItem()).filltank(full);
    machine.inputSlot.put(full);
    machine.resInputSlot.put(new ItemStack(ItemDefinitions.PELLET));
    energy.forceAddEnergy(50);
    MachineFixtureAccess.tick(machine);
    check(
        !machine.canOperate()
            && machine.resInputSlot.get().getCount() == 1
            && energy.getEnergy() == 50
            && Ic2FluidStack.get(machine.inputSlot.get()).getAmountMb() == 80000,
        "full foam reservoir refuses another pellet and preserves every physical bill");
    world.removeBlock(pos, false);
    return new Result(checks, List.copyOf(failures));
  }
}
