package ic2ce.neoforge.reactor;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Actual native core source, persisted heat/output and detached vessel contracts. */
public final class ReactorContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native reactor core: " + label);
  }

  public static Result run(net.minecraft.server.level.ServerLevel world) {
    return new ReactorContracts().exercise(world);
  }

  private Result exercise(net.minecraft.server.level.ServerLevel world) {
    var core = new NuclearCore(BlockPos.ZERO, ReactorBlocks.NUCLEAR_REACTOR.defaultBlockState());
    core.output = 12.25F;
    double nominal = core.getReactorEUEnergyOutput();
    core.drawEnergy(Double.NaN);
    core.drawEnergy(-1);
    check(
        core.getOfferedEnergy() == nominal,
        "invalid source draws do not change the electrical budget");
    core.drawEnergy(nominal / 4);
    core.drawEnergy(nominal / 4);
    check(
        core.getOfferedEnergy() == nominal / 2 && core.getReactorEUEnergyOutput() == nominal,
        "multiple consumers share one tick while the nominal GUI output stays stable");
    core.drawEnergy(Double.MAX_VALUE);
    check(
        core.getOfferedEnergy() == 0, "source cannot deliver more than its production in one tick");
    core.setHeat(Integer.MAX_VALUE);
    core.addHeat(100);
    check(
        core.getHeat() == Integer.MAX_VALUE, "heat arithmetic saturates instead of wrapping cold");
    core.addHeat(Integer.MIN_VALUE);
    check(core.getHeat() == 0, "cooling cannot create negative hull heat");
    core.maxHeat = 0;
    check(
        core.gaugeHeatScaled(160) == 0 && core.getGuiValue("heat") == 0,
        "corrupt capacity cannot divide a GUI gauge by zero");
    core.setHeat(1234);
    core.output = 16.75F;
    core.setMaxHeat(10001);
    var data = new CompoundTag();
    core.saveAdditional(data);
    var restored =
        new NuclearCore(BlockPos.ZERO, ReactorBlocks.NUCLEAR_REACTOR.defaultBlockState());
    restored.load(data);
    check(
        restored.getHeat() == 1234
            && restored.getReactorEnergyOutput() == 16.75F
            && restored.getMaxHeat() == 10001,
        "fractional power and capacity survive native NBT rather than a lossy short");
    check(
        core.getItemAt(-1, 0).isEmpty()
            && core.getItemAt(0, 6).isEmpty()
            && !core.isUsefulItem(ItemStack.EMPTY, true),
        "out-of-grid access and empty insertion return native empty values");
    var chamber =
        new ReactorChamber(BlockPos.ZERO, ReactorBlocks.REACTOR_CHAMBER.defaultBlockState());
    var hatch =
        new VesselHatch(BlockPos.ZERO, ReactorBlocks.REACTOR_ACCESS_HATCH.defaultBlockState());
    check(
        chamber.getItem(0) == ItemStack.EMPTY
            && chamber.removeItem(0, 1) == ItemStack.EMPTY
            && chamber.removeItemNoUpdate(0) == ItemStack.EMPTY,
        "detached chamber never returns null item stacks");
    check(
        hatch.getItem(0) == ItemStack.EMPTY
            && hatch.removeItem(0, 1) == ItemStack.EMPTY
            && hatch.removeItemNoUpdate(0) == ItemStack.EMPTY,
        "detached vessel hatch never returns null item stacks");
    check(
        ReactorStructure.onShell(BlockPos.ZERO, new BlockPos(2, 1, -1))
            && !ReactorStructure.onShell(BlockPos.ZERO, new BlockPos(1, 1, 1))
            && !ReactorStructure.onShell(BlockPos.ZERO, new BlockPos(3, 0, 0)),
        "fluid endpoints belong only to the bounded external shell");
    int cells = 0;
    for (int x = -3; x <= 3; x++)
      for (int y = -3; y <= 3; y++)
        for (int z = -3; z <= 3; z++)
          if (ReactorStructure.onShell(BlockPos.ZERO, new BlockPos(x, y, z))) cells++;
    check(cells == 98, "a five-block vessel has exactly 98 unique shell cells");
    var position = new BlockPos(8280, 100, 8280);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, ReactorBlocks.NUCLEAR_REACTOR.defaultBlockState());
    var nativeCore = (NuclearCore) world.getBlockEntity(position);
    check(
        nativeCore.getReactorSize() == 3,
        "fresh native core exposes three columns before chambers");
    int width = 3;
    for (var face : net.minecraft.core.Direction.values()) {
      world.setBlockAndUpdate(
          position.relative(face), ReactorBlocks.REACTOR_CHAMBER.defaultBlockState());
      check(
          nativeCore.getReactorSize() == ++width,
          "same-tick chamber placement updates live width " + face);
    }
    nativeCore.setItemAt(8, 5, new ItemStack(ReactorItems.REACTOR_COOLANT_CELL));
    check(
        nativeCore.getItemAt(8, 5).is(ReactorItems.REACTOR_COOLANT_CELL),
        "expanded-grid write cannot be discarded by stale layout caching");
    for (var face : net.minecraft.core.Direction.values())
      world.removeBlock(position.relative(face), false);
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
