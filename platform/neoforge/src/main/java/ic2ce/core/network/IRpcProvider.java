package ic2ce.core.network;

public interface IRpcProvider<V> {
   V executeRpc(Object... var1);
}
