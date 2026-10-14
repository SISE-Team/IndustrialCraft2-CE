package ic2ce.neoforge.transport;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.core.block.storage.box.TileEntityStorageBox;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.storage.StationaryFluidStore;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;

/**
 * Real partial-capacity receivers, sparse fairness, priority persistence and paid metered
 * transport.
 */
public final class TransportContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native transport: " + label);
  }

  public static Result run(ServerLevel world) {
    return new TransportContracts().exercise(world);
  }

  private static <T> T place(ServerLevel world, BlockPos position, Block block, Class<T> type) {
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, block.defaultBlockState());
    return type.cast(world.getBlockEntity(position));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8460, 100, 8460);
    var weighted =
        place(
            world,
            position,
            BlockDefinitions.WEIGHTED_FLUID_DISTRIBUTOR,
            PriorityFluidRouter.class);
    weighted.onNetworkEvent(null, Direction.EAST.get3DDataValue());
    check(
        weighted.getPriority().equals(List.of(Direction.EAST)),
        "event direction five remains east instead of bit-mask conversion to west");
    var saved = new CompoundTag();
    saved.putIntArray(
        "priority",
        new int[] {5, 5, weighted.getFacing().get3DDataValue(), Integer.MAX_VALUE, -1, 3});
    weighted.load(saved);
    weighted.load(saved);
    check(
        weighted.getPriority().equals(List.of(Direction.EAST, Direction.SOUTH)),
        "repeated native load replaces duplicate/corrupt/input-face priorities");
    weighted.priority =
        (List)
            new ArrayList<>(
                List.of(
                    "invalid",
                    Direction.EAST,
                    Direction.EAST,
                    weighted.getFacing(),
                    Direction.SOUTH));
    weighted.acceptClientField("priority");
    check(
        weighted.getPriority().equals(List.of(Direction.EAST, Direction.SOUTH)),
        "client field validation removes non-direction values before routing or persistence");
    weighted.saveAdditional(saved);
    weighted.getPriority().clear();
    weighted.saveAdditional(saved);
    check(
        saved.getIntArray("priority").orElseThrow().length == 0,
        "empty configuration overwrites a stale saved output order");
    world.removeBlock(position, false);
    var fair = place(world, position, BlockDefinitions.FLUID_DISTRIBUTOR, FairFluidRouter.class);
    var east = place(world, position.east(), BlockDefinitions.TANK, StationaryFluidStore.class);
    var south = place(world, position.south(), BlockDefinitions.TANK, StationaryFluidStore.class);
    for (int cycle = 0; cycle < 6; cycle++) {
      fair.fluidTank.fillMbUnchecked(Ic2FluidStack.create(Fluids.WATER, 1), false);
      fair.updateEntityServer();
    }
    check(
        east.getFluidTank().getFluidAmount() == 3
            && south.getFluidTank().getFluidAmount() == 3
            && fair.fluidTank.isEmpty(),
        "two sparse real outlets share six one-millibucket arrivals equally");
    east.getFluidTank()
        .fillMbUnchecked(
            Ic2FluidStack.create(Fluids.WATER, east.getFluidTank().getCapacity() - 40), false);
    fair.fluidTank.fillMbUnchecked(Ic2FluidStack.create(Fluids.WATER, 1000), false);
    fair.updateEntityServer();
    check(
        east.getFluidTank().getFluidAmount() == east.getFluidTank().getCapacity()
            && south.getFluidTank().getFluidAmount() == 966
            && fair.fluidTank.isEmpty(),
        "partial first capacity reserves thirty-seven mB and gives the other outlet nine hundred"
            + " sixty-three mB");
    fair.onNetworkEvent(null, 1);
    check(fair.getActive(), "actual GUI event one selects front consolidation");
    world.removeBlock(position, false);
    world.removeBlock(position.east(), false);
    world.removeBlock(position.south(), false);
    var regulator =
        place(world, position, BlockDefinitions.FLUID_REGULATOR, MeteredFluidOutlet.class);
    var receiverPosition = position.relative(regulator.getFacing());
    var receiver =
        place(world, receiverPosition, BlockDefinitions.TANK, StationaryFluidStore.class);
    receiver
        .getFluidTank()
        .fillMbUnchecked(
            Ic2FluidStack.create(Fluids.WATER, receiver.getFluidTank().getCapacity() - 37), false);
    regulator.getFluidTank().fillMbUnchecked(Ic2FluidStack.create(Fluids.WATER, 100), false);
    var energy = regulator.getComponent(ElectricalBuffer.class);
    energy.forceAddEnergy(10);
    regulator.onNetworkEvent(null, 100);
    regulator.onNetworkEvent(null, 1001);
    regulator.updateEntityServer();
    check(
        receiver.getFluidTank().getFluidAmount() == receiver.getFluidTank().getCapacity()
            && regulator.getFluidTank().getFluidAmount() == 63
            && energy.getEnergy() == 0,
        "partial native delivery buys exactly one ten-EU metered batch");
    energy.forceAddEnergy(10);
    regulator.updateEntityServer();
    check(
        energy.getEnergy() == 10 && regulator.getFluidTank().getFluidAmount() == 63,
        "blocked receiver refunds the reserved EU and preserves all remaining fluid");
    regulator.onNetworkEvent(null, Integer.MAX_VALUE);
    regulator.onNetworkEvent(null, Integer.MAX_VALUE);
    check(
        regulator.getoutputmb() == 1000,
        "large rate events cannot overflow the bounded positive setting");
    world.removeBlock(position, false);
    world.removeBlock(receiverPosition, false);
    var items =
        place(
            world, position, BlockDefinitions.WEIGHTED_ITEM_DISTRIBUTOR, PriorityItemRouter.class);
    var first =
        place(world, position.east(), BlockDefinitions.IRON_STORAGE_BOX, TileEntityStorageBox.class)
            .getInventorySlot("inventory");
    var second =
        place(
                world,
                position.south(),
                BlockDefinitions.IRON_STORAGE_BOX,
                TileEntityStorageBox.class)
            .getInventorySlot("inventory");
    for (int index = 0; index < first.size(); index++)
      first.put(index, new ItemStack(Items.BEDROCK, 64));
    first.put(0, new ItemStack(Items.GOLD_INGOT, 59));
    first.clear(1);
    items.getPriority().addAll(List.of(Direction.EAST, Direction.SOUTH));
    items.buffer.put(new ItemStack(Items.GOLD_INGOT, 10));
    items.updateEntityServer();
    check(
        first.get(0).getCount() == 64
            && first.get(1).is(Items.GOLD_INGOT)
            && first.get(1).getCount() == 5
            && second.isEmpty()
            && items.buffer.isEmpty(),
        "highest-priority target uses both native slots before falling back to another inventory");
    world.removeBlock(position, false);
    world.removeBlock(position.east(), false);
    world.removeBlock(position.south(), false);
    return new Result(checks, List.copyOf(failures));
  }
}
