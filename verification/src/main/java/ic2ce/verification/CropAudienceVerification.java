package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.api.crops.*;
import ic2ce.neoforge.agriculture.AgricultureBlocks;
import ic2ce.neoforge.world.WorldData;
import ic2ce.neoforge.network.ReflectionUtil;
import ic2ce.neoforge.agriculture.CropPlot;
import ic2ce.neoforge.agriculture.Cultivar;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Finite real-client arrival test after an unattended native farm; no production instrumentation.
 */
@EventBusSubscriber(modid = "ic2")
public final class CropAudienceVerification {
  private static CropAudienceVerification active;
  private final List<String> failures = new ArrayList<>();
  private final List<CropPlot> plots = new ArrayList<>();
  private final long[] calls = new long[8];
  private ServerLevel world;
  private String phase = "WARMUP";
  private int age, checks;
  private boolean complete;

  @SubscribeEvent
  public static void started(ServerStartedEvent event) throws Exception {
    if (!Boolean.getBoolean("ic2.verifyCropAudience")) return;
    active = new CropAudienceVerification();
    active.world = event.getServer().overworld();
    for (int index = 0; index < 8; index++) {
      int slot = index;
      var position = new BlockPos(16000 + index, 100, 16000);
      active.world.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
      active.world.getChunkAt(position);
      active.world.setBlockAndUpdate(position.below(), Blocks.FARMLAND.defaultBlockState());
      active.world.setBlockAndUpdate(position, AgricultureBlocks.WHEAT_CROP.defaultBlockState());
      var plot = (CropPlot) active.world.getBlockEntity(position);
      var species =
          new CropCard(Cultivar.WHEAT) {
            @Override
            public Block getCropBlock() {
              return AgricultureBlocks.WHEAT_CROP;
            }

            @Override
            public CropProperties getProperties() {
              return new CropProperties(1, 0, 4, 0, 0, 2);
            }

            @Override
            public List<Identifier> getTexturesLocation() {
              return List.of();
            }

            @Override
            public boolean canGrow(ICropTile tile) {
              return true;
            }

            @Override
            public int getGrowthDuration(ICropTile tile) {
              return 1000000;
            }

            @Override
            public int getWeightInfluences(ICropTile tile, int humidity, int nutrients, int air) {
              return 100;
            }

            @Override
            public boolean isWeed(ICropTile tile) {
              return false;
            }

            @Override
            public void tick(ICropTile tile) {
              active.calls[slot]++;
            }
          };
      ReflectionUtil.getFieldRecursive(CropPlot.class, "crop").set(plot, species);
      plot.setStorageWater(200);
      plot.setStorageNutrients(200);
      plot.setScanLevel(4);
      active.plots.add(plot);
    }
    event.getServer().tickRateManager().requestGameToSprint(Integer.MAX_VALUE);
    active.save();
  }

  private void check(boolean good, String text) {
    checks++;
    if (!good) failures.add(text);
  }

  private void change(String next) {
    phase = next;
    age = 0;
    save();
  }

  private boolean nativeTicker(CropPlot plot) throws Exception {
    var chunk = world.getChunkAt(plot.getBlockPos());
    var field = ReflectionUtil.getFieldRecursive(LevelChunk.class, "tickersInLevel");
    return ((Map<?, ?>) field.get(chunk)).containsKey(plot.getBlockPos());
  }

  @SubscribeEvent
  public static void tick(ServerTickEvent.Post event) {
    if (active == null || active.complete) return;
    active.step();
  }

