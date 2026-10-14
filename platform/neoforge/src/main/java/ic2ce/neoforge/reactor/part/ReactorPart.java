package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import ic2ce.api.reactor.IReactorComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A passive reactor part rejects unsupported operations and returns unhandled heat unchanged. */
public abstract class ReactorPart extends Item implements IReactorComponent {
  protected ReactorPart(Properties properties) {
    super(properties);
  }

  @Override
  public void processChamber(
      ItemStack part, IReactor reactor, int x, int y, boolean thermalPhase) {}

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
    return false;
  }

  @Override
  public boolean canStoreHeat(ItemStack part, IReactor reactor, int x, int y) {
    return false;
  }

  @Override
  public int getMaxHeat(ItemStack part, IReactor reactor, int x, int y) {
    return 0;
  }

  @Override
  public int getCurrentHeat(ItemStack part, IReactor reactor, int x, int y) {
    return 0;
  }

  @Override
  public int alterHeat(ItemStack part, IReactor reactor, int x, int y, int requested) {
    return requested;
  }

  @Override
  public float influenceExplosion(ItemStack part, IReactor reactor) {
    return 0;
  }

  @Override
  public boolean canBePlacedIn(ItemStack part, IReactor reactor) {
    return true;
  }
}
