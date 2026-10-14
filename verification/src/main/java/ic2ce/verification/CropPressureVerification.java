package ic2ce.verification;

import com.google.gson.GsonBuilder;
import com.sun.management.ThreadMXBean;
import ic2ce.api.crops.*;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Equal-work native crop scheduling benchmark; only the disposable verification artifact contains
 * this subscriber.
 */
@EventBusSubscriber(modid = "ic2")
public final class CropPressureVerification {
  private static CropPressureVerification active;
  private final ThreadMXBean bean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
  private final List<String> failures = new ArrayList<>();
  private final List<Map<String, Object>> phases = new ArrayList<>();
  private final List<ICropTile> plots = new ArrayList<>();
  private final IdentityHashMap<ICropTile, Integer> indexes = new IdentityHashMap<>();
  private final int[] scales =
      Arrays.stream(System.getProperty("ic2.cropPerfScales", "1024,4096,8192").split(","))
          .mapToInt(Integer::parseInt)
          .toArray();
  private final int warmup = Integer.getInteger("ic2.cropPerfWarmup", 1024),
      measured = Integer.getInteger("ic2.cropPerfTicks", 4096);
  private final long[] wall = new long[measured],
      cpu = new long[measured],
      allocated = new long[measured];
  private long[] calls;
  private long wallStart, cpuStart, allocatedStart;
  private int scale = -1, ticks, sampled;
  private MinecraftServer server;
  private ServerLevel world;
  private boolean complete;

  @SubscribeEvent
  public static void started(ServerStartedEvent event) {
    if (!Boolean.getBoolean("ic2.verifyCropPressure")) return;
    active = new CropPressureVerification();
    active.server = event.getServer();
    active.world = event.getServer().overworld();
    active.bean.setThreadAllocatedMemoryEnabled(true);
    active.bean.setThreadCpuTimeEnabled(true);
    event
        .getServer()
        .getCommands()
        .performPrefixedCommand(
            event.getServer().createCommandSourceStack(), "gamerule minecraft:spawn_mobs false");
    event
        .getServer()
        .getCommands()
        .performPrefixedCommand(
            event.getServer().createCommandSourceStack(), "gamerule minecraft:random_tick_speed 0");
    event
        .getServer()
        .getCommands()
        .performPrefixedCommand(event.getServer().createCommandSourceStack(), "time set noon");
    event.getServer().tickRateManager().requestGameToSprint(Integer.MAX_VALUE);
  }

  @SubscribeEvent
  public static void pre(ServerTickEvent.Pre event) {
    if (active == null || active.complete || active.scale < 0) return;
    if (active.ticks == active.warmup) Arrays.fill(active.calls, 0);
    active.wallStart = System.nanoTime();
    active.cpuStart = active.bean.getCurrentThreadCpuTime();
    active.allocatedStart = active.bean.getThreadAllocatedBytes(Thread.currentThread().threadId());
  }

  @SubscribeEvent
  public static void post(ServerTickEvent.Post event) {
    if (active == null || active.complete) return;
    if (active.scale < 0) {
      active.next();
      return;
    }
    long elapsed = System.nanoTime() - active.wallStart;
    long elapsedCpu = active.bean.getCurrentThreadCpuTime() - active.cpuStart;
    long bytes =
        active.bean.getThreadAllocatedBytes(Thread.currentThread().threadId())
            - active.allocatedStart;
    if (active.ticks >= active.warmup && active.sampled < active.measured) {
      int index = active.sampled++;
      active.wall[index] = elapsed;
      active.cpu[index] = elapsedCpu;
      active.allocated[index] = bytes;
    }
    active.ticks++;
    if (active.sampled == active.measured) active.finishPhase();
  }

