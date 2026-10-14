package ic2ce.api.recipe;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Typed recipe association. Generic value ownership belongs to its adapter; metadata is always
 * owned.
 */
public class MachineRecipe<I, O> {
  private final I input;
  private final O output;
  private final CompoundTag metadata;

  public MachineRecipe(I input, O output) {
    this(input, output, null);
  }

  public MachineRecipe(I input, O output, @Nullable CompoundTag metadata) {
    this.input = input;
    this.output = output;
    this.metadata = metadata == null ? null : metadata.copy();
  }

  public I getInput() {
    return input;
  }

  public O getOutput() {
    return output;
  }

  @Nullable
  public CompoundTag getMetaData() {
    return metadata == null ? null : metadata.copy();
  }

  public <AI> MachineRecipeResult<I, O, AI> getResult(AI adjustedInput) {
    return new MachineRecipeResult<>(this, adjustedInput);
  }
}
