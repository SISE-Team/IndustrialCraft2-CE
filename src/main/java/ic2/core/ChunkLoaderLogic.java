package ic2.core;

import ic2.core.IC2;
import ic2.core.event.WorldData;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Comparator;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;

public final class ChunkLoaderLogic {
   private static TicketType ticketType() {
      return ic2.forge.EnvProxyForge.CHUNK_LOADER_TICKET_TYPE.get();
   }

   public static void addChunkLoader(ServerLevel world, BlockPos pos, LongSet chunks) {
      long loaderChunk = ChunkPos.pack(pos);
      if (!chunks.contains(loaderChunk)) {
         throw new IllegalArgumentException("missing own position");
      } else {
         ChunkLoaderLogic.SavedState state = world.getDataStorage().computeIfAbsent(ChunkLoaderLogic.SavedState.TYPE);
         ((Set)state.chunksToChunkLoaders.computeIfAbsent(loaderChunk, ignore -> new ObjectOpenHashSet(1))).add(pos);
         state.setDirty();
         WorldData worldData = WorldData.get(world);
         LongIterator var7 = chunks.iterator();

         while (var7.hasNext()) {
            long chunk = (Long)var7.next();
            Set<BlockPos> loaders = (Set<BlockPos>)worldData.loadedChunks.computeIfAbsent(chunk, ignore -> new ObjectOpenHashSet(1));
            if (loaders.isEmpty()) {
               addChunkTicket(world, ChunkPos.unpack(chunk));
            }

            loaders.add(pos);
         }

         worldData.chunkLoaders.put(pos, new LongOpenHashSet(chunks));
      }
   }

   public static void removeChunkLoader(ServerLevel world, BlockPos pos) {
      ChunkLoaderLogic.SavedState state = world.getDataStorage().get(ChunkLoaderLogic.SavedState.TYPE);
      if (state != null) {
         long chunkPos = ChunkPos.pack(pos);
         Set<BlockPos> positions = (Set<BlockPos>)state.chunksToChunkLoaders.get(chunkPos);
         if (positions != null && positions.remove(pos) && positions.isEmpty()) {
            state.chunksToChunkLoaders.remove(chunkPos);
         }
         state.setDirty();
      }

      WorldData worldData = WorldData.get(world, false);
      if (worldData != null) {
         disableChunkLoader(world, pos, worldData);
      }
   }

   private static void disableChunkLoader(ServerLevel world, BlockPos pos, WorldData worldData) {
      LongSet positions = (LongSet)worldData.chunkLoaders.remove(pos);
      if (positions != null) {
         LongIterator var4 = positions.iterator();

         while (var4.hasNext()) {
            long chunkPos = (Long)var4.next();
            Set<BlockPos> loaders = (Set<BlockPos>)worldData.loadedChunks.get(chunkPos);
            if (loaders != null && loaders.remove(pos) && loaders.isEmpty()) {
               worldData.loadedChunks.remove(chunkPos);
               removeChunkTicket(world, ChunkPos.unpack(chunkPos));
            }
         }
      }
   }

   public static void updateChunkLoader(ServerLevel world, BlockPos pos, LongSet chunks) {
      long loaderChunk = ChunkPos.pack(pos);
      if (!chunks.contains(loaderChunk)) {
         throw new IllegalArgumentException("missing own position");
      } else {
         WorldData worldData = WorldData.get(world);
         LongSet prev = (LongSet)worldData.chunkLoaders.get(pos);
         if (prev == null) {
            addChunkLoader(world, pos, chunks);
         } else {
            LongIterator it = prev.longIterator();

            while (it.hasNext()) {
               long chunk = it.nextLong();
               if (!chunks.contains(chunk)) {
                  it.remove();
                  Set<BlockPos> loaders = (Set<BlockPos>)worldData.loadedChunks.get(chunk);
                  loaders.remove(pos);
                  if (loaders.isEmpty()) {
                     removeChunkTicket(world, ChunkPos.unpack(chunk));
                     worldData.loadedChunks.remove(chunk);
                  }
               }
            }

            it = chunks.iterator();

            while (it.hasNext()) {
               long chunk = (Long)it.next();
               if (prev.add(chunk)) {
                  Set<BlockPos> loaders = (Set<BlockPos>)worldData.loadedChunks.computeIfAbsent(chunk, ignore -> new ObjectOpenHashSet());
                  if (loaders.isEmpty()) {
                     addChunkTicket(world, ChunkPos.unpack(chunk));
                  }

                  loaders.add(pos);
               }
            }
         }
      }
   }

