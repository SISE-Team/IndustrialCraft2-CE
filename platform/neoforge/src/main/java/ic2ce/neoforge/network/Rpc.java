package ic2ce.neoforge.network;

import ic2ce.engine.async.PendingResult;

/** Native protocol completion adapter; first completion or cancellation wins. */
public final class Rpc<V> extends PendingResult<V> {
  @SuppressWarnings("unchecked")
  public void finish(Object result) {
    complete((V) result);
  }
}
