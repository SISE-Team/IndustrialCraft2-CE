package ic2ce.core.uu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Converts live recipe choices without dropping ingredient slots or their required quantities. */
final class RecipeUtil {
  private RecipeUtil() {}

  public static List<List<LeanItemStack>> fixIngredientSize(List<ItemStack>[] groups) {
    var result = new ArrayList<List<LeanItemStack>>(groups.length);
    for (var group : groups)
      if (group != null) {
        var alternatives = new ArrayList<LeanItemStack>(group.size());
        for (var stack : group)
          if (stack != null && !stack.isEmpty()) alternatives.add(new LeanItemStack(stack, 1));
        result.add(List.copyOf(alternatives));
      }
    return List.copyOf(result);
  }

  public static List<List<LeanItemStack>> convertInputs(List<ItemStack> alternatives) {
    return List.of(convertOutputs(alternatives));
  }

  public static List<List<LeanItemStack>> convertIngredients(List<Ingredient> ingredients) {
    var result = new ArrayList<List<LeanItemStack>>(ingredients.size());
    for (var ingredient : ingredients) {
      var choices = new ArrayList<LeanItemStack>();
      ingredient.items().forEach(holder -> choices.add(new LeanItemStack(holder.value())));
      result.add(List.copyOf(choices));
    }
    return List.copyOf(result);
  }

  public static List<LeanItemStack> convertOutputs(Collection<ItemStack> stacks) {
    var result = new ArrayList<LeanItemStack>(stacks.size());
    for (var stack : stacks)
      if (stack != null && !stack.isEmpty()) result.add(new LeanItemStack(stack));
    return List.copyOf(result);
  }
}
