package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Venom flowers distinguish pigment, poison powder, dormant growth and crouched contact. */
public class VenomSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(3, 3, 1, 3, 3, 3);

  public VenomSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return BlockDefinitions.VENOMILIA_CROP;
  }

  @Override
  public String getDiscoveredBy() {
    return "raGan";
  }

  @Override
  public CropProperties getProperties() {
    return PROPERTIES;
  }

  @Override
  public String[] getAttributes() {
    return new String[] {"Purple", "Flower", "Tulip", "Poison"};
  }

  @Override
  public boolean canGrow(ICropTile plot) {
    int age = plot.getCurrentAge();
    return age == 4 || age <= 3 && plot.getLightLevel() >= 12;
  }

  @Override
  public boolean canBeHarvested(ICropTile plot) {
    return plot.getCurrentAge() >= 3;
  }

  @Override
  public int getOptimalHarvestAge(ICropTile plot) {
    return 3;
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    int age = plot.getCurrentAge();
    return age < 3
        ? ItemStack.EMPTY
        : new ItemStack(age == 4 ? ItemDefinitions.GRIN_POWDER : Items.PURPLE_DYE);
  }

  @Override
  public int getAgeAfterHarvest(ICropTile plot) {
    return 2;
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    return plot.getCurrentAge() >= 2 ? 600 : 400;
  }

  @Override
  public boolean onRightClick(ICropTile plot, net.minecraft.world.entity.player.Player player) {
    if (!player.isShiftKeyDown()) onEntityCollision(plot, player);
    return plot.performManualHarvest();
  }

  @Override
  public boolean onLeftClick(ICropTile plot, net.minecraft.world.entity.player.Player player) {
    if (!player.isShiftKeyDown()) onEntityCollision(plot, player);
    return plot.pick();
  }

  @Override
  public boolean onEntityCollision(ICropTile plot, net.minecraft.world.entity.Entity entity) {
    if (plot.getWorldObj().isClientSide()) return false;
    if (plot.getCurrentAge() == 4
        && entity instanceof net.minecraft.world.entity.LivingEntity living) {
      boolean avoidsContact =
          entity instanceof net.minecraft.world.entity.player.Player
              && entity.isShiftKeyDown()
              && ProjectRuntime.random.nextInt(50) != 0;
      if (!avoidsContact) {
        living.addEffect(
            new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.POISON,
                (ProjectRuntime.random.nextInt(10) + 5) * 20));
        plot.setCurrentAge(3);
        plot.updateState();
      }
    }
    return super.onEntityCollision(plot, entity);
  }

  @Override
  public boolean isWeed(ICropTile plot) {
    return plot.getCurrentAge() == 4 && plot.getStatGrowth() >= 8;
  }
}
