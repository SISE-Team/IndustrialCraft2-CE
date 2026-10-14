package ic2ce.core.energy.leg;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.NodeStats;
import ic2ce.api.energy.tile.*;
import ic2ce.core.energy.grid.*;
import ic2ce.engine.energy.PacketTransfer;
import ic2ce.engine.energy.WeightedTopology;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.damage.DamageProfiles;
import ic2ce.neoforge.explosion.NativeBlast;
import java.io.PrintStream;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Native energy API adapter over an independent indexed routing/accounting engine. */
public class EnergyCalculatorLeg implements IEnergyCalculator {
  private static final class GridData {
    Node[] nodes = new Node[0];
    EnergyPath[][] paths = new EnergyPath[0][];
    int[] activeSources = new int[0];
    double[] demand = new double[0];
    int sourceCount, sinks, calculation = -1;
    boolean hasRoutes;
    final Map<Node, List<EnergyPath>> nodePaths = new IdentityHashMap<>();
    final Set<EnergyPath> effects = Collections.newSetFromMap(new IdentityHashMap<>());
  }

  private static GridData data(Grid grid) {
    GridData state = grid.getData();
    if (state == null) {
      state = new GridData();
      grid.setData(state);
    }
    return state;
  }

  @Override
  public void handleGridChange(Grid grid) {
    rebuild(grid, data(grid));
  }

  @Override
  public boolean runSyncStep(EnergyNetLocal network) {
    boolean active = false;
    for (Tile tile : network.getSources()) {
      var source = (IEnergySource) tile.getMainTile();
      double offered = tile.isDisabled() ? 0 : source.getOfferedEnergy();
      if (offered <= 0) {
        tile.setSourceData(0, 0);
        continue;
      }
      int packets =
          source instanceof IMultiEnergySource multi && multi.sendMultipleEnergyPackets()
              ? multi.getMultipleEnergyPacketAmount()
              : 1;
      if (packets <= 0) {
        tile.setSourceData(0, 0);
        continue;
      }
      int tier = source.getSourceTier();
      if (offered <= 0 || packets <= 0 || tier < 0) tile.setSourceData(0, 0);
      else {
        tile.setSourceData(
            Math.min(offered, EnergyNet.instance.getPowerFromTier(tier) * packets), packets);
        active = true;
      }
    }
    return active;
  }

  @Override
  public boolean runSyncStep(Grid grid) {
    tick(grid, data(grid));
    return false;
  }

  @Override
  public void runAsyncStep(Grid grid) {
    throw new UnsupportedOperationException("Native energy callbacks belong to the world thread");
  }

