package ic2ce.core.event;

import ic2ce.core.energy.grid.EnergyNetLocal;
import ic2ce.core.network.TeUpdateDataServer;
import ic2ce.neoforge.personal.WirelessTradeMarket;
import ic2ce.neoforge.weather.WorldWind;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Service ownership follows native level identity, including simultaneous worlds of one dimension.
 */
public final class WorldData {
  private static final Object WORLD_LOCK = new Object();
  private static volatile Map<Level, WorldData> worlds = Map.of();
  final Queue<IWorldTickCallback> singleUpdates = new ConcurrentLinkedQueue<>();
  final Set<IWorldTickCallback> continuousUpdates = new LinkedHashSet<>();
  boolean continuousUpdatesInUse;
  final Set<IWorldTickCallback> continuousUpdatesToAdd = new LinkedHashSet<>(),
      continuousUpdatesToRemove = new LinkedHashSet<>();
  public final EnergyNetLocal energyNet;
  public final Map<BlockEntity, TeUpdateDataServer> tesToUpdate = new IdentityHashMap<>();
  public final WirelessTradeMarket tradeMarket;
  public final WorldWind windSim;
  public final Map<LevelChunk, CompoundTag> worldGenData = new IdentityHashMap<>();
  public final Set<LevelChunk>
      chunksToDecorate = Collections.newSetFromMap(new IdentityHashMap<>()),
      pendingUnloadChunks = Collections.newSetFromMap(new IdentityHashMap<>());
  public final Long2ObjectMap<Set<BlockPos>> loadedChunks;
  public final Object2ObjectMap<BlockPos, LongSet> chunkLoaders;

  private WorldData(Level world) {
    if (world.isClientSide()) {
      energyNet = null;
      tradeMarket = null;
      windSim = null;
      loadedChunks = null;
      chunkLoaders = null;
    } else {
      energyNet = EnergyNetLocal.create(world);
      tradeMarket = new WirelessTradeMarket(world);
      windSim = new WorldWind(world);
      loadedChunks = new Long2ObjectOpenHashMap<>();
      chunkLoaders = new Object2ObjectOpenHashMap<>();
    }
  }

  public static WorldData get(Level world) {
    return get(world, true);
  }

  public static WorldData get(Level world, boolean create) {
    Objects.requireNonNull(world);
    var services = worlds.get(world);
    if (services != null || !create) return services;
    synchronized (WORLD_LOCK) {
      services = worlds.get(world);
      if (services == null) {
        services = new WorldData(world);
        var next = new IdentityHashMap<Level, WorldData>(worlds);
        next.put(world, services);
        worlds = Collections.unmodifiableMap(next);
      }
      return services;
    }
  }

  public static void onWorldUnload(Level world) {
    WorldData services;
    synchronized (WORLD_LOCK) {
      services = worlds.get(world);
      if (services == null) return;
      var next = new IdentityHashMap<Level, WorldData>(worlds);
      next.remove(world);
      worlds = Collections.unmodifiableMap(next);
    }
    services.singleUpdates.clear();
    services.continuousUpdates.clear();
    services.continuousUpdatesToAdd.clear();
    services.continuousUpdatesToRemove.clear();
    services.tesToUpdate.clear();
    services.worldGenData.clear();
    services.chunksToDecorate.clear();
    services.pendingUnloadChunks.clear();
    if (services.loadedChunks != null) services.loadedChunks.clear();
    if (services.chunkLoaders != null) services.chunkLoaders.clear();
  }
}
