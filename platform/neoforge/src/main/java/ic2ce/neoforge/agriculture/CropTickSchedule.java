package ic2ce.neoforge.agriculture;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/** World-owned due phases; native per-block ticks still publish externally dirtied state. */
public final class CropTickSchedule {
  private static final MethodHandle REBIND_TICKER = tickerBinding();

  private static MethodHandle tickerBinding() {
    try {
      return MethodHandles.privateLookupIn(LevelChunk.class, MethodHandles.lookup())
          .findVirtual(
              LevelChunk.class,
              "updateBlockEntityTicker",
              MethodType.methodType(void.class, BlockEntity.class));
    } catch (ReflectiveOperationException exception) {
      throw new ExceptionInInitializerError(exception);
    }
  }

  private static final class Entry {
    final CropPlot plot;
    Entry previous, next;
    int phase;
    boolean active = true;
    boolean dirtyQueued;

    Entry(CropPlot plot) {
      this.plot = plot;
    }
  }

  private final IdentityHashMap<CropPlot, Entry> entries = new IdentityHashMap<>();
  private Entry[] phases = new Entry[0];
  private int rate;
  private boolean playersPresent;
  private final ArrayList<Entry> dirty = new ArrayList<>();

  public void add(CropPlot plot) {
    refreshRate();
    if (entries.containsKey(plot)) return;
    var entry = new Entry(plot);
    entries.put(plot, entry);
    link(entry);
    queueDirty(plot);
  }

  public void queueDirty(CropPlot plot) {
    var entry = entries.get(plot);
    if (entry == null || entry.dirtyQueued) return;
    entry.dirtyQueued = true;
    dirty.add(entry);
  }

  /** Presence changes rebind native tickers before the world's native block-entity phase. */
  public void refreshAudience(ServerLevel world) {
    boolean present = !world.players().isEmpty();
    if (present == playersPresent) return;
    playersPresent = present;
    for (var entry : entries.values()) {
      var position = entry.plot.getBlockPos();
      var chunk = world.getChunkSource().getChunkNow(position.getX() >> 4, position.getZ() >> 4);
      if (chunk != null && chunk.getBlockEntity(position) == entry.plot) {
        try {
          REBIND_TICKER.invokeExact(chunk, (BlockEntity) entry.plot);
        } catch (Throwable exception) {
          if (exception instanceof Error error) throw error;
          throw new IllegalStateException("Cannot rebind native crop ticker", exception);
        }
      }
    }
  }

  public void remove(CropPlot plot) {
    var entry = entries.remove(plot);
    if (entry == null) return;
    entry.active = false;
    if (entry.previous == null) phases[entry.phase] = entry.next;
    else entry.previous.next = entry.next;
    if (entry.next != null) entry.next.previous = entry.previous;
    // Keep the successor valid for a traversal already inside a native crop callback.
  }

  private void link(Entry entry) {
    entry.phase = entry.plot.phaseForPeriod(phases.length);
    entry.previous = null;
    entry.next = phases[entry.phase];
    if (entry.next != null) entry.next.previous = entry;
    phases[entry.phase] = entry;
  }

  private void refreshRate() {
    if (phases.length != 0 && rate == CropPlot.tickRate) return;
    rate = CropPlot.tickRate;
    phases = new Entry[Math.clamp(rate, 1, 65536)];
    for (var entry : entries.values()) link(entry);
  }

  public void tick(ServerLevel world) {
    if (entries.isEmpty() || !world.tickRateManager().runsNormally()) return;
    refreshRate();
    int phase = Math.floorMod(world.getGameTime(), phases.length);
    for (var entry = phases[phase]; entry != null; ) {
      var next = entry.next;
      var plot = entry.plot;
      var position = plot.getBlockPos();
      long chunkKey = ChunkPos.pack(position);
      if (entry.active
          && !plot.isRemoved()
          && plot.getLevel() == world
          && world.getWorldBorder().isWithinBounds(position)
          && world.shouldTickBlocksAt(chunkKey)
          && world.areEntitiesLoaded(chunkKey)) {
        var chunk = world.getChunkSource().getChunkNow(position.getX() >> 4, position.getZ() >> 4);
        if (chunk != null
            && chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING)
            && chunk.getBlockEntity(position) == plot
            && plot.getType().isValid(chunk.getBlockState(position))) plot.updateEntityServer();
      }
      entry = next;
    }
    int count = dirty.size();
    for (int index = 0; index < count; index++) {
      var entry = dirty.get(index);
      entry.dirtyQueued = false;
      if (entry.active && entry.plot.dirty && !tickDirty(world, entry.plot)) queueDirty(entry.plot);
    }
    dirty.subList(0, count).clear();
  }

  private boolean tickDirty(ServerLevel world, CropPlot plot) {
    var position = plot.getBlockPos();
    long key = ChunkPos.pack(position);
    if (plot.isRemoved()
        || plot.getLevel() != world
        || !world.getWorldBorder().isWithinBounds(position)
        || !world.shouldTickBlocksAt(key)
        || !world.areEntitiesLoaded(key)) return false;
    var chunk = world.getChunkSource().getChunkNow(position.getX() >> 4, position.getZ() >> 4);
    if (chunk != null
        && chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING)
        && chunk.getBlockEntity(position) == plot
        && plot.getType().isValid(chunk.getBlockState(position))) {
      plot.updateEntityServer();
      return true;
    }
    return false;
  }

  public void clear() {
    for (var entry : entries.values()) entry.active = false;
    entries.clear();
    dirty.clear();
    phases = new Entry[0];
  }

  public int size() {
    return entries.size();
  }
}
