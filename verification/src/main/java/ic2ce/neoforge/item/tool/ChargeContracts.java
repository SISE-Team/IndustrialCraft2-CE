package ic2ce.neoforge.item.tool;


import ic2ce.api.item.*;
import ic2ce.core.util.StackTag;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

/**
 * Native component identity, malformed charge, exact billing and a real addon-source refusal
 * reproduce conservation edges.
 */
public final class ChargeContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native charge: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ChargeContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var battery = new ItemStack(ItemDefinitions.RE_BATTERY);
    var manager = ElectricItem.manager;
    check(
        manager.getCharge(battery) == 0 && !StackTag.has(battery),
        "empty charge reads cannot create a component or mutate a blank physical battery");
    check(
        manager.charge(battery, Double.POSITIVE_INFINITY, 1, false, true) == 100
            && !StackTag.has(battery),
        "real native fill simulation respects the battery transfer limit and preserves absence of"
            + " metadata");
    manager.charge(battery, 1000, 1, true, false);
    var before = battery.copy();
    check(
        manager.discharge(battery, 900, 1, true, true, true) == 900
            && ItemStack.matches(battery, before),
        "physical discharge simulation preserves every item component and count");
    check(
        manager.charge(battery, Double.NaN, 1, true, false) == 0
            && manager.discharge(battery, -100, 1, true, true, false) == 0
            && ItemStack.matches(battery, before),
        "NaN and negative requests cannot mutate native charge or manufacture output");
    var stacked = battery.copyWithCount(2);
    before = stacked.copy();
    check(
        manager.charge(stacked, 100, 1, true, false) == 0
            && manager.discharge(stacked, 100, 1, true, true, false) == 0
            && ItemStack.matches(stacked, before),
        "a multi-item stack cannot spend or receive one item's physical charge");
    StackTag.update(
        battery,
        data -> {
          data.putString("owned", "keep");
          data.putDouble("charge", Double.POSITIVE_INFINITY);
          return data;
        });
    check(
        manager.getCharge(battery) == 0 && !manager.canUse(battery, 1),
        "malformed infinite saved charge cannot act as a normal infinite battery");
    manager.charge(battery, 321, 1, true, false);
    check(
        manager.getCharge(battery) == 321
            && StackTag.get(battery).getStringOr("owned", "").equals("keep"),
        "actual charge writes replace malformed numeric energy and preserve unrelated owned"
            + " components");
    StackTag.update(
        battery,
        data -> {
          data.putDouble("charge", 88);
          return data;
        });
    check(
        manager.getCharge(battery) == 88,
        "replacing a native immutable component invalidates the primitive read cache by actual"
            + " identity");
    check(
        !manager.use(battery, 89, null)
            && manager.getCharge(battery) == 88
            && manager.use(battery, 88, null)
            && manager.getCharge(battery) == 0,
        "rejected complete bills debit nothing and exact later consumption clears only the energy"
            + " field");
    var drill = new ItemStack(ItemDefinitions.DRILL);
    manager.charge(drill, 1000, 0, true, false);
    check(
        manager.getCharge(drill) == 0,
        "lower-tier native input cannot fill the higher-tier target");
    manager.charge(battery, 1000, 1, true, false);
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("3890d5db-ab5d-489a-adc1-895ee9a66f8a"), "IC2ChargeProbe"));
    player.getAbilities().instabuild = false;
    player.getInventory().clearContent();
    player.setItemSlot(EquipmentSlot.CHEST, battery);
    manager.chargeFromArmor(drill, player);
    check(
        manager.getCharge(drill) == 1000 && manager.getCharge(battery) == 0,
        "physical equipment-source transfer conserves its complete thousand-EU quantity");
    var rejected = new RejectingSource();
    ElectricItem.registerBackupManager(rejected);
    ElectricItem.registerBackupManager(rejected);
    var diagnostic = new ItemStack(Items.DIAMOND);
    StackTag.update(
        diagnostic,
        data -> {
          data.putBoolean("reject_charge_debit", true);
          return data;
        });
    player.setItemSlot(EquipmentSlot.CHEST, diagnostic);
    var target = new ItemStack(ItemDefinitions.DRILL);
    manager.chargeFromArmor(target, player);
    check(
        manager.getCharge(target) == 0 && rejected.commits > 0,
        "an addon source which refuses the observed debit cannot create an unpaid target charge"
            + " after successful simulation");
    rejected.commits = 0;
    manager.chargeFromArmor(new ItemStack(ItemDefinitions.IRIDIUM_DRILL), player);
    check(
        rejected.commits == 0,
        "actual source tier blocks an incompatible target before any source commit");
    var infinite = new ic2ce.neoforge.item.energy.CreativeChargeSource();
    check(
        infinite.getChargeLevel(diagnostic) == 1
            && infinite.getStackChargeLevel(diagnostic) == 1
            && !infinite.canUse(diagnostic, Double.NaN),
        "explicit creative infinity has finite full visual fractions and rejects NaN operation"
            + " bills");
    player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
    player.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }

  private static final class RejectingSource implements IBackupElectricItemManager {
    int commits;

    @Override
    public boolean handles(ItemStack stack) {
      var data = StackTag.get(stack);
      return stack.is(Items.DIAMOND)
          && data != null
          && data.getBooleanOr("reject_charge_debit", false);
    }

    @Override
    public double charge(
        ItemStack stack, double amount, int tier, boolean unlimited, boolean simulate) {
      return 0;
    }

    @Override
    public double discharge(
        ItemStack stack,
        double amount,
        int tier,
        boolean unlimited,
        boolean external,
        boolean simulate) {
      if (!simulate) {
        commits++;
        return 0;
      }
      return Math.min(1000, amount);
    }

    @Override
    public double getCharge(ItemStack stack) {
      return 1000;
    }

    @Override
    public double getStackCharge(ItemStack stack) {
      return 1000;
    }

    @Override
    public double getMaxCharge(ItemStack stack) {
      return 1000;
    }

    @Override
    public boolean canUse(ItemStack stack, double amount) {
      return amount >= 0 && amount <= 1000;
    }

    @Override
    public boolean use(ItemStack stack, double amount, LivingEntity owner) {
      return false;
    }

    @Override
    public void chargeFromArmor(ItemStack stack, LivingEntity owner) {}

    @Override
    public String getToolTip(ItemStack stack) {
      return "Native source refusal diagnostic";
    }

    @Override
    public int getTier(ItemStack stack) {
      return 1;
    }
  }
}
