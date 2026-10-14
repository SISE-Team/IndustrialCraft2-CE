package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Pigment species require bright light and retain a flowering stage after their dye harvest. */
public class PigmentSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(2, 1, 1, 0, 5, 1);
  public final String name;
  public final Block cropBlock;
  public final String[] attributes;
  public final DyeColor color;
  private final String[] ownedAttributes;
  private final Item dye;

  public PigmentSpecies(ICropType type, Block block, String[] descriptors, DyeColor pigment) {
    super(type);
    name = type.getName();
    cropBlock = Objects.requireNonNull(block);
    color = Objects.requireNonNull(pigment);
    ownedAttributes = descriptors.clone();
    attributes = descriptors.clone();
    dye =
        net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(
            net.minecraft.resources.Identifier.withDefaultNamespace(pigment.getName() + "_dye"));
  }

  @Override
  public Block getCropBlock() {
    return cropBlock;
  }

  @Override
  public String getDiscoveredBy() {
    return name.equals("dandelion") || name.equals("rose") ? "Notch" : "Alblaka";
  }

  @Override
  public CropProperties getProperties() {
    return PROPERTIES;
  }

  @Override
  public String[] getAttributes() {
    return ownedAttributes.clone();
  }

  @Override
  public boolean canGrow(ICropTile plot) {
    return plot.getCurrentAge() < getMaxAge() && plot.getLightLevel() >= 12;
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return new ItemStack(dye);
  }

  @Override
  public int getAgeAfterHarvest(ICropTile plot) {
    return Math.max(0, getMaxAge() - 1);
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge() - 1 ? 600 : 400;
  }
}
