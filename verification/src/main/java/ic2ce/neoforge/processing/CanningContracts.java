package ic2ce.neoforge.processing;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.ICannerEnrichRecipeManager;
import ic2ce.api.recipe.MachineRecipe;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.neoforge.processing.fluid.MultiPolicyCanner;
import ic2ce.neoforge.processing.solid.OreGrinder;
import ic2ce.neoforge.processing.solid.PlateCutter;
import ic2ce.neoforge.processing.thermal.FuelSmelter;
import ic2ce.neoforge.processing.thermal.InductionSmelter;
import ic2ce.neoforge.recipe.input.ExactStackInput;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/** Adversarial native output capacity, container budgets, energy billing and mode persistence. */
public final class CanningContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String description) {
    checks++;
    if (!good) failures.add("native canning: " + description);
  }

  public static Result run(ServerLevel world) {
    return new CanningContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var machine =
        new OreGrinder(
            new BlockPos(8210, 100, 8210), BlockDefinitions.MACERATOR.defaultBlockState());
    machine.setLevel(world);
    machine.setChanged();
    machine.inputSlot.put(new ItemStack(Items.RAW_IRON, 2));
    machine.recipeResult = machine.getRecipeResult();
    check(machine.recipeResult != null, "loaded processing recipe selected");
    machine.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(100);
    machine.progress = (short) (machine.operationLength - 1);
    machine.outputSlot.put(new ItemStack(Items.COAL, 64));
    MachineFixtureAccess.tick(machine);
    check(
        machine.inputSlot.get().getCount() == 2
            && machine.outputSlot.get().is(Items.COAL)
            && machine.getEnergy() == 100,
        "late blocked output cannot consume input or energy");
    machine.outputSlot.clear();
    machine.progress = (short) (machine.operationLength - 1);
    machine.recipeResult = machine.getRecipeResult();
    MachineFixtureAccess.tick(machine);
    check(
        machine.inputSlot.get().getCount() == 1 && !machine.outputSlot.isEmpty(),
        "unblocked input produces exactly one output batch");
    check(
        machine.getEnergy() == 100 - machine.energyConsume,
        "completion bills one processing tick without precharging another");
    var canner =
        new MultiPolicyCanner(
            new BlockPos(8211, 100, 8211), BlockDefinitions.CANNER.defaultBlockState());
    canner.setLevel(world);
    canner.setMode(MultiPolicyCanner.Mode.EnrichLiquid);
    canner.inputSlot.put(new ItemStack(Items.COAL));
    canner.canInputSlot.put(new ItemStack(Items.BUCKET));
    canner.inputTank.fillMbUnchecked(Ic2FluidStack.create(Fluids.WATER, 1000), false);
    canner.outputTank.fillMbUnchecked(Ic2FluidStack.create(Fluids.LAVA, 8000), false);
    var recipe =
        new MachineRecipe<ICannerEnrichRecipeManager.Input, Ic2FluidStack>(
            new ICannerEnrichRecipeManager.Input(
                Ic2FluidStack.create(Fluids.WATER, 1000),
                new ExactStackInput(new ItemStack(Items.COAL))),
            Ic2FluidStack.create(Fluids.LAVA, 2000));
    @SuppressWarnings("unchecked")
    var result =
        (MachineRecipeResult<Object, Object, Object>)
            (MachineRecipeResult<?, ?, ?>)
                recipe.getResult(new ICannerEnrichRecipeManager.RawInput(null, ItemStack.EMPTY));
    boolean overflowRejected = false;
    try {
      canner.operateOnce(result, List.of());
    } catch (IllegalStateException expected) {
      overflowRejected = true;
    }
    check(
        overflowRejected
            && canner.inputSlot.get().is(Items.COAL)
            && canner.inputTank.getFluidAmount() == 1000
            && canner.canInputSlot.get().is(Items.BUCKET),
        "one container cannot satisfy two buckets of output with a full tank");
    canner.outputTank.drainMbUnchecked(1000, false);
    canner.operateOnce(result, List.of());
    check(
        canner.inputSlot.isEmpty() && canner.inputTank.isEmpty() && canner.canInputSlot.isEmpty(),
        "committed enrichment consumes only planned inputs");
    check(
        canner.outputSlot.get().is(Items.LAVA_BUCKET)
            && canner.outputSlot.get().getCount() == 1
            && canner.outputTank.getFluidAmount() == 8000,
        "enrichment output is one real container plus exactly one bucket in the tank");
    canner.progress = 90;
    canner.setMode(MultiPolicyCanner.Mode.EmptyLiquid);
    check(
        canner.progress == 0 && canner.recipeResult == null,
        "mode changes invalidate previous recipe progress");
    var saved = new CompoundTag();
    canner.saveAdditional(saved);
    saved.putInt("mode", 999);
    canner.load(saved);
    check(
        canner.getMode() == MultiPolicyCanner.Mode.BottleSolid,
        "invalid persisted mode has a safe default");
    var induction =
        new InductionSmelter(
            new BlockPos(8212, 100, 8212), BlockDefinitions.INDUCTION_FURNACE.defaultBlockState());
    induction.setLevel(world);
    induction.inputSlotA.put(new ItemStack(Items.RAW_IRON));
    induction.inputSlotB.put(new ItemStack(Items.RAW_GOLD));
    induction.heat = 1000;
    induction.progress = 100;
    induction.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(15);
    MachineFixtureAccess.tick(induction);
    check(
        induction.progress == 100 && induction.getEnergy() == 14,
        "native induction cannot process after heater leaves less than fifteen EU");
    induction.heat = 10000;
    induction.progress = 4000;
    induction.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(100);
    MachineFixtureAccess.tick(induction);
    check(
        induction.inputSlotA.isEmpty()
            && induction.inputSlotB.isEmpty()
            && induction.outputSlotA.get().is(Items.IRON_INGOT)
            && induction.outputSlotB.get().is(Items.GOLD_INGOT),
        "native induction completion commits both distinct smelting lanes");
    var ironFurnace =
        new FuelSmelter(
            new BlockPos(8213, 100, 8213), BlockDefinitions.IRON_FURNACE.defaultBlockState());
    ironFurnace.setLevel(world);
    var malformed = new CompoundTag();
    malformed.putInt("fuel", 100);
    malformed.putInt("totalFuel", 0);
    malformed.putDouble("xp", Double.NaN);
    ironFurnace.load(malformed);
    check(
        ironFurnace.getFuelRatio() == 1
            && MachineFixtureAccess.number(ironFurnace, "xp").doubleValue() == 0,
        "native fuel/experience persistence rejects nonfinite gauges and balances");
    ironFurnace.inputSlot.put(new ItemStack(Items.RAW_IRON));
    ironFurnace.progress = 159;
    MachineFixtureAccess.tick(ironFurnace);
    check(
        ironFurnace.inputSlot.isEmpty()
            && ironFurnace.outputSlot.get().is(Items.IRON_INGOT)
            && ironFurnace.fuel == 99,
        "native combustion completion consumes one burn tick and produces one smelted item");
    var cutter =
        new PlateCutter(
            new BlockPos(8214, 100, 8214), BlockDefinitions.BLOCK_CUTTER.defaultBlockState());
    cutter.setLevel(world);
    cutter.cutterSlot.put(new ItemStack(Items.COAL));
    check(
        cutter.getRecipeResult() == null && cutter.getGuiState("isBladeTooWeak"),
        "native cutter rejects an externally inserted non-blade without a cast failure");
    return new Result(checks, List.copyOf(failures));
  }
}
