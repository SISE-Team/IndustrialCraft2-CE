package ic2.core.util;

import ic2.core.IC2;
import ic2.core.proxy.EnvProxy;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

public final class BiomeUtil {

   public static Biome getBiome(Level world, ResourceKey<Biome> key) {
      return world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).get(key).map(net.minecraft.core.Holder::value).orElse(null);
   }

   public static Holder<Biome> getOriginalBiome(LevelReader world, BlockPos pos) {
      return world.getUncachedNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2);
   }

   public static Holder<Biome> getBiome(LevelReader world, BlockPos pos) {
      return world.getBiome(pos);
   }

   public static void setBiome(LevelReader world, BlockPos pos, Holder<Biome> biome) {
      Objects.requireNonNull(biome, "null biome");
      int x = pos.getX() >> 4;
      int z = pos.getZ() >> 4;
      ChunkAccess chunk = world.getChunk(x, z, ChunkStatus.BIOMES);

      for (LevelChunkSection section : chunk.getSections()) {
         @SuppressWarnings("unchecked")
         PalettedContainer<Holder<Biome>> biomeContainer = (PalettedContainer<Holder<Biome>>)section.getBiomes();

         for (int y = 0; y < 4; y++) {
            biomeContainer.set((pos.getX() & 15) >> 2, y, (pos.getZ() & 15) >> 2, biome);
         }
      }
      chunk.markUnsaved();
   }

   public static void setBiomeAndNotify(Level world, BlockPos pos, Holder<Biome> biome) {
      setBiome(world, pos, biome);
      ChunkSource chunkManager = world.getChunkSource();
      if (chunkManager instanceof ServerChunkCache) {
         LevelChunk chunk = world.getChunkAt(pos);
         var packet = net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket.forChunks(java.util.List.of(chunk));
         ((ServerChunkCache)chunkManager).chunkMap.getPlayers(chunk.getPos(), false).forEach(player -> player.connection.send(packet));
         chunk.markUnsaved();
      } else {
         assert !world.isClientSide() : "Can't notify a server of a client side biome change";

         world.getChunkAt(pos).markUnsaved();
      }
   }

   public static int getBiomeTemperature(Level world, BlockPos pos) {
      Holder<Biome> biome = getBiome(world, pos);
      if (IC2.envProxy.biomeHasType(biome, EnvProxy.BiomeType.HOT)) {
         return 45;
      } else {
         return IC2.envProxy.biomeHasType(biome, EnvProxy.BiomeType.COLD) ? 0 : 25;
      }
   }
}
