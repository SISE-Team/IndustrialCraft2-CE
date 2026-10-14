package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.ICropTile;
import ic2ce.api.crops.ICropType;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Vanilla-origin produce has its own light gate and ordinary versus engineered seed policy. */
public abstract class VanillaProduceSpecies extends MaterialSpecies {
  public VanillaProduceSpecies(ICropType type) {
    super(type);
  }

  protected List<Identifier> getDefaultTexturesLocation() {
    return textures(false);
  }

  @Override
  public List<Identifier> getTexturesLocation() {
    return textures(true);
  }

  @Override
  public String getDiscoveredBy() {
    return "Notch";
  }

  @Override
  public boolean canGrow(ICropTile plot) {
    return plot.getCurrentAge() < getMaxAge() && plot.getLightLevel() >= 9;
  }

  protected abstract ItemStack getSeeds();

  protected abstract ItemStack getProduct();

  @Override
  public ItemStack getGain(ICropTile plot) {
    var product = getProduct();
    return product == null ? ItemStack.EMPTY : product.copy();
  }

  @Override
  public ItemStack getSeeds(ICropTile plot) {
    return plot.getStatGain() <= 1 && plot.getStatGrowth() <= 1 && plot.getStatResistance() <= 1
        ? getSeeds().copy()
        : super.getSeeds(plot);
  }
}
