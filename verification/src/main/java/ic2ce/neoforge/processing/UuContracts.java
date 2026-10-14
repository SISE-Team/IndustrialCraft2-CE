package ic2ce.neoforge.processing;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.IPatternStorage;
import ic2ce.core.item.ItemCrystalMemory;
import ic2ce.neoforge.processing.fabrication.PatternReplicator;
import ic2ce.neoforge.processing.fabrication.PatternScanner;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native UU transactions and corrupt persistence use real item components and tanks. */
public final class UuContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native UU: " + label);
  }

  public static Result run(ServerLevel world) {
    return new UuContracts().exercise(world);
  }

  private static final class Patterns implements IPatternStorage {
    private final List<ItemStack> entries = new ArrayList<>();

    public boolean addPattern(ItemStack pattern) {
      entries.add(pattern.copy());
      return true;
    }

    public List<ItemStack> getPatterns() {
      return entries;
    }
  }

  private static final class Replicator extends PatternReplicator {
    private final Patterns storage = new Patterns();

    Replicator() {
      super(new BlockPos(8220, 100, 8220), BlockDefinitions.REPLICATOR.defaultBlockState());
    }

    @Override
    public IPatternStorage getPatternStorage() {
      return storage;
    }
  }

  private Result exercise(ServerLevel world) {
    var crystal = new ItemStack(ItemDefinitions.CRYSTAL_MEMORY);
    var memory = (ItemCrystalMemory) crystal.getItem();
    var before = crystal.copy();
    check(
        memory.readItemStack(crystal).isEmpty()
            && ItemStack.isSameItemSameComponents(before, crystal),
        "blank crystal read does not create custom data");
    memory.writecontentsTag(world, crystal, new ItemStack(Items.IRON_ORE, 64));
    check(
        memory.readItemStack(crystal).getCount() == 64,
        "crystal storage preserves quantity while machines normalize their own selected patterns");
    memory.writecontentsTag(world, crystal, ItemStack.EMPTY);
    check(memory.readItemStack(crystal).isEmpty(), "writing empty clears an old crystal pattern");
    var scanner =
        new PatternScanner(
            new BlockPos(8221, 100, 8221), BlockDefinitions.UU_SCANNER.defaultBlockState());
    scanner.setLevel(world);
    var malformed = new CompoundTag();
    malformed.putInt("state", -1);
    malformed.putInt("progress", -200);
    scanner.load(malformed);
    check(
        scanner.getState() == PatternScanner.State.IDLE && scanner.progress == 0,
        "negative scanner state and progress are sanitized");
    malformed.put("pattern", new CompoundTag());
    malformed.put("currentStack", new CompoundTag());
    scanner.saveAdditional(malformed);
    check(
        !malformed.contains("pattern") && !malformed.contains("currentStack"),
        "empty scanner save removes stale pattern fields");
    var replicator = new Replicator();
    replicator.setLevel(world);
    replicator.setChanged();
    malformed = new CompoundTag();
    malformed.putInt("mode", -5);
    malformed.putDouble("uuProcessed", Double.NaN);
    malformed.putDouble("extraUuStored", Double.POSITIVE_INFINITY);
    replicator.load(malformed);
    check(
        replicator.getMode() == PatternReplicator.Mode.STOPPED && replicator.uuProcessed == 0,
        "invalid replication mode and nonfinite progress are rejected");
    replicator.storage.addPattern(new ItemStack(Items.IRON_ORE, 64));
    replicator.refreshInfo();
    check(
        replicator.pattern.is(Items.IRON_ORE)
            && replicator.pattern.getCount() == 1
            && replicator.maxIndex == 1,
        "replication owns a unit-normalized selected pattern");
    replicator.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(20000);
    replicator.fluidTank.fillMbUnchecked(
        Ic2FluidStack.create(FluidDefinitions.UU_MATTER.still, 100), false);
    for (int n = 0; n < 10; n++) {
      replicator.patternUu = 0.0001;
      replicator.onNetworkEvent(null, 4);
      MachineFixtureAccess.tick(replicator);
    }
    check(
        replicator.outputSlot.get().getCount() == 10 && replicator.fluidTank.getFluidAmount() == 99,
        "ten fractional ticks consume one millibucket and produce exactly ten paid units");
    check(
        replicator.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getEnergy()
            == 20000 - 512 * 10,
        "each completing replication tick is billed once");
    replicator.patternUu = 0;
    replicator.onNetworkEvent(null, 5);
    replicator.storage.entries.set(0, new ItemStack(Items.STONE));
    MachineFixtureAccess.tick(replicator);
    check(
        replicator.outputSlot.get().is(Items.IRON_ORE)
            && replicator.outputSlot.get().getCount() == 11,
        "continuous selection changes cannot substitute a new output for the item already paid"
            + " for");
    check(
        replicator.pattern.is(Items.STONE)
            && replicator.getMode() == PatternReplicator.Mode.STOPPED,
        "changed continuous pattern stops before paying for a different recipe");
    replicator.patternUu = Double.POSITIVE_INFINITY;
    replicator.onNetworkEvent(null, 4);
    double energy = replicator.getEnergy();
    MachineFixtureAccess.tick(replicator);
    check(
        replicator.getEnergy() == energy && replicator.fluidTank.getFluidAmount() == 99,
        "unknown replication costs do not consume resources");
    return new Result(checks, List.copyOf(failures));
  }
}
