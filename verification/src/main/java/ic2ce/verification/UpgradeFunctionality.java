package ic2ce.verification;

import static ic2ce.verification.AllMachinesVerification.*;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.core.util.StackTag;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.processing.PoweredProcessor;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** Observes real upgrade transfers and processing, independently of the rates matrix. */
final class UpgradeFunctionality {
  private final ExtendedValidation e;
  private final AllMachinesVerification s;
  private final Map<String, NativeBlockEntity> tiles = new LinkedHashMap<>();
  private final Map<String, InventorySlot> sinks = new LinkedHashMap<>();
  private final Map<String, Ic2FluidTank> fluidSinks = new LinkedHashMap<>();
  private final Map<Integer, List<Runnable>> jobs = new TreeMap<>();
  private final List<Map<String, Object>> results = new ArrayList<>();
  private int age, number;

  UpgradeFunctionality(ExtendedValidation owner) {
    e = owner;
    s = owner.s;
  }

  private NativeBlockEntity machine(String label, String id) {
    var pos = new BlockPos(1536 + (number % 8) * 16, 201, 176 + (number / 8) * 16);
    number++;
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++)
        s.level.setChunkForced((pos.getX() >> 4) + x, (pos.getZ() >> 4) + z, true);
    var tile = s.helper(id, pos);
    tiles.put(label, tile);
    return tile;
  }

  private UpgradeInventory upgrades(NativeBlockEntity tile) {
    return (UpgradeInventory) ((InventoryBlockEntity) tile).getInventorySlot("upgrade");
  }

  private void install(NativeBlockEntity tile, ItemStack stack) {
    var slot = upgrades(tile);
    e.check("functional fixture accepts " + stack, slot.accepts(stack));
    slot.put(0, stack);
    slot.onChanged();
    tile.setChanged();
  }

  private void at(int tick, Runnable job) {
    jobs.computeIfAbsent(tick, k -> new ArrayList<>()).add(job);
  }

  private void record(String test, Object actual, boolean passed) {
    e.check(test, passed);
    results.add(Map.of("test", test, "actual", actual, "passed", passed));
  }

  private InventorySlot box(NativeBlockEntity tile) {
    var sink = s.helper("iron_storage_box", tile.getBlockPos().east());
    return (InventorySlot) get(sink, "inventory");
  }

  private ItemStack filtered(String upgrade, ItemStack filter) {
    var result = stack(upgrade, 1);
    var tag = new CompoundTag();
    var list = new ListTag();
    var item = new CompoundTag();
    StackTag.save(s.level.registryAccess(), filter, item);
    item.putByte("Slot", (byte) 0);
    list.add(item);
    tag.put("Items", list);
    tag.putByte("dir", (byte) (Direction.EAST.ordinal() + 1));
    StackTag.set(result, tag);
    return result;
  }

  void prepare() {
    for (String label : List.of("control", "overclock", "transformer", "storage")) {
      var tile = (PoweredProcessor<?, ?, ?>) machine(label, "electric_furnace");
      s.helper("creative_generator", tile.getBlockPos().west());
      if (label.equals("overclock")) install(tile, stack("overclocker_upgrade", 2));
      if (label.equals("transformer")) install(tile, stack("transformer_upgrade", 2));
      if (label.equals("storage")) install(tile, stack("energy_storage_upgrade", 64));
    }
    at(
        20,
        () -> {
          for (String label : List.of("control", "overclock")) {
            var tile = (PoweredProcessor<?, ?, ?>) tiles.get(label);
            tile.inputSlot.put(new ItemStack(Items.IRON_ORE, 8));
            tile.setChanged();
          }
          var trans = (PoweredProcessor<?, ?, ?>) tiles.get("transformer");
          record(
              "transformer stack raises actual sink tier by two",
              trans.getComponent(ElectricalBuffer.class).getSinkTier(),
              trans.getComponent(ElectricalBuffer.class).getSinkTier() == trans.defaultTier + 2);
          var storage = (PoweredProcessor<?, ?, ?>) tiles.get("storage");
          record(
              "storage upgrade increases actual machine buffer",
              storage.getComponent(ElectricalBuffer.class).getCapacity(),
              storage.getComponent(ElectricalBuffer.class).getCapacity()
                  >= storage.defaultEnergyStorage + 640000);
        });
    at(
        110,
        () -> {
          var normal = (PoweredProcessor<?, ?, ?>) tiles.get("control");
          var fast = (PoweredProcessor<?, ?, ?>) tiles.get("overclock");
          record(
              "overclocked furnace completes real recipes before control",
              fast.outputSlot.get().getCount(),
              fast.outputSlot.get().getCount() > normal.outputSlot.get().getCount());
        });
    at(
        600,
        () -> {
          for (String label : List.of("control", "overclock")) {
            var tile = (PoweredProcessor<?, ?, ?>) tiles.get(label);
            int total = tile.inputSlot.get().getCount() + tile.outputSlot.get().getCount();
            record(
                label + " actual furnace conserves all eight inputs",
                total,
                total == 8 && tile.outputSlot.get().is(Items.IRON_INGOT));
          }
        });
    for (String upgrade : List.of("ejector_upgrade", "advanced_ejector_upgrade")) {
      var tile = machine(upgrade, "item_buffer");
      var iron = (InventorySlot) get(tile, "rightcontentSlot");
      var gold = (InventorySlot) get(tile, "leftcontentSlot");
      iron.put(new ItemStack(Items.IRON_INGOT, 13));
      gold.put(new ItemStack(Items.GOLD_INGOT, 11));
      sinks.put(upgrade, box(tile));
      install(
          tile,
          upgrade.startsWith("advanced")
              ? filtered(upgrade, new ItemStack(Items.IRON_INGOT))
              : stack(upgrade, 1));
      at(
          200,
          () -> {
            int n = 0, g = 0;
            for (var item : sinks.get(upgrade)) {
              if (item.is(Items.IRON_INGOT)) n += item.getCount();
              if (item.is(Items.GOLD_INGOT)) g += item.getCount();
            }
            record(upgrade + " transfers iron exactly once", n, n == 13 && iron.isEmpty());
            record(
                upgrade + " applies its real whitelist",
                g,
                upgrade.startsWith("advanced")
                    ? g == 0 && gold.get().getCount() == 11
                    : g == 11 && gold.isEmpty());
          });
    }
    for (String upgrade : List.of("pulling_upgrade", "advanced_pulling_upgrade")) {
      var tile = (PoweredProcessor<?, ?, ?>) machine(upgrade, "electric_furnace");
      var source = box(tile);
      sinks.put(upgrade, source);
      if (upgrade.startsWith("advanced")) {
        source.put(0, new ItemStack(Items.GOLD_ORE, 11));
        source.put(1, new ItemStack(Items.IRON_ORE, 13));
      } else source.put(0, new ItemStack(Items.IRON_ORE, 13));
      install(
          tile,
          upgrade.startsWith("advanced")
              ? filtered(upgrade, new ItemStack(Items.IRON_ORE))
              : stack(upgrade, 1));
      at(
          200,
          () -> {
            record(
                upgrade + " actually pulls thirteen iron ores",
                tile.inputSlot.get().getCount(),
                tile.inputSlot.get().is(Items.IRON_ORE) && tile.inputSlot.get().getCount() == 13);
            if (upgrade.startsWith("advanced"))
              record(
                  "advanced pull leaves excluded gold in source",
                  source.get().getCount(),
                  source.get().is(Items.GOLD_ORE) && source.get().getCount() == 11);
          });
    }
    for (String upgrade : List.of("fluid_ejector_upgrade", "fluid_pulling_upgrade")) {
      var tile = machine(upgrade, "fermenter");
      var adjacent = s.helper("iron_tank", tile.getBlockPos().east());
      var tank = (Ic2FluidTank) get(adjacent, "contents");
      fluidSinks.put(upgrade, tank);
      if (upgrade.equals("fluid_ejector_upgrade"))
        ((Ic2FluidTank) get(tile, "outputTank"))
            .fillMbUnchecked(Ic2FluidStack.create(fluid("biogas"), 1000), false);
      else tank.fillMbUnchecked(Ic2FluidStack.create(fluid("biomass"), 1000), false);
      install(tile, stack(upgrade, 1));
      at(
          200,
          () -> {
            var inside =
                (Ic2FluidTank)
                    get(tile, upgrade.equals("fluid_ejector_upgrade") ? "outputTank" : "inputTank");
            record(
                upgrade + " transfers real 1000mB with conservation",
                List.of(inside.getFluidAmount(), tank.getFluidAmount()),
                inside.getFluidAmount() + tank.getFluidAmount() == 1000
                    && (upgrade.equals("fluid_ejector_upgrade")
                        ? tank.getFluidAmount() == 1000
                        : inside.getFluidAmount() == 1000));
          });
    }
    var inverter = machine("inverter", "induction_furnace");
    install(inverter, stack("redstone_inverter_upgrade", 1));
    at(
        20,
        () ->
            record(
                "redstone inverter produces signal without external source",
                inverter.getComponent(SignalInput.class).getRedstoneInput(),
                inverter.getComponent(SignalInput.class).getRedstoneInput() == 15));
    at(
        40,
        () ->
            s.level.setBlockAndUpdate(
                inverter.getBlockPos().south(), Blocks.REDSTONE_BLOCK.defaultBlockState()));
    at(
        60,
        () ->
            record(
                "redstone inverter suppresses an actual neighboring signal",
                inverter.getComponent(SignalInput.class).getRedstoneInput(),
                inverter.getComponent(SignalInput.class).getRedstoneInput() == 0));
  }

  boolean tick() {
    age++;
    for (var job : jobs.getOrDefault(age, List.of())) job.run();
    if (age < 600) return false;
    e.details.put("upgradeFunctionalCases", results);
    for (var tile : tiles.values()) {
      s.level.setBlockAndUpdate(tile.getBlockPos(), Blocks.AIR.defaultBlockState());
      s.level.setBlockAndUpdate(tile.getBlockPos().east(), Blocks.AIR.defaultBlockState());
      s.level.setBlockAndUpdate(tile.getBlockPos().west(), Blocks.AIR.defaultBlockState());
      s.level.setBlockAndUpdate(tile.getBlockPos().south(), Blocks.AIR.defaultBlockState());
    }
    return true;
  }
}
