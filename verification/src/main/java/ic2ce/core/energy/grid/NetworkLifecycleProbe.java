package ic2ce.core.energy.grid;

import ic2ce.api.energy.tile.*;
import ic2ce.api.info.ILocatable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Exercises the actual registration queues, native callbacks and component cache. */
public final class NetworkLifecycleProbe {
  public record Result(int checks, List<String> failures) {}

  private final List<String> failures = new ArrayList<>();
  private int checks;

  private void check(boolean success, String label) {
    checks++;
    if (!success) failures.add("energy lifecycle: " + label);
  }

  public static Result run(ServerLevel world) {
    return new NetworkLifecycleProbe().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var base = new BlockPos(8192, 100, 8192);
    world.setChunkForced(base.getX() >> 4, base.getZ() >> 4, true);
    world.getChunkAt(base);
    var network = EnergyNetLocal.create(world);
    var source = new Source(world, base);
    var cable = new Cable(world, base.east());
    var sink = new Sink(world, base.east(2));
    network.addTile(source, source.position);
    network.addTile(cable, cable.position);
    network.addTile(sink, sink.position);
    check(network.getIoTile(cable.position) == cable, "pending additions visible immediately");
    tick(network);
    check(
        network.getTile(cable.position) == null && sink.received == 0,
        "one generation placement delay");
    tick(network);
    tick(network);
    double expectedLoss = EnergyNetSettings.roundLossDown ? 0 : 0.202;
    check(
        sink.received > 0
            && Math.abs(source.spent - sink.received - expectedLoss * source.spent / 32) < 1e-8,
        "real native transfer accounts for configured cable loss");
    check(sink.incoming == Direction.WEST, "sink receives direction toward supplying cable");
    check(
        source.callbacksOnOwner && sink.callbacksOnOwner && cable.connectionsOnOwner,
        "all API callbacks execute on world thread");
    check(
        network.getAdjacentConnections(cable)
            == (1 << Direction.WEST.ordinal() | 1 << Direction.EAST.ordinal()),
        "physical cable connection mask");
    int stableId = network.getGridInfos().iterator().next().id;
    var remote = new Source(world, base.south(4));
    network.addTile(remote, remote.position);
    tick(network);
    tick(network);
    tick(network);
    check(
        network.getGridInfos().stream().anyMatch(g -> g.id == stableId),
        "unaffected component preserves compiled generation");
    double before = sink.received;
    network.removeTile(cable, cable.position);
    check(network.getIoTile(cable.position) == null, "removed cable hidden immediately");
    tick(network);
    check(sink.received == before, "disabled cable suspends transfer immediately");
    tick(network);
    tick(network);
    check(sink.received == before, "disconnected topology cannot deliver EU");
    check(
        network.getAdjacentConnections(source) == 0, "neighbor connections updated after removal");
    var replacement = new Cable(world, cable.position);
    network.addTile(replacement, replacement.position);
    tick(network);
    tick(network);
    tick(network);
    check(sink.received > before, "replacement reconnects and resumes native transfer");
    var cancelled = new Cable(world, base.south(6));
    network.addTile(cancelled, cancelled.position);
    network.removeTile(cancelled, cancelled.position);
    tick(network);
    tick(network);
    check(
        network.getIoTile(cancelled.position) == null
            && network.getTile(cancelled.position) == null,
        "add-remove before publication leaves no ghost registration");
    network.addTile(source, source.position);
    tick(network);
    tick(network);
    check(
        network.getTile(source.position).getMainTile() == source,
        "duplicate registration preserves original identity");
    var competing = new Source(world, source.position);
    network.addTile(competing, competing.position);
    tick(network);
    tick(network);
    check(
        network.getTile(source.position).getMainTile() == source,
        "position conflict cannot overwrite a live tile");
    before = sink.received;
    network.removeTile(replacement, replacement.position);
    var finalCable = new Cable(world, replacement.position);
    network.addTile(finalCable, finalCable.position);
    tick(network);
    tick(network);
    tick(network);
    check(
        network.getTile(finalCable.position).getMainTile() == finalCable && sink.received > before,
        "same-tick identity replacement commits atomically");
    var branch = base.east(4).south(8);
    var shared = EnergyNetLocal.create(world);
    var supplier = new Source(world, branch);
    supplier.allSides = true;
    var east = new Cable(world, branch.east());
    var west = new Cable(world, branch.west());
    var first = new Sink(world, branch.east(2));
    var second = new Sink(world, branch.west(2));
    second.side = Direction.EAST;
    for (Location part : List.of(supplier, east, west, first, second))
      shared.addTile(part, part.position);
    for (int i = 0; i < 12; i++) tick(shared);
    check(
        first.received > 0 && second.received > 0,
        "shared source rotates between separate conductor islands");
    check(
        Math.abs(first.received - second.received) <= 32,
        "independent islands receive fair bounded service");
    shared.removeTile(east, east.position);
    tick(shared);
    tick(shared);
    tick(shared);
    check(
        shared.getNodeStats(supplier) != null,
        "statistics tolerate a detached primary endpoint while an extra endpoint survives");
    return new Result(checks, List.copyOf(failures));
  }

