package ic2ce.neoforge.processing.uu;

import ic2ce.engine.async.PriorityExecutor;
import ic2ce.engine.recipe.MaterialCostGraph;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.diagnostics.ProjectLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import net.minecraft.world.item.ItemStack;

/**
 * Builds native recipe identities on the world thread, solves primitive costs off-thread, publishes
 * one immutable generation.
 */
public final class UuGraph {
  private record Snapshot(Map<LeanItemStack, Double> costs) {}

  private record Compiled(MaterialCostGraph graph, List<LeanItemStack> materials, double[] seeds) {}

  private static final Object LOCK = new Object();
  private static final PriorityExecutor WORKERS =
      new PriorityExecutor(Math.max(2, Runtime.getRuntime().availableProcessors()));
  private static final Map<LeanItemStack, Double> initial = new LinkedHashMap<>();
  private static volatile Snapshot published = new Snapshot(Map.of());
  private static volatile Future<Snapshot> calculation;

  private UuGraph() {}

  public static void build(boolean reset) {
    synchronized (LOCK) {
      if (calculation != null)
        throw new IllegalStateException("Material cost calculation is already running");
      long started = System.nanoTime();
      var transformations = new ArrayList<RecipeTransformation>();
      for (var resolver : List.copyOf(UuIndex.instance.resolvers))
        transformations.addAll(resolver.getTransformations());
      var obtainable = new LinkedHashMap<LeanItemStack, Boolean>();
      for (var rule : transformations)
        for (var output : rule.outputs) obtainable.put(output.copyWithSize(1), true);
      for (var seed : initial.keySet()) obtainable.put(seed, true);
      var candidates = List.copyOf(obtainable.keySet());
      for (var resolver : List.copyOf(UuIndex.instance.lateResolvers))
        transformations.addAll(resolver.getTransformations(candidates));
      var compiled = compile(transformations, Map.copyOf(initial));
      calculation =
          WORKERS.submit(
              () -> {
                var values = compiled.graph.solve(compiled.seeds);
                var result = new LinkedHashMap<LeanItemStack, Double>();
                for (int id = 0; id < values.length; id++)
                  result.put(compiled.materials.get(id), values[id]);
                ProjectLog.LOG.debug(
                    DiagnosticArea.Uu,
                    "Material costs: %d nodes, %d transformations, %d ms",
                    values.length,
                    transformations.size(),
                    (System.nanoTime() - started) / 1000000);
                return new Snapshot(java.util.Collections.unmodifiableMap(result));
              });
    }
  }

  private static Compiled compile(
      List<RecipeTransformation> transformations, Map<LeanItemStack, Double> seeds) {
    var ids = new LinkedHashMap<LeanItemStack, Integer>();
    for (var seed : seeds.keySet()) ids.computeIfAbsent(seed.copyWithSize(1), key -> ids.size());
    for (var rule : transformations) {
      rule.merge();
      for (var alternatives : rule.inputs)
        for (var input : alternatives)
          ids.computeIfAbsent(input.copyWithSize(1), key -> ids.size());
      for (var output : rule.outputs)
        ids.computeIfAbsent(output.copyWithSize(1), key -> ids.size());
    }
    var graph = new MaterialCostGraph(ids.size());
    for (var rule : transformations) {
      var nodes = new int[rule.inputs.size()][];
      var quantities = new int[nodes.length][];
      for (int group = 0; group < nodes.length; group++) {
        var alternatives = rule.inputs.get(group);
        nodes[group] = new int[alternatives.size()];
        quantities[group] = new int[alternatives.size()];
        for (int a = 0; a < alternatives.size(); a++) {
          var input = alternatives.get(a);
          nodes[group][a] = ids.get(input);
          quantities[group][a] = input.getSize();
        }
      }
      var outputs = new int[rule.outputs.size()];
      var counts = new int[outputs.length];
      for (int o = 0; o < outputs.length; o++) {
        var output = rule.outputs.get(o);
        outputs[o] = ids.get(output);
        counts[o] = output.getSize();
      }
      graph.add(rule.transformCost, nodes, quantities, outputs, counts);
    }
    var values = new double[ids.size()];
    Arrays.fill(values, Double.POSITIVE_INFINITY);
    for (var seed : seeds.entrySet()) values[ids.get(seed.getKey())] = seed.getValue();
    return new Compiled(graph, List.copyOf(ids.keySet()), values);
  }

  public static void set(ItemStack stack, double value) {
    if (stack == null || stack.isEmpty() || !Double.isFinite(value) || value < 0)
      throw new IllegalArgumentException("Invalid initial material value");
    synchronized (LOCK) {
      if (calculation != null)
        throw new IllegalStateException("Cannot edit material seeds during calculation");
      initial.merge(new LeanItemStack(stack, 1), value, Math::min);
    }
  }

  public static void clearInitialValues() {
    finishCalculation();
    synchronized (LOCK) {
      initial.clear();
    }
  }

  public static double get(ItemStack stack) {
    finishCalculation();
    return stack == null || stack.isEmpty()
        ? Double.POSITIVE_INFINITY
        : published.costs.getOrDefault(new LeanItemStack(stack, 1), Double.POSITIVE_INFINITY);
  }

  public static ItemStack find(ItemStack stack) {
    finishCalculation();
    if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
    var key = new LeanItemStack(stack, 1);
    return published.costs.containsKey(key) ? key.toMcStack() : ItemStack.EMPTY;
  }

  public static Iterator<Map.Entry<ItemStack, Double>> iterator() {
    finishCalculation();
    var source = published.costs.entrySet().iterator();
    return new Iterator<>() {
      public boolean hasNext() {
        return source.hasNext();
      }

      public Map.Entry<ItemStack, Double> next() {
        var entry = source.next();
        return Map.entry(entry.getKey().toMcStack(), entry.getValue());
      }
    };
  }

  private static void finishCalculation() {
    var pending = calculation;
    if (pending == null) return;
    Snapshot completed;
    try {
      completed = pending.get();
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted material cost calculation", interrupted);
    } catch (ExecutionException failed) {
      ProjectLog.LOG.warn(
          DiagnosticArea.Uu,
          failed.getCause(),
          "Material cost calculation failed; preserving the previous generation");
      completed = published;
    }
    synchronized (LOCK) {
      if (calculation == pending) {
        published = completed;
        calculation = null;
      }
    }
  }
}
