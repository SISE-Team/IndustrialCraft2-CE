package ic2ce.verification;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.*;
import ic2ce.api.info.ILocatable;
import ic2ce.core.energy.grid.*;
import ic2ce.core.energy.leg.EnergyCalculatorLeg;
import ic2ce.neoforge.config.ProjectConfiguration;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Production effect calculation with instrumented API tiles and native living entities. */
final class EnergyEffectsRegression {
  record Result(int checks, List<String> failures) {}

  private final ServerLevel world;
  private final List<String> failures = new ArrayList<>();
  private int checks, ordinal;
  private final Constructor<Tile> tileConstructor;
  private final Constructor<NodeLink> linkConstructor;
  private final Constructor<?> pathConstructor;
  private final Field packet;
  private final Method apply;
  private final EnergyNetLocal network;

  static void prepare(ServerLevel world) {
    for (int i = 0; i < 33; i++) {
      var pos = new BlockPos(4096 + i * 16, 100, 4096);
      world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
      world.getChunkAt(pos);
    }
  }

  static Result run(ServerLevel world) {
    try {
      return new EnergyEffectsRegression(world).run();
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  private EnergyEffectsRegression(ServerLevel world) throws ReflectiveOperationException {
    this.world = world;
    network = EnergyNetLocal.create(world);
    tileConstructor =
        Tile.class.getDeclaredConstructor(EnergyNetLocal.class, IEnergyTile.class, List.class);
    linkConstructor = NodeLink.class.getDeclaredConstructor(Node.class, Node.class, double.class);
    Class<?> pathType = Class.forName("ic2ce.core.energy.leg.EnergyPath");
    pathConstructor =
        pathType.getDeclaredConstructor(Node.class, Node.class, List.class, double.class);
    packet = pathType.getDeclaredField("maxPacketConducted");
    apply =
        EnergyCalculatorLeg.class.getDeclaredMethod(
            "applyCableEffects", Collection.class, Level.class);
    for (var accessible : List.of(tileConstructor, linkConstructor, pathConstructor, packet, apply))
      accessible.setAccessible(true);
  }

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("energy effects: " + label);
  }

  private Node node(IEnergyTile tile) throws ReflectiveOperationException {
    return tileConstructor.newInstance(network, tile, List.of(tile)).getNodes().iterator().next();
  }

  private void connect(Node first, Node second) throws ReflectiveOperationException {
    var link = linkConstructor.newInstance(first, second, 0.0);
    first.getLinks().add(link);
    second.getLinks().add(link);
  }

  private Object path(Node source, Node sink, List<Node> conductors, double power)
      throws ReflectiveOperationException {
    var path = pathConstructor.newInstance(source, sink, conductors, 0.0);
    packet.setDouble(path, power);
    return path;
  }

  private Result run() throws ReflectiveOperationException {
    boolean meltdown = ProjectConfiguration.get().get("misc/enableEnetCableMeltdown").getBool();
    boolean explosions = ProjectConfiguration.get().get("misc/enableEnetExplosions").getBool();
    try {
      for (boolean enabled : List.of(false, true))
        for (boolean sinkEffects : List.of(false, true)) {
          ProjectConfiguration.get().set("misc/enableEnetCableMeltdown", enabled);
          ProjectConfiguration.get().set("misc/enableEnetExplosions", sinkEffects);
          for (double power : new double[] {0, 32, 64, 65, 128, 129, 256, 257})
            scenario(power, enabled, sinkEffects, false);
        }
      ProjectConfiguration.get().set("misc/enableEnetCableMeltdown", true);
      ProjectConfiguration.get().set("misc/enableEnetExplosions", true);
      scenario(257, true, true, true);
    } finally {
      ProjectConfiguration.get().set("misc/enableEnetCableMeltdown", meltdown);
      ProjectConfiguration.get().set("misc/enableEnetExplosions", explosions);
    }
    return new Result(checks, failures);
  }

  private void scenario(double power, boolean enabled, boolean sinkEffects, boolean competingPaths)
      throws ReflectiveOperationException {
    BlockPos start = new BlockPos(4096 + ordinal++ * 16, 100, 4096);
    world.setChunkForced(start.getX() >> 4, start.getZ() >> 4, true);
    world.getChunkAt(start);
    var source = new Source(start);
    var conductor = new Conductor(start.east());
    var conductor2 = new Conductor(start.east(2));
    var sink = new Sink(start.east(3));
    var sourceNode = node(source);
    var first = node(conductor);
    var second = node(conductor2);
    var sinkNode = node(sink);
    connect(sourceNode, first);
    connect(first, second);
    connect(second, sinkNode);
    var paths = new ArrayList<Object>();
    paths.add(path(sourceNode, sinkNode, List.of(first, second), power));
    if (competingPaths) paths.add(path(sourceNode, sinkNode, List.of(first, second), 129));
    var near = EntityType.COW.create(world, EntitySpawnReason.COMMAND);
    var far = EntityType.COW.create(world, EntitySpawnReason.COMMAND);
    near.setPos(start.getX() + 1.5, start.getY(), start.getZ() + .5);
    near.setNoAi(true);
    far.setPos(start.getX() + 7.5, start.getY(), start.getZ() + .5);
    far.setNoAi(true);
    check(world.addFreshEntity(near), "add near native actor");
    check(world.addFreshEntity(far), "add far native actor");
    check(
        world
            .getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class,
                new net.minecraft.world.phys.AABB(
                    start.getX(),
                    start.getY() - 1,
                    start.getZ() - 1,
                    start.getX() + 4,
                    start.getY() + 2,
                    start.getZ() + 2))
            .contains(near),
        "near actor visible in native spatial query");
    float health = near.getHealth(), farHealth = far.getHealth();
    apply.invoke(null, paths, world);
    String label = power + "/" + enabled + "/" + sinkEffects + "/" + competingPaths;
    int removed = enabled && power > 256 ? 1 : 0;
    int stripped = enabled && power > 128 && power <= 256 ? 1 : 0;
    check(
        conductor.removed == removed && conductor2.removed == removed,
        "conductor threshold/dedup " + label);
    check(
        conductor.stripped == stripped && conductor2.stripped == stripped,
        "strip threshold/remove precedence " + label);
    boolean overloaded = enabled && sinkEffects && power > 32;
    check(sink.overloads == (overloaded ? 1 : 0), "sink callback count " + label);
    if (overloaded)
      check(
          sink.lastTier == EnergyNet.instance.getTierFromPower(power),
          "maximum packet tier " + label);
    double shock = enabled ? Math.max(0, power - 64) + (competingPaths ? 65 : 0) : 0;
    float expectedHealth = Math.max(0, health - (float) Math.ceil(shock / 64));
    check(
        near.getHealth() == expectedHealth,
        "native shock, local max and path sum "
            + label
            + " expected="
            + expectedHealth
            + " actual="
            + near.getHealth());
    check(far.getHealth() == farHealth, "outside shock bounds " + label);
    near.discard();
    far.discard();
  }

