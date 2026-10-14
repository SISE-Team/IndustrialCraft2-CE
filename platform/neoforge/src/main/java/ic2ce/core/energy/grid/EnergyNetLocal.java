package ic2ce.core.energy.grid;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.NodeStats;
import ic2ce.api.energy.tile.*;
import ic2ce.engine.energy.TerminalComponents;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.io.PrintStream;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** World-thread registration transactions and reusable conductor components. */
public final class EnergyNetLocal {
  private record Change(
      IEnergyTile tile, BlockPos position, List<IEnergyTile> parts, long generation, boolean add) {}

  private record Edge(int a, int b, Direction direction, double loss) {}

  private record EdgeKey(int a, int b, Direction direction, double loss) {}

  private record GridKey(List<Integer> members, List<EdgeKey> edges) {}

  private record Plan(GridKey key, int[] members, List<Edge> edges) {}

  private final Level world;
  private final ArrayDeque<Change> changes = new ArrayDeque<>();
  private final Map<IEnergyTile, Change> additions = new IdentityHashMap<>();
  private final Map<IEnergyTile, Tile> identities = new IdentityHashMap<>();
  private final Map<BlockPos, Tile> positions = new HashMap<>();
  private final LinkedHashSet<Tile> sources = new LinkedHashSet<>();
  private final Collection<Tile> sourceView = Collections.unmodifiableCollection(sources);
  private final Set<IEnergyTile> notifications = Collections.newSetFromMap(new IdentityHashMap<>());
  private Map<GridKey, Grid> components = new LinkedHashMap<>();
  private Grid[] dispatch = new Grid[0];
  private int dispatchCursor;
  private long generation;
  private int nodeId, gridId;
  private boolean transferAtStart;

  public static EnergyNetLocal create(Level world) {
    return new EnergyNetLocal(world);
  }

  private EnergyNetLocal(Level world) {
    this.world = Objects.requireNonNull(world);
  }

  public Level getWorld() {
    return world;
  }

  public Tile getTile(BlockPos position) {
    return positions.get(position);
  }

  public Collection<Tile> getSources() {
    return sourceView;
  }

  int allocateNodeId() {
    return nodeId++;
  }

  int allocateGridId() {
    return gridId++;
  }

  private void ownerThread() {
    var server = world.getServer();
    if (world.isClientSide() || server != null && !server.isSameThread())
      throw new IllegalStateException(
          "Energy registration and transfer require the server world thread");
  }

  void addTile(IEnergyTile tile, BlockPos position) {
    ownerThread();
    Objects.requireNonNull(tile);
    Objects.requireNonNull(position);
    Tile current = identities.get(tile);
    if (additions.containsKey(tile) || current != null && !current.isDisabled()) {
      issue("Duplicate energy tile registration: %s", tile);
      return;
    }
    List<IEnergyTile> parts =
        tile instanceof IMetaDelegate meta ? List.copyOf(meta.getSubTiles()) : List.of(tile);
    if (parts.isEmpty())
      throw new IllegalArgumentException("Energy delegate must expose at least one part");
    Change change = new Change(tile, position.immutable(), parts, generation + 1, true);
    additions.put(tile, change);
    changes.addLast(change);
  }

  void removeTile(IEnergyTile tile, BlockPos position) {
    ownerThread();
    Change addition = additions.remove(tile);
    if (addition != null) changes.remove(addition);
    Tile current = identities.get(tile);
    if (current == null || current.isDisabled()) return;
    current.setDisabled();
    for (Node node : current.nodes) if (node.getGrid() != null) node.getGrid().suspend();
    changes.addLast(new Change(tile, position.immutable(), List.of(), generation + 1, false));
  }

  IEnergyTile getIoTile(BlockPos position) {
    return lookup(position, false);
  }

  IEnergyTile getSubTile(BlockPos position) {
    return lookup(position, true);
  }

  private IEnergyTile lookup(BlockPos position, boolean part) {
    Tile current = positions.get(position);
    if (current != null && !current.isDisabled())
      return part ? current.getSubTileAt(position) : current.getMainTile();
    IEnergyTile result = null;
    for (Change change : changes) {
      if (!change.add()) {
        if (change.position().equals(position)) result = null;
        continue;
      }
      for (IEnergyTile candidate : change.parts())
        if (EnergyNet.instance.getPos(candidate).equals(position))
          result = part ? candidate : change.tile();
    }
    return result;
  }

