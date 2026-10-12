package ic2.core.recipe.v2;

import ic2.api.recipe.MachineRecipe;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Machine recipe whose input/output/meta are parsed from JSON on first access.
 *
 * <p>26.1.2 loads datapack recipes before holder components are bound (see
 * {@link LazyRecipeInput}); deferring the parse keeps the vanilla recipe
 * manager load clean while still producing fully functional recipes by the
 * time machines, JEI or crafting code read them.</p>
 */
public class LazyMachineRecipe<I, O> extends MachineRecipe<I, O> {
   private final Supplier<I> inputSupplier;
   private final Supplier<O> outputSupplier;
   private final Supplier<CompoundTag> metaSupplier;
   private I input;
   private O output;
   private CompoundTag meta;
   private boolean metaResolved;

   public LazyMachineRecipe(Supplier<I> inputSupplier, Supplier<O> outputSupplier) {
      this(inputSupplier, outputSupplier, () -> null);
   }

   public LazyMachineRecipe(Supplier<I> inputSupplier, Supplier<O> outputSupplier, Supplier<CompoundTag> metaSupplier) {
      super(null, null, null);
      this.inputSupplier = inputSupplier;
      this.outputSupplier = outputSupplier;
      this.metaSupplier = metaSupplier;
   }

   @Override
   public I getInput() {
      if (this.input == null) {
         this.input = this.inputSupplier.get();
      }
      return this.input;
   }

   @Override
   public O getOutput() {
      if (this.output == null) {
         this.output = this.outputSupplier.get();
      }
      return this.output;
   }

   @Override
   @Nullable
   public CompoundTag getMetaData() {
      if (!this.metaResolved) {
         this.metaResolved = true;
         this.meta = this.metaSupplier.get();
      }
      return this.meta;
   }
}
