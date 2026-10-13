package ic2.verification;

import com.google.gson.GsonBuilder;
import com.sun.management.ThreadMXBean;
import ic2.core.IC2;
import ic2.core.block.comp.Energy;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.block.wiring.AbstractCableBlock;
import ic2.core.network.DataEncoder;
import ic2.core.network.GrowingBuffer;
import ic2.forge.PerformanceInventoryProbe;
import java.lang.management.ManagementFactory;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.DigestOutputStream;
import jdk.jfr.Event;
import jdk.jfr.Name;
import jdk.jfr.Label;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Repeatable native-server workload. This fixture is never shipped in the normal JAR. */
public final class PerformanceVerification {
    private final ThreadMXBean bean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
    private final List<String> failures = new ArrayList<>();
    private final List<Map<String,Object>> micros = new ArrayList<>(), phases = new ArrayList<>();
    private final List<TileEntityStandardMachine<?,?,?>> machines = new ArrayList<>();
    private final List<BlockPos> circuitBlocks = new ArrayList<>();
    private final List<PerformanceInventoryProbe> inventories = new ArrayList<>();
    private final int[] scales = Arrays.stream(System.getProperty("ic2.perfScales", "128,512,1024").split(",")).mapToInt(Integer::parseInt).toArray();
    private final int warmupTicks = Integer.getInteger("ic2.perfWarmupTicks", 600);
    private final int measuredTicks = Integer.getInteger("ic2.perfMeasuredTicks", 1600);
    private final int transactionsPerTick = Integer.getInteger("ic2.perfTransactionsPerTick", 512);
    private final long[] durations = new long[measuredTicks], cpuTimes = new long[measuredTicks], allocations = new long[measuredTicks];
    private ServerLevel level;
    private MinecraftServer server;
    private boolean started, complete;
    private int scaleIndex = -1, ticks, sampled;
    private long tickWall, tickCpu, tickAllocated, totalOutput, transactions, checksum;
    private String registryFingerprint, cableShapeFingerprint;
    private int cableStates;
    private PhaseEvent phaseEvent;
    private static volatile long consumed;

