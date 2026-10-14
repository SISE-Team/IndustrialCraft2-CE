package ic2ce.neoforge.recipe.codec;

import ic2ce.api.recipe.MachineRecipe;
import ic2ce.engine.recipe.DeferredValue;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;

/**
 * Native recipe values resolve separately only after world binding, with complete thread-safe
 * publication.
 */
public final class DeferredMachineRecipe<I, O> extends MachineRecipe<I, O> {
  private final DeferredValue<I> input;
  private final DeferredValue<O> output;
  private final DeferredValue<CompoundTag> metadata;

  public DeferredMachineRecipe(Supplier<I> input, Supplier<O> output) {
    this(input, output, () -> null);
  }

  public DeferredMachineRecipe(
      Supplier<I> input, Supplier<O> output, Supplier<CompoundTag> metadata) {
    super(null, null, null);
    this.input =
        new DeferredValue<>(
            () -> Objects.requireNonNull(input.get(), "Null deferred recipe input"));
    this.output =
        new DeferredValue<>(
            () -> Objects.requireNonNull(output.get(), "Null deferred recipe output"));
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
  public O getOutput() {
    return output.get();
  }

  @Override
  public CompoundTag getMetaData() {
    var data = metadata.get();
    return data == null ? null : data.copy();
  }
}
