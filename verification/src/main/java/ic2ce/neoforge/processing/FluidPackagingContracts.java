package ic2ce.neoforge.processing;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.processing.fluid.LiquidPackager;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;

/**
 * Real vanilla fluid containers exercise last-tick bills, incompatible tank refusal and stale
 * reservation protection.
 */
public final class FluidPackagingContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native fluid packaging: " + label);
  }

  public static Result run(ServerLevel world) {
    return new FluidPackagingContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var pos = new BlockPos(1660, 110, 1300);
    world.setBlockAndUpdate(pos, BlockDefinitions.FLUID_BOTTLER.defaultBlockState());
    var machine = (LiquidPackager) world.getBlockEntity(pos);
    var energy = machine.getComponent(ElectricalBuffer.class);
    machine.setOverclockRates();
    machine.drainInputSlot.put(new ItemStack(Items.WATER_BUCKET));
    energy.forceAddEnergy(200);
    for (int tick = 0; tick < 99; tick++) MachineFixtureAccess.tick(machine);
    check(
        machine.outputSlot.isEmpty()
            && machine.drainInputSlot.get().is(Items.WATER_BUCKET)
            && machine.fluidTank.isEmpty()
            && energy.getEnergy() == 2,
        "actual draining owns one physical bucket until the final paid processing tick");
    MachineFixtureAccess.tick(machine);
    check(
        machine.drainInputSlot.isEmpty()
            && machine.outputSlot.get().is(Items.BUCKET)
            && machine.fluidTank.getFluidAmount() == 1000
            && energy.getEnergy() == 0,
        "last draining tick pays exactly one bucket and one thousand actual millibuckets");
    machine.outputSlot.clear();
    machine.fillInputSlot.put(new ItemStack(Items.BUCKET, 2));
    energy.forceAddEnergy(200);
    for (int tick = 0; tick < 100; tick++) MachineFixtureAccess.tick(machine);
    check(
        machine.fillInputSlot.get().getCount() == 1
            && machine.outputSlot.get().is(Items.WATER_BUCKET)
            && machine.fluidTank.isEmpty()
            && energy.getEnergy() == 0,
        "filling spends one empty bucket and exact water quantity without deleting the second"
            + " container");
    machine.outputSlot.put(new ItemStack(Items.BEDROCK, 64));
    machine.fillInputSlot.clear();
    machine.drainInputSlot.put(new ItemStack(Items.WATER_BUCKET));
    energy.forceAddEnergy(200);
    MachineFixtureAccess.tick(machine);
    check(
        machine.fluidTank.isEmpty()
            && machine.drainInputSlot.get().is(Items.WATER_BUCKET)
            && energy.getEnergy() == 200,
        "blocked output refuses both native fluid payment and tick energy");
    machine.outputSlot.clear();
    machine.fluidTank.fillMbUnchecked(Ic2FluidStack.create(Fluids.LAVA, 1000), false);
    check(
        machine.getRecipeResult() == null && machine.drainInputSlot.get().is(Items.WATER_BUCKET),
        "actual incompatible fluid identities cannot be mixed by container draining");
    machine.fluidTank.drainMbUnchecked(Integer.MAX_VALUE, false);
    var stale = machine.getRecipeResult();
    machine.drainInputSlot.put(new ItemStack(Items.LAVA_BUCKET));
    boolean rejected = false;
    try {
      machine.operateOnce(stale, MachineFixtureAccess.output(machine, stale.getOutput()));
    } catch (IllegalStateException expected) {
      rejected = true;
    }
    check(
        rejected
            && machine.fluidTank.isEmpty()
            && machine.drainInputSlot.get().is(Items.LAVA_BUCKET)
            && machine.outputSlot.isEmpty(),
        "stale container recipe cannot consume a changed physical owner");
    machine.drainInputSlot.put(new ItemStack(Items.WATER_BUCKET));
    var changedTank = machine.getRecipeResult();
    machine.fluidTank.fillMbUnchecked(Ic2FluidStack.create(Fluids.WATER, 1000), false);
    rejected = false;
    try {
      machine.operateOnce(
          changedTank, MachineFixtureAccess.output(machine, changedTank.getOutput()));
    } catch (IllegalStateException expected) {
      rejected = true;
    }
    check(
        rejected
            && machine.fluidTank.getFluidAmount() == 1000
            && machine.drainInputSlot.get().is(Items.WATER_BUCKET)
            && machine.outputSlot.isEmpty(),
        "changed reservoir invalidates the old native transaction without duplicate output");
    machine.fluidTank.fillMbUnchecked(Ic2FluidStack.create(Fluids.WATER, 7000), false);
    check(
        machine.getRecipeResult() == null && machine.fluidTank.getFluidAmount() == 8000,
        "full native reservoir cannot debit an incoming bucket");
    machine.fluidTank.drainMbUnchecked(Integer.MAX_VALUE, false);
    machine.outputSlot.clear();
    machine.drainInputSlot.put(new ItemStack(Items.WATER_BUCKET));
    machine.progress = 0;
    energy.forceAddEnergy(200);
    for (int tick = 0; tick < 50; tick++) MachineFixtureAccess.tick(machine);
    check(machine.progress == 50, "actual native packaging persists a half-complete paid job");
    var saved = new net.minecraft.nbt.CompoundTag();
    machine.saveAdditional(saved);
    var restored = new LiquidPackager(pos, machine.getBlockState());
    restored.setLevel(world);
    restored.load(saved);
    restored.setOverclockRates();
    check(
        restored.progress == 50 && restored.getRecipeResult() != null && restored.progress == 50,
        "same physical container and fluid job preserve paid native progress after save/restore");
    machine.drainInputSlot.put(new ItemStack(Items.LAVA_BUCKET));
    MachineFixtureAccess.tick(machine);
    check(
        machine.progress == 1
            && machine.drainInputSlot.get().is(Items.LAVA_BUCKET)
            && machine.outputSlot.isEmpty()
            && machine.fluidTank.isEmpty(),
        "changed source fluid starts a new paid job rather than inheriting another bucket's"
            + " progress");
    world.removeBlock(pos, false);
    return new Result(checks, List.copyOf(failures));
  }
}