  NodeStats getNodeStats(IEnergyTile identity) {
    Tile tile = identities.get(identity);
    return tile == null || tile.isDisabled()
        ? null
        : EnergyNetGlobal.getCalculator().getNodeStats(tile);
  }

  int getAdjacentConnections(IEnergyTile identity) {
    Tile tile = identities.get(identity);
    if (tile == null || tile.isDisabled()) return 0;
    BlockPos query = EnergyNet.instance.getPos(identity);
    int mask = 0;
    for (Node node : tile.nodes)
      for (NodeLink link : node.links) {
        for (IEnergyTile part : link.getNeighbor(node).tile.subTiles) {
          Direction direction = between(query, EnergyNet.instance.getPos(part));
          if (direction != null) mask |= 1 << direction.ordinal();
        }
      }
    return mask;
  }

  public Collection<GridInfo> getGridInfos() {
    var result = new ArrayList<GridInfo>(components.size());
    for (Grid grid : components.values()) result.add(grid.getInfo());
    return result;
  }

  boolean dumpDebugInfo(BlockPos position, PrintStream console, PrintStream chat) {
    Tile tile = positions.get(position);
    if (tile == null) return false;
    chat.printf("Tile %s, disabled=%s, endpoints=%d%n", tile, tile.isDisabled(), tile.nodes.size());
    var visited = Collections.newSetFromMap(new IdentityHashMap<Grid, Boolean>());
    for (Node node : tile.nodes) {
      Grid grid = node.getGrid();
      if (grid != null && visited.add(grid)) {
        grid.dumpNodeInfo(node, " ", console, chat);
        grid.dumpInfo(" ", console, chat);
        grid.dumpGraph();
      }
    }
    return true;
  }

  public void onTickStart() {
    ownerThread();
    if (transferAtStart) {
      transferAtStart = false;
      notifyConnections();
      transfer();
    }
  }

  public void onTickEnd() {
    ownerThread();
    if (applyChanges()) {
      rebuildComponents();
      transferAtStart = true;
    } else transfer();
    generation++;
  }

  private boolean applyChanges() {
    var batch = new ArrayList<Change>();
    while (!changes.isEmpty() && changes.getFirst().generation() <= generation)
      batch.add(changes.removeFirst());
    if (batch.isEmpty()) return false;
    boolean changed = false;
    // Position replacements form one transaction: remove stale identities
    // before installing new ones, even if callback order queued the add first.
    for (Change change : batch)
      if (!change.add()) {
        Tile tile = identities.remove(change.tile());
        if (tile == null) continue;
        changed = true;
        sources.remove(tile);
        notifications.remove(change.tile());
        for (BlockPos position : tile.partPositions.keySet()) {
          positions.remove(position, tile);
          notifyNeighbors(position);
        }
        for (var receiver : EnergyNetGlobal.getEventReceivers()) receiver.onRemove(change.tile());
      }
    for (Change change : batch)
      if (change.add()) {
        additions.remove(change.tile(), change);
        if (!valid(change)) continue;
        Tile tile = new Tile(this, change.tile(), change.parts());
        identities.put(change.tile(), tile);
        if (change.tile() instanceof IEnergySource) sources.add(tile);
        for (BlockPos position : tile.partPositions.keySet()) {
          positions.put(position, tile);
          notifyNeighbors(position);
        }
        notifications.add(change.tile());
        changed = true;
        for (var receiver : EnergyNetGlobal.getEventReceivers()) receiver.onAdd(change.tile());
      }
    return changed;
  }

  private boolean valid(Change change) {
    IEnergyTile tile = change.tile();
    if (identities.containsKey(tile)
        || EnergyNet.instance.getWorld(tile) != world
        || !EnergyNet.instance.getPos(tile).equals(change.position())
        || !world.isLoaded(change.position())
        || tile instanceof BlockEntity entity && entity.isRemoved()) return false;
    var unique = new HashSet<BlockPos>();
    for (IEnergyTile part : change.parts()) {
      BlockPos position = EnergyNet.instance.getPos(part);
      if (EnergyNet.instance.getWorld(part) != world
          || positions.containsKey(position)
          || !unique.add(position)) {
        issue("Conflicting energy part at %s", position);
        return false;
      }
    }
    return true;
  }

