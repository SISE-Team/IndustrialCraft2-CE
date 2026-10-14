package ic2ce.api.recipe;

import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

/**
 * Physical matching and quantity are independent of the default native item-only placement display.
 */
@NonExtendable
public interface IRecipeInput {
  boolean matches(ItemStack stack);

  int getAmount();

  List<ItemStack> getInputs();

  default Ingredient getIngredient() {
    if (getAmount() <= 0)
      throw new IllegalArgumentException("Recipe display requires a positive physical quantity");
    var candidates =
        Objects.requireNonNull(getInputs()).stream()
            .filter(stack -> stack != null && !stack.isEmpty())
            .map(ItemStack::getItem)
            .distinct()
            .toList();
    if (candidates.isEmpty())
      throw new IllegalStateException("Recipe input has no native item display");
    return Ingredient.of(candidates.stream());
  }
}
