package ic2ce.neoforge.recipe.processing;

import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.world.item.*;

/** Exact distribution boundaries, ownership, independent managers and actual one-box payment. */
public final class ScrapboxContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String text) {
    checks++;
    if (!ok) failures.add("native scrapbox: " + text);
  }

  public static Result run() {
    return new ScrapboxContracts().exercise();
  }

  private Result exercise() {
    var first = new ScrapboxDrops();
    var iron = new ItemStack(Items.IRON_INGOT, 2);
    first.addDrop(iron, 1);
    first.addDrop(new ItemStack(Items.GOLD_INGOT), 3);
    iron.setCount(64);
    check(
        first.sample(0).is(Items.IRON_INGOT)
            && first.sample(.249).getCount() == 2
            && first.sample(.25).is(Items.GOLD_INGOT)
            && first.sample(1).is(Items.GOLD_INGOT),
        "precise cumulative boundaries preserve weights and owned physical count");
    var second = new ScrapboxDrops();
    second.addDrop(new ItemStack(Items.DIAMOND), 1);
    check(
        second.sample(.99).is(Items.DIAMOND) && first.sample(.5).is(Items.GOLD_INGOT),
        "manager totals are independent");
    for (var stack : first.getDrops().keySet()) stack.setCount(99);
    check(
        first.sample(0).getCount() == 2,
        "public drop map cannot mutate native distribution templates");
    boolean invalid = false;
    try {
      first.addDrop(new ItemStack(Items.DIAMOND), Float.NaN);
    } catch (IllegalArgumentException expected) {
      invalid = true;
    }
    check(invalid && first.getDrops().size() == 2, "invalid weight rejected before publication");
    var box = new ItemStack(ItemDefinitions.SCRAP_BOX, 3);
    check(
        first.getDrop(box, false) != null && box.getCount() == 3,
        "preview leaves physical boxes untouched");
    first.getDrop(box, true);
    check(box.getCount() == 2, "actual drop pays exactly one box");
    first.getOutputFor(box, true);
    check(box.getCount() == 1, "legacy adjusted output pays exactly one box");
    return new Result(checks, List.copyOf(failures));
  }
}
