package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import net.minecraft.world.item.ItemStack;

/** Core heat first moves into a vent; only real removed internal heat becomes emitted HU. */
public class HeatVent extends HeatCell {
  public final int selfVent, reactorVent;

  public HeatVent(Properties properties, int capacity, int selfRate, int coreRate) {
    super(properties, capacity);
    selfVent = Math.max(0, selfRate);
    reactorVent = Math.max(0, coreRate);
  }

  @Override
  public void processChamber(ItemStack part, IReactor reactor, int x, int y, boolean thermalPhase) {
    if (!thermalPhase) return;
    int requested = Math.min(Math.max(0, reactor.getHeat()), reactorVent);
    if (requested > 0) {
      int remainder = Math.clamp(alterHeat(part, reactor, x, y, requested), 0, requested);
      reactor.addHeat(-(requested - remainder));
      if (reactor.getItemAt(x, y) != part) return;
    }
    if (selfVent > 0) {
      int unhandled = Math.clamp(alterHeat(part, reactor, x, y, -selfVent), -selfVent, 0);
      reactor.addEmitHeat(selfVent + unhandled);
    }
  }
}
