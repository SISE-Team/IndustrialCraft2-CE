package ic2ce.api.crops;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Public species extension points with bounded default quantities and explicit native callbacks.
 */
public abstract class CropCard {
  protected final ICropType cropType;

  public CropCard(ICropType type) {
    cropType = Objects.requireNonNull(type);
  }

  public String getId() {
    return cropType.getName();
  }

  public String getOwner() {
    return cropType.getOwner();
  }

  public abstract Block getCropBlock();

  public String getUnlocalizedName() {
    return getOwner() + ".crop." + getId();
  }

  public String getDiscoveredBy() {
    return "unknown";
  }

  public String desc(int line) {
    var attributes = getAttributes();
    if (attributes == null) return "";
    int start = line == 0 ? 0 : 2;
    if (start >= attributes.length) return "";
    var text = new StringBuilder(Objects.toString(attributes[start], ""));
    if (start + 1 < attributes.length)
      text.append(", ").append(Objects.toString(attributes[start + 1], ""));
    if (line == 0 && attributes.length > 2) text.append(',');
    return text.toString();
  }

  public int getRootsLength(ICropTile plot) {
    return 1;
  }

  public abstract CropProperties getProperties();

  public String[] getAttributes() {
    return new String[0];
  }

  public String getSeedType() {
    return "ic2.crop.seeds";
  }

  public int getMaxAge() {
    return cropType.getMaxAge();
  }

  public int getGrowthDuration(ICropTile plot) {
    return (int) Math.clamp((long) getProperties().getTier() * 200, 1, Integer.MAX_VALUE);
  }

  public boolean canGrow(ICropTile plot) {
    return plot.getCurrentAge() < getMaxAge();
  }

  public int getWeightInfluences(ICropTile plot, int humidity, int nutrients, int air) {
    return (int)
        Math.clamp((long) humidity + nutrients + air, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  public boolean canCross(ICropTile plot) {
    return plot.getCurrentAge() >= 2;
  }

  public boolean onRightClick(ICropTile plot, Player player) {
    return plot.performManualHarvest();
  }

  public int getOptimalHarvestAge(ICropTile plot) {
    return getMaxAge();
  }

  public boolean canBeHarvested(ICropTile plot) {
    return plot.getCurrentAge() == getMaxAge();
  }

  public double dropGainChance() {
    return Math.pow(.95, Math.max(0, getProperties().getTier()));
  }

  @Deprecated
  public ItemStack getGain(ICropTile plot) {
    return ItemStack.EMPTY;
  }

  public ItemStack[] getGains(ICropTile plot) {
    var gain = getGain(plot);
    return gain == null || gain.isEmpty() ? new ItemStack[0] : new ItemStack[] {gain.copy()};
  }

  public int getAgeAfterHarvest(ICropTile plot) {
    return 0;
  }

  public boolean onLeftClick(ICropTile plot, Player player) {
    return plot.pick();
  }

  public float dropSeedChance(ICropTile plot) {
    int age = plot.getCurrentAge();
    if (age <= 0) return 0;
    return (float) ((age == 1 ? .25 : .5) * Math.pow(.8, Math.max(0, getProperties().getTier())));
  }

  public ItemStack getSeeds(ICropTile plot) {
    return plot.generateSeeds(
        plot.getCrop(),
        plot.getStatGrowth(),
        plot.getStatGain(),
        plot.getStatResistance(),
        plot.getScanLevel());
  }

  public void onNeighbourChange(ICropTile plot) {}

  public boolean isRedstoneSignalEmitter(ICropTile plot) {
    return false;
  }

  public int getEmittedRedstoneSignal(ICropTile plot) {
    return 0;
  }

  public void onBlockDestroyed(ICropTile plot) {}

  public int getEmittedLight(ICropTile plot) {
    return 0;
  }

  public boolean onEntityCollision(ICropTile plot, Entity entity) {
    return entity instanceof LivingEntity && entity.isSprinting();
  }

  public void tick(ICropTile plot) {}

  public boolean isWeed(ICropTile plot) {
    return plot.getCurrentAge() >= 1
        && (plot.getCrop() == Crops.weed || plot.getStatGrowth() >= 24);
  }

  public Level getWorld(ICropTile plot) {
    return plot.getWorldObj();
  }

  public abstract List<Identifier> getTexturesLocation();
}
