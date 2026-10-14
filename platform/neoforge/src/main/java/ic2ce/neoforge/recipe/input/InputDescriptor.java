package ic2ce.neoforge.recipe.input;

import com.google.gson.JsonElement;
import ic2ce.api.recipe.IRecipeInput;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Display samples are disposable views; live predicates never use a borrowed sample. */
public abstract class InputDescriptor implements IRecipeInput {
  public abstract JsonElement toJson();

  protected abstract List<ItemStack> listStacks();

  @Override
  public final List<ItemStack> getInputs() {
    int count = positive(getAmount());
    return listStacks().stream()
        .filter(stack -> stack != null && !stack.isEmpty())
        .map(stack -> stack.copyWithCount(count))
        .toList();
  }

  public static int positive(int amount) {
    if (amount < 1)
      throw new IllegalArgumentException("Recipe quantity must be positive: " + amount);
    return amount;
  }
}