  private void step() {
    age++;
    try {
      if (phase.equals("WARMUP") && age == 300) {
        check(world.players().isEmpty(), "unattended phase has no actual player");
        for (var plot : plots)
          check(!nativeTicker(plot), "unattended crop has no idle native ticker");
        Arrays.fill(calls, 0);
        change("UNATTENDED_COUNT");
      } else if (phase.equals("UNATTENDED_COUNT") && age == 1024) {
        for (long count : calls)
          check(count == 4, "each unattended plot receives exactly four growth callbacks");
        world.getServer().tickRateManager().stopSprinting();
        change("WAITING_FOR_PLAYER");
      } else if (phase.equals("WAITING_FOR_PLAYER") && !world.players().isEmpty()) {
        var actor = world.players().getFirst();
        actor.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        actor.teleportTo(16003.5, 101, 16003.5);
        change("PLAYER_SETTLE");
      } else if (phase.equals("PLAYER_SETTLE") && age == 200) {
        for (var plot : plots)
          check(nativeTicker(plot), "actual player arrival restores native crop ticker");
        Arrays.fill(calls, 0);
        change("OBSERVED_COUNT");
      } else if (phase.equals("OBSERVED_COUNT") && age == 512) {
        for (long count : calls)
          check(
              count == 2,
              "each observed plot receives exactly two callbacks without duplicate native/scheduled"
                  + " growth");
        var client =
            new com.google.gson.Gson()
                .fromJson(
                    Files.readString(Path.of(System.getProperty("ic2.cropAudienceClientReport"))),
                    Map.class);
        check(
            Boolean.TRUE.equals(client.get("complete")),
            "real client receives all eight crop phenotype/care states");
        world.getServer().tickRateManager().setFrozen(true);
        Arrays.fill(calls, 0);
        change("FROZEN");
      } else if (phase.equals("FROZEN") && age == 40) {
        for (long count : calls) check(count == 0, "frozen server has no crop growth callback");
        world.getServer().tickRateManager().setFrozen(false);
        change("WAITING_FOR_LOGOUT");
      } else if (phase.equals("WAITING_FOR_LOGOUT") && world.players().isEmpty()) {
        change("LOGOUT_SETTLE");
      } else if (phase.equals("LOGOUT_SETTLE") && age == 40) {
        for (var plot : plots)
          check(!nativeTicker(plot), "actual player logout removes idle crop tickers");
        int before = WorldData.get(world).cropTicks.size();
        for (var plot : plots) world.removeBlock(plot.getBlockPos(), false);
        check(
            WorldData.get(world).cropTicks.size() == before - plots.size(),
            "actual block removal releases every farm schedule entry");
        complete = failures.isEmpty();
        change(complete ? "COMPLETE" : "FAILED");
        world.getServer().halt(false);
      }
      if (age % 20 == 0) save();
    } catch (Exception failure) {
      failures.add(failure.toString());
      change("FAILED");
      world.getServer().halt(false);
    }
  }

  private void save() {
    try {
      Files.writeString(
          Path.of("crop-audience-verification.json"),
          new GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  Map.of(
                      "phase",
                      phase,
                      "age",
                      age,
                      "checks",
                      checks,
                      "complete",
                      complete,
                      "calls",
                      calls,
                      "failures",
                      failures)));
    } catch (Exception failure) {
      throw new IllegalStateException(failure);
    }
  }

  @EventBusSubscriber(modid = "ic2", value = net.neoforged.api.distmarker.Dist.CLIENT)
  public static final class Client {
    @SubscribeEvent
    public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if (!Boolean.getBoolean("ic2.verifyCropAudience")) return;
      var mc = net.minecraft.client.Minecraft.getInstance();
      if (mc.level == null || mc.player == null) return;
      int synced = 0;
      for (int index = 0; index < 8; index++)
        if (mc.level.getBlockEntity(new BlockPos(16000 + index, 100, 16000))
                instanceof CropPlot plot
            && plot.getScanLevel() == 4
            && plot.getGrowthPoints() > 0
            && plot.getStorageWater() > 0
            && plot.getStorageNutrients() > 0) synced++;
      try {
        Files.writeString(
            Path.of("crop-audience-client.json"),
            new GsonBuilder()
                .create()
                .toJson(
                    Map.of(
                        "complete",
                        synced == 8,
                        "syncedPlots",
                        synced,
                        "updatedAt",
                        System.currentTimeMillis())));
        var serverReport = System.getProperty("ic2.cropAudienceServerReport");
        if (serverReport != null && Files.isRegularFile(Path.of(serverReport))) {
          var state =
              new com.google.gson.Gson()
                  .fromJson(Files.readString(Path.of(serverReport)), Map.class);
          if ("WAITING_FOR_LOGOUT".equals(state.get("phase"))) mc.stop();
        }
      } catch (Exception failure) {
        throw new IllegalStateException(failure);
      }
    }
  }
}
