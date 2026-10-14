package ic2ce.neoforge.recipe.processing;

import ic2ce.api.recipe.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Two physical item bills remain independent; normal and one-sided acceptance lookup share an
 * atomic ordered recipe generation.
 */
public final class SolidCanningRecipes implements ICannerBottleRecipeManager {
  private record Query(ItemStack container, ItemStack fill, boolean hint) {}

  private record Row(
      Input input,
      int containers,
      int resources,
      ItemStack output,
      CompoundTag metadata,
      MachineRecipe<Input, ItemStack> recipe) {}

  private final CanningRecipeCatalog<Query, Row> table =
      new CanningRecipeCatalog<>(
          query -> BuiltInRegistries.ITEM.getId(query.fill.getItem()),
          SolidCanningRecipes::matches,
          row -> row.input.fill);

  private static boolean matches(Row row, Query query) {
    return (query.container.isEmpty()
            ? query.hint
            : row.input.container.matches(query.container)
                && (query.hint || query.container.getCount() >= row.containers))
        && (query.fill.isEmpty()
            ? query.hint
            : row.input.fill.matches(query.fill)
                && (query.hint || query.fill.getCount() >= row.resources));
  }

  private static ItemStack stack(ItemStack value) {
    return value == null ? ItemStack.EMPTY : value;
  }

  public boolean addRecipe(
      IRecipeInput container, IRecipeInput fill, ItemStack output, boolean replace) {
    return addRecipe(new Input(container, fill), output, null, replace);
  }

  public boolean addRecipe(Input input, ItemStack output, CompoundTag metadata, boolean replace) {
    Objects.requireNonNull(input);
    Objects.requireNonNull(output);
    if (output.isEmpty()) throw new IllegalArgumentException("Empty solid canning output");
    int containers = CanningRecipeCatalog.quantity(input.container),
        resources = CanningRecipeCatalog.quantity(input.fill);
    var identity = new Input(input.container, input.fill);
    var owned = output.copy();
    var saved = metadata == null ? null : metadata.copy();
    var recipe =
        new MachineRecipe<Input, ItemStack>(identity, owned, saved) {
          @Override
          public ItemStack getOutput() {
            return owned.copy();
          }
        };
    var row = new Row(identity, containers, resources, owned, saved, recipe);
    return table.add(row, previous -> overlap(identity, previous.input), replace);
  }

  private static boolean overlap(Input first, Input second) {
    for (var container : first.container.getInputs())
      if (second.container.matches(container))
        for (var fill : first.fill.getInputs()) if (second.fill.matches(fill)) return true;
    for (var container : second.container.getInputs())
      if (first.container.matches(container))
        for (var fill : second.fill.getInputs()) if (first.fill.matches(fill)) return true;
    return false;
  }

  @Override
  @Deprecated
  public void addRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output) {
    if (!addRecipe(container, fill, output, false))
      throw new IllegalStateException("Ambiguous solid canning recipe");
  }

  private Row select(ItemStack container, ItemStack fill, boolean hint) {
    container = stack(container);
    fill = stack(fill);
    if (container.isEmpty() && fill.isEmpty() || !hint && (container.isEmpty() || fill.isEmpty()))
      return null;
    var query = new Query(container, fill, hint);
    if (fill.isEmpty()) {
      for (var row : table.rows()) if (matches(row, query)) return row;
      return null;
    }
    return table.find(query);
  }

  @Override
  public MachineRecipeResult<Input, ItemStack, RawInput> apply(RawInput input, boolean hint) {
    if (input == null) return null;
    var row = select(input.container, input.fill, hint);
    if (row == null) return null;
    return row.recipe.getResult(
        new RawInput(
            input.container.isEmpty()
                ? ItemStack.EMPTY
                : input.container.copyWithCount(
                    Math.max(0, input.container.getCount() - row.containers)),
            input.fill.isEmpty()
                ? ItemStack.EMPTY
                : input.fill.copyWithCount(Math.max(0, input.fill.getCount() - row.resources))));
  }

  @Override
  @Deprecated
  public RecipeOutput getOutputFor(
      ItemStack container, ItemStack fill, boolean adjustInput, boolean hint) {
    var row = select(container, fill, hint);
    if (row == null) return null;
    if (adjustInput && !hint) {
      if (container != null && !container.isEmpty()) container.shrink(row.containers);
      if (fill != null && !fill.isEmpty()) fill.shrink(row.resources);
    }
    return new RecipeOutput(row.metadata, row.output.copy());
  }

  @Override
  public boolean isIterable() {
    return true;
  }

  @Override
  public Iterable<? extends MachineRecipe<Input, ItemStack>> getRecipes() {
    return () ->
        new Iterator<>() {
          private final Iterator<Row> rows = table.rows().iterator();
          private Row current;

          public boolean hasNext() {
            return rows.hasNext();
          }

          public MachineRecipe<Input, ItemStack> next() {
            current = rows.next();
            return new MachineRecipe<>(
                new Input(current.input.container, current.input.fill),
                current.output.copy(),
                current.metadata);
          }

          public void remove() {
            if (current == null) throw new IllegalStateException();
            table.remove(current);
            current = null;
          }
        };
  }
}