  private void next() {
    for (var plot : plots) {
      world.removeBlock(plot.getPosition(), false);
      world.removeBlock(plot.getPosition().below(), false);
    }
    plots.clear();
    indexes.clear();
    if (++scale == scales.length) {
      complete = true;
      server.tickRateManager().stopSprinting();
      save();
      server.halt(false);
      return;
    }
    ticks = sampled = 0;
    calls = new long[scales[scale]];
    ProjectRuntime.random.setSeed(382711);
    var block =
        BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("ic2", "wheat_crop"));
    var type =
        new ICropType() {
          @Override
          public String getName() {
            return "wheat";
          }

          @Override
          public String getOwner() {
            return "ic2";
          }

          @Override
          public Block getCropBlock() {
            return block;
          }

          @Override
          public int getMaxAge() {
            return 7;
          }
        };
    var properties = new CropProperties(1, 0, 4, 0, 0, 2);
    var species =
        new CropCard(type) {
          @Override
          public Block getCropBlock() {
            return block;
          }

          @Override
          public CropProperties getProperties() {
            return properties;
          }

          @Override
          public List<Identifier> getTexturesLocation() {
            return List.of();
          }

          @Override
          public boolean canGrow(ICropTile plot) {
            return true;
          }

          @Override
          public int getGrowthDuration(ICropTile plot) {
            return 1000000;
          }

          @Override
          public int getWeightInfluences(ICropTile plot, int humidity, int nutrients, int air) {
            return 100;
          }

          @Override
          public boolean isWeed(ICropTile plot) {
            return false;
          }

          @Override
          public void tick(ICropTile plot) {
            calls[indexes.get(plot)]++;
          }
        };
    for (int index = 0; index < calls.length; index++) {
      var position = new BlockPos(16000 + index % 128, 100, 16000 + index / 128);
      world.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
      world.getChunkAt(position);
      world.setBlockAndUpdate(position.below(), Blocks.FARMLAND.defaultBlockState());
      world.setBlockAndUpdate(position, block.defaultBlockState());
      var nativePlot = world.getBlockEntity(position);
      if (!(nativePlot instanceof ICropTile plot))
        throw new IllegalStateException("Missing real crop at " + position);
      try {
        var field = nativePlot.getClass().getDeclaredField("crop");
        field.setAccessible(true);
        field.set(nativePlot, species);
      } catch (ReflectiveOperationException error) {
        throw new IllegalStateException(error);
      }
      plot.setStorageWater(200);
      plot.setStorageNutrients(200);
      plots.add(plot);
      indexes.put(plot, index);
    }
    save();
  }

  private void finishPhase() {
    long expected = measured / 256, total = 0;
    for (int index = 0; index < calls.length; index++) {
      total += calls[index];
      if (calls[index] != expected)
        failures.add(
            "Plot " + index + " received " + calls[index] + " growth calls; expected " + expected);
      if (plots.get(index).getGrowthPoints() <= 0 || plots.get(index).getCrop() == null)
        failures.add("Native growth lost at plot " + index);
    }
    var sorted = wall.clone();
    Arrays.sort(sorted);
    var row = new LinkedHashMap<String, Object>();
    row.put("crops", calls.length);
    row.put("measuredTicks", measured);
    row.put("growthCalls", total);
    row.put("growthCallsPerPlot", expected);
    row.put("meanMSPT", Arrays.stream(wall).average().orElseThrow() / 1e6);
    row.put("p95MSPT", sorted[(int) (sorted.length * .95)] / 1e6);
    row.put("p99MSPT", sorted[(int) (sorted.length * .99)] / 1e6);
    row.put("maxMSPT", sorted[sorted.length - 1] / 1e6);
    row.put("meanThreadCpuMSPT", Arrays.stream(cpu).average().orElseThrow() / 1e6);
    row.put("allocatedBytesPerTick", Arrays.stream(allocated).average().orElseThrow());
    row.put("tickWallNanoseconds", wall.clone());
    row.put("tickCpuNanoseconds", cpu.clone());
    row.put("tickAllocatedBytes", allocated.clone());
    phases.add(row);
    save();
    next();
  }

  private void save() {
    var result = new LinkedHashMap<String, Object>();
    result.put("run", System.getProperty("ic2.perfRun", "unknown"));
    result.put("complete", complete && failures.isEmpty());
    result.put("failures", failures);
    result.put("phases", phases);
    result.put("javaVersion", System.getProperty("java.runtime.version"));
    result.put("vmArguments", ManagementFactory.getRuntimeMXBean().getInputArguments());
    result.put(
        "method",
        "Actual forced native farm chunks, normal placed crop ticking, 256-tick growth cadence,"
            + " identical bounded high-duration species observer, exact per-plot call counts; fresh"
            + " warmed JVMs. Random block ticking disabled for equal soil and no unrelated age"
            + " changes.");
    try {
      Files.writeString(
          Path.of("ic2-crop-pressure.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(result));
    } catch (java.io.IOException error) {
      throw new java.io.UncheckedIOException(error);
    }
  }
}
