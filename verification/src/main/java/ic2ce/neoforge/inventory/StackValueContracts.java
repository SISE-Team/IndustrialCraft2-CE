package ic2ce.neoforge.inventory;

import ic2ce.neoforge.item.StackTag;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * Actual native inventory conservation, component identity and payment failures are checked
 * independently of helper plans.
 */
public final class StackValueContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String text) {
    checks++;
    if (!ok) failures.add("native stack values: " + text);
  }

  public static Result run(ServerLevel world) {
    return new StackValueContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var actor =
        FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("bb659dc5-1111-4111-8111-555500001616"), "StackValueProbe"));
    actor.getAbilities().instabuild = false;
    actor.getInventory().clearContent();
    actor.getInventory().setItem(0, new ItemStack(Items.COAL, 2));
    actor.getInventory().setItem(1, new ItemStack(Items.COAL, 3));
    check(
        !StackUtil.consumeFromPlayerInventory(actor, stack -> stack.is(Items.COAL), 6, false)
            && actor.getInventory().getItem(0).getCount() == 2
            && actor.getInventory().getItem(1).getCount() == 3,
        "insufficient complete player bill cannot report success or consume a partial source");
    check(
        StackUtil.consumeFromPlayerInventory(actor, stack -> stack.is(Items.COAL), 5, true)
            && actor.getInventory().getItem(0).getCount() == 2,
        "complete multi-slot prediction is inert");
    var paid = StackUtil.consumeFromPlayerInventoryAndGet(actor, stack -> stack.is(Items.COAL), 5);
    check(
        paid.is(Items.COAL)
            && paid.getCount() == 5
            && actor.getInventory().getItem(0).isEmpty()
            && actor.getInventory().getItem(1).isEmpty(),
        "owned multi-slot payment combines the actual item identity and pays every requested item"
            + " once");
    var named = new ItemStack(Items.COAL);
    named.set(DataComponents.CUSTOM_NAME, Component.literal("owned"));
    var plain = new ItemStack(Items.COAL);
    check(
        !StackUtil.checkItemEqualityStrict(named, plain)
            && !StackUtil.checkItemEquality(named, plain),
        "native custom-name components prevent incompatible item merging");
    var battery = new ItemStack(ic2ce.neoforge.registry.ItemDefinitions.RE_BATTERY);
    var another = battery.copy();
    StackTag.update(
        battery,
        tag -> {
          tag.putDouble("charge", 7);
          return tag;
        });
    StackTag.update(
        another,
        tag -> {
          tag.putDouble("charge", 15);
          return tag;
        });
    check(
        StackUtil.checkItemEquality(battery, another)
            && !StackUtil.checkItemEqualityStrict(battery, another),
        "ordinary comparisons explicitly ignore charge metadata while physical strict comparison"
            + " retains it");
    actor.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.COAL, 2));
    actor.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
    check(
        StackUtil.consume(actor, InteractionHand.OFF_HAND, stack -> stack.is(Items.COAL), 2)
            && actor.getOffhandItem().isEmpty()
            && actor.getMainHandItem().is(Items.STICK),
        "actual off-hand bill preserves independent main-hand equipment");
    var sword = new ItemStack(Items.WOODEN_SWORD);
    sword.setDamageValue(sword.getMaxDamage() - 1);
    actor.setItemInHand(InteractionHand.OFF_HAND, sword);
    check(
        StackUtil.damage(actor, InteractionHand.OFF_HAND, StackUtil.anyStack, 1)
            && actor.getOffhandItem().isEmpty(),
        "successfully paid last durability unit returns success after the tool becomes empty");
    var random = new Random(0x71ad);
    for (int scenario = 0; scenario < 500; scenario++) {
      var inventory = new SimpleContainer(9);
      int before = 0;
      for (int slot = 0; slot < 9; slot++) {
        int amount = random.nextInt(65);
        if (amount > 0) inventory.setItem(slot, new ItemStack(Items.COAL, amount));
        before += amount;
      }
      var offered = new ItemStack(Items.COAL, random.nextInt(65));
      int extra = offered.getCount();
      var result =
          StackUtil.balanceStacks(
              inventory,
              pair -> pair.b != 3,
              StackUtil.getSlotsFromInv(inventory),
              List.of(offered));
      int after = result.a.stream().mapToInt(ItemStack::getCount).sum();
      boolean bounds = true;
      for (int slot = 0; slot < 9; slot++) {
        after += inventory.getItem(slot).getCount();
        bounds &= inventory.getItem(slot).getCount() <= 64;
      }
      check(
          after == before + extra && bounds && offered.getCount() == extra,
          "native balancing conserves independent counts through restricted slots and owned excess:"
              + " "
              + scenario);
    }
    actor.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }
}
