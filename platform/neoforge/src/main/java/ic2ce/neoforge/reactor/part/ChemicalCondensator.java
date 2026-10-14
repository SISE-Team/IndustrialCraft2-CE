package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import net.minecraft.world.item.ItemStack;

/** Chemical condensators absorb heat up to saturation and cannot be cooled by vents. */
public class ChemicalCondensator extends WearableReactorPart {
  public ChemicalCondensator(Properties properties, int capacity) {
    super(properties, capacity);
  }

  @Override
  public boolean canStoreHeat(ItemStack part, IReactor reactor, int x, int y) {
    return getUse(part) < getMaxUse();
  }

  @Override
  public int getMaxHeat(ItemStack part, IReactor reactor, int x, int y) {
    return getMaxUse();
  }

  @Override
  public int getCurrentHeat(ItemStack part, IReactor reactor, int x, int y) {
    return getUse(part);
  }

  @Override
  public int alterHeat(ItemStack part, IReactor reactor, int x, int y, int requested) {
    if (requested <= 0) return requested;
    int present = getUse(part), accepted = Math.min(requested, getMaxUse() - present);
    if (accepted > 0) setUse(part, present + accepted);
    return requested - accepted;
  }
}
