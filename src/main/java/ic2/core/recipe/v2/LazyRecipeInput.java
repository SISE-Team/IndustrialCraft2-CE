package ic2.core.recipe.v2;

import ic2.api.recipe.IRecipeInput;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Defers the parsing of an IC2 recipe input until first use.
 *
 * <p>26.1.2 parses datapack recipes while loading a world's resources, which
 * happens BEFORE holder components are bound and before static-registry tags
 * are usable. Constructing ItemStacks or resolving item tags at parse time
 * therefore throws. Wrapping the parse logic in this class postpones it to the
 * first {@link #matches}/{@link #getInputs} call, which only ever happens once
 * a world is running.</p>
 */
public final class LazyRecipeInput implements IRecipeInput {
   private final Supplier<IRecipeInput> supplier;
   private final Ingredient placementIngredient;
   private IRecipeInput delegate;

   public LazyRecipeInput(Supplier<IRecipeInput> supplier) {
      this(supplier, null);
   }

   public LazyRecipeInput(Supplier<IRecipeInput> supplier, Ingredient placementIngredient) {
      this.supplier = supplier;
      this.placementIngredient = placementIngredient;
   }

   /** Forces (and returns) the parsed delegate. */
   public IRecipeInput unwrap() {
      if (this.delegate == null) {
         this.delegate = this.supplier.get();
      }
      return this.delegate;
   }

   @Override
   public boolean matches(ItemStack stack) {
      return this.unwrap().matches(stack);
   }

   @Override
   public int getAmount() {
      return this.unwrap().getAmount();
   }

   @Override
   public List<ItemStack> getInputs() {
      return this.unwrap().getInputs();
   }

   @Override
   public Ingredient getIngredient() {
      return this.placementIngredient != null ? this.placementIngredient : this.unwrap().getIngredient();
   }
}
