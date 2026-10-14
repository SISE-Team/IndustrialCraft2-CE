package ic2ce.engine.energy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/** Connected conductor islands; terminals never join otherwise separate wires. */
public final class TerminalComponents {
  private TerminalComponents() {}

  public static List<int[]> partition(boolean[] conductors, int[] offsets, int[] neighbors) {
    int size = conductors.length;
    if (offsets.length != size + 1 || offsets[0] != 0 || offsets[size] != neighbors.length)
      throw new IllegalArgumentException("Invalid adjacency offsets");
    int[] parent = new int[size];
    for (int i = 0; i < size; i++) parent[i] = i;
    for (int node = 0; node < size; node++) {
      if (offsets[node] > offsets[node + 1]) throw new IllegalArgumentException("Unsorted offsets");
      for (int e = offsets[node]; e < offsets[node + 1]; e++) {
        int target = neighbors[e];
        if (target < 0 || target >= size) throw new IllegalArgumentException("Invalid neighbor");
        if (conductors[node] && conductors[target]) {
          int a = root(parent, node), b = root(parent, target);
          parent[Math.max(a, b)] = Math.min(a, b);
        }
      }
    }
    var islands = new LinkedHashMap<Integer, LinkedHashSet<Integer>>();
    boolean[] included = new boolean[size];
    for (int node = 0; node < size; node++) {
      if (!conductors[node]) continue;
      var island = islands.computeIfAbsent(root(parent, node), _ -> new LinkedHashSet<>());
      island.add(node);
      included[node] = true;
      for (int e = offsets[node]; e < offsets[node + 1]; e++) {
        int target = neighbors[e];
        if (!conductors[target]) {
          island.add(target);
          included[target] = true;
        }
      }
    }
    var result = new ArrayList<int[]>();
    for (var island : islands.values()) {
      int[] members = island.stream().mapToInt(Integer::intValue).toArray();
      Arrays.sort(members);
      result.add(members);
    }
    // Direct terminal connections are independent, including terminals
    // already attached to a conductor island. They must not bridge it.
    for (int node = 0; node < size; node++) {
      if (conductors[node]) continue;
      for (int e = offsets[node]; e < offsets[node + 1]; e++) {
        int target = neighbors[e];
        if (!conductors[target] && node < target) {
          result.add(new int[] {node, target});
          included[node] = included[target] = true;
        }
      }
    }
    for (int node = 0; node < size; node++) if (!included[node]) result.add(new int[] {node});
    return List.copyOf(result);
  }

  private static int root(int[] parent, int node) {
    int root = node;
    while (parent[root] != root) root = parent[root];
    while (parent[node] != node) {
      int next = parent[node];
      parent[node] = root;
      node = next;
    }
    return root;
  }
}
