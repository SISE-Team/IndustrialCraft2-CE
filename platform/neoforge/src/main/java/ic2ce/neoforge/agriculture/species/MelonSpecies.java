package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/**
 * Melons independently choose whole fruit or bounded slices and retain their stem after harvesting.
 */
public class MelonSpecies extends FruitStemSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(2, 0, 4, 0, 2, 0);
  private static final String[] ATTRIBUTES = new String[] {"Green", "Food", "Stem"};

  public MelonSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return BlockDefinitions.MELON_CROP;
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
    return "Chao";
  }

  @Override
  protected ItemStack getProduct() {
    return ProjectRuntime.random.nextInt(3) == 0
        ? new ItemStack(Blocks.MELON)
        : new ItemStack(Items.MELON, ProjectRuntime.random.nextInt(4) + 2);
  }

  @Override
  protected ItemStack getSeeds() {
    return new ItemStack(Items.MELON_SEEDS, ProjectRuntime.random.nextInt(2) + 1);
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge() - 1 ? 700 : 250;
  }
}