   public static void onWorldLoad(ServerLevel world) {
      ChunkLoaderLogic.SavedState state = world.getDataStorage().get(ChunkLoaderLogic.SavedState.TYPE);
      if (state != null && !state.chunksToChunkLoaders.isEmpty()) {
         WorldData worldData = WorldData.get(world);
         ObjectIterator var3 = state.chunksToChunkLoaders.long2ObjectEntrySet().iterator();

         while (var3.hasNext()) {
            Entry<Set<BlockPos>> entry = (Entry<Set<BlockPos>>)var3.next();
            long chunkPos = entry.getLongKey();
            Set<BlockPos> loaders = (Set<BlockPos>)entry.getValue();
            worldData.loadedChunks.put(chunkPos, new ObjectOpenHashSet(loaders));

            for (BlockPos pos : loaders) {
               ((LongSet)worldData.chunkLoaders.computeIfAbsent(pos, ignore -> new LongOpenHashSet(1))).add(chunkPos);
            }

            addChunkTicket(world, ChunkPos.unpack(chunkPos));
         }
      }
   }

   public static void onChunkUnload(LevelChunk chunk) {
      assert !chunk.getLevel().isClientSide();

      ServerLevel world = (ServerLevel)chunk.getLevel();
      ChunkLoaderLogic.SavedState state = world.getDataStorage().get(ChunkLoaderLogic.SavedState.TYPE);
      if (state != null && !state.chunksToChunkLoaders.isEmpty()) {
         Set<BlockPos> loaders = (Set<BlockPos>)state.chunksToChunkLoaders.get(chunk.getPos().pack());
         if (loaders != null && !loaders.isEmpty()) {
            WorldData worldData = WorldData.get(world, false);
            if (worldData != null) {
               for (BlockPos pos : loaders) {
                  disableChunkLoader(world, pos, worldData);
               }
            }
         }
      }
   }

   private static void addChunkTicket(ServerLevel world, ChunkPos pos) {
      world.getChunkSource().addTicketWithRadius(ticketType(), pos, 2);
   }

   private static void removeChunkTicket(ServerLevel world, ChunkPos pos) {
      world.getChunkSource().removeTicketWithRadius(ticketType(), pos, 2);
   }

   private static final class SavedState extends SavedData {
      final Long2ObjectMap<Set<BlockPos>> chunksToChunkLoaders = new Long2ObjectOpenHashMap();

      static final com.mojang.serialization.Codec<ChunkLoaderLogic.SavedState> CODEC = BlockPos.CODEC.listOf().xmap(
         ChunkLoaderLogic.SavedState::fromPositions,
         ChunkLoaderLogic.SavedState::allPositions
      );
      static final net.minecraft.world.level.saveddata.SavedDataType<ChunkLoaderLogic.SavedState> TYPE = new net.minecraft.world.level.saveddata.SavedDataType<>(
         IC2.getIdentifier("loaded_chunks"), ChunkLoaderLogic.SavedState::new, ChunkLoaderLogic.SavedState.CODEC
      );

      SavedState() {
      }

      private static ChunkLoaderLogic.SavedState fromPositions(java.util.List<BlockPos> positions) {
         ChunkLoaderLogic.SavedState state = new ChunkLoaderLogic.SavedState();
         for (BlockPos pos : positions) {
            ((Set)state.chunksToChunkLoaders.computeIfAbsent(ChunkPos.pack(pos), ignore -> new ObjectOpenHashSet(1))).add(pos);
         }
         return state;
      }

      private static java.util.List<BlockPos> allPositions(ChunkLoaderLogic.SavedState state) {
         java.util.List<BlockPos> positions = new java.util.ArrayList<>();
         for (Set<BlockPos> set : state.chunksToChunkLoaders.values()) {
            positions.addAll(set);
         }
         return positions;
      }
   }
}