  private static void rebuild(Grid grid, GridData state) {
    state.nodes = grid.getNodes().toArray(Node[]::new);
    int count = state.nodes.length;
    state.paths = new EnergyPath[count][];
    state.activeSources = new int[count];
    state.demand = new double[count];
    state.nodePaths.clear();
    state.effects.clear();
    state.calculation = -1;
    state.hasRoutes = false;
    var indices = new IdentityHashMap<Node, Integer>(count);
    var roles = new WeightedTopology.Role[count];
    int[] offsets = new int[count + 1];
    for (int i = 0; i < count; i++) {
      indices.put(state.nodes[i], i);
      roles[i] =
          switch (state.nodes[i].getType()) {
            case Source -> WeightedTopology.Role.SOURCE;
            case Conductor -> WeightedTopology.Role.CONDUCTOR;
            case Sink -> WeightedTopology.Role.SINK;
          };
      offsets[i + 1] = offsets[i] + state.nodes[i].getLinks().size();
    }
    int[] targets = new int[offsets[count]];
    double[] losses = new double[targets.length];
    for (int i = 0; i < count; i++) {
      int edge = offsets[i];
      for (NodeLink link : state.nodes[i].getLinks()) {
        targets[edge] = indices.get(link.getNeighbor(state.nodes[i]));
        losses[edge++] = link.getLoss();
      }
    }
    var workspace = new WeightedTopology(roles, offsets, targets, losses).workspace();
    for (int source = 0; source < count; source++) {
      if (roles[source] != WeightedTopology.Role.SOURCE) continue;
      workspace.solve(source);
      var sinks = new ArrayList<Integer>();
      for (int i = 0; i < count; i++)
        if (roles[i] == WeightedTopology.Role.SINK && Double.isFinite(workspace.lossTo(i)))
          sinks.add(i);
      sinks.sort(Comparator.comparingDouble(workspace::lossTo).thenComparingInt(Integer::intValue));
      var unique = new LinkedHashMap<IEnergyTile, EnergyPath>();
      for (int sink : sinks) {
        double loss = workspace.lossTo(sink);
        IEnergyTile tile = state.nodes[sink].getTile().getMainTile();
        EnergyPath existing = unique.get(tile);
        if (existing != null && existing.loss <= loss) continue;
        int[] route = workspace.routeTo(sink);
        var conductors = new ArrayList<Node>(Math.max(0, route.length - 2));
        for (int i = 1; i < route.length - 1; i++) conductors.add(state.nodes[route[i]]);
        var path =
            new EnergyPath(
                state.nodes[source],
                state.nodes[sink],
                conductors,
                EnergyNetSettings.roundLossDown ? Math.floor(loss) : loss);
        path.targetIndex = sink;
        unique.put(tile, path);
      }
      state.paths[source] = unique.values().toArray(EnergyPath[]::new);
      state.hasRoutes |= state.paths[source].length != 0;
      for (EnergyPath path : state.paths[source]) {
        state.nodePaths.computeIfAbsent(path.source, n -> new ArrayList<>()).add(path);
        state.nodePaths.computeIfAbsent(path.target, n -> new ArrayList<>()).add(path);
        for (Node conductor : path.conductors)
          state.nodePaths.computeIfAbsent(conductor, n -> new ArrayList<>()).add(path);
      }
    }
  }

  private static void tick(Grid grid, GridData state) {
    if (!state.hasRoutes) return;
    state.sourceCount = state.sinks = 0;
    Arrays.fill(state.demand, 0);
    int calculation = ++state.calculation;
    for (int i = 0; i < state.nodes.length; i++) {
      Node node = state.nodes[i];
      Tile tile = node.getTile();
      if (tile.isDisabled()) continue;
      if (node.getType() == NodeType.Source
          && state.paths[i] != null
          && state.paths[i].length != 0
          && tile.getAmount() > 0) state.activeSources[state.sourceCount++] = i;
      else if (node.getType() == NodeType.Sink) {
        double demand = ((IEnergySink) tile.getMainTile()).getDemandedEnergy();
        if (demand > 0) {
          state.demand[i] = demand;
          state.sinks++;
        }
      }
    }
    if (state.sourceCount == 0 || state.sinks == 0) return;
    Level world = grid.getEnergyNet().getWorld();
    RandomSource random = world.getRandom();
    int offset = state.sourceCount == 1 ? 0 : random.nextInt(state.sourceCount);
    boolean shuffle = (world.getGameTime() & 3) != 0;
    for (int i = 0; i < state.sourceCount && state.sinks != 0; i++) {
      int sourceId = state.activeSources[(offset + i) % state.sourceCount];
      Tile tile = state.nodes[sourceId].getTile();
      var source = (IEnergySource) tile.getMainTile();
      EnergyPath[] paths = state.paths[sourceId];
      int pathOffset = shuffle && paths.length > 1 ? random.nextInt(paths.length) : 0;
      double original = tile.getAmount(), remaining = original;
      int packets = tile.getPacketCount();
      double packetLimit =
          packets == 1 ? original : EnergyNet.instance.getPowerFromTier(source.getSourceTier());
      while (packets-- > 0 && remaining > 0) {
        double packet = Math.min(remaining, packetLimit), unused = packet;
        for (int j = 0; j < paths.length && unused > 0; j++)
          unused -= emit(paths[(j + pathOffset) % paths.length], unused, state, calculation);
        double spent = packet - unused;
        if (spent <= 0) break;
        remaining -= spent;
      }
      double used = original - Math.max(0, remaining);
      if (used > 0) {
        tile.setAmount(remaining);
        source.drawEnergy(used);
      }
    }
    if (!state.effects.isEmpty()) {
      applyCableEffects(state.effects, world);
      state.effects.clear();
    }
  }

