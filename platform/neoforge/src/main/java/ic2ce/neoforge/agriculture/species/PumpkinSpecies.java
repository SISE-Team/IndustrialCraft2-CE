package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Perennial pumpkin fruit has a distinct final-stage growth delay and one-to-three seeds. */
public class PumpkinSpecies extends FruitStemSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(1, 0, 1, 0, 3, 1);
  private static final String[] ATTRIBUTES = new String[] {"Orange", "Decoration", "Stem"};

  public PumpkinSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return BlockDefinitions.PUMPKIN_CROP;
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
  protected ItemStack getProduct() {
    return new ItemStack(Blocks.PUMPKIN);
  }

  @Override
  protected ItemStack getSeeds() {
    return new ItemStack(Items.PUMPKIN_SEEDS, ProjectRuntime.random.nextInt(3) + 1);
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge() - 1 ? 600 : 200;
  }
}
