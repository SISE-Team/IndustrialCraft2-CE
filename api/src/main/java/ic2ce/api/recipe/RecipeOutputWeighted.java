package ic2ce.api.recipe;

import ic2ce.engine.recipe.WeightedChoice;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import net.minecraft.world.item.ItemStack;

/** Weighted recipe templates. Views are read-only; edits use addOutput/clear. */
public final class RecipeOutputWeighted {
  private final ArrayList<ItemStack> outputs = new ArrayList<>();
  private final ArrayList<Integer> weights = new ArrayList<>();
  private final List<Integer> weightView = Collections.unmodifiableList(weights);
  private final WeightedChoice choice = new WeightedChoice();

  public RecipeOutputWeighted addOutput(ItemStack template, int weight) {
    Objects.requireNonNull(template);
    if (template.isEmpty()) throw new IllegalArgumentException("Empty weighted output");
    choice.add(weight);
    outputs.add(template.copy());
    weights.add(weight);
    return this;
  }

  public ItemStack drawOutput() {
    long total = choice.totalWeight();
    return total == 0
        ? null
        : outputs.get(choice.select(ThreadLocalRandom.current().nextLong(total))).copy();
  }

  public void clear() {
    outputs.clear();
    weights.clear();
    choice.clear();
  }

  public List<ItemStack> getOutputs() {
    return outputs.stream().map(ItemStack::copy).toList();
  }

  public List<Integer> getWeights() {
    return weightView;
  }

  public void forEach(BiConsumer<ItemStack, Integer> consumer) {
    Objects.requireNonNull(consumer);
    for (int i = 0; i < outputs.size(); i++) consumer.accept(outputs.get(i).copy(), weights.get(i));
  }
}
