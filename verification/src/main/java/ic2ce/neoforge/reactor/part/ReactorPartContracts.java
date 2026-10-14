package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Independent native pulse, heat remainder, cooling, exchanger and wear contracts. */
public final class ReactorPartContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native reactor parts: " + label);
  }

  private static final class Core implements IReactor {
    final ItemStack[][] grid = new ItemStack[9][6];
    int heat, capacity = 10000, emitted;
    float output, modifier = 1;
    boolean fluid;

    Core() {
      for (var column : grid) java.util.Arrays.fill(column, ItemStack.EMPTY);
    }

    public ItemStack getItemAt(int x, int y) {
      return x < 0 || x >= 9 || y < 0 || y >= 6 ? ItemStack.EMPTY : grid[x][y];
    }

    public void setItemAt(int x, int y, ItemStack item) {
      if (x >= 0 && x < 9 && y >= 0 && y < 6) grid[x][y] = item == null ? ItemStack.EMPTY : item;
    }

    public int getHeat() {
      return heat;
    }

    public void setHeat(int amount) {
      heat = Math.max(0, amount);
    }

    public int addHeat(int amount) {
      heat = (int) Math.clamp((long) heat + amount, 0, Integer.MAX_VALUE);
      return heat;
    }

    public int getMaxHeat() {
      return capacity;
    }

    public void setMaxHeat(int maximum) {
      capacity = maximum;
    }

    public void addEmitHeat(int amount) {
      emitted += amount;
    }

    public float getHeatEffectModifier() {
      return modifier;
    }

    public void setHeatEffectModifier(float value) {
      modifier = value;
    }

    public float getReactorEnergyOutput() {
      return output;
    }

    public double getReactorEUEnergyOutput() {
      return output * 5;
    }

    public float addOutput(float value) {
      return output += value;
    }

    public void explode() {}

    public int getTickRate() {
      return 20;
    }

    public boolean produceEnergy() {
      return true;
    }

    public boolean isFluidCooled() {
      return fluid;
    }

    public BlockEntity getCoreTe() {
      return null;
    }

    public Level getWorldObj() {
      return null;
    }

    public BlockPos getPosition() {
      return BlockPos.ZERO;
    }
  }

  public static Result run() {
    return new ReactorPartContracts().exercise();
  }

  private Result exercise() {
    var core = new Core();
    var cell = new ItemStack(ItemDefinitions.REACTOR_COOLANT_CELL);
    var storage = (HeatCell) cell.getItem();
    core.setItemAt(1, 1, cell);
    int maximum = storage.getMaxHeat(cell, core, 1, 1);
    storage.setUse(cell, maximum - 1);
    int excess = storage.alterHeat(cell, core, 1, 1, 100);
    check(
        excess == 98 && core.getItemAt(1, 1).isEmpty(),
        "melting storage returns positive excess instead of cooling the reactor");
    cell = new ItemStack(ItemDefinitions.REACTOR_COOLANT_CELL);
    core.setItemAt(1, 1, cell);
    storage.setUse(cell, 5);
    check(
        storage.alterHeat(cell, core, 1, 1, -10) == -5 && storage.getUse(cell) == 0,
        "cooling returns exactly the unhandled negative remainder");
    storage.setUse(cell, Integer.MIN_VALUE);
    check(
        storage.getUse(cell) == 0 && Double.isFinite(storage.getUseFraction(cell)),
        "invalid saved wear never creates negative heat or a nonfinite gauge");
    var fuel = new ItemStack(ItemDefinitions.URANIUM_FUEL_ROD);
    var uranium = (UraniumFuel) fuel.getItem();
    core = new Core();
    core.setItemAt(2, 2, fuel);
    uranium.processChamber(fuel, core, 2, 2, false);
    uranium.processChamber(fuel, core, 2, 2, true);
    check(
        core.output == 1 && core.heat == 4 && uranium.getUse(fuel) == 1,
        "single isolated uranium has one pulse, four HU and one wear unit");
    core = new Core();
    core.setItemAt(2, 2, fuel);
    var left = new ItemStack(ItemDefinitions.REACTOR_COOLANT_CELL);
    var right = new ItemStack(ItemDefinitions.REACTOR_COOLANT_CELL);
    core.setItemAt(1, 2, left);
    core.setItemAt(3, 2, right);
    uranium.processChamber(fuel, core, 2, 2, true);
    check(
        storage.getUse(left) == 2 && storage.getUse(right) == 2 && core.heat == 0,
        "fuel partitions four HU across two native storage recipients without a queue");
    var moxStack = new ItemStack(ItemDefinitions.MOX_FUEL_ROD);
    var mox = (MoxFuel) moxStack.getItem();
    core = new Core();
    core.heat = 5000;
    mox.acceptUraniumPulse(moxStack, core, fuel, 2, 2, 1, 2, false);
    check(core.output == 3, "MOX electrical pulse uses the actual hull-temperature ratio");
    core.capacity = 0;
    core.output = 0;
    mox.acceptUraniumPulse(moxStack, core, fuel, 2, 2, 1, 2, false);
    check(
        core.output == 1 && Float.isFinite(core.output),
        "zero hull capacity cannot create infinite MOX output");
    var condensatorStack = new ItemStack(ItemDefinitions.RSH_CONDENSATOR);
    var condensator = (ChemicalCondensator) condensatorStack.getItem();
    core = new Core();
    int capacity = condensator.getMaxHeat(condensatorStack, core, 1, 1);
    condensator.setUse(condensatorStack, capacity - 3);
    check(
        condensator.alterHeat(condensatorStack, core, 1, 1, 10) == 7
            && condensator.getCurrentHeat(condensatorStack, core, 1, 1) == capacity,
        "condensator absorption never exceeds its chemical capacity");
    check(
        condensator.alterHeat(condensatorStack, core, 1, 1, -10) == -10,
        "ordinary cooling cannot recharge a saturated condensator");
    return new Result(checks, List.copyOf(failures));
  }
}