  private abstract class Located implements IEnergyTile, ILocatable {
    final BlockPos pos;

    Located(BlockPos pos) {
      this.pos = pos;
    }

    public BlockPos getPosition() {
      return pos;
    }

    public Level getWorldObj() {
      return world;
    }
  }

  private final class Source extends Located implements IEnergySource {
    Source(BlockPos pos) {
      super(pos);
    }

    public double getOfferedEnergy() {
      return 1024;
    }

    public void drawEnergy(double amount) {}

    public int getSourceTier() {
      return 3;
    }

    public boolean emitsEnergyTo(IEnergyAcceptor acceptor, Direction side) {
      return true;
    }
  }

  private final class Sink extends Located implements IEnergySink, IOverloadHandler {
    int overloads, lastTier;

    Sink(BlockPos pos) {
      super(pos);
    }

    public double getDemandedEnergy() {
      return 1024;
    }

    public int getSinkTier() {
      return 1;
    }

    public double injectEnergy(Direction side, double amount, double voltage) {
      return 0;
    }

    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction side) {
      return true;
    }

    public boolean onOverload(int tier) {
      overloads++;
      lastTier = tier;
      return true;
    }
  }

  private final class Conductor extends Located implements IEnergyConductor {
    int removed, stripped;

    Conductor(BlockPos pos) {
      super(pos);
    }

    public double getConductionLoss() {
      return 0;
    }

    public double getInsulationEnergyAbsorption() {
      return 64;
    }

    public double getInsulationBreakdownEnergy() {
      return 128;
    }

    public double getConductorBreakdownEnergy() {
      return 256;
    }

    public void removeInsulation() {
      stripped++;
    }

    public void removeConductor() {
      removed++;
    }

    public boolean emitsEnergyTo(IEnergyAcceptor acceptor, Direction side) {
      return true;
    }

    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction side) {
      return true;
    }
  }
}
