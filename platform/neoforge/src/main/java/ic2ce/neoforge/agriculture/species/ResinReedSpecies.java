package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/**
 * Sticky reeds distinguish immature cane from mature resin and preserve one or two stages after
 * resin collection.
 */
public class ResinReedSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(4, 2, 0, 1, 0, 1);
  private static final String[] ATTRIBUTES = new String[] {"Reed", "Resin"};

  public ResinReedSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return BlockDefinitions.STICKY_REED_CROP;
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
    return "raa1337";
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
    int age = plot.getCurrentAge();
    return age <= 0
        ? ItemStack.EMPTY
        : age < getMaxAge()
            ? new ItemStack(Items.SUGAR_CANE, age)
            : new ItemStack(ItemDefinitions.RESIN);
  }

  @Override
  public int getAgeAfterHarvest(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge() ? 2 - ProjectRuntime.random.nextInt(2) : 0;
  }

  @Override
  public boolean onEntityCollision(ICropTile plot, Entity entity) {
    return false;
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge() ? 400 : 100;
  }
}