  private static void tick(EnergyNetLocal network) {
    network.onTickStart();
    network.onTickEnd();
  }

  private abstract static class Location implements ILocatable, IEnergyTile {
    final ServerLevel world;
    final BlockPos position;
    boolean connectionsOnOwner = true;

    Location(ServerLevel world, BlockPos position) {
      this.world = world;
      this.position = position;
    }

    @Override
    public Level getWorldObj() {
      return world;
    }

    @Override
    public BlockPos getPosition() {
      return position;
    }

    @Override
    public void onConnectionChange() {
      connectionsOnOwner &= world.getServer().isSameThread();
    }
  }

  private static final class Source extends Location implements IEnergySource {
    double spent;
    boolean callbacksOnOwner = true, allSides;

    Source(ServerLevel world, BlockPos position) {
      super(world, position);
    }

    @Override
    public double getOfferedEnergy() {
      callbacksOnOwner &= world.getServer().isSameThread();
      return 32;
    }

    @Override
    public int getSourceTier() {
      return 1;
    }

    @Override
    public void drawEnergy(double amount) {
      callbacksOnOwner &= world.getServer().isSameThread();
      spent += amount;
    }

    @Override
    public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side) {
      return allSides || side == Direction.EAST;
    }
  }

  private static final class Sink extends Location implements IEnergySink {
    double received;
    Direction incoming, side = Direction.WEST;
    boolean callbacksOnOwner = true;

    Sink(ServerLevel world, BlockPos position) {
      super(world, position);
    }

    @Override
    public double getDemandedEnergy() {
      callbacksOnOwner &= world.getServer().isSameThread();
      return 1000000;
    }

    @Override
    public int getSinkTier() {
      return 2;
    }

    @Override
    public double injectEnergy(Direction direction, double amount, double voltage) {
      callbacksOnOwner &= world.getServer().isSameThread();
      incoming = direction;
      received += amount;
      return 0;
    }

    @Override
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction side) {
      return side == this.side;
    }
  }

  private static final class Cable extends Location implements IEnergyConductor {
    Cable(ServerLevel world, BlockPos position) {
      super(world, position);
    }

    @Override
    public double getConductionLoss() {
      return 0.2;
    }

    @Override
    public double getInsulationEnergyAbsorption() {
      return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getInsulationBreakdownEnergy() {
      return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getConductorBreakdownEnergy() {
      return Double.POSITIVE_INFINITY;
    }

    @Override
    public void removeInsulation() {
      throw new AssertionError("Unexpected insulation overload");
    }

    @Override
    public void removeConductor() {
      throw new AssertionError("Unexpected conductor overload");
    }

    @Override
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction side) {
      return true;
    }

    @Override
    public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side) {
      return true;
    }
  }
}