  private static double emit(EnergyPath path, double offer, GridData state, int calculation) {
    Tile target = path.target.getTile();
    double demand = state.demand[path.targetIndex];
    if (target.isDisabled() || demand <= 0) return 0;
    double amount = PacketTransfer.offeredToSink(offer, path.loss, demand);
    if (amount <= 0) return 0;
    var sink = (IEnergySink) target.getMainTile();
    double rejected =
        sink.injectEnergy(
            path.targetDirection, amount, EnergyNet.instance.getTierFromPower(amount));
    var receipt = PacketTransfer.account(amount, rejected, path.loss, demand);
    if (receipt.spent() <= 0) return 0;
    if (path.lastCalcId != calculation) {
      path.lastCalcId = calculation;
      path.energySupplied = path.maxPacketConducted = 0;
    }
    path.energySupplied += receipt.accepted();
    path.maxPacketConducted = Math.max(path.maxPacketConducted, receipt.spent());
    if (receipt.spent() > path.minEffectEnergy
        || amount > EnergyNet.instance.getPowerFromTier(sink.getSinkTier()))
      state.effects.add(path);
    if (receipt.closeSink()) {
      state.demand[path.targetIndex] = 0;
      state.sinks--;
    }
    return receipt.spent();
  }

  @Override
  public NodeStats getNodeStats(Tile tile) {
    double input = 0, output = 0, packet = 0;
    for (Node node : tile.getNodes()) {
      if (node.getGrid() == null) continue;
      GridData state = node.getGrid().getData();
      if (state == null) continue;
      double amount = 0;
      for (EnergyPath path : state.nodePaths.getOrDefault(node, List.of())) {
        if (path.lastCalcId != state.calculation) continue;
        amount += path.energySupplied;
        packet = Math.max(packet, path.maxPacketConducted);
      }
      if (node.getType() != NodeType.Source) input += amount;
      if (node.getType() != NodeType.Sink) output += amount;
    }
    return new NodeStats(input, output, EnergyNet.instance.getTierFromPower(packet));
  }

  @Override
  public void dumpNodeInfo(Node node, String prefix, PrintStream console, PrintStream chat) {
    if (node.getGrid() == null) {
      chat.printf("%sendpoint not published%n", prefix);
      return;
    }
    GridData state = data(node.getGrid());
    List<EnergyPath> paths = state.nodePaths.getOrDefault(node, List.of());
    chat.printf("%s%d connected paths%n", prefix, paths.size());
    double total = 0, maximum = 0;
    for (int i = 0; i < paths.size(); i++) {
      EnergyPath path = paths.get(i);
      boolean used = path.lastCalcId == state.calculation;
      if (used) {
        total += path.energySupplied;
        maximum = Math.max(maximum, path.maxPacketConducted);
      }
      if (i < 8)
        chat.printf(
            "%s%s -> %s (%s)%n",
            prefix, path.source, path.target, used ? path.energySupplied + " EU" : "idle");
    }
    if (paths.size() > 8) chat.printf("%s... (%d more)%n", prefix, paths.size() - 8);
    chat.printf("%slast tick: %.2f EU, max packet %.2f EU%n", prefix, total, maximum);
  }

