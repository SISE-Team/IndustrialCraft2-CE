package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.agriculture.AgricultureBlocks;
import ic2ce.neoforge.agriculture.AgricultureItems;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/**
 * Coffee has separate flowering, immature harvest and bean stages; early harvest yields no beans.
 */
public class CoffeeSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(7, 1, 4, 1, 2, 0);
  private static final String[] ATTRIBUTES = new String[] {"Leaves", "Ingredient", "Beans"};

  public CoffeeSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return AgricultureBlocks.COFFEE_CROP;
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
    return "Snoochy";
  }

  @Override
  public boolean canGrow(ICropTile plot) {
    return plot.getCurrentAge() < getMaxAge() && plot.getLightLevel() >= 9;
  }

  @Override
  public int getWeightInfluences(ICropTile plot, int humidity, int nutrients, int air) {
    return (int)
        Math.clamp(
            .4 * humidity + 1.4 * nutrients + 1.2 * air, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    int age = plot.getCurrentAge();
    return age == getMaxAge() - 2 ? 700 : age == getMaxAge() - 3 ? 2100 : 1400;
  }

  @Override
  public boolean canBeHarvested(ICropTile plot) {
    return plot.getCurrentAge() >= getMaxAge() - 1;
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge() - 1
        ? ItemStack.EMPTY
        : new ItemStack(AgricultureItems.COFFEE_BEANS);
  }

  @Override
  public int getAgeAfterHarvest(ICropTile plot) {
    return Math.max(0, getMaxAge() - 2);
  }
}
