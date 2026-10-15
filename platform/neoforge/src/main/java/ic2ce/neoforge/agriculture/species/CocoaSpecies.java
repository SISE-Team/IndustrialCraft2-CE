package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.agriculture.AgricultureBlocks;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/**
 * Cocoa requires stored nutrients and preserves a fruiting stem after its distinct final-stage
 * delay.
 */
public class CocoaSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(3, 1, 3, 0, 4, 0);
  private static final String[] ATTRIBUTES = new String[] {"Brown", "Food", "Stem"};

  public CocoaSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return AgricultureBlocks.COCOA_CROP;
  }

  @Override
  public CropProperties getProperties() {
    return PROPERTIES;
  }

  @Override
  public String[] getAttributes() {
    return ATTRIBUTES.clone();
  }

  @Override
  public String getDiscoveredBy() {
    return "Notch";
  }

  @Override
  public boolean canGrow(ICropTile plot) {
    return plot.getCurrentAge() < getMaxAge() && plot.getStorageNutrients() >= 3;
  }

  @Override
  public int getWeightInfluences(ICropTile plot, int humidity, int nutrients, int air) {
    return (int)
        Math.clamp(
            .8 * humidity + 1.3 * nutrients + .9 * air, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return new ItemStack(Items.COCOA_BEANS);
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge() - 1 ? 900 : 400;
  }

  @Override
  public int getAgeAfterHarvest(ICropTile plot) {
    return Math.max(0, getMaxAge() - 1);
  }
}
