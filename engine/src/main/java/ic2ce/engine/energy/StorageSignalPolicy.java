package ic2ce.engine.energy;

/** Seven electrical storage control modes use independent signal and discharge decisions. */
public final class StorageSignalPolicy {
  private StorageSignalPolicy() {}

  public static boolean signal(int mode, double stored, double capacity, double packet) {
    return switch (mode) {
      case 1 -> stored >= capacity - packet * 20;
      case 2 -> stored > packet && stored < capacity - packet;
      case 3 -> stored < capacity - packet;
      case 4 -> stored < packet;
      default -> false;
    };
  }

  public static boolean discharge(
      int mode, boolean powered, double stored, double capacity, double packet) {
    return switch (mode) {
      case 5 -> !powered;
      case 6 -> !powered || stored > capacity - packet * 20;
      default -> true;
    };
  }
}
