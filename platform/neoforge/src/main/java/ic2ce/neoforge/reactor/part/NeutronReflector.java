package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import ic2ce.api.reactor.IReactorComponent;
import net.minecraft.world.item.ItemStack;

/** Disposable neutron reflector echoes only the energy phase and wears only on thermal pulses. */
public class NeutronReflector extends WearableReactorPart {
  public NeutronReflector(Properties properties, int lifetime) {
    super(properties, lifetime);
  }

  @Override
  public boolean acceptUraniumPulse(
      ItemStack part,
      IReactor reactor,
      ItemStack source,
      int x,
      int y,
      int sourceX,
      int sourceY,
      boolean thermalPhase) {
    if (source == null
        || source.isEmpty()
        || !(source.getItem() instanceof IReactorComponent component)
        || component instanceof NeutronReflector
        || component instanceof PermanentReflector) return false;
    if (thermalPhase) {
      if (getUse(part) >= getMaxUse() - 1) reactor.setItemAt(x, y, ItemStack.EMPTY);
      else incrementUse(part);
    } else component.acceptUraniumPulse(source, reactor, part, sourceX, sourceY, x, y, false);
    return true;
  }

  @Override
  public float influenceExplosion(ItemStack part, IReactor reactor) {
    return -1;
  }
}
