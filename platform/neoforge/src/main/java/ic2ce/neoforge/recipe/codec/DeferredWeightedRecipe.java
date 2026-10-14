package ic2ce.neoforge.recipe.codec;

import ic2ce.api.recipe.MachineRecipeWeighted;
import ic2ce.api.recipe.RecipeOutputWeighted;
import ic2ce.engine.recipe.DeferredValue;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Weighted parsing publishes the finished table; failed/reentrant parsing cannot expose a partial
 * distribution.
 */
public final class DeferredWeightedRecipe<I> extends MachineRecipeWeighted<I> {
  private final DeferredValue<I> input;
  private final DeferredValue<RecipeOutputWeighted> outputs;
  private final DeferredValue<CompoundTag> metadata;

  public DeferredWeightedRecipe(
      Supplier<I> input, Consumer<RecipeOutputWeighted> parse, Supplier<CompoundTag> metadata) {
    super(null, new RecipeOutputWeighted(), null);
    this.input =
        new DeferredValue<>(
            () -> Objects.requireNonNull(input.get(), "Null weighted recipe input"));
    this.outputs =
        new DeferredValue<>(
            () -> {
              var table = new RecipeOutputWeighted();
              parse.accept(table);
              return table;
            });
    this.metadata =
        new DeferredValue<>(
            () -> {
              var data = metadata.get();
              return data == null ? null : data.copy();
            });
  }

  @Override
  public I getInput() {
    return input.get();
  }

  @Override
  public RecipeOutputWeighted getOutputWeighted() {
    return outputs.get();
  }

  @Override
  public Collection<ItemStack> getOutput() {
    return super.getOutput();
  }

  @Override
  public CompoundTag getMetaData() {
    var data = metadata.get();
    return data == null ? null : data.copy();
  }
}
