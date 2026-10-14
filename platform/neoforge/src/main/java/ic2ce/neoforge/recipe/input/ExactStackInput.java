package ic2ce.neoforge.recipe.input;

import com.google.gson.JsonElement;
import ic2ce.neoforge.recipe.codec.RecipeValueCodec;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

/** Exact native components are part of identity; count is a separate consumption bill. */
public final class ExactStackInput extends InputDescriptor {
  private final ItemStack prototype;
  private final int hash;

  public ExactStackInput(ItemStack stack) {
    Objects.requireNonNull(stack);
    if (stack.isEmpty()) throw new IllegalArgumentException("Empty recipe input");
    prototype = stack.copy();
    hash = 31 * ItemStack.hashItemAndComponents(prototype) + prototype.getCount();
  }

  public ItemStack prototype() {
    return prototype.copy();
  }

  @Override
  public boolean matches(ItemStack subject) {
    return subject != null
        && !subject.isEmpty()
        && ItemStack.isSameItemSameComponents(prototype, subject);
  }

  @Override
  public int getAmount() {
    return prototype.getCount();
  }

  @Override
  protected List<ItemStack> listStacks() {
    return List.of(prototype);
  }

  @Override
  public Ingredient getIngredient() {
    return DataComponentIngredient.of(true, prototype);
  }

  @Override
  public JsonElement toJson() {
    return RecipeValueCodec.resultToJson(prototype);
  }

  @Override
  public boolean equals(Object value) {
    return value instanceof ExactStackInput other && ItemStack.matches(prototype, other.prototype);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    return "ExactInput[" + prototype + "]";
  }
}
