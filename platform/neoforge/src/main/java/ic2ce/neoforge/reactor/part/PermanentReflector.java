package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import ic2ce.api.reactor.IReactorComponent;
import ic2ce.core.profile.NotClassic;
import net.minecraft.world.item.ItemStack;

/** Permanent reflector shares pulse direction semantics but has no wear counter. */
@NotClassic
public class PermanentReflector extends ReactorPart {
  public PermanentReflector(Properties properties) {
    super(properties);
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
    if (!thermalPhase)
      component.acceptUraniumPulse(source, reactor, part, sourceX, sourceY, x, y, false);
    return true;
  }

  @Override
  public float influenceExplosion(ItemStack part, IReactor reactor) {
    return -1;
  }
}
