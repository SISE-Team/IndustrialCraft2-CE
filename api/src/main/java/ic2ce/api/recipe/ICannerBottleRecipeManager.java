package ic2ce.api.recipe;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/** Solid canning keeps two independent physical item bills. */
public interface ICannerBottleRecipeManager
    extends IMachineRecipeManager<
        ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> {
  @Deprecated
  void addRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output);

  @Deprecated
  RecipeOutput getOutputFor(
      ItemStack container, ItemStack fill, boolean adjustInput, boolean acceptanceOnly);

  final class Input {
    public final IRecipeInput container, fill;

    public Input(IRecipeInput container, IRecipeInput fill) {
      this.container = Objects.requireNonNull(container);
      this.fill = Objects.requireNonNull(fill);
    }

    public boolean matches(ItemStack container, ItemStack fill) {
      return container != null
          && fill != null
          && !container.isEmpty()
          && !fill.isEmpty()
          && this.container.matches(container)
          && this.fill.matches(fill);
    }
  }

  final class RawInput {
    public final ItemStack container, fill;

    public RawInput(ItemStack container, ItemStack fill) {
      this.container = Objects.requireNonNull(container).copy();
      this.fill = Objects.requireNonNull(fill).copy();
    }
  }
}
