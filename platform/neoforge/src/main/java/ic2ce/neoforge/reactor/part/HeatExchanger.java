package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import ic2ce.api.reactor.IReactorComponent;
import ic2ce.engine.reactor.HeatBalance;
import net.minecraft.world.item.ItemStack;

/**
 * Capacity-weighted exchange treats side and hull rate limits independently and never overshoots
 * equilibrium.
 */
public class HeatExchanger extends HeatCell {
  public final int switchSide, switchReactor;

  public HeatExchanger(Properties properties, int capacity, int sideRate, int hullRate) {
    super(properties, capacity);
    switchSide = Math.max(0, sideRate);
    switchReactor = Math.max(0, hullRate);
  }

  @Override
  public void processChamber(ItemStack part, IReactor reactor, int x, int y, boolean thermalPhase) {
    if (!thermalPhase) return;
    if (switchSide > 0) {
      exchange(part, reactor, x, y, x - 1, y);
      exchange(part, reactor, x, y, x + 1, y);
      exchange(part, reactor, x, y, x, y - 1);
      exchange(part, reactor, x, y, x, y + 1);
    }
    int moved =
        HeatBalance.transfer(
            getUse(part), getMaxUse(), reactor.getHeat(), reactor.getMaxHeat(), switchReactor);
    if (moved != 0) {
      reactor.addHeat(moved);
      alterHeat(part, reactor, x, y, -moved);
    }
  }

  private void exchange(ItemStack part, IReactor reactor, int x, int y, int targetX, int targetY) {
    var target = reactor.getItemAt(targetX, targetY);
    if (target == null
        || target.isEmpty()
        || !(target.getItem() instanceof IReactorComponent component)
        || !component.canStoreHeat(target, reactor, targetX, targetY)) return;
    int moved =
        HeatBalance.transfer(
            getUse(part),
            getMaxUse(),
            component.getCurrentHeat(target, reactor, targetX, targetY),
            component.getMaxHeat(target, reactor, targetX, targetY),
            switchSide);
    if (moved == 0) return;
    int remainder = component.alterHeat(target, reactor, targetX, targetY, moved);
    int accepted =
        moved > 0
            ? moved - Math.clamp(remainder, 0, moved)
            : moved - Math.clamp(remainder, moved, 0);
    if (accepted != 0) alterHeat(part, reactor, x, y, -accepted);
  }
}
