package ic2ce.core.network;

import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.level.ServerPlayer;

/** Bounded client requests and an explicitly registered server provider table. */
public final class RpcHandler {
  private static final ConcurrentHashMap<String, IRpcProvider<?>> providers =
      new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<Integer, Rpc<?>> pending = new ConcurrentHashMap<>();
  private static final java.util.concurrent.Semaphore slots =
      new java.util.concurrent.Semaphore(1024);
  private static final AtomicInteger sequence = new AtomicInteger();

  private RpcHandler() {}

  public static boolean registerProvider(IRpcProvider<?> provider) {
    Objects.requireNonNull(provider);
    return providers.putIfAbsent(provider.getClass().getName(), provider) == null;
  }

  public static <V> Rpc<V> run(Class<? extends IRpcProvider<V>> provider, Object... arguments) {
    var result = new Rpc<V>();
    if (arguments == null || arguments.length > 32) {
      result.fail(new IllegalArgumentException("Invalid RPC argument count"));
      return result;
    }
    if (!slots.tryAcquire()) {
      result.fail(new IllegalStateException("Too many pending IndustrialCraft2-CE requests"));
      return result;
    }
    int candidate;
    do {
      candidate = sequence.getAndIncrement();
    } while (pending.putIfAbsent(candidate, result) != null);
    final int id = candidate;
    result
        .completion()
        .whenComplete(
            (_, _) -> {
              pending.remove(id, result);
              slots.release();
            });
    result.completion().orTimeout(2, TimeUnit.SECONDS);
    try {
      ProjectRuntime.network
          .get(false)
          .initiateRpc(id, Objects.requireNonNull(provider), arguments);
    } catch (RuntimeException error) {
      result.fail(error);
    }
    return result;
  }

  public static void onDisconnect() {
    for (Rpc<?> request : pending.values()) request.cancel(false);
    pending.clear();
  }

  static void processRpcRequest(GrowingBuffer input, ServerPlayer player) throws IOException {
    int id = input.readInt();
    String type = input.readString();
    if (type.length() > 512) throw new IOException("Invalid RPC provider name");
    IRpcProvider<?> provider = providers.get(type);
    Object result = null;
    if (provider != null) {
      Object decoded =
          DataEncoder.getValue(DataEncoder.decodeArguments(input, 32), player.level().getServer());
      if (!(decoded instanceof Object[] arguments) || arguments.length > 32)
        throw new IOException("Invalid RPC arguments");
      try {
        result = provider.executeRpc(arguments);
      } catch (RuntimeException error) {
        ProjectRuntime.log.debug(DiagnosticArea.Network, error, "Rejected RPC provider input");
      }
    } else
      ProjectRuntime.log.warn(
          DiagnosticArea.Network, "Unregistered RPC provider requested by %s", player.getName());
    var response = new GrowingBuffer(256);
    SubPacketType.Rpc.writeTo(response);
    response.writeInt(id);
    DataEncoder.encode(response, result, true);
    response.flip();
    ProjectRuntime.network.get(true).sendS2CPacket(player, response, true);
  }

  static void processRpcResponse(GrowingBuffer input) throws IOException {
    int id = input.readInt();
    Rpc<?> request = pending.remove(id);
    if (request == null) return;
    try {
      request.finish(DataEncoder.decode(input));
    } catch (IOException | RuntimeException error) {
      request.fail(error);
      throw error;
    }
  }
}