  private static void applyCableEffects(Collection<EnergyPath> paths, Level world) {
    if (!ProjectConfiguration.get().get("misc/enableEnetCableMeltdown").getBool()) return;
    Set<Tile> removed = null, stripped = null;
    Map<Tile, Double> overloaded = null;
    Map<LivingEntity, Double> shocks = null;
    for (EnergyPath path : paths) {
      double power = path.maxPacketConducted;
      boolean conductorOverload =
          power > path.minConductorBreakdownEnergy || power > path.minInsulationBreakdownEnergy;
      if (conductorOverload)
        for (Node node : path.conductors) {
          Tile tile = node.getTile();
          var cable = (IEnergyConductor) tile.getMainTile();
          if (power > cable.getConductorBreakdownEnergy()) {
            if (removed == null) removed = Collections.newSetFromMap(new IdentityHashMap<>());
            removed.add(tile);
          } else if (power > cable.getInsulationBreakdownEnergy()) {
            if (stripped == null) stripped = Collections.newSetFromMap(new IdentityHashMap<>());
            stripped.add(tile);
          }
        }
      if (power > path.minInsulationEnergyAbsorption) {
        var nearby =
            world.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                    path.minX - 1,
                    path.minY - 1,
                    path.minZ - 1,
                    path.maxX + 2,
                    path.maxY + 2,
                    path.maxZ + 2),
                EntitySelector.LIVING_ENTITY_STILL_ALIVE);
        if (!nearby.isEmpty()) {
          var local = new IdentityHashMap<LivingEntity, Integer>();
          for (Node node : path.conductors) {
            var cable = (IEnergyConductor) node.getTile().getMainTile();
            if (power <= cable.getInsulationEnergyAbsorption()) continue;
            int energy = (int) (power - cable.getInsulationEnergyAbsorption());
            for (IEnergyTile part : node.getTile().getSubTiles()) {
              BlockPos position = EnergyNet.instance.getPos(part);
              AABB area =
                  new AABB(
                      position.getX() - 1,
                      position.getY() - 1,
                      position.getZ() - 1,
                      position.getX() + 2,
                      position.getY() + 2,
                      position.getZ() + 2);
              for (LivingEntity entity : nearby)
                if (energy > local.getOrDefault(entity, Integer.MIN_VALUE)
                    && entity.getBoundingBox().intersects(area)) local.put(entity, energy);
            }
          }
          if (!local.isEmpty() && shocks == null) shocks = new IdentityHashMap<>();
          if (shocks != null)
            for (var hit : local.entrySet())
              shocks.merge(hit.getKey(), hit.getValue().doubleValue(), Double::sum);
        }
      }
      var sink = (IEnergySink) path.target.getTile().getMainTile();
      if (conductorOverload || power > EnergyNet.instance.getPowerFromTier(sink.getSinkTier())) {
        if (overloaded == null) overloaded = new IdentityHashMap<>();
        overloaded.merge(path.target.getTile(), power, Math::max);
      }
    }
    if (removed != null)
      for (Tile tile : removed) ((IEnergyConductor) tile.getMainTile()).removeConductor();
    if (stripped != null)
      for (Tile tile : stripped)
        if (removed == null || !removed.contains(tile))
          ((IEnergyConductor) tile.getMainTile()).removeInsulation();
    if (overloaded != null)
      for (var failure : overloaded.entrySet())
        explodeTile(world, failure.getKey(), failure.getValue());
    if (shocks != null)
      for (var hit : shocks.entrySet()) {
        int damage = (int) Math.ceil(hit.getValue() / 64);
        if (hit.getKey().isAlive() && damage > 0)
          hit.getKey().hurt(DamageProfiles.electricity(), damage);
      }
  }

  private static void explodeTile(Level world, Tile tile, double packet) {
    if (!ProjectConfiguration.get().get("misc/enableEnetExplosions").getBool()) return;
    int tier = EnergyNet.instance.getTierFromPower(packet);
    var sink = (IEnergySink) tile.getMainTile();
    for (IEnergyTile part : tile.getSubTiles()) {
      BlockPos position = EnergyNet.instance.getPos(part);
      var actual = world.getBlockEntity(position);
      if (sink instanceof IOverloadHandler handler && handler.onOverload(tier)
          || actual instanceof IOverloadHandler actualHandler && actualHandler.onOverload(tier))
        continue;
      float power = 2.5F;
      IExplosionPowerOverride override =
          sink instanceof IExplosionPowerOverride declared
              ? declared
              : actual instanceof IExplosionPowerOverride declared ? declared : null;
      if (override != null) {
        if (!override.shouldExplode()) continue;
        power = override.getExplosionPower(tier, power);
      }
      world.removeBlock(position, false);
      new NativeBlast(
              world,
              null,
              position.getX() + .5,
              position.getY() + .5,
              position.getZ() + .5,
              power,
              .75F,
              NativeBlast.Type.Electrical)
          .doExplosion();
    }
  }
}
