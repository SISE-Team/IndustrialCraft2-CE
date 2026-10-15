package ic2ce.neoforge.processing.uu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/** Ingredient slots remain independent choices; output quantities coalesce by material identity. */
public final class RecipeTransformation {
  public final double transformCost;
  public List<List<LeanItemStack>> inputs;
  public List<LeanItemStack> outputs;

  public RecipeTransformation(
      double overhead, List<List<LeanItemStack>> inputs, LeanItemStack... outputs) {
    this(overhead, inputs, Arrays.asList(outputs));
  }

  public RecipeTransformation(
      double overhead, List<List<LeanItemStack>> inputs, List<LeanItemStack> outputs) {
    if (!Double.isFinite(overhead) || overhead < 0)
      throw new IllegalArgumentException("Invalid material transformation cost");
    transformCost = overhead;
    this.inputs = new ArrayList<>(inputs.size());
    for (var alternatives : inputs) this.inputs.add(new ArrayList<>(alternatives));
    this.outputs = new ArrayList<>(outputs);
  }

  protected void merge() {
    var cleaned = new ArrayList<List<LeanItemStack>>(inputs.size());
    for (var alternatives : inputs) {
      var minimums = new LinkedHashMap<LeanItemStack, LeanItemStack>();
      for (var candidate : alternatives)
        if (candidate != null && candidate.getSize() > 0)
          minimums.merge(candidate, candidate, (a, b) -> a.getSize() <= b.getSize() ? a : b);
      cleaned.add(List.copyOf(minimums.values()));
    }
    var totals = new LinkedHashMap<LeanItemStack, LeanItemStack>();
    for (var output : outputs)
      if (output != null && output.getSize() > 0)
        totals.merge(
            output, output, (a, b) -> a.copyWithSize(Math.addExact(a.getSize(), b.getSize())));
    inputs = List.copyOf(cleaned);
    outputs = List.copyOf(totals.values());
  }

  @Override
  public String toString() {
    return "{ " + transformCost + " + " + inputs + " -> " + outputs + " }";
  }
}
