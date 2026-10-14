package ic2ce.neoforge.agriculture;

import ic2ce.api.crops.*;
import ic2ce.core.util.StackTag;
import ic2ce.neoforge.agriculture.item.EncodedSeed;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

/**
 * Actual native registration, planting and crossing; no fake inventory or replacement crop
 * implementations.
 */
public final class CropCatalogContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean value, String label) {
    checks++;
    if (!value) failures.add("native crop catalog: " + label);
  }

  public static Result run(ServerLevel world) {
    return new CropCatalogContracts().exercise(world);
  }

  private static CropPlot place(ServerLevel world, BlockPos position, Block block) {
    world.getChunkAt(position);
    world.setBlockAndUpdate(position.below(), Blocks.FARMLAND.defaultBlockState());
    var state = block.defaultBlockState();
    if (state.hasProperty(ic2ce.neoforge.block.MachineBlockType.CROSSING_BASE))
      state = state.setValue(ic2ce.neoforge.block.MachineBlockType.CROSSING_BASE, false);
    world.setBlockAndUpdate(position, state);
    return (CropPlot) world.getBlockEntity(position);
  }

  private static void overrideSpecies(CropPlot plot, CropCard species) {
    try {
      var field = CropPlot.class.getDeclaredField("crop");
      field.setAccessible(true);
      field.set(plot, species);
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  private Result exercise(ServerLevel world) {
    var catalog = new CropCatalog();
    var wheat = CropCatalog.cropWheat;
    catalog.registerCrop(wheat);
    var snapshot = catalog.getCrops();
    check(
        catalog.getCropCard(BlockDefinitions.WHEAT_CROP) == wheat
            && catalog.getCropCard("ic2", "wheat") == wheat,
        "both direct native block and owner/name keys resolve the registered species");
    catalog.registerCrop(wheat);
    check(catalog.getCrops().size() == 1, "same-object bootstrap registration is idempotent");
    boolean rejected = false;
    try {
      catalog.registerCrop(new ic2ce.neoforge.agriculture.species.WheatSpecies(Cultivar.WHEAT));
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(
        rejected && catalog.getCropCard("ic2", "wheat") == wheat,
        "duplicate registration fails before replacing the working native species");
    catalog.registerCrop(CropCatalog.cropCarrots);
    check(
        snapshot.size() == 1 && catalog.getCrops().size() == 2,
        "previous immutable generation remains iterable after a later registration");
    rejected = false;
    try {
      snapshot.clear();
    } catch (UnsupportedOperationException expected) {
      rejected = true;
    }
    check(rejected, "addon cannot clear the published species list");
    var base = new ItemStack(Items.WHEAT_SEEDS, 4);
    catalog.registerBaseSeed(base, wheat, 2, 99, -1, 7);
    base.shrink(3);
    check(
        catalog.getBaseSeed(base) == null
            && catalog.requiredSeedQuantity(new ItemStack(Items.WHEAT_SEEDS, 4)) == 4,
        "registration owns the required seed count and rejects an undersized held stack");
    var first = catalog.getBaseSeed(new ItemStack(Items.WHEAT_SEEDS, 4));
    var edited = new BaseSeed(first.crop, first.size, 0, first.statGain, first.statResistance);
    check(
        edited.statGrowth == 0
            && first.statGrowth == 31
            && catalog.getBaseSeed(new ItemStack(Items.WHEAT_SEEDS, 4)).statGrowth == 31
            && !catalog.registerBaseSeed(new ItemStack(Items.WHEAT_SEEDS), wheat, 0, 0, 0, 0),
        "independent immutable seed genotype cannot corrupt bounded stored genetics and duplicate"
            + " seed keys leave them intact");
    var encoded = EncodedSeed.generateItemStackFromValues(wheat, 1000, -3, 9, 1000);
    var item = (EncodedSeed) encoded.getItem();
    check(
        item.getGrowthFromStack(encoded) == 31
            && item.getGainFromStack(encoded) == 0
            && item.getScannedFromStack(encoded) == 4,
        "generated seed metadata is bounded without narrow-byte truncation");
    StackTag.update(
        encoded,
        data -> {
          data.putString("addon", "preserved");
          data.putByte("growth", (byte) 23);
          return data;
        });
    item.setResistanceFromStack(encoded, 100);
    item.incrementScannedFromStack(encoded);
    check(
        item.getGrowthFromStack(encoded) == 23
            && item.getResistanceFromStack(encoded) == 31
            && item.getScannedFromStack(encoded) == 4
            && StackTag.get(encoded).getStringOr("addon", "").equals("preserved"),
        "legacy numeric seed reads and later component writes preserve addon data and cap scan"
            + " exhaustion");
    var blank = new ItemStack(ItemDefinitions.CROP_SEED_BACK);
    item.setCropFromStack(blank, wheat);
    check(
        item.getCropFromStack(blank) == wheat,
        "assigning a species initializes an actually blank physical seed component");
    var position = new BlockPos(9120, 100, 9120);
    var plot = place(world, position, BlockDefinitions.CROP_STICK);
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("ed6c64b8-ff58-411a-ae3a-58e1c6d79f3c"), "IC2SeedProbe"));
    player.getInventory().clearContent();
    player.getAbilities().instabuild = false;
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.POPPY, 3));
    check(
        !plot.rightClick(player, InteractionHand.MAIN_HAND)
            && plot.getCrop() == null
            && player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 3,
        "three physical poppies cannot satisfy a four-poppy registered seed recipe");
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.POPPY, 5));
    int plantingLight = plot.getLightLevel();
    var seedSpec = Crops.instance.getBaseSeed(player.getItemInHand(InteractionHand.MAIN_HAND));
    String plantingBefore =
        ", removed="
            + plot.isRemoved()
            + ", crossing="
            + plot.isCrossingBase()
            + ", plotSpecies="
            + plot.getCrop()
            + ", seed="
            + (seedSpec == null ? "null" : seedSpec.crop.getId())
            + ", quantity="
            + ((CropCatalog) Crops.instance)
                .requiredSeedQuantity(player.getItemInHand(InteractionHand.MAIN_HAND))
            + ", canGrow="
            + CropCatalog.cropRedFlower.canGrow(plot);
    boolean planted = plot.rightClick(player, InteractionHand.MAIN_HAND);
    check(
        planted
            && ((CropPlot) world.getBlockEntity(position)).getCrop() == CropCatalog.cropRedFlower
            && player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1,
        "actual native planting consumes four poppies and installs the selected flower block;"
            + " planted="
            + planted
            + ", light="
            + plantingLight
            + ", count="
            + player.getItemInHand(InteractionHand.MAIN_HAND).getCount()
            + plantingBefore);
    world.removeBlock(position, false);
    plot = place(world, position, BlockDefinitions.CROP_STICK);
    plot.setCrossingBase(true);
    plot.setStorageWeedEX(150);
    var parent = place(world, position.east(), BlockDefinitions.WHEAT_CROP);
    parent.setCurrentAge(7);
    parent.setStatGrowth(17);
    parent.setStatGain(12);
    parent.setStatResistance(4);
    ProjectRuntime.random.setSeed(4129);
    for (int attempt = 0; attempt < 128 && !plot.isRemoved(); attempt++) plot.performTick();
    var spread = (CropPlot) world.getBlockEntity(position);
    check(
        spread.getCrop() == wheat
            && spread.getStatGrowth() == 17
            && spread.getStatGain() == 12
            && spread.getStatResistance() == 4,
        "single real adjacent mature parent spreads identical genetics into the real crossing"
            + " plot");
    world.removeBlock(position, false);
    plot = place(world, position, BlockDefinitions.CROP_STICK);
    plot.setCrossingBase(true);
    plot.setStorageWeedEX(150);
    parent.setStatGrowth(10);
    var second = place(world, position.west(), BlockDefinitions.WHEAT_CROP);
    second.setCurrentAge(7);
    second.setStatGrowth(10);
    second.setStatGain(12);
    second.setStatResistance(4);
    ProjectRuntime.random.setSeed(7729);
    for (int attempt = 0; attempt < 256 && !plot.isRemoved(); attempt++) plot.performTick();
    var crossed = (CropPlot) world.getBlockEntity(position);
    check(
        crossed.getCrop() != null
            && crossed.getStatGrowth() >= 8
            && crossed.getStatGrowth() <= 12
            && crossed.getStatGain() >= 10
            && crossed.getStatGain() <= 14
            && crossed.getStatResistance() >= 2
            && crossed.getStatResistance() <= 6,
        "two actual mature parents breed a physically replaced species with bounded inherited"
            + " mutation");
    world.removeBlock(position, false);
    plot = place(world, position, BlockDefinitions.CROP_STICK);
    plot.setCrossingBase(true);
    plot.setStorageWeedEX(150);
    var incompatible =
        new CropCard(Cultivar.WHEAT) {
          @Override
          public CropProperties getProperties() {
            return new CropProperties(
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE);
          }

          @Override
          public List<Identifier> getTexturesLocation() {
            return List.of();
          }

          @Override
          public Block getCropBlock() {
            return BlockDefinitions.WHEAT_CROP;
          }

          @Override
          public boolean canGrow(ICropTile crop) {
            return true;
          }
        };
    overrideSpecies(parent, incompatible);
    overrideSpecies(second, incompatible);
    ProjectRuntime.random.setSeed(1337);
    for (int attempt = 0; attempt < 256 && !plot.isRemoved(); attempt++) plot.performTick();
    check(
        !plot.isRemoved() && plot.getCrop() == null && plot.isCrossingBase(),
        "zero total native crossing weight leaves the owned plot intact without nextInt zero-bound"
            + " failure");
    for (var cell : List.of(position, position.east(), position.west())) {
      world.removeBlock(cell, false);
      world.removeBlock(cell.below(), false);
    }
    player.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }
}
