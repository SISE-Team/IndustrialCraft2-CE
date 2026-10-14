package ic2ce.neoforge.recipe.processing;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Enrichment separates exact component-sensitive fluid payment from a positive additive bill and
 * owned output fluid.
 */
public final class EnrichmentRecipes implements ICannerEnrichRecipeManager {
  private record Query(Ic2FluidStack fluid, ItemStack additive, boolean hint) {}

  private record Row(
      Input identity,
      Ic2FluidStack fluid,
      int additives,
      Ic2FluidStack output,
      CompoundTag metadata,
      MachineRecipe<Input, Ic2FluidStack> recipe) {}

  private final CanningRecipeCatalog<Query, Row> table =
      new CanningRecipeCatalog<>(
          query -> BuiltInRegistries.ITEM.getId(query.additive.getItem()),
          EnrichmentRecipes::matches,
          row -> row.identity.additive);

  private static boolean matches(Row row, Query query) {
    boolean emptyFluid = query.fluid == null || query.fluid.isEmpty(),
        emptyItem = query.additive == null || query.additive.isEmpty();
    return (emptyFluid
            ? query.hint
            : row.fluid.hasExactFluid(query.fluid)
                && (query.hint || query.fluid.getAmountMb() >= row.fluid.getAmountMb()))
        && (emptyItem
            ? query.hint
            : row.identity.additive.matches(query.additive)
                && (query.hint || query.additive.getCount() >= row.additives));
  }

  public boolean addRecipe(
      Input input, Ic2FluidStack output, CompoundTag metadata, boolean replace) {
    Objects.requireNonNull(input);
    Objects.requireNonNull(output);
    if (input.fluid == null || input.fluid.isEmpty() || output.isEmpty())
      throw new IllegalArgumentException("Enrichment needs positive real fluid quantities");
    int additives = CanningRecipeCatalog.quantity(input.additive);
    var fluid = input.fluid.copy();
    var identity = new Input(fluid, input.additive);
    var owned = output.copy();
    var saved = metadata == null ? null : metadata.copy();
    var recipe =
        new MachineRecipe<Input, Ic2FluidStack>(identity, owned, saved) {
          @Override
          public Ic2FluidStack getOutput() {
            return owned.copy();
          }
        };
    var row = new Row(identity, fluid, additives, owned, saved, recipe);
    return table.add(
        row,
        previous -> {
          if (!previous.fluid.hasExactFluid(fluid)) return false;
          for (var item : identity.additive.getInputs())
            if (previous.identity.additive.matches(item)) return true;
          for (var item : previous.identity.additive.getInputs())
            if (identity.additive.matches(item)) return true;
          return false;
        },
        replace);
  }

  public void addRecipe(Ic2FluidStack fluid, IRecipeInput additive, Ic2FluidStack output) {
    if (!addRecipe(new Input(fluid, additive), output, null, false))
      throw new IllegalStateException("Ambiguous enrichment recipe");
  }

  private Row select(Ic2FluidStack fluid, ItemStack additive, boolean hint) {
    boolean emptyFluid = fluid == null || fluid.isEmpty(),
        emptyItem = additive == null || additive.isEmpty();
    if (emptyFluid && emptyItem || !hint && (emptyFluid || emptyItem)) return null;
    var query = new Query(fluid, additive, hint);
    if (emptyItem) {
      for (var row : table.rows()) if (matches(row, query)) return row;
      return null;
    }
    return table.find(query);
  }

  @Override
  public MachineRecipeResult<Input, Ic2FluidStack, RawInput> apply(RawInput input, boolean hint) {
    if (input == null) return null;
    var row = select(input.fluid, input.additive, hint);
    if (row == null) return null;
    int quantity =
        input.fluid == null ? 0 : Math.max(0, input.fluid.getAmountMb() - row.fluid.getAmountMb());
    var fluid = quantity == 0 ? null : input.fluid.copyWithAmountMb(quantity);
    var item =
        input.additive == null || input.additive.isEmpty()
            ? ItemStack.EMPTY
            : input.additive.copyWithCount(Math.max(0, input.additive.getCount() - row.additives));
    return row.recipe.getResult(new RawInput(fluid, item));
  }

  @Override
  @Deprecated
  public RecipeOutput getOutputFor(
      Ic2FluidStack fluid, ItemStack additive, boolean adjustInput, boolean hint) {
    var row = select(fluid, additive, hint);
    if (row == null) return null;
    if (adjustInput && !hint) {
      if (fluid != null) fluid.setAmountMb(fluid.getAmountMb() - row.fluid.getAmountMb());
      if (additive != null && !additive.isEmpty()) additive.shrink(row.additives);
    }
    var encoded = new CompoundTag();
    row.output.toNbt(encoded);
    return new RecipeOutput(encoded);
  }

  @Override
  public boolean isIterable() {
    return true;
  }

  @Override
  public Iterable<? extends MachineRecipe<Input, Ic2FluidStack>> getRecipes() {
    return () ->
        new Iterator<>() {
          private final Iterator<Row> rows = table.rows().iterator();
          private Row current;

          public boolean hasNext() {
            return rows.hasNext();
          }

          public MachineRecipe<Input, Ic2FluidStack> next() {
            current = rows.next();
            return new MachineRecipe<>(
                new Input(current.fluid, current.identity.additive),
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
