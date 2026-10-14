package ic2ce.core.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.storage.StationaryFluidStore;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Actual placed tank/service adapters and detached menu owners preserve native world identity. */
public final class FluidServiceContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String text) {
    checks++;
    if (!ok) failures.add("native fluid/menu service: " + text);
  }

  public static Result run(ServerLevel world) {
    return new FluidServiceContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(1760, 130, 1300);
    world.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
    world.setBlockAndUpdate(position, BlockDefinitions.TANK.defaultBlockState());
    var tank = (StationaryFluidStore) world.getBlockEntity(position);
    var water = Ic2FluidStack.create(Fluids.WATER, 700);
    check(
        FluidHandler.fillMb(tank, Direction.UP, water, true) == 700
            && tank.getFluidTank().isEmpty()
            && water.getAmountMb() == 700,
        "actual native placed tank fill prediction preserves storage and request");
    check(
        FluidHandler.fillMb(tank, Direction.UP, water, false) == 700
            && tank.getFluidTank().getFluidAmount() == 700
            && FluidHandler.isFluidBlock(tank, Direction.UP),
        "actual native capability dispatcher preserves the placed tank identity and commits the"
            + " fluid");
    var result = FluidHandler.drainMb(tank, Direction.UP, 300, false);
    check(
        result.getAmountMb() == 300
            && result.hasExactFluid(Fluids.WATER)
            && tank.getFluidTank().getFluidAmount() == 400,
        "actual native drain keeps the independent fluid identity and quantity");
    check(
        FluidHandler.drainMb(
                    (net.minecraft.world.level.block.entity.BlockEntity) null,
                    Direction.UP,
                    300,
                    false)
                .isEmpty()
            && FluidHandler.fillMb(tank, Direction.UP, null, false) == 0
            && FluidHandler.drainMb(ItemStack.EMPTY, 100, false, null).isEmpty(),
        "absent native operation subjects are inert and cannot throw before admission");
    check(
        FluidHandler.isGaseous(FluidDefinitions.STEAM.still)
            && !FluidHandler.isGaseous(FluidDefinitions.CONSTRUCTION_FOAM.still)
            && FluidHandler.getAllFluids() == FluidHandler.getAllFluids(),
        "declared gas phases and stable native source catalog remain distinct");
    var actor =
        FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("d8bc92f6-1111-4111-8111-555500001717"), "FluidMenuProbe"));
    actor.setPos(position.getX() + .5, position.getY() + 1, position.getZ() + .5);
    var detached = new StationaryFluidStore(position, BlockDefinitions.TANK.defaultBlockState());
    detached.setLevel(world);
    check(
        !detached.openManagedBe(actor, InteractionHand.MAIN_HAND),
        "detached block-entity copy cannot open an actual attached native menu");
    var stack = new ItemStack(ItemDefinitions.TOOL_BOX);
    actor.setItemInHand(InteractionHand.MAIN_HAND, stack);
    var storage =
        new ic2ce.neoforge.handheld.ToolboxStorage(actor, InteractionHand.MAIN_HAND, stack, 9);
    actor.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
    check(
        !storage.openManagedItem(actor, InteractionHand.MAIN_HAND, null),
        "stale handheld storage cannot open a menu for another physical item");
    actor.getInventory().clearContent();
    world.removeBlock(position, false);
    world.setChunkForced(position.getX() >> 4, position.getZ() >> 4, false);
    return new Result(checks, List.copyOf(failures));
  }
}
