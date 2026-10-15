package ic2ce.neoforge.world;

import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import net.minecraft.world.level.Level;

/** World-thread callback phases defer reentrant one-shot work to the next native tick. */
public final class TickHandler {
  private static final boolean DEBUG = System.getProperty("ic2.debugupdate") != null;
  private static final Map<IWorldTickCallback, Throwable> TRACES =
      DEBUG ? Collections.synchronizedMap(new WeakHashMap<>()) : null;
  private static volatile Throwable lastDebugTrace;

  private TickHandler() {}

  public static void onWorldTickStart(Level world) {
    if (world.isClientSide()) return;
    var data = WorldData.get(world, false);
    if (data == null) return;
    dispatchCallbacks(world, data);
    if (world instanceof net.minecraft.server.level.ServerLevel server)
      data.cropTicks.refreshAudience(server);
    data.windSim.updateWind();
    data.energyNet.onTickStart();
  }

  public static void onWorldTickEnd(Level world) {
    if (!world.isClientSide()) {
      var data = WorldData.get(world, false);
      if (data != null) {
        if (world instanceof net.minecraft.server.level.ServerLevel server)
          data.cropTicks.tick(server);
        data.energyNet.onTickEnd();
        ProjectRuntime.network.get(true).onTickEnd(data);
      }
    }
  }

  public static void onServerTick() {}

  public static void onClientTick() {
    ProjectRuntime.keyboard.sendKeyUpdate();
    ProjectRuntime.soundManager.tick();
    var world = ProjectRuntime.sideProxy.getPlayerWorld();
    if (world != null) dispatchCallbacks(world, WorldData.get(world));
  }

  public static void requestSingleWorldTick(Level world, IWorldTickCallback callback) {
    Objects.requireNonNull(callback);
    if (DEBUG) TRACES.put(callback, new Throwable());
    WorldData.get(world).singleUpdates.add(callback);
  }

  public static void requestContinuousWorldTick(Level world, IWorldTickCallback callback) {
    Objects.requireNonNull(callback);
    var data = WorldData.get(world);
    if (data.continuousUpdatesInUse) {
      data.continuousUpdatesToRemove.remove(callback);
      data.continuousUpdatesToAdd.add(callback);
    } else data.continuousUpdates.add(callback);
    if (DEBUG) TRACES.put(callback, new Throwable());
  }

  public static void removeContinuousWorldTick(Level world, IWorldTickCallback callback) {
    var data = WorldData.get(world, false);
    if (data == null) return;
    if (data.continuousUpdatesInUse) {
      data.continuousUpdatesToAdd.remove(callback);
      data.continuousUpdatesToRemove.add(callback);
    } else data.continuousUpdates.remove(callback);
    if (DEBUG) TRACES.remove(callback);
  }

  public static Throwable getLastDebugTrace() {
    return lastDebugTrace;
  }

  static void dispatchCallbacks(Level world, WorldData data) {
    // Capturing the initial count bounds this phase, even when a callback requeues itself.
    int count = data.singleUpdates.size();
    for (int index = 0; index < count; index++) {
      var callback = data.singleUpdates.poll();
      if (callback == null) break;
      if (DEBUG) lastDebugTrace = TRACES.remove(callback);
      callback.onTick(world);
    }
    data.continuousUpdatesInUse = true;
    try {
      for (var callback : data.continuousUpdates) {
        if (data.continuousUpdatesToRemove.contains(callback)) continue;
        if (DEBUG) lastDebugTrace = TRACES.get(callback);
        callback.onTick(world);
      }
    } finally {
      data.continuousUpdatesInUse = false;
      lastDebugTrace = null;
      data.continuousUpdates.removeAll(data.continuousUpdatesToRemove);
      data.continuousUpdates.addAll(data.continuousUpdatesToAdd);
      data.continuousUpdatesToRemove.clear();
      data.continuousUpdatesToAdd.clear();
    }
  }
}
