package ic2ce.neoforge.runtime;

import ic2ce.neoforge.audio.AudioService;
import ic2ce.neoforge.network.NetworkManager;
import java.util.Objects;

/**
 * Physical-side services. The mod entrypoint installs the client implementations before any
 * registration callback runs; a dedicated server keeps the server defaults and never resolves a
 * client class.
 */
public final class SideServices {
  private static final NetworkManager SERVER_NETWORK = new NetworkManager();
  private static volatile SideProxy proxy = new SideProxyServer();
  private static volatile NetworkManager clientNetwork;

  private SideServices() {}

  /** Called once from the client mod constructor. */
  public static synchronized void installClient(SideProxy client, NetworkManager network) {
    if (clientNetwork != null) throw new IllegalStateException("Repeated client side services");
    proxy = Objects.requireNonNull(client);
    clientNetwork = Objects.requireNonNull(network);
  }

  public static SideProxy proxy() {
    return proxy;
  }

  public static Keyboard keyboard() {
    return proxy.getKeyboard();
  }

  public static AudioService audio() {
    return proxy.getSoundManager();
  }

  /** The logical server network manager when simulating, otherwise the client one (null on a dedicated server). */
  public static NetworkManager network(boolean simulating) {
    return simulating ? SERVER_NETWORK : clientNetwork;
  }
}
