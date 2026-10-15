package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.agriculture.AgricultureBlocks;
import ic2ce.neoforge.agriculture.CropCatalog;
import ic2ce.neoforge.item.consumable.ConsumableItems;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/**
 * Root material controls saturated bonus growth and rare native species transformation, with an
 * independent yield policy.
 */
public class FrostWartSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(5, 2, 4, 0, 3, 0);
  private static final String[] ATTRIBUTES = new String[] {"Blue", "Aether", "Consumable", "Snow"};

  public FrostWartSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return AgricultureBlocks.TERRA_WART_CROP;
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
    return .8;
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return new ItemStack(ConsumableItems.TERRA_WART);
  }

  @Override
  public void tick(ICropTile plot) {
    if (plot.isBlockBelow(Blocks.SNOW)) {
      if (canGrow(plot))
        plot.setGrowthPoints((int) Math.min(Integer.MAX_VALUE, plot.getGrowthPoints() + 100L));
    } else if (plot.isBlockBelow(Blocks.SOUL_SAND)
        && plot.getWorldObj().getRandom().nextInt(300) == 0)
      plot.setCrop(CropCatalog.cropNetherWart);
  }

  @Override
  public int getRootsLength(ICropTile plot) {
    return 5;
  }
}
