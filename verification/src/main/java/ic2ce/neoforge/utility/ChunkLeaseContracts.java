package ic2ce.neoforge.utility;

import ic2ce.core.event.WorldData;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.world.ChunkLeases;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/** Shared actual native ticket owners and bounded corrupt-data/GUI selection boundaries. */
public final class ChunkLeaseContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native chunk lease: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ChunkLeaseContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var first = new BlockPos(8620, 100, 8620);
    var second = first.east();
    long own = ChunkPos.pack(first),
        shared = new ChunkPos((first.getX() >> 4) + 1, first.getZ() >> 4).pack();
    var desired = new LongOpenHashSet(new long[] {own, shared});
    var data = WorldData.get(world);
    ChunkLeases.addChunkLoader(world, first, desired);
    ChunkLeases.addChunkLoader(world, second, desired);
    desired.clear();
    check(
        data.loadedChunks.get(shared).size() == 2 && data.chunkLoaders.get(first).size() == 2,
        "two real ticket owners keep independent snapshots after caller mutation");
    ChunkLeases.addChunkLoader(world, first, new LongOpenHashSet(new long[] {own, shared}));
    check(
        data.loadedChunks.get(shared).size() == 2,
        "idempotent registration never adds a duplicate ticket owner");
    ChunkLeases.removeChunkLoader(world, first);
    check(
        data.loadedChunks.get(shared).contains(second) && data.loadedChunks.get(shared).size() == 1,
        "removing one owner keeps the other owner's actual shared ticket");
    ChunkLeases.updateChunkLoader(world, second, new LongOpenHashSet(new long[] {own}));
    check(
        !data.loadedChunks.containsKey(shared) && data.loadedChunks.get(own).contains(second),
        "selection shrink releases only its last shared owner");
    ChunkLeases.releaseRuntime(world, second);
    check(
        !data.chunkLoaders.containsKey(second) && !data.loadedChunks.containsKey(own),
        "temporary release removes runtime ownership independently of saved registration");
    ChunkLeases.removeChunkLoader(world, second);
    world.getChunkAt(first);
    world.setBlockAndUpdate(first, BlockDefinitions.CHUNK_LOADER.defaultBlockState());
    var loader = (ChunkAnchor) world.getBlockEntity(first);
    var tag = new CompoundTag();
    var chunks = new ListTag();
    chunks.add(LongTag.valueOf(new ChunkPos(Integer.MAX_VALUE, Integer.MIN_VALUE).pack()));
    for (int x = -4; x <= 4; x++)
      for (int z = -4; z <= 4; z++)
        chunks.add(
            LongTag.valueOf(new ChunkPos((first.getX() >> 4) + x, (first.getZ() >> 4) + z).pack()));
    tag.put("loadedChunks", chunks);
    loader.load(tag);
    check(
        loader.getLoadedChunks().size() == 9 && loader.getLoadedChunks().contains(own),
        "saved selection enforces nine nearby chunks and always retains the owner");
    check(
        !loader.isChunkInRange(new ChunkPos(Integer.MAX_VALUE, Integer.MIN_VALUE)),
        "range arithmetic cannot overflow into an accepted remote chunk");
    try {
      loader.getLoadedChunks().clear();
      check(false, "GUI view cannot bypass the mandatory owner");
    } catch (UnsupportedOperationException expected) {
      check(loader.getLoadedChunks().contains(own), "GUI view cannot bypass the mandatory owner");
    }
    world.removeBlock(first, false);
    return new Result(checks, List.copyOf(failures));
  }
}
