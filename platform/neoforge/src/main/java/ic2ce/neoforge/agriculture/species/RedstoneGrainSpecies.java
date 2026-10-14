package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/**
 * Red wheat uses dim light, native redstone-conditioned produce and mature redstone/light output.
 */
public class RedstoneGrainSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(6, 3, 0, 0, 2, 0);
  private static final String[] ATTRIBUTES = new String[] {"Red", "Redstone", "Wheat"};

  public RedstoneGrainSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return BlockDefinitions.RED_WHEAT_CROP;
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
  public boolean canGrow(ICropTile plot) {
    int light = plot.getLightLevel();
    return plot.getCurrentAge() < getMaxAge() && light >= 5 && light <= 10;
  }

  @Override
  public double dropGainChance() {
    return .5;
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    boolean powered = plot.getWorldObj().getBestNeighborSignal(plot.getPosition()) > 0;
    return new ItemStack(
        powered || plot.getWorldObj().getRandom().nextBoolean() ? Items.REDSTONE : Items.WHEAT);
  }

  @Override
  public boolean isRedstoneSignalEmitter(ICropTile plot) {
    return true;
  }

  @Override
  public int getEmittedRedstoneSignal(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge() ? 15 : 0;
  }

  @Override
  public int getEmittedLight(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge() ? 7 : 0;
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return 600;
  }

  @Override
  public int getAgeAfterHarvest(ICropTile plot) {
    return 1;
  }
}
