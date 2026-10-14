package ic2ce.neoforge.recipe.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Native ingredients retain their live tag/custom predicate and fresh display samples. */
public final class NativeIngredientInput extends InputDescriptor {
  private final Ingredient ingredient;
  private final int amount;

  public NativeIngredientInput(Ingredient ingredient, int amount) {
    this.ingredient = Objects.requireNonNull(ingredient);
    this.amount = positive(amount);
  }

  @Override
  public boolean matches(ItemStack stack) {
    return stack != null && !stack.isEmpty() && ingredient.test(stack);
  }

  @Override
  protected List<ItemStack> listStacks() {
    return (ingredient.getCustomIngredient() instanceof MaterialTagIngredient material
            ? material.items()
            : ingredient.items())
        .map(ItemStack::new)
        .toList();
  }

  @Override
  public int getAmount() {
    return amount;
  }

  @Override
  public Ingredient getIngredient() {
    return ingredient;
  }

  @Override
  public JsonElement toJson() {
    JsonElement encoded =
        Ingredient.CODEC
            .encodeStart(ic2ce.neoforge.recipe.codec.RecipeValueCodec.nativeOps(), ingredient)
            .getOrThrow(IllegalStateException::new);
    if (amount == 1) return encoded;
    JsonObject object = new JsonObject();
    object.add("ingredient", encoded);
    object.addProperty("count", amount);
    return object;
  }

  @Override
  public boolean equals(Object value) {
    return value instanceof NativeIngredientInput other
        && amount == other.amount
        && ingredient.equals(other.ingredient);
  }

  @Override
  public int hashCode() {
    return 31 * ingredient.hashCode() + amount;
  }

  @Override
  public String toString() {
    return "NativeInput[" + amount + ", " + ingredient + "]";
  }
}
