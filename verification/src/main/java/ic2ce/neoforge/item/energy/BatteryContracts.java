package ic2ce.neoforge.item.energy;

import ic2ce.api.item.*;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

/** Physical handheld sources, limited targets and hostile native addon callbacks. */
public final class BatteryContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native battery: " + label);
  }

  public static Result run(ServerLevel world) {
    return new BatteryContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var manager = ElectricItem.manager;
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("04899aec-7c2e-4d94-b651-6028971db781"), "IC2BatteryProbe"));
    player.getInventory().clearContent();
    player.getAbilities().instabuild = false;
    var source = charged(ItemDefinitions.RE_BATTERY, 1000);
    var drill = new ItemStack(ItemDefinitions.DRILL);
    var saw = new ItemStack(ItemDefinitions.CHAINSAW);
    var battery = new ItemStack(ItemDefinitions.RE_BATTERY);
    player.setItemInHand(InteractionHand.MAIN_HAND, source);
    player.getInventory().setItem(1, drill);
    player.getInventory().setItem(2, saw);
    player.getInventory().setItem(3, battery);
    source.getItem().use(world, player, InteractionHand.MAIN_HAND);
    check(
        manager.getCharge(source) == 600
            && manager.getCharge(drill) == 200
            && manager.getCharge(saw) == 200
            && manager.getCharge(battery) == 0,
        "rechargeable supply applies twice its rate independently to two tools and skips another"
            + " output-capable battery");
    double before = manager.getCharge(source);
    check(
        ItemChargeTransfers.move(source, source, 100, 1) == 0
            && manager.getCharge(source) == before,
        "physical source identity cannot transfer to itself");
    var full = charged(ItemDefinitions.DRILL, Double.POSITIVE_INFINITY);
    check(
        ItemChargeTransfers.move(source, full, 1000, 1) == 0 && manager.getCharge(source) == before,
        "a full target cannot consume any prepaid source energy");
    check(
        ItemChargeTransfers.move(source, new ItemStack(ItemDefinitions.IRIDIUM_DRILL), 1000, 1) == 0
            && manager.getCharge(source) == before,
        "an incompatible native target tier cannot debit the source");
    var probe = new CallbackManager();
    ElectricItem.registerBackupManager(probe);
    var rejects = marked(Items.EMERALD, "reject");
    check(
        ItemChargeTransfers.move(source, rejects, 100, 1) == 0
            && manager.getCharge(source) == before,
        "an addon accepting simulation but rejecting commit returns all actual prepaid energy");
    var refusing = marked(Items.DIAMOND, "source");
    check(
        ItemChargeTransfers.move(refusing, drill, 100, 1) == 0 && manager.getCharge(drill) == 200,
        "an addon source refusing its actual debit cannot credit a native tool");
    var recursive = marked(Items.EMERALD, "recursive");
    probe.source = source;
    probe.target = recursive;
    double sent = ItemChargeTransfers.move(source, recursive, 100, 1);
    check(
        sent == 100 && manager.getCharge(source) == before - 100 && probe.recursiveCredit == 0,
        "an addon callback cannot recursively spend a transfer already in progress");
    player.getInventory().clearContent();
    var cell = new ItemStack(ItemDefinitions.SINGLE_USE_BATTERY);
    drill = new ItemStack(ItemDefinitions.DRILL);
    saw = new ItemStack(ItemDefinitions.CHAINSAW);
    manager.charge(drill, manager.getMaxCharge(drill) - 500, 1, true, false);
    player.setItemInHand(InteractionHand.OFF_HAND, cell);
    player.getInventory().setItem(1, drill);
    player.getInventory().setItem(2, saw);
    cell.getItem().use(world, player, InteractionHand.OFF_HAND);
    check(
        player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()
            && manager.getCharge(drill) == manager.getMaxCharge(drill)
            && manager.getCharge(saw) == 700,
        "the final physical disposable cell pays exactly 1200 EU across partial and empty tools and"
            + " disappears from its actual hand");
    player.getInventory().clearContent();
    cell = new ItemStack(ItemDefinitions.SINGLE_USE_BATTERY, 2);
    player.setItemInHand(InteractionHand.OFF_HAND, cell);
    player.getInventory().setItem(1, marked(Items.EMERALD, "reject"));
    cell.getItem().use(world, player, InteractionHand.OFF_HAND);
    check(
        cell.getCount() == 2,
        "a disposable source is refunded when every addon target rejects actual delivery");
    player.getInventory().clearContent();
    cell = new ItemStack(ItemDefinitions.SINGLE_USE_BATTERY);
    player.setItemInHand(InteractionHand.OFF_HAND, cell);
    player.getInventory().setItem(1, marked(Items.EMERALD, "reject"));
    cell.getItem().use(world, player, InteractionHand.OFF_HAND);
    check(
        player.getItemInHand(InteractionHand.OFF_HAND).is(ItemDefinitions.SINGLE_USE_BATTERY)
            && cell.getCount() == 1,
        "refunding the last cell restores its actual physical item identity");
    player.getInventory().clearContent();
    cell = new ItemStack(ItemDefinitions.SINGLE_USE_BATTERY, 2);
    player.setItemInHand(InteractionHand.OFF_HAND, cell);
    player.getInventory().setItem(1, full);
    cell.getItem().use(world, player, InteractionHand.OFF_HAND);
    check(cell.getCount() == 2, "a full hotbar never spends a disposable cell");
    player.getInventory().clearContent();
    cell = new ItemStack(ItemDefinitions.SINGLE_USE_BATTERY, 2);
    drill = new ItemStack(ItemDefinitions.DRILL);
    player.getAbilities().instabuild = true;
    player.setItemInHand(InteractionHand.OFF_HAND, cell);
    player.getInventory().setItem(1, drill);
    cell.getItem().use(world, player, InteractionHand.OFF_HAND);
    check(
        cell.getCount() == 2 && manager.getCharge(drill) == 1200,
        "creative handheld use keeps the original physical cell count and one bounded charge"
            + " budget");
    player.getInventory().clearContent();
    player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    player.getAbilities().instabuild = false;
    return new Result(checks, List.copyOf(failures));
  }

  private static ItemStack charged(Item item, double amount) {
    var result = new ItemStack(item);
    ElectricItem.manager.charge(result, amount, Integer.MAX_VALUE, true, false);
    return result;
  }

  private static ItemStack marked(Item item, String kind) {
    var result = new ItemStack(item);
    StackTag.update(
        result,
        data -> {
          data.putString("battery_probe", kind);
          return data;
        });
    return result;
  }

  private static final class CallbackManager implements IBackupElectricItemManager {
    ItemStack source, target;
    double recursiveCredit;

    private String kind(ItemStack stack) {
      var data = StackTag.get(stack);
      return data == null ? "" : data.getStringOr("battery_probe", "");
    }

    @Override
    public boolean handles(ItemStack stack) {
      return !kind(stack).isEmpty();
    }

    @Override
    public double charge(
        ItemStack stack, double amount, int tier, boolean unlimited, boolean simulate) {
      if (simulate) return Math.min(1000, amount);
      if (!kind(stack).equals("recursive")) return 0;
      recursiveCredit += ItemChargeTransfers.move(source, target, amount, tier);
      return amount;
    }

    @Override
    public double discharge(
        ItemStack stack,
        double amount,
        int tier,
        boolean unlimited,
        boolean external,
        boolean simulate) {
      return kind(stack).equals("source") && simulate ? Math.min(1000, amount) : 0;
    }

    @Override
    public double getCharge(ItemStack stack) {
      return kind(stack).equals("source") ? 1000 : 0;
    }

    @Override
    public double getStackCharge(ItemStack stack) {
      return getCharge(stack);
    }

    @Override
    public double getMaxCharge(ItemStack stack) {
      return 1000;
    }

    @Override
    public boolean canUse(ItemStack stack, double amount) {
      return false;
    }

    @Override
    public boolean use(ItemStack stack, double amount, LivingEntity owner) {
      return false;
    }

    @Override
    public void chargeFromArmor(ItemStack stack, LivingEntity owner) {}

    @Override
    public String getToolTip(ItemStack stack) {
      return "Battery callback diagnostic";
    }

    @Override
    public int getTier(ItemStack stack) {
      return 1;
    }
  }
}
