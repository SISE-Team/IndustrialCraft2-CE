package ic2ce.neoforge.recipe.codec;

import ic2ce.api.recipe.IRecipeInput;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Registry-dependent input publishes once, after world component binding, to all querying threads.
 */
public final class DeferredInput implements IRecipeInput {
  private Supplier<IRecipeInput> supplier;
  private final Ingredient placementIngredient;
  private volatile IRecipeInput delegate;
  private boolean resolving;

  public DeferredInput(Supplier<IRecipeInput> supplier) {
    this(supplier, null);
  }

  public DeferredInput(Supplier<IRecipeInput> supplier, Ingredient placementIngredient) {
    this.supplier = Objects.requireNonNull(supplier);
    this.placementIngredient = placementIngredient;
  }

  public IRecipeInput resolvedInput() {
    return delegate;
  }

  public IRecipeInput unwrap() {
    var current = delegate;
    if (current != null) return current;
    synchronized (this) {
      if (delegate != null) return delegate;
      if (resolving) throw new IllegalStateException("Recursive recipe input resolution");
      resolving = true;
      try {
        current = Objects.requireNonNull(supplier.get(), "Recipe input supplier returned null");
        if (current == this) throw new IllegalStateException("Self-referential recipe input");
        delegate = current;
        supplier = null;
        return current;
      } finally {
        resolving = false;
      }
    }
  }

  @Override
  public boolean matches(ItemStack stack) {
    return unwrap().matches(stack);
  }

  @Override
  public int getAmount() {
    return unwrap().getAmount();
  }

  @Override
  public List<ItemStack> getInputs() {
    return unwrap().getInputs();
  }

  @Override
  public Ingredient getIngredient() {
    return placementIngredient != null ? placementIngredient : unwrap().getIngredient();
  }
}
