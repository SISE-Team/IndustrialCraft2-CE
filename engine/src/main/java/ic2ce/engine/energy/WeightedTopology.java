package ic2ce.engine.energy;

import java.util.Arrays;

/** Immutable compressed adjacency; workspaces are owned by one calculation thread. */
public final class WeightedTopology {
  public enum Role {
    SOURCE,
    CONDUCTOR,
    SINK
  }

  private final Role[] roles;
  private final int[] offsets, targets;
  private final double[] losses;

  public WeightedTopology(Role[] roles, int[] offsets, int[] targets, double[] losses) {
    if (offsets.length != roles.length + 1
        || targets.length != losses.length
        || offsets[0] != 0
        || offsets[roles.length] != targets.length)
      throw new IllegalArgumentException("Malformed topology");
    for (int node = 0; node < roles.length; node++) {
      if (roles[node] == null || offsets[node] > offsets[node + 1])
        throw new IllegalArgumentException("Malformed node");
    }
    for (int edge = 0; edge < targets.length; edge++) {
      if (targets[edge] < 0
          || targets[edge] >= roles.length
          || !Double.isFinite(losses[edge])
          || losses[edge] < 0) throw new IllegalArgumentException("Invalid edge");
    }
    this.roles = roles.clone();
    this.offsets = offsets.clone();
    this.targets = targets.clone();
    this.losses = losses.clone();
  }

  public Workspace workspace() {
    return new Workspace();
  }

  public final class Workspace {
    private final double[] distance = new double[roles.length];
    private final int[] previous = new int[roles.length],
        heap = new int[roles.length],
        location = new int[roles.length];
    private int size, source;

    public void solve(int source) {
      if (source < 0 || source >= roles.length || roles[source] != Role.SOURCE)
        throw new IllegalArgumentException("Not a source");
      this.source = source;
      Arrays.fill(distance, Double.POSITIVE_INFINITY);
      Arrays.fill(previous, -1);
      Arrays.fill(location, -1);
      size = 0;
      distance[source] = 0;
      offer(source);
      while (size != 0) {
        int node = poll();
        if (roles[node] == Role.SINK) continue;
        for (int edge = offsets[node]; edge < offsets[node + 1]; edge++) {
          int target = targets[edge];
          if (roles[target] == Role.SOURCE) continue;
          double proposed = distance[node] + losses[edge];
          if (proposed < distance[target]) {
            distance[target] = proposed;
            previous[target] = node;
            offer(target);
          }
        }
      }
    }

    public double lossTo(int target) {
      return distance[target];
    }

    /** Full node sequence, including source and sink. Empty for unreachable targets. */
    public int[] routeTo(int target) {
      if (!Double.isFinite(distance[target])) return new int[0];
      int length = 1;
      for (int node = target; node != source; node = previous[node]) {
        if (node < 0 || length > roles.length)
          throw new IllegalStateException("Broken predecessor tree");
        length++;
      }
      int[] route = new int[length];
      for (int node = target, offset = length - 1; offset >= 0; node = previous[node])
        route[offset--] = node;
      return route;
    }

    private boolean before(int a, int b) {
      int compare = Double.compare(distance[a], distance[b]);
      return compare < 0 || compare == 0 && a < b;
    }

    private void offer(int node) {
      int at = location[node];
      if (at < 0) {
        at = size++;
        heap[at] = node;
        location[node] = at;
      }
      while (at != 0) {
        int parent = (at - 1) >>> 1;
        if (!before(node, heap[parent])) break;
        heap[at] = heap[parent];
        location[heap[at]] = at;
        at = parent;
      }
      heap[at] = node;
      location[node] = at;
    }

    private int poll() {
      int result = heap[0], replacement = heap[--size];
      location[result] = -1;
      if (size == 0) return result;
      int at = 0;
      while (at * 2 + 1 < size) {
        int child = at * 2 + 1;
        if (child + 1 < size && before(heap[child + 1], heap[child])) child++;
        if (!before(heap[child], replacement)) break;
        heap[at] = heap[child];
        location[heap[at]] = at;
        at = child;
      }
      heap[at] = replacement;
      location[replacement] = at;
      return result;
    }
  }
}
