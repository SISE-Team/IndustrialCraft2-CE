package ic2ce.neoforge.item.upgrade;

import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;

/** Native submenu close writes survive a new physical editor and change real filter decisions. */
public final class UpgradeMenuContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native upgrade menu: " + label);
  }

  public static Result run(ServerLevel world) {
    return new UpgradeMenuContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("6cf67f6c-a118-4ad4-9c08-a2bb2c60f95e"), "IC2FilterMenuProbe"));
    player.getInventory().clearContent();
    var source = new ItemStack(ItemDefinitions.ADVANCED_EJECTOR_UPGRADE, 32);
    player.setItemInHand(InteractionHand.MAIN_HAND, source);
    var base = new AdvancedFilterInventory(player, InteractionHand.MAIN_HAND, source);
    var option = new NumericFilterEditor(base, "energy");
    var menu = option.new ContainerValueConfig(121);
    menu.comparisonType = FilterMode.RANGE;
    menu.normalBox = "4000";
    menu.extraBox = "6000";
    menu.normalSetting = IntegerRelation.LESS_OR_EQUAL;
    menu.extraSetting = IntegerRelation.LESS_OR_EQUAL;
    menu.removed(player);
    var saved = StackTag.get(source).getCompoundOrEmpty("energySettings");
    var settings = new NumericFilter(saved);
    check(
        source.getCount() == 32
            && settings.doComparison(4000)
            && settings.doComparison(6000)
            && !settings.doComparison(3999)
            && !settings.doComparison(6001),
        "actual native range-menu close preserves stack quantity and writes both inclusive"
            + " comparisons into the root component");
    var fresh = new AdvancedFilterInventory(player, InteractionHand.MAIN_HAND, source);
    var restored = new NumericFilterEditor(fresh, "energy");
    check(
        restored.initialComparisonType == FilterMode.RANGE
            && restored.initialNormalBox.equals("4000")
            && restored.initialExtraBox.equals("6000"),
        "a newly constructed editor restores actual saved numeric values");
    var malformed = restored.new ContainerValueConfig(122);
    malformed.normalBox = "untrusted-integer";
    malformed.extraBox = "99999999999999999999999999";
    malformed.removed(player);
    check(
        FilterMode.getFromNBT(
                StackTag.get(source)
                    .getCompoundOrEmpty("energySettings")
                    .getByteOr("type", (byte) -1))
            == FilterMode.DIRECT,
        "malformed synchronized scalar fields normalize before persisted machine filtering");
    var data = StackTag.get(source);
    data.remove("energySettings");
    data.remove("Items");
    StackTag.set(source, data);
    var tagBase = new AdvancedFilterInventory(player, InteractionHand.MAIN_HAND, source);
    var tagOption = new MaterialTagEditor(tagBase);
    var tagMenu = tagOption.new ContainerEditOre(123);
    tagMenu.selectedTag = "minecraft:planks";
    tagMenu.removed(player);
    var matcher = CompiledUpgradeFilter.of(source);
    check(
        matcher.apply(new ItemStack(Items.OAK_PLANKS))
            && matcher.apply(new ItemStack(Items.BIRCH_PLANKS))
            && !matcher.apply(new ItemStack(Items.IRON_INGOT)),
        "real native tag editor selects multiple actual material variants and rejects non-tag"
            + " inputs");
    var previous = matcher;
    StackTag.update(
        source,
        root -> {
          root.getCompoundOrEmpty("oreSettings").putString("tag", "minecraft:invalid_unregistered");
          var tag = root.getCompoundOrEmpty("oreSettings");
          tag.putString("tag", "minecraft:invalid_unregistered");
          root.put("oreSettings", tag);
          return root;
        });
    matcher = CompiledUpgradeFilter.of(source);
    check(
        matcher != previous && !matcher.apply(new ItemStack(Items.OAK_PLANKS)),
        "replacing the actual settings component invalidates cached tag decisions immediately");
    tagBase.onScreenClosed(player);
    player.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }
}
