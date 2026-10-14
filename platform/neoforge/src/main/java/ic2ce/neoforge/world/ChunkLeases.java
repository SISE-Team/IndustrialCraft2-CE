package ic2ce.neoforge.world;

import com.mojang.serialization.Codec;
import ic2ce.core.*;
import ic2ce.core.event.IWorldTickCallback;
import ic2ce.core.event.TickHandler;
import ic2ce.core.event.WorldData;
import ic2ce.neoforge.runtime.ProjectRuntime;
import ic2ce.neoforge.utility.ChunkAnchor;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * World-thread chunk-ticket ownership. Shared selections retain a ticket until their last owner
 * exits.
 */
public final class ChunkLeases {
  private ChunkLeases() {}

  private static void ownerThread(ServerLevel world) {
    if (world.getServer() != null && !world.getServer().isSameThread())
      throw new IllegalStateException("Chunk leases require the native world thread");
  }

  private static void ticket(ServerLevel world, long chunk, boolean add) {
    var type = ic2ce.neoforge.EnvProxyNeoForge.CHUNK_LOADER_TICKET_TYPE.get();
    if (add) world.getChunkSource().addTicketWithRadius(type, ChunkPos.unpack(chunk), 2);
    else world.getChunkSource().removeTicketWithRadius(type, ChunkPos.unpack(chunk), 2);
  }

  private static LongSet ownedSelection(BlockPos owner, LongSet selection) {
    Objects.requireNonNull(selection);
    if (!selection.contains(ChunkPos.pack(owner)))
      throw new IllegalArgumentException("Chunk selection must contain its owner");
    return new LongOpenHashSet(selection);
  }

  private static void reconcile(ServerLevel world, BlockPos position, LongSet requested) {
    var data = WorldData.get(world);
    var owner = position.immutable();
    var old = data.chunkLoaders.get(owner);
    if (old != null)
      for (var iterator = old.iterator(); iterator.hasNext(); ) {
        long chunk = iterator.nextLong();
        if (requested.contains(chunk)) continue;
        var owners = data.loadedChunks.get(chunk);
        if (owners != null && owners.remove(owner) && owners.isEmpty()) {
          data.loadedChunks.remove(chunk);
          ticket(world, chunk, false);
        }
      }
    for (var iterator = requested.iterator(); iterator.hasNext(); ) {
      long chunk = iterator.nextLong();
      var owners = data.loadedChunks.get(chunk);
      if (owners == null) {
        owners = new LinkedHashSet<>(2);
        data.loadedChunks.put(chunk, owners);
      }
      if (owners.isEmpty()) ticket(world, chunk, true);
      owners.add(owner);
    }
    data.chunkLoaders.put(owner, requested);
  }

  public static void addChunkLoader(ServerLevel world, BlockPos position, LongSet chunks) {
    ownerThread(world);
    var owner = Objects.requireNonNull(position).immutable();
    var selection = ownedSelection(owner, chunks);
    var saved = world.getDataStorage().computeIfAbsent(SavedState.TYPE);
    if (saved.owners.computeIfAbsent(ChunkPos.pack(owner), _ -> new LinkedHashSet<>(2)).add(owner))
      saved.setDirty();
    reconcile(world, owner, selection);
  }

  public static void updateChunkLoader(ServerLevel world, BlockPos position, LongSet chunks) {
    addChunkLoader(world, position, chunks);
  }

  public static void releaseRuntime(ServerLevel world, BlockPos position) {
    ownerThread(world);
    var data = WorldData.get(world, false);
    if (data == null) return;
    var old = data.chunkLoaders.remove(position);
    if (old == null) return;
    for (var iterator = old.iterator(); iterator.hasNext(); ) {
      long chunk = iterator.nextLong();
      var owners = data.loadedChunks.get(chunk);
      if (owners != null && owners.remove(position) && owners.isEmpty()) {
        data.loadedChunks.remove(chunk);
        ticket(world, chunk, false);
      }
    }
  }

  public static void removeChunkLoader(ServerLevel world, BlockPos position) {
    ownerThread(world);
    var saved = world.getDataStorage().get(SavedState.TYPE);
    if (saved != null) {
      long chunk = ChunkPos.pack(position);
      var owners = saved.owners.get(chunk);
      if (owners != null && owners.remove(position)) {
        if (owners.isEmpty()) saved.owners.remove(chunk);
        saved.setDirty();
      }
    }
    releaseRuntime(world, position);
  }

  public static void onWorldLoad(ServerLevel world) {
    ownerThread(world);
    var saved = world.getDataStorage().get(SavedState.TYPE);
    if (saved == null) return;
    var generation = WorldData.get(world);
    for (var position : SavedState.positions(saved)) {
      reconcile(world, position, new LongOpenHashSet(new long[] {ChunkPos.pack(position)}));
      TickHandler.requestSingleWorldTick(
          world,
          new IWorldTickCallback() {
            private int retries;

            @Override
            public void onTick(Level observed) {
              if (observed != world || WorldData.get(world, false) != generation) return;
              if (!world.isLoaded(position)) {
                if (++retries < 200) {
                  TickHandler.requestSingleWorldTick(world, this);
                  return;
                }
                removeChunkLoader(world, position);
                return;
              }
              if (world.getBlockEntity(position) instanceof ChunkAnchor loader
                  && !loader.isRemoved()
                  && loader.getActive()) addChunkLoader(world, position, loader.getLoadedChunks());
              else removeChunkLoader(world, position);
            }
          });
    }
  }

  public static void onChunkUnload(LevelChunk chunk) {
    if (!(chunk.getLevel() instanceof ServerLevel world)) return;
    var saved = world.getDataStorage().get(SavedState.TYPE);
    if (saved == null) return;
    var owners = saved.owners.get(chunk.getPos().pack());
    if (owners != null) for (var owner : owners) releaseRuntime(world, owner);
  }

  private static final class SavedState extends SavedData {
    private final Long2ObjectMap<Set<BlockPos>> owners = new Long2ObjectOpenHashMap<>();
    private static final Codec<SavedState> CODEC =
        BlockPos.CODEC.listOf().xmap(SavedState::read, SavedState::positions);
    private static final SavedDataType<SavedState> TYPE =
        new SavedDataType<>(ProjectRuntime.getIdentifier("loaded_chunks"), SavedState::new, CODEC);

    private static SavedState read(List<BlockPos> positions) {
      var state = new SavedState();
      for (var position : positions) {
        var owner = position.immutable();
        state.owners.computeIfAbsent(ChunkPos.pack(owner), _ -> new LinkedHashSet<>(2)).add(owner);
      }
      return state;
    }

    private static List<BlockPos> positions(SavedState state) {
      var positions = new ArrayList<BlockPos>();
      for (var owners : state.owners.values()) positions.addAll(owners);
      positions.sort(Comparator.comparingLong(BlockPos::asLong));
      return positions;
    }
  }
}
