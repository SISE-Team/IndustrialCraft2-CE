package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.agriculture.AgricultureBlocks;
import ic2ce.neoforge.agriculture.AgricultureItems;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Hops keep a second-stage perennial vine with a separate fixed growth period. */
public class HopSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(5, 2, 2, 0, 1, 1);
  private static final String[] ATTRIBUTES = new String[] {"Green", "Ingredient", "Wheat"};

  public HopSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return AgricultureBlocks.HOPS_CROP;
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
  public int getGrowthDuration(ICropTile plot) {
    return 600;
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return new ItemStack(AgricultureItems.HOPS);
  }

  @Override
  public int getAgeAfterHarvest(ICropTile plot) {
    return 2;
  }
}
