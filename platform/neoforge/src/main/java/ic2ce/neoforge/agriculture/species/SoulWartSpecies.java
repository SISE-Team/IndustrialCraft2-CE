package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.agriculture.CropCatalog;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/**
 * Root material controls saturated bonus growth and rare native species transformation, with an
 * independent yield policy.
 */
public class SoulWartSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(5, 4, 2, 0, 2, 1);
  private static final String[] ATTRIBUTES =
      new String[] {"Red", "Nether", "Ingredient", "Soulsand"};

  public SoulWartSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return BlockDefinitions.NETHER_WART_CROP;
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
  public double dropGainChance() {
    return 2.0;
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return new ItemStack(Items.NETHER_WART);
  }

  @Override
  public void tick(ICropTile plot) {
    if (plot.isBlockBelow(Blocks.SOUL_SAND)) {
      if (canGrow(plot))
        plot.setGrowthPoints((int) Math.min(Integer.MAX_VALUE, plot.getGrowthPoints() + 100L));
    } else if (plot.isBlockBelow(Blocks.SNOW) && plot.getWorldObj().getRandom().nextInt(300) == 0)
      plot.setCrop(CropCatalog.cropTerraWart);
  }

  @Override
  public int getRootsLength(ICropTile plot) {
    return 5;
  }

  @Override
  public String getDiscoveredBy() {
    return "Notch";
  }
}
