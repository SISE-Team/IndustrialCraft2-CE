package ic2ce.neoforge.item.upgrade;

import ic2ce.api.item.ElectricItem;
import ic2ce.api.upgrade.*;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.item.energy.BatteryItems;
import ic2ce.neoforge.processing.ProcessingBlocks;
import ic2ce.neoforge.storage.StorageBlocks;
import ic2ce.neoforge.transport.TransportBlocks;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/** Own native filter prototypes, corrupt settings and real directional transfers. */
public final class UpgradeContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native upgrade: " + label);
  }

  public static Result run(ServerLevel world) {
    return new UpgradeContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    for (int raw = -128; raw <= 127; raw++)
      check(
          IntegerRelation.getFromNBT((byte) raw) != null
              && FilterMode.getFromNBT((byte) raw) != null
              && MetadataMatch.getFromNBT((byte) raw) != null,
          "all saved policy bytes decode without indexing outside native modes");
    var malformed = settings(2, "huge-invalid-integer");
    var numeric = new NumericFilter(malformed);
    check(
        !numeric.doComparison(100) && !numeric.doComparison(Integer.MIN_VALUE),
        "malformed scalar text rejects candidates without throwing on every machine tick");
    var boundary = settings(2, "2147483647");
    var rule = new NumericFilter(boundary);
    check(
        rule.doComparison(Integer.MAX_VALUE) && !rule.doComparison(Integer.MAX_VALUE - 1),
        "compile-once numeric threshold preserves exact signed-integer boundary");
    var blank = new ItemStack(UpgradeItems.EJECTOR_UPGRADE);
    check(
        MachineUpgrade.getDirection(blank) == null && !StackTag.has(blank),
        "reading an unconfigured upgrade direction never writes a custom-data component");
    var invert = (MachineUpgrade) UpgradeItems.REDSTONE_INVERTER_UPGRADE;
    check(
        invert.getRedstoneInput(blank, null, -100) == 15
            && invert.getRedstoneInput(blank, null, 100) == 0,
        "native redstone inversion clamps external source strength");
    var prototype = new ItemStack(UpgradeItems.EJECTOR_UPGRADE);
    UpgradeRegistry.register(prototype);
    prototype.setCount(7);
    ItemStack found = null;
    for (var registered : UpgradeRegistry.getUpgrades())
      if (registered.is(UpgradeItems.EJECTOR_UPGRADE)) {
        found = registered;
        break;
      }
    check(
        found != null && found.getCount() == 1,
        "registered native upgrade owns its count instead of borrowing the caller's mutable"
            + " prototype");
    found.setCount(55);
    int count = 0;
    for (var registered : UpgradeRegistry.getUpgrades())
      if (registered.is(UpgradeItems.EJECTOR_UPGRADE)) count = registered.getCount();
    check(count == 1, "native enumeration returns independent physical upgrade stacks");
    int number = 0;
    for (boolean pull : new boolean[] {false, true})
      for (String mode :
          List.of(
              "ignored",
              "damage_match",
              "damage_reject",
              "fuzzy_charge_ignored",
              "exact_charge_reject",
              "exact_match",
              "energy_match",
              "energy_reject",
              "comparison_match",
              "comparison_reject",
              "range_match",
              "range_reject")) {
        var pos = new BlockPos(9600 + number++ * 16, 100, 9600);
        world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
        world.getChunkAt(pos);
        world.setBlock(
            pos,
            pull
                ? ProcessingBlocks.RECYCLER.defaultBlockState()
                : TransportBlocks.ITEM_BUFFER.defaultBlockState(),
            3);
        world.setBlock(pos.east(), StorageBlocks.IRON_STORAGE_BOX.defaultBlockState(), 3);
        var parent = (InventoryBlockEntity) world.getBlockEntity(pos);
        var box = (InventoryBlockEntity) world.getBlockEntity(pos.east());
        var inside = parent.getInventorySlot(pull ? "input" : "rightcontent");
        var outside = box.getInventorySlot("inventory");
        var filter = new ItemStack(Items.IRON_PICKAXE);
        filter.setDamageValue(10);
        var sample = filter.copy();
        var tag = new CompoundTag();
        boolean expected = !mode.endsWith("reject");
        if (mode.equals("ignored")) sample.setDamageValue(20);
        if (mode.startsWith("damage")) {
          tag.put("metaSettings", settings(1, ""));
          if (!expected) sample.setDamageValue(20);
        }
        if (mode.contains("charge")
            || mode.startsWith("energy")
            || mode.startsWith("comparison")
            || mode.startsWith("range")) {
          filter = battery(5000);
          sample = battery(expected ? 5000 : 7000);
        }
        if (mode.startsWith("fuzzy") || mode.startsWith("exact")) {
          var data = new CompoundTag();
          data.putByte("type", (byte) (mode.startsWith("fuzzy") ? 1 : 2));
          tag.put("nbtSettings", data);
          if (mode.startsWith("fuzzy")) sample = battery(7000);
        }
        if (mode.startsWith("energy")
            || mode.startsWith("comparison")
            || mode.startsWith("range")) {
          var data =
              settings(
                  mode.startsWith("energy") ? 1 : mode.startsWith("comparison") ? 2 : 3, "4000");
          if (mode.startsWith("range")) {
            data.putString("extra", "6000");
            data.putByte("extraComp", (byte) 0);
          }
          if (mode.startsWith("comparison") && !expected) sample = battery(3000);
          tag.put("energySettings", data);
        }
        var entries = new ListTag();
        var encoded = new CompoundTag();
        StackTag.save(world.registryAccess(), filter, encoded);
        encoded.putByte("Slot", (byte) 0);
        entries.add(encoded);
        tag.put("Items", entries);
        tag.putByte("dir", (byte) (Direction.EAST.ordinal() + 1));
        var upgrade =
            new ItemStack(
                pull
                    ? UpgradeItems.ADVANCED_PULLING_UPGRADE
                    : UpgradeItems.ADVANCED_EJECTOR_UPGRADE);
        StackTag.set(upgrade, tag);
        var item = (MachineUpgrade) upgrade.getItem();
        var compiled = CompiledUpgradeFilter.of(upgrade);
        check(
            compiled == CompiledUpgradeFilter.of(upgrade) && compiled.apply(sample) == expected,
            "native component generation reuses immutable filter and preserves mode " + mode);
        var source = pull ? outside : inside;
        var target = pull ? inside : outside;
        source.put(sample.copy());
        boolean moved = item.onTick(upgrade, (IUpgradableBlock) parent);
        int total = 0;
        for (var value : source) total += value.getCount();
        for (var value : target) total += value.getCount();
        check(
            !target.isEmpty() == expected && moved == expected && total == 1,
            "physical directional "
                + (pull ? "pull" : "eject")
                + " preserves one item through "
                + mode);
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        world.setBlock(pos.east(), Blocks.AIR.defaultBlockState(), 3);
      }
    return new Result(checks, List.copyOf(failures));
  }

  private static CompoundTag settings(int type, String normal) {
    var result = new CompoundTag();
    result.putBoolean("active", true);
    result.putByte("type", (byte) type);
    result.putString("normal", normal);
    result.putByte("normalComp", (byte) 0);
    return result;
  }

  private static ItemStack battery(double energy) {
    var result = new ItemStack(BatteryItems.RE_BATTERY);
    ElectricItem.manager.charge(result, energy, 1, true, false);
    return result;
  }
}
