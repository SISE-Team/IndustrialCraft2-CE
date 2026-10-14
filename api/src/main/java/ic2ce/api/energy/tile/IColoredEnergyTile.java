package ic2ce.api.energy.tile;

import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

/** Null pigment is a wildcard; each side contacts the opposite face of its actual neighbor. */
public interface IColoredEnergyTile extends IEnergyTile {
  DyeColor getColor(Direction side);

  default boolean canInteractWith(IEnergyTile neighbor, Direction side) {
    Objects.requireNonNull(side);
    if (neighbor == null) return false;
    if (!(neighbor instanceof IColoredEnergyTile colored)) return true;
    var own = getColor(side);
    var other = colored.getColor(side.getOpposite());
    return own == null || other == null || own == other;
  }
}
