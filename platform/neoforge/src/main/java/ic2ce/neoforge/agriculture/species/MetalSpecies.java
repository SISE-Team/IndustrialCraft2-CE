package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import java.util.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Metal roots gate only the final stage; input tags, descriptors and yields are owned. */
public class MetalSpecies extends StoredYieldSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(6, 2, 0, 0, 1, 0);
  protected final Block cropBlock;
  protected final String[] cropAttributes;
  protected final List<TagKey<Block>> cropRootsRequirement;

  public MetalSpecies(
      ICropType type,
      Block block,
      String[] attributes,
      Collection<TagKey<Block>> roots,
      ItemStack product) {
    super(type, product);
    cropBlock = Objects.requireNonNull(block);
    cropAttributes = attributes.clone();
    cropRootsRequirement = roots == null ? List.of() : List.copyOf(roots);
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
  public boolean canGrow(ICropTile plot) {
    int age = plot.getCurrentAge(), gated = getMaxAge() - 1;
    if (age < gated) return true;
    if (age != gated) return false;
    if (cropRootsRequirement.isEmpty()) return true;
    for (var tag : cropRootsRequirement) if (plot.isBlockBelow(tag)) return true;
    return false;
  }

  @Override
  public int getRootsLength(ICropTile plot) {
    return 5;
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return cropDrop.copy();
  }

  @Override
  public double dropGainChance() {
    return super.dropGainChance() * .5;
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge() - 1 ? 2000 : 800;
  }

  @Override
  public int getAgeAfterHarvest(ICropTile plot) {
    return 1;
  }
}
