package ic2ce.engine.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;

/**
 * Compiles material transformations into a {@link MaterialCostGraph}. Materials are identified by an
 * equality key with quantity removed; node indices follow first appearance: seeds, then each rule's
 * ingredients and outputs in order.
 *
 * @param <M> material key with value equality that ignores quantity
 */
public final class MaterialCostCompiler<M> {
  /** One transformation: overhead, ingredient alternatives per slot, and outputs. */
  public record Transformation<M>(double overhead, List<List<M>> inputs, List<M> outputs) {}

  /** A compiled graph, its node materials in index order and seed costs by index. */
  public record Compiled<M>(MaterialCostGraph graph, List<M> materials, double[] seeds) {}

  private final ToIntFunction<M> quantity;
  private final UnaryOperator<M> unit;

  /**
   * @param quantity amount of a material entry
   * @param unit the same material with quantity one, used as the node key
   */
  public MaterialCostCompiler(ToIntFunction<M> quantity, UnaryOperator<M> unit) {
    this.quantity = quantity;
    this.unit = unit;
  }

  public Compiled<M> compile(List<Transformation<M>> transformations, Map<M, Double> seeds) {
    var ids = new LinkedHashMap<M, Integer>();
    for (var seed : seeds.keySet()) ids.computeIfAbsent(unit.apply(seed), key -> ids.size());
    for (var rule : transformations) {
      for (var alternatives : rule.inputs())
        for (var input : alternatives) ids.computeIfAbsent(unit.apply(input), key -> ids.size());
      for (var output : rule.outputs()) ids.computeIfAbsent(unit.apply(output), key -> ids.size());
    }
    var graph = new MaterialCostGraph(ids.size());
    for (var rule : transformations) {
      var nodes = new int[rule.inputs().size()][];
      var quantities = new int[nodes.length][];
      for (int group = 0; group < nodes.length; group++) {
        var alternatives = rule.inputs().get(group);
        nodes[group] = new int[alternatives.size()];
        quantities[group] = new int[alternatives.size()];
        for (int a = 0; a < alternatives.size(); a++) {
          var input = alternatives.get(a);
          nodes[group][a] = ids.get(input);
          quantities[group][a] = quantity.applyAsInt(input);
        }
      }
      var outputs = new int[rule.outputs().size()];
      var counts = new int[outputs.length];
      for (int o = 0; o < outputs.length; o++) {
        var output = rule.outputs().get(o);
        outputs[o] = ids.get(output);
        counts[o] = quantity.applyAsInt(output);
      }
      graph.add(rule.overhead(), nodes, quantities, outputs, counts);
    }
    var values = new double[ids.size()];
    Arrays.fill(values, Double.POSITIVE_INFINITY);
    for (var seed : seeds.entrySet()) values[ids.get(seed.getKey())] = seed.getValue();
    return new Compiled<>(graph, List.copyOf(new ArrayList<>(ids.keySet())), values);
  }
}
