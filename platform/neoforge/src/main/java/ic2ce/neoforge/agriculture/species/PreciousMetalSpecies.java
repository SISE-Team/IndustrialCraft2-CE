package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import java.util.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Precious mineral yield and growth delays differ from the ordinary metal protocol. */
public class PreciousMetalSpecies extends MetalSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(6, 2, 0, 0, 2, 0);

  public PreciousMetalSpecies(
      ICropType type,
      Block block,
      String[] attributes,
      Collection<TagKey<Block>> roots,
      ItemStack product) {
    super(type, block, attributes, roots, product);
  }

  @Override
  public CropProperties getProperties() {
    return PROPERTIES;
  }

  @Override
  public double dropGainChance() {
    return Math.pow(.95, PROPERTIES.tier());
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge() - 1 ? 2200 : 750;
  }
}
