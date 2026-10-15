package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.agriculture.AgricultureBlocks;
import java.util.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Ordinary reeds yield their current height, resist trampling and regrow from zero. */
public class ReedSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(2, 0, 0, 1, 0, 2);
  private static final String[] ATTRIBUTES = new String[] {"Reed"};

  public ReedSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return AgricultureBlocks.REED_CROP;
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
    return "Notch";
  }

  @Override
  public int getWeightInfluences(ICropTile plot, int humidity, int nutrients, int air) {
    return (int)
        Math.clamp(1.2 * humidity + nutrients + .8 * air, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  @Override
  public boolean canBeHarvested(ICropTile plot) {
    return plot.getCurrentAge() > 0;
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return plot.getCurrentAge() <= 0
        ? ItemStack.EMPTY
        : new ItemStack(Items.SUGAR_CANE, Math.min(getMaxAge(), plot.getCurrentAge()));
  }

  @Override
  public boolean onEntityCollision(ICropTile plot, Entity entity) {
    return false;
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return 200;
  }
}
