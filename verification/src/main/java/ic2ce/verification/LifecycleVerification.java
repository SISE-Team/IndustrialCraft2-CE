package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.IEnergyTile;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.storage.StationaryFluidStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Actual saved chunk unload/reload, rather than only an in-memory NBT reconstruction. */
public final class LifecycleVerification {
  public static volatile boolean finished;
  private String phase = "WAITING_FOR_MACHINE_SUITE";
  private int ticks, checks;
  private final List<String> failures = new ArrayList<>();
  private final BlockPos position = new BlockPos(16008, 181, 16008);
  private NativeBlockEntity bank;
  private StationaryFluidStore tank;
  private BlockEntity previousBank, previousTank;
  private IEnergyTile previousCable;

  private void check(boolean passed, String label) {
    checks++;
    if (!passed) failures.add(label);
  }

  private void save() throws Exception {
    Files.writeString(
        Path.of("ic2-lifecycle-verification.json"),
        new GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(
                Map.of(
                    "phase",
                    phase,
                    "checks",
                    checks,
                    "complete",
                    finished && failures.isEmpty(),
                    "finished",
                    finished,
                    "failures",
                    failures,
                    "chunkX",
                    position.getX() >> 4,
                    "chunkZ",
                    position.getZ() >> 4)));
  }

  private void transition(String next) throws Exception {
    phase = next;
    ticks = 0;
    save();
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (finished) return;
    var suite = AllMachinesVerification.ACTIVE;
    if (suite == null || !suite.baseFinished()) return;
    try {
      ticks++;
      if (phase.equals("WAITING_FOR_MACHINE_SUITE")) {
        suite.level.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
        suite.level.getChunkAt(position);
        bank = suite.helper("mfsu", position);
        tank = (StationaryFluidStore) suite.helper("tank", position.east(2));
        suite.level.setBlockAndUpdate(
            position.south(2), BlockDefinitions.INSULATED_COPPER_CABLE.defaultBlockState());
        transition("WAITING_FOR_NATIVE_LOAD");
        suite.level.getServer().tickRateManager().requestGameToSprint(400);
      } else if (phase.equals("WAITING_FOR_NATIVE_LOAD")) {
        if (((Number) AllMachinesVerification.get(bank, "loadState")).intValue() != 2
            || ((Number) AllMachinesVerification.get(tank, "loadState")).intValue() != 2) {
          if (ticks > 1200)
            throw new IllegalStateException(
                "Lifecycle fixtures did not become native loaded blocks");
          return;
        }
        bank.getComponent(ElectricalBuffer.class).forceAddEnergy(12345);
        tank.getFluidTank()
            .fillMbUnchecked(
                Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 2468), false);
        bank.setChanged();
        tank.setChanged();
        previousBank = bank;
        previousTank = tank;
        previousCable = EnergyNet.instance.getTile(suite.level, position.south(2));
        check(previousCable != null, "actual loaded cable owns an electrical conductor");
        suite.level.getChunkSource().save(true);
        suite.level.setChunkForced(position.getX() >> 4, position.getZ() >> 4, false);
        transition("WAITING_FOR_REAL_UNLOAD");
        suite.level.getServer().tickRateManager().requestGameToSprint(1200);
      } else if (phase.equals("WAITING_FOR_REAL_UNLOAD")) {
        if (suite.level.getChunkSource().getChunkNow(position.getX() >> 4, position.getZ() >> 4)
                != null
            || !previousBank.isRemoved()
            || !previousTank.isRemoved()) {
          if (ticks > 1800)
            throw new IllegalStateException(
                "The unforced distant native chunk and its block entities did not finish"
                    + " unloading");
          return;
        }
        check(
            previousBank.isRemoved() && previousTank.isRemoved(),
            "actual native unload removes both old block entities");
        check(
            EnergyNet.instance.getTile(suite.level, position.south(2)) == null,
            "actual chunk unload removes the old cable endpoint without reloading the chunk");
        suite.level.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
        suite.level.getChunkAt(position);
        transition("WAITING_FOR_REAL_RELOAD");
        suite.level.getServer().tickRateManager().requestGameToSprint(400);
      } else if (phase.equals("WAITING_FOR_REAL_RELOAD")) {
        var reloadedBank = suite.level.getBlockEntity(position);
        var reloadedTank = suite.level.getBlockEntity(position.east(2));
        if (!(reloadedBank instanceof NativeBlockEntity nativeBank)
            || !(reloadedTank instanceof StationaryFluidStore nativeTank)
            || ((Number) AllMachinesVerification.get(nativeBank, "loadState")).intValue() != 2
            || ((Number) AllMachinesVerification.get(nativeTank, "loadState")).intValue() != 2) {
          if (ticks > 1200)
            throw new IllegalStateException("Saved native block entities did not reload");
          return;
        }
        check(
            reloadedBank != previousBank && reloadedTank != previousTank,
            "reload creates new actual block entities");
        check(
            nativeBank.getComponent(ElectricalBuffer.class).getEnergy() == 12345,
            "native disk reload preserves exactly the stored energy");
        check(
            nativeTank.getFluidTank().getFluidAmount() == 2468
                && nativeTank
                    .getFluidTank()
                    .hasExactFluid(net.minecraft.world.level.material.Fluids.WATER),
            "native disk reload preserves exactly the stored fluid");
        var reloadedCable = EnergyNet.instance.getTile(suite.level, position.south(2));
        check(
            reloadedCable != null && reloadedCable != previousCable,
            "actual disk reload registers a fresh native conductor identity");
        suite.level.removeBlock(position.south(2), false);
        suite.level.removeBlock(position, false);
        suite.level.removeBlock(position.east(2), false);
        suite.level.setChunkForced(position.getX() >> 4, position.getZ() >> 4, false);
        suite.level.getServer().tickRateManager().stopSprinting();
        finished = true;
        transition(failures.isEmpty() ? "COMPLETE" : "FAILED");
      }
      if (ticks % 100 == 0) save();
    } catch (Throwable error) {
      failures.add(error.toString());
      finished = true;
      phase = "FAILED";
      suite.level.getServer().tickRateManager().stopSprinting();
      try {
        save();
      } catch (Exception reportError) {
        error.addSuppressed(reportError);
      }
      error.printStackTrace();
    }
  }
}