  private void notifyNeighbors(BlockPos position) {
    for (Direction direction : Direction.values()) {
      Tile neighbor = positions.get(position.relative(direction));
      if (neighbor != null && !neighbor.isDisabled()) notifications.add(neighbor.getMainTile());
    }
  }

  private void notifyConnections() {
    IEnergyTile[] batch = notifications.toArray(IEnergyTile[]::new);
    notifications.clear();
    for (IEnergyTile tile : batch) {
      Tile registered = identities.get(tile);
      if (registered != null
          && !registered.isDisabled()
          && world.isLoaded(EnergyNet.instance.getPos(tile))) tile.onConnectionChange();
    }
  }

  private void transfer() {
    var calculator = EnergyNetGlobal.getCalculator();
    if (components.isEmpty() || !calculator.runSyncStep(this)) return;
    int count = dispatch.length;
    for (int i = 0; i < count; i++) {
      Grid grid = dispatch[(dispatchCursor + i) % count];
      if (grid.isSuspended()) continue;
      if (calculator.runSyncStep(grid))
        throw new IllegalStateException("World callback requested asynchronous execution");
    }
    dispatchCursor = count == 0 ? 0 : (dispatchCursor + 1) % count;
  }

  @SuppressWarnings("unchecked")
  private void rebuildComponents() {
    var logical = new ArrayList<Node>();
    for (Tile tile : identities.values()) if (!tile.isDisabled()) logical.addAll(tile.primaryNodes);
    logical.sort(Comparator.comparingInt(node -> node.uid));
    int count = logical.size();
    var indices = new IdentityHashMap<Node, Integer>(count);
    List<Edge>[] adjacent = (List<Edge>[]) new List<?>[count];
    boolean[] conductors = new boolean[count];
    for (int i = 0; i < count; i++) {
      indices.put(logical.get(i), i);
      adjacent[i] = new ArrayList<>(6);
      conductors[i] = logical.get(i).nodeType == NodeType.Conductor;
    }
    var pairs = new HashSet<Long>();
    for (int a = 0; a < count; a++) {
      Node first = logical.get(a);
      for (IEnergyTile part : first.tile.subTiles)
        for (Direction direction : Direction.values()) {
          BlockPos position = EnergyNet.instance.getPos(part).relative(direction);
          Tile neighbor = positions.get(position);
          if (neighbor == null || neighbor == first.tile || neighbor.isDisabled()) continue;
          IEnergyTile otherPart = neighbor.getSubTileAt(position);
          for (Node second : neighbor.primaryNodes) {
            int b = indices.get(second);
            if (a >= b || !connects(first, second, part, otherPart, direction)) continue;
            long pair = (long) a << 32 | (b & 0xffffffffL);
            if (!pairs.add(pair)) continue;
            double loss = (first.getInnerLoss() + second.getInnerLoss()) * 0.5;
            if (!Double.isFinite(loss) || loss < 0) {
              issue("Invalid cable resistance: %s", loss);
              loss = 0;
            }
            Edge edge = new Edge(a, b, direction, loss);
            adjacent[a].add(edge);
            adjacent[b].add(edge);
          }
        }
    }
    int[] offsets = new int[count + 1];
    for (int i = 0; i < count; i++) offsets[i + 1] = offsets[i] + adjacent[i].size();
    int[] neighbors = new int[offsets[count]];
    for (int i = 0; i < count; i++) {
      int e = offsets[i];
      for (Edge edge : adjacent[i]) neighbors[e++] = edge.a() == i ? edge.b() : edge.a();
    }
    List<int[]> islands = TerminalComponents.partition(conductors, offsets, neighbors);
    var plans = new ArrayList<Plan>(islands.size());
    int[] stamps = new int[count];
    int stamp = 0;
    for (int[] members : islands) {
      stamp++;
      boolean hasConductor = false;
      var memberKeys = new ArrayList<Integer>(members.length);
      for (int member : members) {
        stamps[member] = stamp;
        memberKeys.add(logical.get(member).uid);
        hasConductor |= conductors[member];
      }
      var edges = new ArrayList<Edge>();
      var edgeKeys = new ArrayList<EdgeKey>();
      for (int member : members)
        for (Edge edge : adjacent[member]) {
          if (edge.a() != member || stamps[edge.b()] != stamp) continue;
          if (hasConductor && !conductors[edge.a()] && !conductors[edge.b()]) continue;
          edges.add(edge);
          edgeKeys.add(
              new EdgeKey(
                  logical.get(edge.a()).uid,
                  logical.get(edge.b()).uid,
                  edge.direction(),
                  edge.loss()));
        }
      edgeKeys.sort(Comparator.comparingInt(EdgeKey::a).thenComparingInt(EdgeKey::b));
      plans.add(
          new Plan(new GridKey(List.copyOf(memberKeys), List.copyOf(edgeKeys)), members, edges));
    }
    var wanted = new HashSet<GridKey>();
    for (Plan plan : plans) wanted.add(plan.key());
    for (var previous : components.entrySet())
      if (!wanted.contains(previous.getKey())) previous.getValue().release();
    Map<GridKey, Grid> next = new LinkedHashMap<>(plans.size());
    Node[] assigned = new Node[count];
    for (Plan plan : plans) {
      Grid grid = components.get(plan.key());
      if (grid == null) {
        var endpoints = new ArrayList<Node>(plan.members().length);
        for (int index : plan.members()) {
          Node primary = logical.get(index), endpoint = primary;
          if (primary.getGrid() != null) {
            endpoint = new Node(allocateNodeId(), primary.tile, primary.nodeType);
            endpoint.setExtraNode(true);
            primary.tile.nodes.add(endpoint);
          }
          assigned[index] = endpoint;
          endpoints.add(endpoint);
        }
        for (Edge edge : plan.edges()) {
          Node first = assigned[edge.a()], second = assigned[edge.b()];
          NodeLink link = new NodeLink(first, second, edge.loss(), edge.direction());
          first.links.add(link);
          second.links.add(link);
        }
        grid = new Grid(this, endpoints);
        EnergyNetGlobal.getCalculator().handleGridChange(grid);
      }
      next.put(plan.key(), grid);
    }
    components = next;
    dispatch = next.values().toArray(Grid[]::new);
    dispatchCursor = dispatch.length == 0 ? 0 : dispatchCursor % dispatch.length;
  }

