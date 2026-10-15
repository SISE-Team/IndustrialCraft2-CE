package ic2ce.neoforge.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.processing.ProcessingBlocks;
import ic2ce.neoforge.processing.solid.OreGrinder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;

/** Native conservation, request immutability, side masks, dynamic hooks and saved state. */
public final class FluidContracts {
  public record Result(int checks, List<String> failures) {}

  private final List<String> failures = new ArrayList<>();
  private int checks;

  private void check(boolean success, String label) {
    checks++;
    if (!success) failures.add("native fluids: " + label);
  }

  public static Result run(ServerLevel world) {
    return new FluidContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    Fluid water = net.minecraft.world.level.material.Fluids.WATER;
    var parent = new OreGrinder(BlockPos.ZERO, ProcessingBlocks.MACERATOR.defaultBlockState());
    parent.setLevel(world);
    var component = new FluidPorts(parent);
    var first =
        component.addTank(
            "first",
            400,
            InventorySlot.Access.IO,
            InventorySlot.InvSide.TOP,
            FluidPorts.fluidPredicate(water));
    var second =
        component.addTank(
            "second",
            600,
            InventorySlot.Access.IO,
            InventorySlot.InvSide.TOP,
            FluidPorts.fluidPredicate(water));
    var request = Ic2FluidStack.create(water, 1500);
    check(
        component.fillMb(Direction.UP, request, true) == 1000
            && request.getAmountMb() == 1500
            && first.isEmpty()
            && second.isEmpty(),
        "aggregate fill prediction leaves request and storage unchanged");
    check(
        component.fillMb(Direction.NORTH, request, false) == 0,
        "sided fill rejects unrelated direction");
    check(
        component.fillMb(Direction.UP, request, false) == 1000
            && request.getAmountMb() == 1500
            && first.getFluidAmount() == 400
            && second.getFluidAmount() == 600,
        "aggregate fill conserves capacity and request quantity");
    var drain = Ic2FluidStack.create(water, 750);
    check(
        component.drainMb(Direction.UP, drain, true) == 750
            && first.getFluidAmount() + second.getFluidAmount() == 1000
            && drain.getAmountMb() == 750,
        "aggregate drain prediction has no side effects");
    check(
        component.drainMb(Direction.UP, drain, false) == 750
            && first.isEmpty()
            && second.getFluidAmount() == 250
            && drain.getAmountMb() == 750,
        "aggregate drain conserves quantity across two tanks");
    check(
        component.drainMb(Direction.UP, 1000, false).getAmountMb() == 250 && second.isEmpty(),
        "amount-based drain clamps to actual contents");
    var save = new CompoundTag();
    first.toNbt(save);
    first.fillMb(request, false);
    first.toNbt(save);
    var restored = new Ic2FluidTank(400);
    restored.fromNbt(save);
    check(
        restored.getFluidAmount() == 400 && !save.contains("Empty"),
        "reusing an empty save tag cannot erase a later full tank");
    first.setCapacity(100);
    check(
        first.fillMb(request, false) == 0 && first.getFluidAmount() == 400,
        "capacity shrink retains existing fluid");
    var dynamic = new AtomicReference<List<FluidPorts.InternalFluidTank>>(List.of(first));
    component.addUnmanagedTankHook(dynamic::get);
    int count = 0;
    for (var ignored : component.getAllTanks()) count++;
    check(count == 2, "a dynamic alias cannot count an owned tank twice");
    second.fillMb(request, false);
    check(
        component.drainMb(Direction.UP, Ic2FluidStack.create(water, 1200), true) == 1000,
        "duplicate tank references cannot overpredict available volume");
    var remoteParent =
        new OreGrinder(BlockPos.ZERO, ProcessingBlocks.MACERATOR.defaultBlockState());
    remoteParent.setLevel(world);
    var remote = new FluidPorts(remoteParent).addTank("remote", 300);
    dynamic.set(List.of(remote));
    count = 0;
    for (var ignored : component.getAllTanks()) count++;
    check(count == 3, "dynamic hooks observe replacement tank identities");
    component.changeConnectivity(second, InventorySlot.Access.O, InventorySlot.InvSide.BOTTOM);
    check(
        !second.canFill(Direction.UP) && second.canDrain(Direction.DOWN),
        "connectivity changes update independent direction masks");
    var buckets = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUCKET, 2);
    var volume = Ic2FluidStack.create(water, 1500);
    var converted =
        ic2ce.neoforge.fluid.LiquidUtil.fillContainer(
            buckets, volume, ic2ce.api.util.FluidContainerOutputMode.EmptyFullToOutput);
    check(
        converted != null
            && converted.fluidChange.getAmountMb() == 1000
            && converted.inPlaceOutput.getCount() == 1
            && converted.extraOutput.is(net.minecraft.world.item.Items.WATER_BUCKET),
        "stacked bucket plan consumes exactly one container");
    check(
        buckets.getCount() == 2 && volume.getAmountMb() == 1500,
        "container planning retains caller item and fluid quantities");
    check(
        !ic2ce.neoforge.fluid.LiquidUtil.isFillableFluidContainer(buckets, List.of()),
        "empty fluid whitelist cannot accept arbitrary registry fluids");
    var sourceSlot =
        new ic2ce.neoforge.inventory.FluidContainerInventory(parent, "contract_liquid_in", 1);
    sourceSlot.put(
        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
    var tank = new Ic2FluidTank(2000);
    var remainder =
        new org.apache.commons.lang3.mutable.MutableObject<net.minecraft.world.item.ItemStack>();
    check(
        sourceSlot.transferToTank(tank, remainder, true)
            && tank.isEmpty()
            && sourceSlot.get().is(net.minecraft.world.item.Items.WATER_BUCKET),
        "container to tank prediction is pure");
    check(
        sourceSlot.transferToTank(tank, remainder, false)
            && tank.getFluidAmount() == 1000
            && sourceSlot.isEmpty()
            && remainder.getValue().is(net.minecraft.world.item.Items.BUCKET),
        "container to tank commit conserves the bucket and liquid");
    sourceSlot.setOpType(ic2ce.neoforge.inventory.FluidContainerInventory.OpType.Fill);
    sourceSlot.put(buckets.copy());
    check(
        sourceSlot.transferFromTank(tank, remainder, true)
            && tank.getFluidAmount() == 1000
            && sourceSlot.get().getCount() == 2,
        "tank to container prediction is pure");
    check(
        sourceSlot.transferFromTank(tank, remainder, false)
            && tank.isEmpty()
            && sourceSlot.get().getCount() == 1
            && remainder.getValue().is(net.minecraft.world.item.Items.WATER_BUCKET),
        "tank to container commit preserves remaining containers");
    var shortTank = new Ic2FluidTank(999);
    sourceSlot.setOpType(ic2ce.neoforge.inventory.FluidContainerInventory.OpType.Drain);
    sourceSlot.put(
        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
    check(
        !sourceSlot.transferToTank(shortTank, remainder, false)
            && shortTank.isEmpty()
            && sourceSlot.get().is(net.minecraft.world.item.Items.WATER_BUCKET),
        "indivisible bucket cannot be lost to insufficient tank capacity");
    return new Result(checks, List.copyOf(failures));
  }
}
