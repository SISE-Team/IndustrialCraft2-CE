package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Fungal products have a separate short growth period and owned reusable harvest prototype. */
public class MushroomSpecies extends StoredYieldSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(2, 0, 4, 0, 0, 4);
  protected final String[] cropAttributes;
  protected final Block cropBlock;

  public MushroomSpecies(ICropType type, Block block, String[] attributes, ItemStack product) {
    super(type, product);
    cropBlock = Objects.requireNonNull(block);
    cropAttributes = attributes.clone();
  }

  @Override
  public Block getCropBlock() {
    return cropBlock;
  }

  @Override
  public CropProperties getProperties() {
    return PROPERTIES;
  }

  @Override
  public String[] getAttributes() {
    return cropAttributes.clone();
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return cropDrop.copy();
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return 200;
  }
}
