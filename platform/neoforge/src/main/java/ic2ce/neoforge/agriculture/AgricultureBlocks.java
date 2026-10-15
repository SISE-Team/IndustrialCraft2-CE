package ic2ce.neoforge.agriculture;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/** Agriculture blocks, staged in declaration order. */
public final class AgricultureBlocks {
  private static final java.util.function.Supplier<Properties> cropSettings =
      () -> BlockProfiles.plant().strength(0.8F, 0.2F).sound(SoundType.CROP).noCollision();
  public static final Block CROP_HARVESTER =
      BlockDefinitions.register(
          "crop_harvester",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("crop_harvester")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              AutoHarvester.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block CROPMATRON =
      BlockDefinitions.register(
          "cropmatron",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("cropmatron")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              FluidCropCaretaker.class,
              true,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block CLASSIC_CROPMATRON =
      BlockDefinitions.register(
          "classic_cropmatron",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "classic_cropmatron")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              CellCropCaretaker.class,
              false,
              MachineBlockType.DefaultDrop.Machine,
              DirectionSets.horizontalFacings,
              true));
  public static final Block CROP_STICK =
      BlockDefinitions.register(
          "crop_stick",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "crop_stick"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.NONE));
  public static final Block WEED_CROP =
      BlockDefinitions.register(
          "weed_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "weed_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.WEED));
  public static final Block WHEAT_CROP =
      BlockDefinitions.register(
          "wheat_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "wheat_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.WHEAT));
  public static final Block CARROTS_CROP =
      BlockDefinitions.register(
          "carrots_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "carrots_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.CARROTS));
  public static final Block POTATO_CROP =
      BlockDefinitions.register(
          "potato_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "potato_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.POTATO));
  public static final Block BEETROOTS_CROP =
      BlockDefinitions.register(
          "beetroots_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "beetroots_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.BEETROOTS));
  public static final Block PUMPKIN_CROP =
      BlockDefinitions.register(
          "pumpkin_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "pumpkin_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.PUMPKIN));
  public static final Block MELON_CROP =
      BlockDefinitions.register(
          "melon_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "melon_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.MELON));
  public static final Block DANDELION_CROP =
      BlockDefinitions.register(
          "dandelion_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "dandelion_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.DANDELION));
  public static final Block POPPY_CROP =
      BlockDefinitions.register(
          "poppy_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "poppy_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.POPPY));
  public static final Block BLACKTHORN_CROP =
      BlockDefinitions.register(
          "blackthorn_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "blackthorn_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.BLACKTHORN));
  public static final Block TULIP_CROP =
      BlockDefinitions.register(
          "tulip_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "tulip_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.TULIP));
  public static final Block CYAZINT_CROP =
      BlockDefinitions.register(
          "cyazint_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "cyazint_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.CYAZINT));
  public static final Block VENOMILIA_CROP =
      BlockDefinitions.register(
          "venomilia_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "venomilia_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.VENOMILIA));
  public static final Block REED_CROP =
      BlockDefinitions.register(
          "reed_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "reed_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.REED));
  public static final Block STICKY_REED_CROP =
      BlockDefinitions.register(
          "sticky_reed_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "sticky_reed_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.STICKY_REED));
  public static final Block COCOA_CROP =
      BlockDefinitions.register(
          "cocoa_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "cocoa_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.COCOA));
  public static final Block FLAX_CROP =
      BlockDefinitions.register(
          "flax_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "flax_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.FLAX));
  public static final Block RED_MUSHROOM_CROP =
      BlockDefinitions.register(
          "red_mushroom_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "red_mushroom_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.RED_MUSHROOM));
  public static final Block BROWN_MUSHROOM_CROP =
      BlockDefinitions.register(
          "brown_mushroom_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "brown_mushroom_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.BROWN_MUSHROOM));
  public static final Block NETHER_WART_CROP =
      BlockDefinitions.register(
          "nether_wart_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "nether_wart_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.NETHER_WART));
  public static final Block TERRA_WART_CROP =
      BlockDefinitions.register(
          "terra_wart_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "terra_wart_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.TERRA_WART));
  public static final Block OAK_SAPLING_CROP =
      BlockDefinitions.register(
          "oak_sapling_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "oak_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.OAK_SAPLING));
  public static final Block SPRUCE_SAPLING_CROP =
      BlockDefinitions.register(
          "spruce_sapling_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "spruce_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.SPRUCE_SAPLING));
  public static final Block BIRCH_SAPLING_CROP =
      BlockDefinitions.register(
          "birch_sapling_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "birch_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.BIRCH_SAPLING));
  public static final Block JUNGLE_SAPLING_CROP =
      BlockDefinitions.register(
          "jungle_sapling_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "jungle_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.JUNGLE_SAPLING));
  public static final Block ACACIA_SAPLING_CROP =
      BlockDefinitions.register(
          "acacia_sapling_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "acacia_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.ACACIA_SAPLING));
  public static final Block DARK_OAK_SAPLING_CROP =
      BlockDefinitions.register(
          "dark_oak_sapling_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "dark_oak_sapling_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.DARK_OAK_SAPLING));
  public static final Block FERRU_CROP =
      BlockDefinitions.register(
          "ferru_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "ferru_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.FERRU));
  public static final Block CYPRIUM_CROP =
      BlockDefinitions.register(
          "cyprium_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "cyprium_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.CYPRIUM));
  public static final Block STAGNIUM_CROP =
      BlockDefinitions.register(
          "stagnium_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "stagnium_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.STAGNIUM));
  public static final Block PLUMBISCUS_CROP =
      BlockDefinitions.register(
          "plumbiscus_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "plumbiscus_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.PLUMBISCUS));
  public static final Block AURELIA_CROP =
      BlockDefinitions.register(
          "aurelia_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "aurelia_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.AURELIA));
  public static final Block SHINING_CROP =
      BlockDefinitions.register(
          "shining_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "shining_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.SHINING));
  public static final Block RED_WHEAT_CROP =
      BlockDefinitions.register(
          "red_wheat_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "red_wheat_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.RED_WHEAT));
  public static final Block COFFEE_CROP =
      BlockDefinitions.register(
          "coffee_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "coffee_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.COFFEE));
  public static final Block HOPS_CROP =
      BlockDefinitions.register(
          "hops_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "hops_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.HOPS));
  public static final Block EATING_PLANT_CROP =
      BlockDefinitions.register(
          "eating_plant_crop",
          MachineBlockType.create(
              BlockDefinitions.withId(cropSettings, "eating_plant_crop"),
              CropPlot.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false,
              Cultivar.EATING_PLANT));

  private AgricultureBlocks() {}

  public static void init() {}
}
