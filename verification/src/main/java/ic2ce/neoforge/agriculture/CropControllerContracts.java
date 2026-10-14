package ic2ce.neoforge.agriculture;

import ic2ce.api.crops.Crops;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/**
 * Native phenotype bounds, numeric legacy persistence, no narrow progress wrap and block
 * replacement ownership.
 */
public final class CropControllerContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native crop controller: " + label);
  }

  public static Result run(ServerLevel world) {
    return new CropControllerContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(9040, 100, 9040);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position.below(), Blocks.FARMLAND.defaultBlockState());
    world.setBlockAndUpdate(position, BlockDefinitions.WHEAT_CROP.defaultBlockState());
    var crop = (CropPlot) world.getBlockEntity(position);
    crop.setStatGrowth(Integer.MAX_VALUE);
    crop.setStatGain(-10);
    crop.setStatResistance(40);
    check(
        crop.getStatGrowth() == 31 && crop.getStatGain() == 0 && crop.getStatResistance() == 31,
        "native phenotype setters stay within legal zero-to-thirty-one bounds");
    crop.setStorageWater(Integer.MAX_VALUE);
    crop.setStorageWeedEX(-1);
    crop.setStorageNutrients(1000);
    check(
        crop.getStorageWater() == 200
            && crop.getStorageWeedEX() == 0
            && crop.getStorageNutrients() == 200,
        "native care setters cannot create short wrap or negative capacities");
    crop.setGrowthPoints(33000);
    crop.setCurrentAge(1);
    crop.getCustomData().putString("probe", "owned");
    var saved = new CompoundTag();
    crop.saveAdditional(saved);
    check(
        saved.getIntOr("growthPoints", 0) == 33000,
        "native progress persists beyond the old signed short boundary");
    crop.load(saved);
    saved.getCompoundOrEmpty("customData").putString("probe", "mutated");
    check(
        crop.getGrowthPoints() == 33000
            && crop.getCustomData().getStringOr("probe", "").equals("owned"),
        "native numeric reload preserves long progress and takes owned custom data");
    var legacy = new CompoundTag();
    legacy.putByte("statGrowth", (byte) 21);
    legacy.putByte("statGain", (byte) 22);
    legacy.putByte("statResistance", (byte) 23);
    legacy.putShort("storageWater", (short) 75);
    legacy.putShort("growthPoints", (short) 12345);
    crop.load(legacy);
    check(
        crop.getStatGrowth() == 21
            && crop.getStatGain() == 22
            && crop.getStatResistance() == 23
            && crop.getStorageWater() == 75
            && crop.getGrowthPoints() == 12345,
        "legacy byte/short crop values read correctly into bounded wider native fields");
    var fields = crop.getNetworkedFields();
    check(
        fields.stream().distinct().count() == fields.size(),
        "native crop synchronizes each field once rather than duplicating age");
    crop.setCurrentAge(0);
    crop.setStatGrowth(31);
    crop.setStatGain(0);
    crop.setStatResistance(31);
    crop.setTerrainHumidity(100);
    crop.setTerrainAirQuality(100);
    crop.setTerrainNutrients(100);
    crop.setGrowthPoints(32760);
    crop.performGrowthTick();
    check(
        crop.getGrowthPoints() > 32767,
        "actual native growth crosses the signed short boundary without negative progress");
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                java.util.UUID.fromString("0cf6fcc4-3ec2-4e94-9dd1-eebd52bdb915"), "IC2CropProbe"));
    player.getInventory().clearContent();
    player.getAbilities().instabuild = false;
    crop.setStorageWater(0);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemDefinitions.WATER_CELL, 2));
    check(
        crop.rightClick(player, InteractionHand.MAIN_HAND)
            && crop.getStorageWater() == 10
            && player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1
            && player.getInventory().countItem(ItemDefinitions.EMPTY_CELL) == 1,
        "manual use of a stacked physical water cell conserves the empty container in the native"
            + " player inventory");
    crop.setStorageWater(0);
    player.getAbilities().instabuild = true;
    check(
        crop.rightClick(player, InteractionHand.MAIN_HAND)
            && crop.getStorageWater() == 10
            && player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1
            && player.getInventory().countItem(ItemDefinitions.EMPTY_CELL) == 1,
        "creative crop watering updates the real crop without consuming the held cell or"
            + " manufacturing an empty container");
    player.getAbilities().instabuild = false;
    player.getInventory().clearContent();
    crop.setStorageWeedEX(3);
    check(
        crop.hasWeedEX() && crop.getStorageWeedEX() == 0 && !crop.hasWeedEX(),
        "one partial protection dose cannot underflow into negative herbicide");
    crop.setStorageWater(77);
    crop.setStorageNutrients(88);
    crop.setStorageWeedEX(99);
    crop.setScanLevel(4);
    var original = crop;
    var replacement = crop.transformCropBlock(Crops.weed, 0);
    check(
        replacement != null
            && replacement != original
            && original.isRemoved()
            && replacement.getCrop() == Crops.weed
            && replacement.getStorageWater() == 77
            && replacement.getStorageNutrients() == 88
            && replacement.getStorageWeedEX() == 99,
        "actual species change installs a new native block entity and retains precisely its owned"
            + " care stores");
    if (replacement != null) {
      replacement.setStatGrowth(30);
      replacement.getCustomData().putString("stale", "data");
      replacement.reset();
    }
    var bare = (CropPlot) world.getBlockEntity(position);
    check(
        bare != null
            && bare.getCrop() == null
            && !bare.isCrossingBase()
            && bare.getStatGrowth() == 0
            && bare.getScanLevel() == 0
            && bare.getCustomData().isEmpty(),
        "native reset installs ordinary sticks and clears the removed species phenotype and custom"
            + " data");
    bare.setCrossingBase(true);
    check(
        bare.isCrossingBase() && bare.getCurrentAge() == 0,
        "bare native plot enables crossing sticks and reads its zero age without constructing an"
            + " invalid integer property");
    bare.setCrossingBase(false);
    check(!bare.isCrossingBase(), "crossing state can be independently disabled");
    check(
        !bare.tryPlantIn(null, 0, 0, 0, 0, 0)
            && world.getBlockState(position).is(BlockDefinitions.CROP_STICK),
        "null seed species cannot consume or alter the bare plot");
    world.removeBlock(position, false);
    world.removeBlock(position.below(), false);
    return new Result(checks, List.copyOf(failures));
  }
}
