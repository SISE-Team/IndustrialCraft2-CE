package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Vanilla produce and independently owned seeds; species descriptors are immutable. */
public class BeetSpecies extends VanillaProduceSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(1, 0, 4, 0, 1, 2);
  private static final String[] ATTRIBUTES = new String[] {"Red", "Food", "Beetroot"};

  public BeetSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return BlockDefinitions.BEETROOTS_CROP;
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
  public ItemStack getProduct() {
    return new ItemStack(Items.BEETROOT);
  }

  @Override
  public ItemStack getSeeds() {
    return new ItemStack(Items.BEETROOT_SEEDS);
  }
}
