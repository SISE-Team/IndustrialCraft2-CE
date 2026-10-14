package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/**
 * Weeds spread independently, yield no harvest and cannot be picked through the manual seed
 * interaction.
 */
public class WeedSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(0, 0, 0, 1, 0, 5);
  private static final String[] ATTRIBUTES = new String[] {"Weed", "Bad"};

  public WeedSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return BlockDefinitions.WEED_CROP;
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
  public int getOptimalHarvestAge(ICropTile plot) {
    return 1;
  }

  @Override
  public boolean onLeftClick(ICropTile plot, net.minecraft.world.entity.player.Player player) {
    return false;
  }

  @Override
  public boolean canBeHarvested(ICropTile plot) {
    return false;
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return ItemStack.EMPTY;
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return 300;
  }

  @Override
  public boolean onEntityCollision(ICropTile plot, Entity entity) {
    return false;
  }
}
