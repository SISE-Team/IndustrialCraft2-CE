package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.agriculture.AgricultureBlocks;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Flax is fully cut back after producing fiber, unlike the perennial fruit and vine policies. */
public class FlaxSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(2, 1, 1, 2, 0, 1);
  private static final String[] ATTRIBUTES = new String[] {"Silk", "Vine", "Addictive"};

  public FlaxSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return AgricultureBlocks.FLAX_CROP;
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
    return "Eloraam";
  }

  @Override
  public boolean canGrow(ICropTile plot) {
    return plot.getCurrentAge() < getMaxAge() && plot.getLightLevel() >= 9;
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return new ItemStack(Items.STRING);
  }

  @Override
  public int getAgeAfterHarvest(ICropTile plot) {
    return 0;
  }
}
