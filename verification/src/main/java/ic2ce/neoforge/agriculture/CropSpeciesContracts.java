package ic2ce.neoforge.agriculture;

import ic2ce.api.crops.*;
import ic2ce.neoforge.agriculture.species.MetalSpecies;
import ic2ce.neoforge.material.MaterialItems;
import ic2ce.neoforge.runtime.SharedRandom;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/**
 * Every registered species uses an actual native block/phenotype, plus distinct roots, poison and
 * attack protocols.
 */
public final class CropSpeciesContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native crop species: " + label);
  }

  public static Result run(ServerLevel world) {
    return new CropSpeciesContracts().exercise(world);
  }

  private CropPlot place(ServerLevel world, BlockPos position, CropCard species) {
    world.getChunkAt(position);
    world.setBlockAndUpdate(position.below(), Blocks.FARMLAND.defaultBlockState());
    world.setBlockAndUpdate(position, species.getCropBlock().defaultBlockState());
    return (CropPlot) world.getBlockEntity(position);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(9168, 100, 9168);
    SharedRandom.INSTANCE.setSeed(774492);
    for (var species : Crops.instance.getCrops()) {
      var plot = place(world, position, species);
      check(
          plot != null && plot.getCrop() == species,
          species.getId() + " resolves its own physical native block entity");
      check(
          species.getProperties() == species.getProperties(),
          species.getId() + " reuses immutable phenotype coordinates");
      var descriptors = species.getAttributes();
      var original = descriptors.clone();
      if (descriptors.length > 0) descriptors[0] = "borrowed mutation";
      check(
          Arrays.equals(species.getAttributes(), original),
          species.getId() + " does not expose mutable internal breeding descriptors");
      plot.setCurrentAge(species.getMaxAge());
      check(
          plot.getCurrentAge() == species.getMaxAge(),
          species.getId() + " stores its mature age in the actual native state");
      var output = species.getGains(plot);
      boolean valid = output != null;
      if (output != null) for (var item : output) valid &= item != null && item.getCount() >= 0;
      check(valid, species.getId() + " publishes only owned non-null harvest outputs");
      world.removeBlock(position, false);
    }
    var plot = place(world, position, CropCatalog.cropCoffee);
    plot.setCurrentAge(3);
    check(
        CropCatalog.cropCoffee.canBeHarvested(plot)
            && CropCatalog.cropCoffee.getGains(plot).length == 0,
        "immature coffee can be harvested but cannot yield beans");
    plot.setCurrentAge(4);
    check(
        CropCatalog.cropCoffee.getGains(plot)[0].is(AgricultureItems.COFFEE_BEANS),
        "fully ripened coffee produces the actual bean item");
    world.removeBlock(position, false);
    plot = place(world, position, CropCatalog.cropStickyReed);
    plot.setCurrentAge(1);
    check(
        CropCatalog.cropStickyReed.getGain(plot).is(Items.SUGAR_CANE),
        "immature sticky reed yields cane");
    plot.setCurrentAge(3);
    var after = CropCatalog.cropStickyReed.getAgeAfterHarvest(plot);
    check(
        CropCatalog.cropStickyReed.getGain(plot).is(MaterialItems.RESIN)
            && after >= 1
            && after <= 2,
        "mature sticky reed yields resin and independently retains one or two stages");
    world.removeBlock(position, false);
    plot = place(world, position, CropCatalog.cropRedWheat);
    plot.setCurrentAge(6);
    check(
        CropCatalog.cropRedWheat.getEmittedRedstoneSignal(plot) == 15 && plot.getLightValue() == 7,
        "mature red wheat exposes its own native signal and light outputs");
    plot.setCurrentAge(1);
    check(
        CropCatalog.cropRedWheat.getEmittedRedstoneSignal(plot) == 0 && plot.getLightValue() == 0,
        "red wheat harvest state turns both outputs off");
    world.removeBlock(position, false);
    plot = place(world, position, CropCatalog.cropFerru);
    plot.setCurrentAge(CropCatalog.cropFerru.getMaxAge() - 1);
    world.setBlockAndUpdate(position.below(2), Blocks.STONE.defaultBlockState());
    check(
        !CropCatalog.cropFerru.canGrow(plot),
        "ordinary metal final growth remains blocked without the required physical root material");
    world.setBlockAndUpdate(position.below(2), Blocks.IRON_BLOCK.defaultBlockState());
    check(
        CropCatalog.cropFerru.canGrow(plot),
        "actual compatible iron root unlocks the ordinary metal final stage");
    var attributes = new String[] {"owned"};
    var prototype = new ItemStack(Items.IRON_INGOT, 2);
    var roots = new ArrayList<net.minecraft.tags.TagKey<Block>>();
    var owned =
        new MetalSpecies(Cultivar.FERRU, AgricultureBlocks.FERRU_CROP, attributes, roots, prototype);
    attributes[0] = "changed";
    prototype.setCount(99);
    roots.add(net.minecraft.tags.BlockTags.LOGS);
    check(
        owned.getAttributes()[0].equals("owned")
            && owned.getGain(plot).getCount() == 2
            && owned.canGrow(plot),
        "constructor caller cannot mutate owned metal requirements, descriptors or harvest"
            + " quantity");
    world.removeBlock(position, false);
    plot = place(world, position, CropCatalog.cropNetherWart);
    plot.setCurrentAge(0);
    world.setBlockAndUpdate(position.below(2), Blocks.SOUL_SAND.defaultBlockState());
    plot.setGrowthPoints(Integer.MAX_VALUE - 20);
    CropCatalog.cropNetherWart.tick(plot);
    check(
        plot.getGrowthPoints() == Integer.MAX_VALUE,
        "soul-sand bonus saturates native progress instead of signed overflow");
    world.removeBlock(position, false);
    plot = place(world, position, CropCatalog.cropVenomilia);
    plot.setCurrentAge(4);
    var victim = new net.minecraft.world.entity.animal.cow.Cow(EntityType.COW, world);
    victim.setPos(position.getX() + .5, position.getY(), position.getZ() + .5);
    world.addFreshEntity(victim);
    CropCatalog.cropVenomilia.onEntityCollision(plot, victim);
    var effect = victim.getEffect(net.minecraft.world.effect.MobEffects.POISON);
    check(
        plot.getCurrentAge() == 3
            && effect != null
            && effect.getDuration() >= 100
            && effect.getDuration() <= 280,
        "real venom contact applies bounded poison and physically deactivates the armed flower"
            + " stage");
    victim.discard();
    world.removeBlock(position, false);
    plot = place(world, position, CropCatalog.cropEatingPlant);
    plot.setCurrentAge(3);
    world.setBlockAndUpdate(position.below(2), Blocks.LAVA.defaultBlockState());
    victim = new net.minecraft.world.entity.animal.cow.Cow(EntityType.COW, world);
    victim.setPos(position.getX() + .5, position.getY(), position.getZ() + .5);
    world.addFreshEntity(victim);
    float health = victim.getHealth();
    CropCatalog.cropEatingPlant.tick(plot);
    check(
        victim.getHealth() < health
            && plot.getCustomData().getBooleanOr("eaten", false)
            && victim.getEffect(net.minecraft.world.effect.MobEffects.SLOWNESS) != null,
        "actual carnivorous plant damages one physical living victim and records its owned"
            + " digestion state; health="
            + victim.getHealth()
            + ", light="
            + plot.getLightLevel());
    victim.discard();
    world.removeBlock(position, false);
    world.removeBlock(position.below(), false);
    world.removeBlock(position.below(2), false);
    return new Result(checks, List.copyOf(failures));
  }
}
