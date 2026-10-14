package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.ICropTile;
import ic2ce.api.crops.ICropType;
import java.util.List;
import net.minecraft.resources.Identifier;

/** Fruit-bearing stems retain their own moisture weighting and perennial harvest age. */
public abstract class FruitStemSpecies extends VanillaProduceSpecies {
  public FruitStemSpecies(ICropType type) {
    super(type);
  }

  @Override
  public List<Identifier> getTexturesLocation() {
    return getDefaultTexturesLocation();
  }

  @Override
  public int getWeightInfluences(ICropTile plot, int humidity, int nutrients, int air) {
    return (int)
        Math.clamp(humidity * 1.1 + nutrients * .9 + air, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  @Override
  public int getAgeAfterHarvest(ICropTile plot) {
    return Math.max(0, getMaxAge() - 1);
  }
}
