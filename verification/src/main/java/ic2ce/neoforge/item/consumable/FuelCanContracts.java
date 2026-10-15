package ic2ce.neoforge.item.consumable;

import ic2ce.neoforge.item.StackTag;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Native component replacement updates fuel immediately; physical remainder templates own every
 * created stack.
 */
public final class FuelCanContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run() {
    var failures = new ArrayList<String>();
    int checks = 0;
    var item = (FuelCan) ConsumableItems.FILLED_FUEL_CAN;
    var stack = new ItemStack(item);
    for (int ticks : new int[] {0, 1, 400, 20000, -1, Integer.MAX_VALUE, 0}) {
      StackTag.update(
          stack,
          data -> {
            data.putInt("value", ticks);
            return data;
          });
      checks++;
      if (item.getBurnTime(stack, RecipeType.SMELTING, null) != Math.max(0, ticks)
          || item.getBurnTime(stack, RecipeType.SMELTING, null) != Math.max(0, ticks))
        failures.add(
            "fuel can: immutable physical component generation retained a stale or negative burn"
                + " value");
    }
    checks++;
    if (item.getBurnTime(ItemStack.EMPTY, RecipeType.SMELTING, null) != 0
        || item.getBurnTime(new ItemStack(Items.COAL), RecipeType.SMELTING, null) != 0)
      failures.add("fuel can: unrelated or empty physical stacks supplied fuel");
    var template = item.getCraftingRemainder(stack);
    var first = template.create();
    first.setCount(0);
    checks++;
    if (!template.create().is(ConsumableItems.EMPTY_FUEL_CAN) || template.create().getCount() != 1)
      failures.add(
          "fuel can: one consumed physical fuel can does not leave an independent empty can");
    checks++;
    if (item.getCraftingRemainder(new ItemStack(Items.COAL)) != null)
      failures.add("fuel can: unrelated native item instance acquired an empty fuel container");
    return new Result(checks, List.copyOf(failures));
  }
}
