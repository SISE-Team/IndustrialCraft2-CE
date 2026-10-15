package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.*;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/** Carnivorous crops select one real victim without shuffling an entire entity list. */
public class CarnivorousSpecies extends MaterialSpecies {
  private static final CropProperties PROPERTIES = new CropProperties(6, 1, 1, 3, 1, 4);

  public CarnivorousSpecies(ICropType type) {
    super(type);
  }

  @Override
  public Block getCropBlock() {
    return BlockDefinitions.EATING_PLANT_CROP;
  }

  @Override
  public String getDiscoveredBy() {
    return "Hasudako";
  }

  @Override
  public CropProperties getProperties() {
    return PROPERTIES;
  }

  @Override
  public String[] getAttributes() {
    return new String[] {"Bad", "Food"};
  }

  @Override
  public boolean canGrow(ICropTile plot) {
    int age = plot.getCurrentAge();
    return plot.getLightLevel() > 10
        && (age < 2 || age < getMaxAge() && plot.isBlockBelow(Blocks.LAVA));
  }

  @Override
  public int getOptimalHarvestAge(ICropTile plot) {
    return Math.max(0, getMaxAge() - 2);
  }

  @Override
  public boolean canBeHarvested(ICropTile plot) {
    int age = plot.getCurrentAge();
    return age >= getOptimalHarvestAge(plot) && age < getMaxAge();
  }

  @Override
  public ItemStack getGain(ICropTile plot) {
    return canBeHarvested(plot) ? new ItemStack(Blocks.CACTUS) : ItemStack.EMPTY;
  }

  @Override
  public int getRootsLength(ICropTile plot) {
    return 5;
  }

  @Override
  public int getGrowthDuration(ICropTile plot) {
    var biome = ic2ce.core.util.BiomeUtil.getBiome(plot.getWorldObj(), plot.getPosition());
    double biomeFactor =
        ProjectRuntime.envProxy.biomeHasType(biome, BiomeType.SWAMP)
                || ProjectRuntime.envProxy.biomeHasType(biome, BiomeType.MOUNTAIN)
            ? 1 / 1.5
            : 1;
    return Math.max(
        1, (int) (1200 * biomeFactor / (1 + Math.max(0, plot.getTerrainAirQuality()) / 10.0)));
  }

  @Override
  public void tick(ICropTile plot) {
    var world = plot.getWorldObj();
    if (world == null || world.isClientSide() || plot.getCurrentAge() <= 0) return;
    var position = plot.getPosition();
    double x = position.getX() + .5, z = position.getZ() + .5;
    var data = plot.getCustomData();
    if (data.getBooleanOr("eaten", false)) {
      ic2ce.neoforge.inventory.StackUtil.dropAsEntity(world, position, new ItemStack(Items.ROTTEN_FLESH));
      data.putBoolean("eaten", false);
    }
    var nearby =
        world.getEntitiesOfClass(
            net.minecraft.world.entity.LivingEntity.class,
            new net.minecraft.world.phys.AABB(
                x - 1, position.getY(), z - 1, x + 1, position.getY() + 2, z + 1),
            entity ->
                entity.isAlive()
                    && net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(
                        entity));
    if (nearby.isEmpty()) return;
    var victim = nearby.get(ProjectRuntime.random.nextInt(nearby.size()));
    victim.setDeltaMovement(
        (x - victim.getX()) * .5,
        Math.min(victim.getDeltaMovement().y(), -.05),
        (z - victim.getZ()) * .5);
    victim.hurt(victim.damageSources().generic(), (plot.getCurrentAge() + 1) * 2F);
    if (!metalArmor(victim)) {
      victim.addEffect(
          new net.minecraft.world.effect.MobEffectInstance(
              net.minecraft.world.effect.MobEffects.SLOWNESS, 64, 50));
      victim.addEffect(
          new net.minecraft.world.effect.MobEffectInstance(
              net.minecraft.world.effect.MobEffects.INVISIBILITY, 64));
      victim.addEffect(
          new net.minecraft.world.effect.MobEffectInstance(
              net.minecraft.world.effect.MobEffects.BLINDNESS, 64));
    }
    if (canGrow(plot))
      plot.setGrowthPoints((int) Math.min(Integer.MAX_VALUE, plot.getGrowthPoints() + 100L));
    world.playSound(
        null,
        x,
        position.getY() + .5,
        z,
        net.minecraft.sounds.SoundEvents.GENERIC_EAT,
        net.minecraft.sounds.SoundSource.BLOCKS,
        1,
        .9F + ProjectRuntime.random.nextFloat() * .1F);
    data.putBoolean("eaten", true);
  }

  private static boolean metalArmor(net.minecraft.world.entity.LivingEntity entity) {
    if (!(entity instanceof net.minecraft.world.entity.player.Player player)) return false;
    for (var armor : ic2ce.core.util.Util.armorItems(player))
      if (!armor.isEmpty() && ic2ce.api.item.ItemWrapper.isMetalArmor(armor, player)) return true;
    return false;
  }
}
