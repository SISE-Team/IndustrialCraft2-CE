package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.ICropType;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/** Constructor input is owned; each harvest obtains a separate mutable output. */
public abstract class StoredYieldSpecies extends MaterialSpecies {
  protected final ItemStack cropDrop;

  public StoredYieldSpecies(ICropType type, ItemStack prototype) {
    super(type);
    cropDrop = Objects.requireNonNull(prototype).copy();
  }
}
