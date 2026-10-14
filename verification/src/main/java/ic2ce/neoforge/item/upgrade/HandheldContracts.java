package ic2ce.neoforge.item.upgrade;

import ic2ce.core.util.StackTag;
import ic2ce.neoforge.handheld.ToolboxStorage;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;

/** Native handheld identity, quantity, ghost filters and real toolbox contents. */
public final class HandheldContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native handheld: " + label);
  }

  public static Result run(ServerLevel world) {
    return new HandheldContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("190713af-e702-43ad-bb5f-6a0e6ed2a29f"), "IC2HandheldProbe"));
    player.getInventory().clearContent();
    var upgrade = new ItemStack(ItemDefinitions.ADVANCED_EJECTOR_UPGRADE, 64);
    player.setItemInHand(InteractionHand.MAIN_HAND, upgrade);
    var inventory = new AdvancedFilterInventory(player, InteractionHand.MAIN_HAND, upgrade);
    var original = new ItemStack(Items.IRON_INGOT, 17);
    inventory.setItem(0, original);
    original.setCount(1);
    check(
        player.getMainHandItem() == upgrade
            && upgrade.getCount() == 64
            && inventory.getItem(0).getCount() == 17,
        "saving a 64-upgrade physical stack keeps its original count and owns the ghost prototype");
    inventory.onEvent("meta");
    inventory.onEvent("energy");
    check(
        StackTag.get(upgrade).getCompoundOrEmpty("metaSettings").getBooleanOr("active", false)
            && StackTag.get(upgrade)
                .getCompoundOrEmpty("energySettings")
                .getBooleanOr("active", false),
        "actual advanced menu toggle callbacks write both independent setting groups");
    var other = new ItemStack(ItemDefinitions.ADVANCED_EJECTOR_UPGRADE, 64);
    check(
        !inventory.isThisContainer(other),
        "another identical untagged upgrade stack cannot impersonate the opened physical UID");
    inventory.onScreenClosed(player);
    check(
        !StackTag.get(upgrade).contains("uid")
            && upgrade.getCount() == 64
            && StackTag.get(upgrade).getListOrEmpty("Items").size() == 1,
        "ordinary native close preserves filter data and complete stack quantity while retiring the"
            + " session UID");
    var reopened = new AdvancedFilterInventory(player, InteractionHand.MAIN_HAND, upgrade);
    check(
        reopened.getItem(0).is(Items.IRON_INGOT) && reopened.getItem(0).getCount() == 17,
        "native reopen reads the exact saved ghost slot quantity");
    var thrown = upgrade.copyWithCount(1);
    reopened.saveAsThrown(thrown);
    check(
        thrown.getCount() == 1
            && !StackTag.get(thrown).contains("uid")
            && !reopened.stillValid(player),
        "actual thrown quantity remains one and owns its persisted filters after the session"
            + " closes");
    var box = new ItemStack(ItemDefinitions.TOOL_BOX);
    player.setItemInHand(InteractionHand.OFF_HAND, box);
    var toolbox = new ToolboxStorage(player, InteractionHand.OFF_HAND, box, 9);
    toolbox.setItem(2, new ItemStack(ItemDefinitions.WRENCH));
    var taken = toolbox.removeItem(2, -1);
    check(
        taken.isEmpty() && toolbox.getItem(2).is(ItemDefinitions.WRENCH),
        "negative extraction cannot increase or manufacture native tool quantity");
    taken = toolbox.removeItem(2, 1);
    check(
        taken.is(ItemDefinitions.WRENCH) && toolbox.getItem(2).isEmpty(),
        "native toolbox extraction returns exactly the stored physical tool and persists empty"
            + " contents");
    toolbox.setItem(3, new ItemStack(ItemDefinitions.CUTTER));
    toolbox.onScreenClosed(player);
    var restored = new ToolboxStorage(player, InteractionHand.OFF_HAND, box, 9);
    check(
        restored.getItem(3).is(ItemDefinitions.CUTTER) && restored.getItem(2).isEmpty(),
        "offhand toolbox close and reopen preserve both the retained and previously removed slots");
    restored.setItem(4, box);
    check(
        restored.getItem(4).isEmpty() && player.getOffhandItem().is(ItemDefinitions.TOOL_BOX),
        "a self-referential physical container cannot recurse into its own saved contents");
    restored.onScreenClosed(player);
    player.getInventory().clearContent();
    player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    return new Result(checks, List.copyOf(failures));
  }
}
