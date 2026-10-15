package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.neoforge.world.WorldData;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.utility.ChunkAnchor;
import ic2ce.neoforge.world.ChunkLeases;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Three different server processes prove persisted ticket restoration, ghost removal and final
 * cleanup.
 */
public final class ChunkLeaseRestartProbe {
  private final String phase = System.getProperty("ic2.chunkLeaseStage");
  private final BlockPos owner = new BlockPos(24008, 181, 24008),
      second = owner.east(4),
      inactive = owner.south(5),
      ghost = owner.east(96);
  private final long shared = new ChunkPos((owner.getX() >> 4) + 1, owner.getZ() >> 4).pack();
  private final long other = new ChunkPos(owner.getX() >> 4, (owner.getZ() >> 4) + 1).pack();
  private int ticks, checks;
  private boolean finished;
  private final List<String> failures = new ArrayList<>();
  private MinecraftServer server;
  private ServerLevel world;

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add(label);
  }

  @SubscribeEvent
  public void started(ServerStartedEvent event) {
    server = event.getServer();
    world = server.overworld();
    try {
      if (phase.equals("seed")) {
        check(
            !Files.exists(Path.of("chunk-restart-seed.json")),
            "seed belongs to a fresh owned test world");
        world.setChunkForced(owner.getX() >> 4, owner.getZ() >> 4, true);
        world.getChunkAt(owner);
        for (var position : List.of(owner, second, inactive))
          world.setBlockAndUpdate(position, BlockDefinitions.CHUNK_LOADER.defaultBlockState());
        var left = (ChunkAnchor) world.getBlockEntity(owner);
        var right = (ChunkAnchor) world.getBlockEntity(second);
        left.energy.forceAddEnergy(2500);
        right.energy.forceAddEnergy(2500);
        left.addChunkToLoaded(ChunkPos.unpack(shared));
        left.addChunkToLoaded(ChunkPos.unpack(other));
        right.addChunkToLoaded(ChunkPos.unpack(shared));
        left.setChanged();
        right.setChanged();
        ChunkLeases.addChunkLoader(
            world, inactive, new LongOpenHashSet(new long[] {ChunkPos.pack(inactive)}));
        ChunkLeases.releaseRuntime(world, inactive);
        ChunkLeases.addChunkLoader(
            world, ghost, new LongOpenHashSet(new long[] {ChunkPos.pack(ghost)}));
        ChunkLeases.releaseRuntime(world, ghost);
      } else if (!phase.equals("restore") && !phase.equals("empty"))
        throw new IllegalArgumentException("Unknown restart probe phase");
    } catch (Throwable error) {
      failures.add(error.toString());
      finish();
    }
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (finished || world == null) return;
    try {
      ticks++;
      var services = WorldData.get(world);
      if (phase.equals("seed") && ticks == 80) {
        var left = (ChunkAnchor) world.getBlockEntity(owner);
        var right = (ChunkAnchor) world.getBlockEntity(second);
        check(
            left.getActive()
                && right.getActive()
                && left.energy.getEnergy() > 0
                && right.energy.getEnergy() > 0,
            "both real anchors run on paid native ticks before shutdown");
        check(
            services.chunkLoaders.get(owner).size() == 3
                && services.chunkLoaders.get(second).size() == 2
                && services.loadedChunks.get(shared).size() == 2,
            "two actual anchors share a native neighbor ticket");
        world.setChunkForced(owner.getX() >> 4, owner.getZ() >> 4, false);
        left.setChanged();
        right.setChanged();
        world.getChunkSource().save(true);
        finish();
      } else if (phase.equals("restore") && ticks >= 100) {
        if (services.chunkLoaders.size() != 2
            || !services.chunkLoaders.containsKey(owner)
            || !services.chunkLoaders.containsKey(second)) {
          if (ticks > 600) {
            check(
                false,
                "exact two persisted active anchors restore and stale owners retire; actual="
                    + services.chunkLoaders.keySet());
            finish();
          }
          return;
        }
        check(
            world.getChunkSource().getChunkNow(owner.getX() >> 4, owner.getZ() >> 4) != null
                && world
                        .getChunkSource()
                        .getChunkNow(ChunkPos.unpack(shared).x(), ChunkPos.unpack(shared).z())
                    != null,
            "persisted IC2 tickets load distant owner and selected chunks without a player or a"
                + " vanilla forced ticket");
        check(
            services.chunkLoaders.get(owner).size() == 3
                && services.chunkLoaders.get(second).size() == 2
                && services.loadedChunks.get(shared).size() == 2,
            "complete distinct selections and shared native ownership survive a real server-process"
                + " restart");
        check(
            !services.chunkLoaders.containsKey(inactive)
                && !services.chunkLoaders.containsKey(ghost),
            "inactive native block and missing-block saved owners cannot leave ghost leases");
        check(
            world.getBlockEntity(owner) instanceof ChunkAnchor left
                && left.getActive()
                && left.energy.getEnergy() > 0,
            "restored anchor preserves paid EU and executes actual native ticks");
        world.removeBlock(owner, false);
        check(
            !services.chunkLoaders.containsKey(owner)
                && services.loadedChunks.get(shared).size() == 1
                && services.loadedChunks.get(shared).contains(second),
            "breaking one restored owner preserves the other real owner's shared ticket");
        world.removeBlock(second, false);
        world.removeBlock(inactive, false);
        check(
            services.chunkLoaders.isEmpty() && services.loadedChunks.isEmpty(),
            "breaking final owner retires every runtime lease");
        world.getChunkSource().save(true);
        finish();
      } else if (phase.equals("empty") && ticks == 100) {
        check(
            services.chunkLoaders.isEmpty() && services.loadedChunks.isEmpty(),
            "third fresh server process restores no deleted owner or ghost lease");
        check(
            world.getChunkSource().getChunkNow(owner.getX() >> 4, owner.getZ() >> 4) == null,
            "deleted anchors cannot keep their distant chunk loaded after another process restart");
        finish();
      }
    } catch (Throwable error) {
      failures.add(error.toString());
      finish();
    }
  }

  private void finish() {
    finished = true;
    try {
      Files.writeString(
          Path.of("chunk-restart-" + phase + ".json"),
          new GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  Map.of(
                      "stage",
                      phase,
                      "checks",
                      checks,
                      "ticks",
                      ticks,
                      "complete",
                      failures.isEmpty(),
                      "failures",
                      failures,
                      "execution",
                      "actual_native_server_process_restart")));
    } catch (Exception error) {
      error.printStackTrace();
    }
    if (server != null) server.halt(false);
  }
}
