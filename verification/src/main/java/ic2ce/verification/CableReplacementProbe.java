package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.api.energy.EnergyNet;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.cable.ConductorBlock;
import ic2ce.neoforge.cable.RedstoneGateBlock;
import ic2ce.neoforge.component.ElectricalBuffer;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Actual same-tick destruction/replacement with deferred energy-net changes. */
public final class CableReplacementProbe {
  private record Circuit(String id, BlockState cable, BlockPos first, NativeBlockEntity sink) {}

  private final List<Circuit> circuits = new ArrayList<>();
  private final List<String> failures = new ArrayList<>();
  private final List<Map<String, Object>> cases = new ArrayList<>();
  private final Map<String, Double> before = new HashMap<>();
  private ServerLevel level;
  private int ticks, checks;

  private void check(boolean condition, String message) {
    checks++;
    if (!condition) failures.add(message);
  }

  @SubscribeEvent
  public void started(ServerStartedEvent event) {
    level = event.getServer().overworld();
    int index = 0;
    var generator =
        BuiltInRegistries.BLOCK.getValue(
            Identifier.fromNamespaceAndPath("ic2", "creative_generator"));
    var bank = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("ic2", "mfsu"));
    for (var id :
        BuiltInRegistries.BLOCK.keySet().stream()
            .filter(id -> id.getNamespace().equals("ic2"))
            .sorted()
            .toList()) {
      var block = BuiltInRegistries.BLOCK.getValue(id);
      if (!(block instanceof ConductorBlock cable) || cable.isFoam()) continue;
      var base = new BlockPos(2048 + index++ * 16, 80, 256);
      level.setChunkForced(base.getX() >> 4, base.getZ() >> 4, true);
      level.getChunkAt(base);
      var state = cable.defaultBlockState();
      if (cable instanceof RedstoneGateBlock)
        state = state.setValue(RedstoneGateBlock.active, true);
      level.setBlock(base, generator.defaultBlockState(), 3);
      for (int x = 1; x <= 4; x++) {
        if (cable instanceof RedstoneGateBlock)
          level.setBlock(base.east(x).below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        level.setBlock(base.east(x), state, 3);
      }
      level.setBlock(base.east(5), bank.defaultBlockState(), 3);
      circuits.add(
          new Circuit(
              id.toString(),
              state,
              base.east(),
              (NativeBlockEntity) level.getBlockEntity(base.east(5))));
    }
    check(circuits.size() == 14, "all fourteen ordinary cable variants");
    event.getServer().tickRateManager().requestGameToSprint(500);
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (level == null) return;
    ticks++;
    if (ticks == 100 || ticks == 200) {
      for (var circuit : circuits) {
        double energy = circuit.sink.getComponent(ElectricalBuffer.class).getEnergy();
        check(energy > 0, "initial real EU delivery " + circuit.id);
        before.put(circuit.id, energy);
        int repeats = ticks == 100 ? 1 : 10;
        for (int pass = 0; pass < repeats; pass++)
          for (int x = 0; x < 4; x++) {
            var pos = circuit.first.east(x);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            check(
                EnergyNet.instance.getTile(level, pos) == null,
                "removed node hidden immediately " + circuit.id + " pass=" + pass);
            level.setBlock(pos, circuit.cable, 3);
            check(
                EnergyNet.instance.getTile(level, pos) != null,
                "replacement node queued immediately " + circuit.id + " pass=" + pass);
          }
      }
    }
    if (ticks == 180 || ticks == 280) {
      for (var circuit : circuits) {
        double energy = circuit.sink.getComponent(ElectricalBuffer.class).getEnergy();
        boolean delivered = energy > before.get(circuit.id);
        check(
            delivered,
            "EU continues after same-tick replacement "
                + circuit.id
                + " rounds="
                + (ticks == 180 ? 1 : 10));
        cases.add(
            Map.of(
                "id",
                circuit.id,
                "replacementRounds",
                ticks == 180 ? 1 : 10,
                "energyBefore",
                before.get(circuit.id),
                "energyAfter",
                energy,
                "passed",
                delivered));
      }
    }
    if (ticks == 300) {
      var report =
          Map.of(
              "checks",
              checks,
              "cases",
              cases,
              "failures",
              failures,
              "complete",
              failures.isEmpty(),
              "ticks",
              ticks);
      try {
        Files.writeString(
            Path.of("ic2-cable-replacement-verification.json"),
            new GsonBuilder().setPrettyPrinting().create().toJson(report));
      } catch (Exception error) {
        throw new IllegalStateException(error);
      }
      event.getServer().tickRateManager().stopSprinting();
      event.getServer().halt(false);
    }
  }
}
