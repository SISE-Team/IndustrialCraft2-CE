package ic2ce.engine.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Monotone hypergraph relaxation: ingredient alternatives, quantities and per-output unit costs.
 */
public final class MaterialCostGraph {
  private record Rule(
      double overhead, int[][] nodes, int[][] quantities, int[] outputs, int[] outputQuantities) {}

  private final int nodeCount;
  private final List<Rule> rules = new ArrayList<>();

  public MaterialCostGraph(int nodeCount) {
    if (nodeCount < 0) throw new IllegalArgumentException("Negative graph size");
    this.nodeCount = nodeCount;
  }

  private void node(int id) {
    if (id < 0 || id >= nodeCount) throw new IllegalArgumentException("Unknown material node");
  }

  public void add(
      double overhead,
      int[][] ingredients,
      int[][] quantities,
      int[] outputs,
      int[] outputQuantities) {
    if (!Double.isFinite(overhead)
        || overhead < 0
        || ingredients.length != quantities.length
        || outputs.length != outputQuantities.length)
      throw new IllegalArgumentException("Invalid material transformation");
    var ownedNodes = new int[ingredients.length][];
    var ownedQuantities = new int[ingredients.length][];
    for (int n = 0; n < ingredients.length; n++) {
      if (ingredients[n].length != quantities[n].length)
        throw new IllegalArgumentException("Ingredient quantity mismatch");
      ownedNodes[n] = ingredients[n].clone();
      ownedQuantities[n] = quantities[n].clone();
      for (int a = 0; a < ingredients[n].length; a++) {
        node(ingredients[n][a]);
        if (quantities[n][a] <= 0)
          throw new IllegalArgumentException("Nonpositive ingredient quantity");
      }
    }
    for (int n = 0; n < outputs.length; n++) {
      node(outputs[n]);
      if (outputQuantities[n] <= 0)
        throw new IllegalArgumentException("Nonpositive output quantity");
    }
    rules.add(
        new Rule(overhead, ownedNodes, ownedQuantities, outputs.clone(), outputQuantities.clone()));
  }

  public double[] solve(double[] seeds) {
    if (seeds.length != nodeCount) throw new IllegalArgumentException("Seed dimension mismatch");
    var costs = seeds.clone();
    for (double cost : costs)
      if (Double.isNaN(cost) || cost < 0) throw new IllegalArgumentException("Invalid seed cost");
    int ruleCount = rules.size();
    if (ruleCount == 0) return costs;
    var degree = new int[nodeCount];
    for (var rule : rules) for (var group : rule.nodes) for (int id : group) degree[id]++;
    var dependencies = new int[nodeCount][];
    for (int id = 0; id < nodeCount; id++) {
      dependencies[id] = new int[degree[id]];
      degree[id] = 0;
    }
    for (int r = 0; r < ruleCount; r++)
      for (var group : rules.get(r).nodes) for (int id : group) dependencies[id][degree[id]++] = r;
    var pending = new boolean[ruleCount];
    Arrays.fill(pending, true);
    var queue = new int[ruleCount];
    for (int r = 0; r < ruleCount; r++) queue[r] = r;
    int head = 0, tail = 0, size = ruleCount;
    while (size > 0) {
      int r = queue[head];
      head = head + 1 == ruleCount ? 0 : head + 1;
      size--;
      pending[r] = false;
      var rule = rules.get(r);
      double total = rule.overhead;
      for (int g = 0; g < rule.nodes.length; g++) {
        double cheapest = Double.POSITIVE_INFINITY;
        for (int a = 0; a < rule.nodes[g].length; a++)
          cheapest = Math.min(cheapest, costs[rule.nodes[g][a]] * rule.quantities[g][a]);
        total += cheapest;
        if (!Double.isFinite(total)) break;
      }
      if (!Double.isFinite(total)) continue;
      for (int o = 0; o < rule.outputs.length; o++) {
        int id = rule.outputs[o];
        double next = total / rule.outputQuantities[o];
        if (next + 1e-9 >= costs[id]) continue;
        costs[id] = next;
        for (int dependent : dependencies[id])
          if (!pending[dependent]) {
            pending[dependent] = true;
            queue[tail] = dependent;
            tail = tail + 1 == ruleCount ? 0 : tail + 1;
            size++;
          }
      }
    }
    return costs;
  }
}
