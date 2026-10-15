package ic2ce.neoforge.registry;

import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.item.energy.BatteryItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Real registered item variants resolve through native output callbacks and stable textual tab IDs.
 */
public final class CreativeContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run() {
    var failures = new ArrayList<String>();
    int checks = 0;
    var stacks = new ArrayList<ItemStack>();
    var tools = Identifier.fromNamespaceAndPath("ic2", "tools_and_utilities");
    CreativeContents.accept(tools, (stack, visibility) -> stacks.add(stack));
    checks++;
    if (stacks.size() < 87
        || stacks.stream().anyMatch(stack -> stack.isEmpty() || stack.getCount() != 1))
      failures.add(
          "creative: native physical tool tab omitted members or emitted invalid quantities");
    var batteries = stacks.stream().filter(stack -> stack.is(BatteryItems.RE_BATTERY)).toList();
    checks++;
    if (batteries.size() != 2
        || batteries.stream().noneMatch(stack -> ElectricItem.manager.getCharge(stack) == 0)
        || batteries.stream().noneMatch(stack -> ElectricItem.manager.getCharge(stack) > 0))
      failures.add("creative: typed actual battery variants lost empty or charged state");
    if (!batteries.isEmpty()) batteries.getFirst().setCount(0);
    var next = new ArrayList<ItemStack>();
    CreativeContents.accept(tools, (stack, visibility) -> next.add(stack));
    checks++;
    if (next.stream().filter(stack -> stack.is(BatteryItems.RE_BATTERY)).count() != 2)
      failures.add("creative: borrowed output mutation changed a later generation");
    var bucket = FluidDefinitions.BIOGAS.bucket;
    CreativeContents.assignDynamic(
        bucket, Identifier.fromNamespaceAndPath("ic2", "tools_and_utilities"));
    checks++;
    if (!CreativeContents.isIn(bucket, CreativeTabs.TOOLS_AND_UTILITIES)
        || !CreativeContents.isIn(BatteryItems.RE_BATTERY, tools))
      failures.add(
          "creative: equivalent native tab identifiers broke static or dynamic membership");
    var emitted = new ArrayList<ItemStack>();
    CreativeContents.accept(
        CreativeTabs.TOOLS_AND_UTILITIES, (stack, visibility) -> emitted.add(stack));
    checks++;
    if (emitted.stream().filter(stack -> stack.is(bucket)).count() != 1)
      failures.add("creative: dynamic bucket assignment duplicated or omitted the physical item");
    return new Result(checks, List.copyOf(failures));
  }
}
