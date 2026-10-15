package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.runtime.SharedRandom;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Woody species retain a perennial trunk, independent sapling draws and an oak-only apple draw. */
public class TreeSpecies extends StoredYieldSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(3, 1, 0, 4, 4, 0);
  protected final Block cropBlock;
  protected final String saplingName;
  protected final ItemStack cropSapling;
  private final boolean appleBearing;

  public TreeSpecies(
      ICropType type, Block block, String seedName, ItemStack product, ItemStack sapling) {
    super(type, product);
    cropBlock = Objects.requireNonNull(block);
    saplingName = "ic2.crop." + Objects.requireNonNull(seedName);
    cropSapling = sapling.copy();
    appleBearing = type.getName().equalsIgnoreCase("oak_sapling");
  }

  @Override
  public Block getCropBlock() {
    return cropBlock;
  }

  @Override
  public String getSeedType() {
    return saplingName;
  }

  @Override
  public String getDiscoveredBy() {
    return "Speiger";
  }

  @Override
  public CropProperties getProperties() {
    return PROPERTIES;
  }

  @Override
  public String[] getAttributes() {
    return new String[] {"Leaves", "Sapling", "Green"};
  }

  @Override
  public boolean canGrow(ICropTile plot) {
    return plot.getCurrentAge() < getMaxAge() && plot.getLightLevel() >= 9;
  }

  @Override
  public ItemStack[] getGains(ICropTile plot) {
    var produced = new ItemStack[3];
    int count = 0;
    produced[count++] = cropDrop.copy();
    if (SharedRandom.INSTANCE.nextInt(100) >= 75) produced[count++] = cropSapling.copy();
    if (appleBearing && SharedRandom.INSTANCE.nextInt(100) >= 75)
      produced[count++] = new ItemStack(Items.APPLE);
    return Arrays.copyOf(produced, count);
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return plot.getCurrentAge() >= getMaxAge() - 1 ? 150 : 600;
  }

  @Override
  public int getAgeAfterHarvest(ICropTile plot) {
    return Math.max(0, getMaxAge() - 1);
  }
}
