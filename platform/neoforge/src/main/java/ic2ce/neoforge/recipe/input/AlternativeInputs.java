package ic2ce.neoforge.recipe.input;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import ic2ce.api.recipe.IRecipeInput;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;

/** Ordered alternatives own their structure and share an explicit consumption count. */
public final class AlternativeInputs extends InputDescriptor {
  private final List<IRecipeInput> alternatives;
  private final int amount;

  public AlternativeInputs(int amount, IRecipeInput... inputs) {
    this(amount, Arrays.asList(inputs));
  }

  public AlternativeInputs(int amount, List<IRecipeInput> inputs) {
    this.amount = positive(amount);
    alternatives = List.copyOf(Objects.requireNonNull(inputs));
  }

  public List<IRecipeInput> alternatives() {
    return alternatives;
  }

  @Override
  public boolean matches(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return false;
    for (var input : alternatives) if (input.matches(stack)) return true;
    return false;
  }

  @Override
  public int getAmount() {
    return amount;
  }

  @Override
  protected List<ItemStack> listStacks() {
    return alternatives.stream().flatMap(input -> input.getInputs().stream()).toList();
  }

  @Override
  public Ingredient getIngredient() {
    return CompoundIngredient.of(
        alternatives.stream().map(IRecipeInput::getIngredient).toArray(Ingredient[]::new));
  }

  @Override
  public JsonElement toJson() {
    var array = new JsonArray();
    for (var input : alternatives)
      array.add(
          input instanceof InputDescriptor nativeInput
              ? nativeInput.toJson()
              : Ingredient.CODEC
                  .encodeStart(JsonOps.INSTANCE, input.getIngredient())
                  .getOrThrow(IllegalStateException::new));
    var object = new JsonObject();
    object.add("any", array);
    object.addProperty("count", amount);
    return object;
  }

  @Override
  public boolean equals(Object value) {
    return value instanceof AlternativeInputs other
        && amount == other.amount
        && alternatives.equals(other.alternatives);
  }

  @Override
  public int hashCode() {
    return 31 * alternatives.hashCode() + amount;
  }

  @Override
  public String toString() {
    return "AlternativeInput[" + amount + ", " + alternatives + "]";
  }
}
