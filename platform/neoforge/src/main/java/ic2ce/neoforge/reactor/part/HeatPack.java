package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import ic2ce.api.reactor.IReactorComponent;
import ic2ce.core.profile.NotExperimental;
import net.minecraft.world.item.ItemStack;

/**
 * Heat packs warm neighboring storage toward a stack-dependent target without overflow or cooling a
 * hotter part.
 */
@NotExperimental
public class HeatPack extends ReactorPart {
  protected final int maxPer, heatPer;

  public HeatPack(Properties properties, int target, int rate) {
    super(properties);
    maxPer = Math.max(0, target);
    heatPer = Math.max(0, rate);
  }

  @Override
  public void processChamber(ItemStack pack, IReactor reactor, int x, int y, boolean thermalPhase) {
    if (!thermalPhase || pack.isEmpty()) return;
    warm(reactor, pack.getCount(), x + 1, y);
    warm(reactor, pack.getCount(), x - 1, y);
    warm(reactor, pack.getCount(), x, y + 1);
    warm(reactor, pack.getCount(), x, y - 1);
  }

  private void warm(IReactor reactor, int count, int x, int y) {
    long goal = (long) maxPer * count;
    if (reactor.getHeat() >= goal) return;
    var target = reactor.getItemAt(x, y);
    if (target != null
        && !target.isEmpty()
        && target.getItem() instanceof IReactorComponent component
        && component.canStoreHeat(target, reactor, x, y)) {
      long deficit = goal - component.getCurrentHeat(target, reactor, x, y);
      int amount =
          (int) Math.clamp(Math.min((long) heatPer * count, deficit), 0, Integer.MAX_VALUE);
      if (amount > 0) component.alterHeat(target, reactor, x, y, amount);
    }
  }

  @Override
  public float influenceExplosion(ItemStack pack, IReactor reactor) {
    return pack.getCount() / 10.0F;
  }
}
