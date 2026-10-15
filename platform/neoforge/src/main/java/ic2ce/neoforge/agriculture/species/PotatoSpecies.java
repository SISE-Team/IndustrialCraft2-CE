package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.agriculture.AgricultureBlocks;
import ic2ce.neoforge.runtime.SharedRandom;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/**
 * Potatoes permit early harvesting; only fully mature produce has the one-in-twenty poison draw.
 */
public class PotatoSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(2, 0, 4, 0, 0, 2);
  private static final String[] ATTRIBUTES = new String[] {"Yellow", "Food", "Potato"};

  public PotatoSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return AgricultureBlocks.POTATO_CROP;
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
  public boolean canGrow(ICropTile plot) {
    return plot.getCurrentAge() < getMaxAge() && plot.getLightLevel() >= 9;
  }

  @Override
  public int getOptimalHarvestAge(ICropTile plot) {
    return Math.max(0, getMaxAge() - 1);
  }

  @Override
  public boolean canBeHarvested(ICropTile plot) {
    return plot.getCurrentAge() >= getOptimalHarvestAge(plot);
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    int age = plot.getCurrentAge();
    if (age < getOptimalHarvestAge(plot)) return ItemStack.EMPTY;
    return new ItemStack(
        age >= getMaxAge() && SharedRandom.INSTANCE.nextInt(20) == 0
            ? Items.POISONOUS_POTATO
            : Items.POTATO);
  }
}
