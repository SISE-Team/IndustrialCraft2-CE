package ic2ce.verification;

import static ic2ce.verification.AllMachinesVerification.*;

import ic2ce.neoforge.reactor.NuclearCore;
import ic2ce.neoforge.reactor.part.WearableReactorPart;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** Full grids, fuel end-of-life, breeding, MOX heat response and fluid mode. */
final class ReactorFunctionality {
  private final ExtendedValidation e;
  private final AllMachinesVerification s;
  private final Map<String, NuclearCore> cores = new LinkedHashMap<>();
  private final List<Map<String, Object>> results = new ArrayList<>();
  private int number, age;
  private long preparedAt;
  private boolean primed;
  private final Map<String, Integer> initialTickers = new LinkedHashMap<>();
  private final Map<String, List<ItemStack>> pendingFuel = new LinkedHashMap<>();
  private final Map<String, Integer> pendingHeat = new LinkedHashMap<>();

  ReactorFunctionality(ExtendedValidation owner) {
    e = owner;
    s = owner.s;
  }

  private NuclearCore core(String label) {
    var pos = new BlockPos(3072 + (number % 6) * 256, 181, 1024 + (number / 6) * 256);
    number++;
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++) {
        s.level.setChunkForced((pos.getX() >> 4) + x, (pos.getZ() >> 4) + z, true);
        s.level.getChunk((pos.getX() >> 4) + x, (pos.getZ() >> 4) + z);
      }
    var core = (NuclearCore) s.helper("nuclear_reactor", pos);
    for (var direction : Direction.values()) s.helper("reactor_chamber", pos.relative(direction));
    s.level.setBlockAndUpdate(pos.above(2), Blocks.REDSTONE_BLOCK.defaultBlockState());
    cores.put(label, core);
    return core;
  }

  private void record(String test, Object actual, boolean passed) {
    e.check(test, passed);
    results.add(Map.of("test", test, "actual", actual, "passed", passed));
  }

  void prepare() {
    for (String fuel :
        List.of(
            "uranium_fuel_rod",
            "dual_uranium_fuel_rod",
            "quad_uranium_fuel_rod",
            "mox_fuel_rod",
            "dual_mox_fuel_rod",
            "quad_mox_fuel_rod")) {
      var core = core(fuel);
      var rod = stack(fuel, 1);
      var item = (WearableReactorPart) rod.getItem();
      item.setUse(rod, ((Number) get(item, "maxUse")).intValue() - 1);
      core.setItemAt(2, 2, rod);
      core.setChanged();
    }
    var full = core("full_safe_grid");
    for (int x = 0; x < 9; x++)
      for (int y = 0; y < 6; y++) full.setItemAt(x, y, stack("advanced_heat_vent", 1));
    full.setItemAt(4, 3, stack("uranium_fuel_rod", 1));
    full.setChanged();
    var overload = core("full_overload_grid");
    for (int x = 0; x < 9; x++)
      for (int y = 0; y < 6; y++) overload.setItemAt(x, y, stack("quad_uranium_fuel_rod", 1));
    overload.setChanged();
    var mox = core("hot_mox");
    mox.setHeat(5000);
    mox.setItemAt(2, 2, stack("mox_fuel_rod", 1));
    mox.setChanged();
    var breeder = core("breeder");
    breeder.setHeat(6000);
    breeder.setItemAt(2, 2, stack("uranium_fuel_rod", 1));
    var cell = stack("depleted_isotope_fuel_rod", 1);
    var item = (WearableReactorPart) cell.getItem();
    item.setUse(cell, ((Number) get(item, "maxUse")).intValue() - 1);
    breeder.setItemAt(3, 2, cell);
    breeder.setChanged();
    var lithium = core("lithium");
    lithium.setHeat(6000);
    lithium.setItemAt(2, 2, stack("uranium_fuel_rod", 1));
    lithium.setItemAt(3, 2, stack("lithium_fuel_rod", 1));
    lithium.setChanged();
    var fluid = core("fluid_mode");
    var pos = fluid.getBlockPos();
    s.level.setBlockAndUpdate(pos.above(2), Blocks.AIR.defaultBlockState());
    for (int x = -2; x <= 2; x++)
      for (int y = -2; y <= 2; y++)
        for (int z = -2; z <= 2; z++)
          if (Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2)
            s.level.setBlockAndUpdate(
                pos.offset(x, y, z), block("reactor_vessel").defaultBlockState());
    s.helper("reactor_redstone_port", pos.above(2));
    s.level.setBlockAndUpdate(pos.above(3), Blocks.REDSTONE_BLOCK.defaultBlockState());
    for (int x = 0; x < 9; x++)
      for (int y = 0; y < 6; y++) fluid.setItemAt(x, y, stack("advanced_heat_vent", 1));
    fluid.setItemAt(4, 3, stack("uranium_fuel_rod", 1));
    fluid.inputTank.fillMbUnchecked(
        ic2ce.api.fluid.Ic2FluidStack.create(fluid("coolant"), 10000), false);
    fluid.setChanged();
    for (var entry : cores.entrySet()) {
      var core = entry.getValue();
      var contents = new ArrayList<ItemStack>();
      for (int slot = 0; slot < 54; slot++) contents.add(core.reactorSlot.get(slot).copy());
      pendingFuel.put(entry.getKey(), contents);
      pendingHeat.put(entry.getKey(), core.getHeat());
      core.reactorSlot.clear();
      core.setHeat(0);
      core.setChanged();
      initialTickers.put(entry.getKey(), core.updateTicker);
    }
    preparedAt = System.nanoTime();
  }

  boolean tick() {
    if (!primed) {
      if (System.nanoTime() - preparedAt < 10_000_000_000L) return false;
      var readiness = new ArrayList<Map<String, Object>>();
      boolean all = true;
      for (var entry : cores.entrySet()) {
        var core = entry.getValue();
        boolean loaded = ic2ce.core.util.Util.isAreaLoaded(s.level, core.getBlockPos(), 8);
        boolean signal = core.redstone.hasRedstoneInput();
        int initial = initialTickers.get(entry.getKey());
        boolean ticking = core.updateTicker - initial >= core.getTickRate();
        int width = core.getReactorSize();
        readiness.add(
            Map.of(
                "case",
                entry.getKey(),
                "areaLoaded",
                loaded,
                "redstone",
                signal,
                "initialTicker",
                initial,
                "updateTicker",
                core.updateTicker,
                "actualTicking",
                ticking,
                "width",
                width));
        all &= loaded && signal && ticking && width == 9;
      }
      e.details.put("reactorDeepReadiness", readiness);
      if (!all) {
        if (System.nanoTime() - preparedAt > 120_000_000_000L) {
          e.check("deep reactor fixtures become loaded, ticking and powered", false);
          return true;
        }
        return false;
      }
      e.check(
          "all twelve deep reactor fixtures are loaded, ticking and powered before inputs", true);
      for (var entry : cores.entrySet()) {
        var core = entry.getValue();
        for (int slot = 0; slot < 54; slot++)
          core.reactorSlot.put(slot, pendingFuel.get(entry.getKey()).get(slot).copy());
        core.setHeat(pendingHeat.get(entry.getKey()));
        core.setChanged();
      }
      primed = true;
      s.level.getServer().tickRateManager().requestGameToSprint(1000);
    }
    age++;
    if (age == 100) {
      for (String fuel :
          List.of(
              "uranium_fuel_rod",
              "dual_uranium_fuel_rod",
              "quad_uranium_fuel_rod",
              "mox_fuel_rod",
              "dual_mox_fuel_rod",
              "quad_mox_fuel_rod")) {
        var core = cores.get(fuel);
        String expected = "depleted_" + fuel;
        record(
            fuel + " actually converts at its last fuel cycle",
            String.valueOf(core.getItemAt(2, 2)),
            core.getItemAt(2, 2).is(stack(expected, 1).getItem()));
      }
      var full = cores.get("full_safe_grid");
      record(
          "54-slot mixed cooling grid remains intact and produces 5 EU/t",
          full.getOfferedEnergy(),
          s.level.getBlockEntity(full.getBlockPos()) == full
              && full.getReactorSize() == 9
              && full.getHeat() < 1000
              && Math.abs(full.getOfferedEnergy() - 5) < 0.001);
      var overload = cores.get("full_overload_grid");
      record(
          "unsafe 54-slot fuel grid triggers actual reactor meltdown",
          overload.getHeat(),
          s.level.getBlockEntity(overload.getBlockPos()) != overload);
      var mox = cores.get("hot_mox");
      double expected = 5.0 * (1 + 4.0 * mox.getHeat() / mox.getMaxHeat());
      record(
          "MOX output follows independently calculated core temperature",
          mox.getOfferedEnergy(),
          Math.abs(mox.getOfferedEnergy() - expected) < 0.001);
      var breeder = cores.get("breeder");
      record(
          "hot breeder pulse converts exhausted isotope rod",
          String.valueOf(breeder.getItemAt(3, 2)),
          breeder.getItemAt(3, 2).is(stack("re_enriched_uranium", 1).getItem()));
      var lithium = cores.get("lithium");
      var item = lithium.getItemAt(3, 2);
      var component = (WearableReactorPart) item.getItem();
      record(
          "hot reactor pulses advance lithium breeding",
          ic2ce.core.util.StackTag.getOrCreate(item).getIntOr("use", 0),
          ic2ce.core.util.StackTag.getOrCreate(item).getIntOr("use", 0) > 0
              && ic2ce.core.util.StackTag.getOrCreate(item).getIntOr("use", 0)
                  < ((Number) get(component, "maxUse")).intValue());
      var fluid = cores.get("fluid_mode");
      record(
          "full fluid reactor heats actual coolant without volume loss",
          fluid.outputTank.getFluidAmount(),
          fluid.isFluidCooled()
              && fluid.outputTank.getFluidAmount() > 0
              && fluid.inputTank.getFluidAmount() + fluid.outputTank.getFluidAmount() == 10000);
      var saved = full.saveWithFullMetadata(s.level.registryAccess());
      var restored =
          (NuclearCore)
              net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                  full.getBlockPos(), full.getBlockState(), saved, s.level.registryAccess());
      record(
          "54-slot working reactor survives serialized inventory and heat",
          restored.reactorSlot.size(),
          restored.getHeat() == full.getHeat()
              && restored.reactorSlot.rawSize() == 54
              && restored.getItemAt(4, 3).is(stack("uranium_fuel_rod", 1).getItem()));
      for (String label : List.of("full_safe_grid", "hot_mox"))
        s.level.setBlockAndUpdate(
            cores.get(label).getBlockPos().above(2), Blocks.AIR.defaultBlockState());
    }
    if (age == 140) {
      for (String label : List.of("full_safe_grid", "hot_mox")) {
        var core = cores.get(label);
        record(
            label + " really stops after removing signal",
            core.getOfferedEnergy(),
            core.getOfferedEnergy() == 0);
      }
      var fluid = cores.get("fluid_mode");
      s.level.setBlockAndUpdate(
          fluid.getBlockPos().offset(2, 2, 2), Blocks.AIR.defaultBlockState());
      s.level.setBlockAndUpdate(
          fluid.getBlockPos().above(2), Blocks.REDSTONE_BLOCK.defaultBlockState());
    }
    if (age == 180) {
      var fluid = cores.get("fluid_mode");
      record(
          "broken vessel returns full reactor to EU mode",
          fluid.isFluidCooled(),
          !fluid.isFluidCooled() && fluid.getOfferedEnergy() > 0);
    }
    if (age < 200) return false;
    e.details.put("reactorFunctionalCases", results);
    for (var core : cores.values()) {
      var pos = core.getBlockPos();
      for (int x = -3; x <= 3; x++)
        for (int y = -3; y <= 3; y++)
          for (int z = -3; z <= 3; z++)
            s.level.setBlockAndUpdate(pos.offset(x, y, z), Blocks.AIR.defaultBlockState());
    }
    return true;
  }
}
