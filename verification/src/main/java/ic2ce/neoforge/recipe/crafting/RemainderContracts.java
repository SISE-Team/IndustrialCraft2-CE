package ic2ce.neoforge.recipe.crafting;

import ic2ce.neoforge.item.tool.ToolItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;

/**
 * Native crafting inputs are observed after ordinary, final, unbreakable and consuming tool uses.
 */
public final class RemainderContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run() {
    var failures = new ArrayList<String>();
    int checks = 0;
    var hammer = new ItemStack(ToolItems.FORGE_HAMMER);
    hammer.setDamageValue(7);
    var milk = new ItemStack(Items.MILK_BUCKET);
    var input = CraftingInput.of(2, 1, List.of(hammer, milk));
    var remainders = CraftingRemainders.get(input, false);
    checks++;
    if (remainders.get(0).getDamageValue() != 8
        || hammer.getDamageValue() != 7
        || remainders.get(0) == hammer)
      failures.add("crafting: tool wear mutated a borrowed input");
    checks++;
    if (!remainders.get(1).is(Items.BUCKET) || !milk.is(Items.MILK_BUCKET))
      failures.add("crafting: actual native bucket remainder replaced its source");
    hammer.setDamageValue(hammer.getMaxDamage() - 1);
    checks++;
    if (!CraftingRemainders.get(input, false).get(0).isEmpty())
      failures.add("crafting: exhausted final tool use created a reusable remainder");
    hammer.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
    checks++;
    var immortal = CraftingRemainders.get(input, false).get(0);
    if (immortal.isEmpty()
        || immortal.getDamageValue() != hammer.getDamageValue()
        || immortal == hammer)
      failures.add("crafting: unbreakable tool was consumed, worn or exported by reference");
    checks++;
    if (CraftingRemainders.get(input, true).stream().anyMatch(stack -> !stack.isEmpty()))
      failures.add("crafting: consuming recipe retained physical ingredients");
    return new Result(checks, List.copyOf(failures));
  }
}
