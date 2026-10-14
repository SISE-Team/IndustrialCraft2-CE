package ic2ce.engine.energy;

/** Energy accounting; no callbacks, tile objects or world access. */
public final class PacketTransfer {
  private PacketTransfer() {}

  public record Receipt(double accepted, double spent, boolean closeSink) {}

  private static final Receipt REJECTED = new Receipt(0, 0, false);

  public static double offeredToSink(double offer, double pathLoss, double demand) {
    return Math.max(0, Math.min(offer - pathLoss, demand));
  }

  public static Receipt account(double offered, double rejected, double loss, double demand) {
    if (rejected >= offered) return REJECTED;
    double accepted = offered - rejected;
    return new Receipt(accepted, Math.max(0, accepted + loss), offered >= demand || rejected > 0);
  }
}
