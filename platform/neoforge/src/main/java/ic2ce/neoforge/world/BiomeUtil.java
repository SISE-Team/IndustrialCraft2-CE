package ic2ce.neoforge.world;

import ic2ce.api.crops.BiomeType;
import ic2ce.neoforge.runtime.EnvProxy;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/**
 * Native quart-column terrain edits skip unchanged palette entries and publish one biome-only
 * client packet.
 */
public final class BiomeUtil {
  private BiomeUtil() {}

  public static Biome getBiome(Level world, ResourceKey<Biome> key) {
    return world
        .registryAccess()
        .lookupOrThrow(Registries.BIOME)
        .get(key)
        .map(Holder::value)
        .orElse(null);
  }

  public static Holder<Biome> getOriginalBiome(LevelReader world, BlockPos pos) {
    return world.getUncachedNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2);
  }

  public static Holder<Biome> getBiome(LevelReader world, BlockPos pos) {
    return world.getBiome(pos);
  }

  private static boolean edit(ChunkAccess chunk, BlockPos pos, Holder<Biome> biome) {
    int x = (pos.getX() & 15) >> 2, z = (pos.getZ() & 15) >> 2;
    boolean changed = false;
    for (var section : chunk.getSections()) {
      var values = section.getBiomes();
      if (!(values instanceof PalettedContainer<Holder<Biome>> palette))
        throw new IllegalStateException("Native biome palette is not writable");
      for (int y = 0; y < 4; y++)
        if (!palette.get(x, y, z).equals(biome)) {
          palette.set(x, y, z, biome);
          changed = true;
        }
    }
    if (changed) chunk.markUnsaved();
    return changed;
  }

  public static void setBiome(LevelReader world, BlockPos pos, Holder<Biome> biome) {
    Objects.requireNonNull(biome);
    edit(world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.BIOMES), pos, biome);
  }

  public static void setBiomeAndNotify(Level world, BlockPos pos, Holder<Biome> biome) {
    Objects.requireNonNull(biome);
    if (world.isClientSide()) return;
    var chunk = world.getChunkAt(pos);
    if (!edit(chunk, pos, biome)) return;
    if (world.getChunkSource() instanceof ServerChunkCache cache) {
      var packet =
          net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket.forChunks(
              List.of(chunk));
      for (var actor : cache.chunkMap.getPlayers(chunk.getPos(), false))
        actor.connection.send(packet);
    }
  }

  public static int getBiomeTemperature(Level world, BlockPos pos) {
    var biome = world.getBiome(pos);
    return EnvProxy.get().biomeHasType(biome, BiomeType.HOT)
        ? 45
        : EnvProxy.get().biomeHasType(biome, BiomeType.COLD) ? 0 : 25;
  }
}
