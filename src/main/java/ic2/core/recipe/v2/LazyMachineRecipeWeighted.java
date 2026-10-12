package ic2.core.recipe.v2;

import ic2.api.recipe.MachineRecipeWeighted;
import ic2.api.recipe.RecipeOutputWeighted;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Weighted machine recipe with the input and weighted outputs parsed lazily.
 *
 * <p>The {@link RecipeOutputWeighted} handed to the super constructor starts
 * empty and is populated on first access by {@link #ensureParsed()} (see
 * {@link LazyMachineRecipe} for why parsing must be deferred).</p>
 */
public class LazyMachineRecipeWeighted<I> extends MachineRecipeWeighted<I> {
   private final Supplier<I> inputSupplier;
   private final Supplier<CompoundTag> metaSupplier;
   private final java.util.function.Consumer<RecipeOutputWeighted> outputParser;
   private I input;
   private CompoundTag meta;
   private boolean metaResolved;
   private boolean parsed;

   public LazyMachineRecipeWeighted(
      Supplier<I> inputSupplier,
      java.util.function.Consumer<RecipeOutputWeighted> outputParser,
      Supplier<CompoundTag> metaSupplier
   ) {
      super(null, new RecipeOutputWeighted(), null);
      this.inputSupplier = inputSupplier;
      this.outputParser = outputParser;
      this.metaSupplier = metaSupplier;
   }

   private void ensureParsed() {
      if (!this.parsed) {
         this.parsed = true;
         this.outputParser.accept(this.getOutputWeighted());
      }
   }

   @Override
   public I getInput() {
      if (this.input == null) {
         this.input = this.inputSupplier.get();
      }
      return this.input;
   }

   @Override
   public java.util.Collection<ItemStack> getOutput() {
      this.ensureParsed();
      return super.getOutput();
   }

   @Override
   public RecipeOutputWeighted getOutputWeighted() {
      this.ensureParsed();
      return super.getOutputWeighted();
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
