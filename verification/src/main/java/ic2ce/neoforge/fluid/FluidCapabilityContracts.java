package ic2ce.neoforge.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.TransferReviewProbe;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.processing.solid.OreGrinder;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Native dynamic tank addressing and nested rollback across independent tank journals. */
public final class FluidCapabilityContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String text) {
    checks++;
    if (!ok) failures.add("fluid capability: " + text);
  }

  public static Result run(ServerLevel world) {
    return new FluidCapabilityContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var owner = new OreGrinder(BlockPos.ZERO, BlockDefinitions.MACERATOR.defaultBlockState());
    owner.setLevel(world);
    var ports = new FluidPorts(owner);
    var first = ports.addTank("left", 1000, InventorySlot.Access.IO, InventorySlot.InvSide.TOP);
    var second = ports.addTank("right", 1000, InventorySlot.Access.IO, InventorySlot.InvSide.TOP);
    var up = new BlockSideFluidHandler(ports, owner, Direction.UP);
    var north = new BlockSideFluidHandler(ports, owner, Direction.NORTH);
    var water =
        FluidResource.of(EnvFluidHandlerNeoForge.getForgeFs(Ic2FluidStack.create(Fluids.WATER, 1)));
    check(up.size() == 2 && north.size() == 0, "actual sides expose distinct native tank counts");
    try (var root = Transaction.openRoot()) {
      check(up.insert(0, water, 400, root) == 400, "first actual tank accepts its bounded bill");
      try (var nested = Transaction.open(root)) {
        check(
            up.insert(1, water, 700, nested) == 700,
            "second tank participates in independent nested journal");
      }
      check(
          first.getFluidAmount() == 400 && second.isEmpty(),
          "nested rollback restores only its second tank without undoing the parent transfer");
      root.commit();
    }
    check(
        first.getFluidAmount() == 400 && second.isEmpty(),
        "outer commit preserves its independently admitted tank state");
    try (var root = Transaction.openRoot()) {
      up.insert(0, water, 600, root);
      try (var nested = Transaction.open(root)) {
        up.insert(1, water, 500, nested);
        nested.commit();
      }
    }
    check(
        first.getFluidAmount() == 400 && second.isEmpty(),
        "root rollback restores both root and committed-child journals");
    var lava = FluidResource.of(Fluids.LAVA);
    try (var root = Transaction.openRoot()) {
      check(
          up.extract(0, lava, 100, root) == 0 && up.extract(0, water, 100, root) == 100,
          "component-sensitive extraction refuses another fluid and pays only the requested"
              + " quantity");
      root.commit();
    }
    check(first.getFluidAmount() == 300, "committed native extraction conserves stored fluid");
    ports.changeConnectivity(first, InventorySlot.Access.IO, InventorySlot.InvSide.BOTTOM);
    check(
        up.size() == 1 && up.getAmountAsLong(0) == 0,
        "live connectivity updates do not retain an obsolete cached slot-to-tank mapping");
    var snapshot = new FluidTankInfo(127, 255, 1000, Ic2FluidStack.create(Fluids.WATER, 300));
    var copy = snapshot.getContent();
    copy.setAmountMb(1);
    check(
        snapshot.getDrainSideMask() == 63
            && snapshot.getFillSideMask() == 63
            && snapshot.getContent().getAmountMb() == 300,
        "native fluid view masks bounds and owns exported contents");
    owner.setRemoved();
    try (var root = Transaction.openRoot()) {
      check(
          up.insert(0, water, 100, root) == 0,
          "removed native owner cannot commit another capability mutation");
    }
    var transfers = TransferReviewProbe.run(world);
    checks += (int) transfers.get("checks");
    for (var failure : (List<?>) transfers.get("failures"))
      failures.add("native cell/item bridge: " + failure);
    return new Result(checks, List.copyOf(failures));
  }
}