  private static boolean connects(
      Node first, Node second, IEnergyTile part, IEnergyTile otherPart, Direction direction) {
    IEnergyTile a = first.tile.getMainTile(), b = second.tile.getMainTile();
    if (first.nodeType != NodeType.Sink && second.nodeType != NodeType.Source) {
      IEnergyEmitter emitter = part instanceof IEnergyEmitter value ? value : (IEnergyEmitter) a;
      IEnergyAcceptor acceptor =
          otherPart instanceof IEnergyAcceptor value ? value : (IEnergyAcceptor) b;
      if (emitter.emitsEnergyTo((IEnergyAcceptor) b, direction)
          && acceptor.acceptsEnergyFrom((IEnergyEmitter) a, direction.getOpposite())) return true;
    }
    if (first.nodeType != NodeType.Source && second.nodeType != NodeType.Sink) {
      IEnergyAcceptor acceptor =
          part instanceof IEnergyAcceptor value ? value : (IEnergyAcceptor) a;
      IEnergyEmitter emitter =
          otherPart instanceof IEnergyEmitter value ? value : (IEnergyEmitter) b;
      return acceptor.acceptsEnergyFrom((IEnergyEmitter) b, direction)
          && emitter.emitsEnergyTo((IEnergyAcceptor) a, direction.getOpposite());
    }
    return false;
  }

  private static Direction between(BlockPos from, BlockPos to) {
    int x = to.getX() - from.getX(), y = to.getY() - from.getY(), z = to.getZ() - from.getZ();
    if (Math.abs(x) + Math.abs(y) + Math.abs(z) != 1) return null;
    return x != 0
        ? (x > 0 ? Direction.EAST : Direction.WEST)
        : y != 0
            ? (y > 0 ? Direction.UP : Direction.DOWN)
            : z > 0 ? Direction.SOUTH : Direction.NORTH;
  }

  private static void issue(String format, Object value) {
    if (EnergyNetSettings.logGridUpdateIssues)
      ProjectRuntime.log.warn(DiagnosticArea.EnergyNet, format, value);
  }
}