    @SubscribeEvent public void started(ServerStartedEvent event) {
        server = event.getServer(); level = server.overworld();
        if (!bean.isThreadAllocatedMemorySupported() || !bean.isCurrentThreadCpuTimeSupported()) {
            throw new IllegalStateException("Native per-thread CPU/allocation counters required");
        }
        bean.setThreadAllocatedMemoryEnabled(true); bean.setThreadCpuTimeEnabled(true);
        IC2.random.setSeed(20261013L);
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "gamerule minecraft:advance_time false");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "gamerule minecraft:spawn_mobs false");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set noon");
        setupBoxes();
        failures.addAll(PerformanceInventoryProbe.verifyCustomContainerFallback());
        fingerprint();
        started = true;
        server.tickRateManager().requestGameToSprint(Integer.MAX_VALUE);
    }

    private static Block block(String name) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("ic2", name));
    }

    private Ic2TileEntity place(String name, BlockPos pos) {
        level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
        level.getChunkAt(pos);
        level.setBlock(pos.below(), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
        level.setBlock(pos, block(name).defaultBlockState(), 3);
        if (!(level.getBlockEntity(pos) instanceof Ic2TileEntity tile)) throw new IllegalStateException("Missing native entity: " + name);
        return tile;
    }

    private void setupBoxes() {
        int i = 0;
        for (String name : List.of("wooden_storage_box", "iron_storage_box", "bronze_storage_box", "steel_storage_box", "iridium_storage_box")) {
            var inventory = new PerformanceInventoryProbe((TileEntityInventory) place(name, new BlockPos(160 + i++ * 16, 80, 160)));
            failures.addAll(inventory.verifyNestedTransactions()); inventories.add(inventory);
        }
    }

    @SubscribeEvent public void pre(ServerTickEvent.Pre event) {
        if (!started || complete || scaleIndex < 0) return;
        if (ticks == warmupTicks) { phaseEvent = new PhaseEvent(); phaseEvent.kind = "stress"; phaseEvent.circuits = scales[scaleIndex]; phaseEvent.begin(); }
        tickWall = System.nanoTime(); tickCpu = bean.getCurrentThreadCpuTime();
        tickAllocated = bean.getThreadAllocatedBytes(Thread.currentThread().threadId());
        for (int i = 0; i < transactionsPerTick; i++) {
            long moved = inventories.get(i % inventories.size()).cycle(1);
            checksum += moved;
        }
        transactions += transactionsPerTick;
    }

    @SubscribeEvent public void post(ServerTickEvent.Post event) {
        if (!started || complete) return;
        try {
            if (scaleIndex < 0) { microbenchmarks(); nextScale(); return; }
            long elapsed = System.nanoTime() - tickWall;
            long cpu = bean.getCurrentThreadCpuTime() - tickCpu;
            long allocated = bean.getThreadAllocatedBytes(Thread.currentThread().threadId()) - tickAllocated;
            if (ticks >= warmupTicks && sampled < measuredTicks) {
                durations[sampled] = elapsed; cpuTimes[sampled] = cpu; allocations[sampled++] = allocated;
            }
            for (var machine : machines) {
                for (var stack : machine.outputSlot) if (!stack.isEmpty()) totalOutput += stack.getCount();
                machine.outputSlot.clear();
                if (machine.inputSlot.get().getCount() < 8) machine.inputSlot.put(new ItemStack(Items.RAW_IRON, 64));
            }
            ticks++;
            if (sampled == measuredTicks) finishScale();
        } catch (Throwable error) {
            failures.add(error.toString()); complete = true; save(); server.halt(false);
        }
    }

    private void nextScale() {
        for (var pos : circuitBlocks) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        circuitBlocks.clear(); machines.clear();
        scaleIndex++;
        if (scaleIndex == scales.length) {
            for (var inventory : inventories) if (!inventory.conserved()) failures.add("inventory conservation");
            if (checksum != transactions * 2) failures.add("committed transfer counts");
            complete = true; consumed = checksum; server.tickRateManager().stopSprinting(); save(); server.halt(false); return;
        }
        ticks = sampled = 0; totalOutput = 0;
        for (int i = 0; i < scales[scaleIndex]; i++) {
            var base = new BlockPos(256 + scaleIndex * 512 + (i % 32) * 8, 80, 256 + (i / 32) * 4);
            place("creative_generator", base); circuitBlocks.add(base);
            for (int x = 1; x <= 4; x++) {
                var pos = base.east(x); level.setBlock(pos, block("tin_cable").defaultBlockState(), 3); circuitBlocks.add(pos);
            }
            var target = base.east(5);
            var machine = (TileEntityStandardMachine<?,?,?>) place("macerator", target);
            machine.inputSlot.put(new ItemStack(Items.RAW_IRON, 64));
            machine.upgradeSlot.put(new ItemStack(blockItem("overclocker_upgrade"), 8));
            machine.upgradeSlot.onChanged(); machine.setChanged();
            machines.add(machine); circuitBlocks.add(target);
        }
        save();
        System.out.println("IC2_PERF SCALE " + scales[scaleIndex]);
    }

    private static net.minecraft.world.item.Item blockItem(String name) {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("ic2", name));
    }

    private void finishScale() {
        phaseEvent.end(); phaseEvent.commit();
        int powered = 0;
        for (var machine : machines) if (machine.getComponent(Energy.class).getEnergy() > 0) powered++;
        if (powered != machines.size() || totalOutput <= machines.size()) failures.add("actual powered processing scale=" + scales[scaleIndex]);
        var row = new LinkedHashMap<String,Object>();
        row.put("circuits", scales[scaleIndex]); row.put("nativeMachines", scales[scaleIndex] * 2);
        row.put("cableBlocks", scales[scaleIndex] * 4); row.put("warmupTicks", warmupTicks); row.put("measuredTicks", sampled);
        row.put("meanMSPT", mean(durations) / 1e6); row.put("p50MSPT", percentile(durations, .5) / 1e6);
        row.put("p95MSPT", percentile(durations, .95) / 1e6); row.put("p99MSPT", percentile(durations, .99) / 1e6);
        row.put("meanServerThreadCpuMS", mean(cpuTimes) / 1e6); row.put("serverThreadAllocatedBytesPerTick", mean(allocations));
        row.put("processedItems", totalOutput); row.put("poweredMachines", powered); row.put("transactionsPerTick", transactionsPerTick);
        row.put("tickDurationsNanoseconds", durations.clone()); row.put("tickCpuNanoseconds", cpuTimes.clone()); row.put("tickAllocatedBytes", allocations.clone());
        phases.add(row); nextScale();
    }

    private void microbenchmarks() throws Exception {
        // Native storage sizes and actual production adapter; mutation is observable.
        for (var inventory : inventories) {
            for (int i = 0; i < 8; i++) consumed += inventory.cycle(4000);
            long cpu = bean.getCurrentThreadCpuTime(), allocated = bean.getThreadAllocatedBytes(Thread.currentThread().threadId());
            long start = System.nanoTime(); long accepted = inventory.cycle(40000); long duration = System.nanoTime() - start;
            micros.add(Map.of("kind", "inventory", "slots", inventory.slots(), "operations", 40000, "nsPerOperation", duration / 40000.0,
                "cpuNsPerOperation", (bean.getCurrentThreadCpuTime() - cpu) / 40000.0,
                "allocatedBytesPerOperation", (bean.getThreadAllocatedBytes(Thread.currentThread().threadId()) - allocated) / 40000.0));
            if (accepted != 80000 || !inventory.conserved()) failures.add("inventory micro conservation " + inventory.slots());
        }
        var factories = BuiltInRegistries.BLOCK.keySet().stream().filter(id -> id.getNamespace().equals("ic2"))
            .map(BuiltInRegistries.BLOCK::getValue).filter(Ic2TileEntityBlock.class::isInstance).map(Ic2TileEntityBlock.class::cast).toList();
        for (int i = 0; i < 10000; i++) consumeFactory(factories, i);
        long allocated = bean.getThreadAllocatedBytes(Thread.currentThread().threadId()); long start = System.nanoTime();
        for (int i = 0; i < 80000; i++) consumeFactory(factories, i);
        micros.add(Map.of("kind", "entity_factory", "types", factories.size(), "operations", 80000, "nsPerOperation", (System.nanoTime() - start) / 80000.0,
            "allocatedBytesPerOperation", (bean.getThreadAllocatedBytes(Thread.currentThread().threadId()) - allocated) / 80000.0));
        for (int workers : new int[]{1, 4, 8}) codec(workers);
    }

    private void consumeFactory(List<Ic2TileEntityBlock> factories, int i) {
        var block = factories.get(i % factories.size());
        var entity = block.createBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        if (entity.getBlockState().getBlock() != block) throw new IllegalStateException("factory state changed");
        consumed += entity.getType().hashCode();
    }

    private record CodecResult(long operations, long bytes, long allocated, String digest) {}

    private void codec(int workers) throws Exception {
        try (var executor = Executors.newFixedThreadPool(workers, Thread.ofPlatform().daemon().name("ic2-performance-codec-", 0).factory())) {
            List<Callable<CodecResult>> warmup = new ArrayList<>(), measured = new ArrayList<>();
            for (int i = 0; i < workers; i++) { warmup.add(() -> codecBatch(20000)); measured.add(() -> codecBatch(100000)); }
            for (int i = 0; i < 5; i++) for (var result : executor.invokeAll(warmup)) consumed += result.get().bytes;
            var event = new PhaseEvent(); event.kind = "codec"; event.circuits = workers; event.begin();
            long start = System.nanoTime(); var results = executor.invokeAll(measured); long duration = System.nanoTime() - start;
            event.end(); event.commit();
            long bytes = 0, allocated = 0; String digest = null;
            for (var result : results) {
                var value = result.get(); bytes += value.bytes; allocated += value.allocated;
                if (digest != null && !digest.equals(value.digest)) failures.add("codec concurrent wire differs"); digest = value.digest;
            }
            consumed += bytes;
            micros.add(Map.of("kind", "codec", "workers", workers, "operations", workers * 100000L,
                "nsPerOperation", duration / (workers * 100000.0), "allocatedBytesPerOperation", allocated / (workers * 100000.0),
                "encodedBytes", bytes, "wireDigestPerWorker", digest));
        }
    }

    private CodecResult codecBatch(int operations) throws Exception {
        var position = new BlockPos.MutableBlockPos(19, 80, 29);
        var payload = new ArrayList<>(List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH));
        var immutablePayload = List.of(Direction.UP, Direction.DOWN);
        var buffer = new GrowingBuffer(512); var digest = MessageDigest.getInstance("SHA-256");
        var sink = new DigestOutputStream(OutputStream.nullOutputStream(), digest);
        long allocated = bean.getThreadAllocatedBytes(Thread.currentThread().threadId()), bytes = 0;
        for (int i = 0; i < operations; i++) {
            buffer.clear(); DataEncoder.encode(buffer, payload, true);
            DataEncoder.encode(buffer, position, true); DataEncoder.encode(buffer, immutablePayload, true); buffer.flip();
            int size = buffer.available(); bytes += size;
            // Bulk digest observes every encoded byte without allocating a buffer per operation.
            buffer.writeTo(sink);
        }
        return new CodecResult(operations, bytes, bean.getThreadAllocatedBytes(Thread.currentThread().threadId()) - allocated,
            HexFormat.of().formatHex(digest.digest()));
    }

    private static double mean(long[] values) { return Arrays.stream(values).average().orElse(0); }
    private static long percentile(long[] values, double p) { long[] copy = values.clone(); Arrays.sort(copy); return copy[(int)Math.ceil(copy.length * p) - 1]; }

    private void save() {
        var report = new LinkedHashMap<String,Object>();
        report.put("run", System.getProperty("ic2.perfRun", "unknown")); report.put("javaVersion", System.getProperty("java.runtime.version"));
        report.put("vmArguments", ManagementFactory.getRuntimeMXBean().getInputArguments()); report.put("processors", Runtime.getRuntime().availableProcessors());
        report.put("microbenchmarks", micros); report.put("stressPhases", phases); report.put("failures", failures);
        report.put("transactions", transactions); report.put("transactionChecksum", checksum); report.put("complete", complete && failures.isEmpty());
        report.put("registryStateFingerprint", registryFingerprint); report.put("cableShapeFingerprint", cableShapeFingerprint); report.put("cableStatesCompared", cableStates);
        report.put("method", "Actual isolated NeoForge server, placed IC2 circuits, normal machine/energy callbacks, native capability transactions, native codec; warmed phases and independent JVM forks. Sprinting removes the TPS ceiling; tick wall/CPU/allocation measure the same pre/post boundary.");
        try { Files.writeString(Path.of("ic2-performance-verification.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }

    private void fingerprint() {
        try {
            var definitions = MessageDigest.getInstance("SHA-256"); var geometry = MessageDigest.getInstance("SHA-256");
            for (var id : BuiltInRegistries.BLOCK.keySet().stream().filter(id -> id.getNamespace().equals("ic2")).sorted().toList()) {
                var block = BuiltInRegistries.BLOCK.getValue(id);
                definitions.update((id + ":" + block.defaultBlockState() + ":" + block.getStateDefinition().getProperties().toString()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                if (block instanceof Ic2TileEntityBlock machine) {
                    definitions.update((machine.getTeClass().getName() + ":" + machine.canActive() + ":" + machine.getCropType() + ":" + machine.getCropMaxAge() + ":" + machine.getSupportedFacings().stream().map(Direction::getSerializedName).sorted().toList()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                if (block instanceof AbstractCableBlock) {
                    for (var state : block.getStateDefinition().getPossibleStates()) {
                        cableStates++;
                        String shapes = state + ":" + state.getShape(level, BlockPos.ZERO).toAabbs() + ":" + state.getCollisionShape(level, BlockPos.ZERO).toAabbs() + ":" + state.getOcclusionShape().toAabbs();
                        geometry.update(shapes.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    }
                }
            }
            registryFingerprint = HexFormat.of().formatHex(definitions.digest()); cableShapeFingerprint = HexFormat.of().formatHex(geometry.digest());
        } catch (Exception error) { throw new IllegalStateException("State/shape fingerprint", error); }
    }

    @Name("ic2.PerformancePhase") @Label("IC2 measured performance phase")
    public static final class PhaseEvent extends Event {
        public String kind;
        public int circuits;
    }
}
