package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import ic2ce.api.reactor.IReactorComponent;
import net.minecraft.world.item.ItemStack;

/** Neighbor vents cool each adjacent heat-capable component independently. */
public class NeighborHeatVent extends ReactorPart {
  public final int sideVent;

  public NeighborHeatVent(Properties properties, int rate) {
    super(properties);
    sideVent = Math.max(0, rate);
  }

  @Override
  public void processChamber(ItemStack part, IReactor reactor, int x, int y, boolean thermalPhase) {
    if (!thermalPhase || sideVent == 0) return;
    cool(reactor, x - 1, y);
    cool(reactor, x + 1, y);
    cool(reactor, x, y - 1);
    cool(reactor, x, y + 1);
  }

  private void cool(IReactor reactor, int x, int y) {
    var neighbor = reactor.getItemAt(x, y);
    if (neighbor != null
        && !neighbor.isEmpty()
        && neighbor.getItem() instanceof IReactorComponent component
        && component.canStoreHeat(neighbor, reactor, x, y)) {
      int remaining =
          Math.clamp(component.alterHeat(neighbor, reactor, x, y, -sideVent), -sideVent, 0);
      reactor.addEmitHeat(sideVent + remaining);
    }
  }
}
