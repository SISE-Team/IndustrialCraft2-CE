package ic2ce.neoforge.world;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/** Immutable native state dispatch, skipping entire sections without load-aware palette entries. */
public final class ChunkLoadAwareBlockHandler {
  private static volatile Map<BlockState, ChunkLoadAwareBlock> handlers = Map.of();

  private ChunkLoadAwareBlockHandler() {}

  public static synchronized void init() {
    var generation = new IdentityHashMap<BlockState, ChunkLoadAwareBlock>();
    for (var block : BuiltInRegistries.BLOCK)
      if (block instanceof ChunkLoadAwareBlock aware) {
        for (var state :
            Objects.requireNonNull(aware.getLoadAwareState(block), "Load-aware state collection")) {
          if (state == null || state.getBlock() != block)
            throw new IllegalArgumentException("Load-aware block declared a foreign state");
          generation.put(state, aware);
        }
      }
    handlers = Collections.unmodifiableMap(generation);
  }

  public static void onChunkLoad(LevelChunk chunk) {
    dispatch(chunk, true);
  }

  public static void onChunkUnload(LevelChunk chunk) {
    dispatch(chunk, false);
  }

  private static void dispatch(LevelChunk chunk, boolean entering) {
    var world = chunk.getLevel();
    if (world.isClientSide()) return;
    var snapshot = handlers;
    if (snapshot.isEmpty()) return;
    var sections = chunk.getSections();
    int baseX = chunk.getPos().getMinBlockX(), baseZ = chunk.getPos().getMinBlockZ();
    for (int index = 0; index < sections.length; index++) {
      var section = sections[index];
      if (section.hasOnlyAir()) continue;
      var palette = section.getStates();
      if (!palette.maybeHas(snapshot::containsKey)) continue;
      int baseY = chunk.getMinY() + index * 16;
      BlockState previous = null;
      ChunkLoadAwareBlock handler = null;
      for (int cell = 0; cell < 4096; cell++) {
        int x = cell & 15, z = cell >>> 4 & 15, y = cell >>> 8;
        var state = palette.get(x, y, z);
        if (state != previous) {
          previous = state;
          handler = snapshot.get(state);
        }
        if (handler == null) continue;
        var position = new BlockPos(baseX + x, baseY + y, baseZ + z);
        if (entering) handler.onLoad(state, world, position);
        else handler.onUnload(state, world, position);
      }
    }
  }
}
