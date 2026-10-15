package ic2ce.neoforge.agriculture;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Agriculture block entity types, staged in declaration order. */
public final class AgricultureBlockEntities {
  public static final BlockEntityType<AutoHarvester> CROP_HARVESTER =
      BlockEntityDefinitions.register("crop_harvester", AutoHarvester::new, AgricultureBlocks.CROP_HARVESTER);
  public static final BlockEntityType<FluidCropCaretaker> CROPMATRON =
      BlockEntityDefinitions.register("cropmatron", FluidCropCaretaker::new, AgricultureBlocks.CROPMATRON);
  public static final BlockEntityType<CellCropCaretaker> CLASSIC_CROPMATRON =
      BlockEntityDefinitions.register("classic_cropmatron", CellCropCaretaker::new, AgricultureBlocks.CLASSIC_CROPMATRON);
  public static final BlockEntityType<CropPlot> CROP_STICK =
      BlockEntityDefinitions.register("crop_stick", CropPlot::new, AgricultureBlocks.CROP_STICK);
  public static final BlockEntityType<CropPlot> WEED_CROP =
      BlockEntityDefinitions.register("weed_crop", CropPlot::new, AgricultureBlocks.WEED_CROP);
  public static final BlockEntityType<CropPlot> WHEAT_CROP =
      BlockEntityDefinitions.register("wheat_crop", CropPlot::new, AgricultureBlocks.WHEAT_CROP);
  public static final BlockEntityType<CropPlot> CARROTS_CROP =
      BlockEntityDefinitions.register("carrots_crop", CropPlot::new, AgricultureBlocks.CARROTS_CROP);
  public static final BlockEntityType<CropPlot> POTATO_CROP =
      BlockEntityDefinitions.register("potato_crop", CropPlot::new, AgricultureBlocks.POTATO_CROP);
  public static final BlockEntityType<CropPlot> BEETROOTS_CROP =
      BlockEntityDefinitions.register("beetroots_crop", CropPlot::new, AgricultureBlocks.BEETROOTS_CROP);
  public static final BlockEntityType<CropPlot> PUMPKIN_CROP =
      BlockEntityDefinitions.register("pumpkin_crop", CropPlot::new, AgricultureBlocks.PUMPKIN_CROP);
  public static final BlockEntityType<CropPlot> MELON_CROP =
      BlockEntityDefinitions.register("melon_crop", CropPlot::new, AgricultureBlocks.MELON_CROP);
  public static final BlockEntityType<CropPlot> DANDELION_CROP =
      BlockEntityDefinitions.register("dandelion_crop", CropPlot::new, AgricultureBlocks.DANDELION_CROP);
  public static final BlockEntityType<CropPlot> POPPY_CROP =
      BlockEntityDefinitions.register("poppy_crop", CropPlot::new, AgricultureBlocks.POPPY_CROP);
  public static final BlockEntityType<CropPlot> BLACKTHORN_CROP =
      BlockEntityDefinitions.register("blackthorn_crop", CropPlot::new, AgricultureBlocks.BLACKTHORN_CROP);
  public static final BlockEntityType<CropPlot> TULIP_CROP =
      BlockEntityDefinitions.register("tulip_crop", CropPlot::new, AgricultureBlocks.TULIP_CROP);
  public static final BlockEntityType<CropPlot> CYAZINT_CROP =
      BlockEntityDefinitions.register("cyazint_crop", CropPlot::new, AgricultureBlocks.CYAZINT_CROP);
  public static final BlockEntityType<CropPlot> VENOMILIA_CROP =
      BlockEntityDefinitions.register("venomilia_crop", CropPlot::new, AgricultureBlocks.VENOMILIA_CROP);
  public static final BlockEntityType<CropPlot> REED_CROP =
      BlockEntityDefinitions.register("reed_crop", CropPlot::new, AgricultureBlocks.REED_CROP);
  public static final BlockEntityType<CropPlot> STICKY_REED_CROP =
      BlockEntityDefinitions.register("sticky_reed_crop", CropPlot::new, AgricultureBlocks.STICKY_REED_CROP);
  public static final BlockEntityType<CropPlot> COCOA_CROP =
      BlockEntityDefinitions.register("cocoa_crop", CropPlot::new, AgricultureBlocks.COCOA_CROP);
  public static final BlockEntityType<CropPlot> FLAX_CROP =
      BlockEntityDefinitions.register("flax_crop", CropPlot::new, AgricultureBlocks.FLAX_CROP);
  public static final BlockEntityType<CropPlot> RED_MUSHROOM_CROP =
      BlockEntityDefinitions.register("red_mushroom_crop", CropPlot::new, AgricultureBlocks.RED_MUSHROOM_CROP);
  public static final BlockEntityType<CropPlot> BROWN_MUSHROOM_CROP =
      BlockEntityDefinitions.register("brown_mushroom_crop", CropPlot::new, AgricultureBlocks.BROWN_MUSHROOM_CROP);
  public static final BlockEntityType<CropPlot> NETHER_WART_CROP =
      BlockEntityDefinitions.register("nether_wart_crop", CropPlot::new, AgricultureBlocks.NETHER_WART_CROP);
  public static final BlockEntityType<CropPlot> TERRA_WART_CROP =
      BlockEntityDefinitions.register("terra_wart_crop", CropPlot::new, AgricultureBlocks.TERRA_WART_CROP);
  public static final BlockEntityType<CropPlot> OAK_SAPLING_CROP =
      BlockEntityDefinitions.register("oak_sapling_crop", CropPlot::new, AgricultureBlocks.OAK_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> SPRUCE_SAPLING_CROP =
      BlockEntityDefinitions.register("spruce_sapling_crop", CropPlot::new, AgricultureBlocks.SPRUCE_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> BIRCH_SAPLING_CROP =
      BlockEntityDefinitions.register("birch_sapling_crop", CropPlot::new, AgricultureBlocks.BIRCH_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> JUNGLE_SAPLING_CROP =
      BlockEntityDefinitions.register("jungle_sapling_crop", CropPlot::new, AgricultureBlocks.JUNGLE_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> ACACIA_SAPLING_CROP =
      BlockEntityDefinitions.register("acacia_sapling_crop", CropPlot::new, AgricultureBlocks.ACACIA_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> DARK_OAK_SAPLING_CROP =
      BlockEntityDefinitions.register("dark_oak_sapling_crop", CropPlot::new, AgricultureBlocks.DARK_OAK_SAPLING_CROP);
  public static final BlockEntityType<CropPlot> FERRU_CROP =
      BlockEntityDefinitions.register("ferru_crop", CropPlot::new, AgricultureBlocks.FERRU_CROP);
  public static final BlockEntityType<CropPlot> CYPRIUM_CROP =
      BlockEntityDefinitions.register("cyprium_crop", CropPlot::new, AgricultureBlocks.CYPRIUM_CROP);
  public static final BlockEntityType<CropPlot> STAGNIUM_CROP =
      BlockEntityDefinitions.register("stagnium_crop", CropPlot::new, AgricultureBlocks.STAGNIUM_CROP);
  public static final BlockEntityType<CropPlot> PLUMBISCUS_CROP =
      BlockEntityDefinitions.register("plumbiscus_crop", CropPlot::new, AgricultureBlocks.PLUMBISCUS_CROP);
  public static final BlockEntityType<CropPlot> AURELIA_CROP =
      BlockEntityDefinitions.register("aurelia_crop", CropPlot::new, AgricultureBlocks.AURELIA_CROP);
  public static final BlockEntityType<CropPlot> SHINING_CROP =
      BlockEntityDefinitions.register("shining_crop", CropPlot::new, AgricultureBlocks.SHINING_CROP);
  public static final BlockEntityType<CropPlot> RED_WHEAT_CROP =
      BlockEntityDefinitions.register("red_wheat_crop", CropPlot::new, AgricultureBlocks.RED_WHEAT_CROP);
  public static final BlockEntityType<CropPlot> COFFEE_CROP =
      BlockEntityDefinitions.register("coffee_crop", CropPlot::new, AgricultureBlocks.COFFEE_CROP);
  public static final BlockEntityType<CropPlot> HOPS_CROP =
      BlockEntityDefinitions.register("hops_crop", CropPlot::new, AgricultureBlocks.HOPS_CROP);
  public static final BlockEntityType<CropPlot> EATING_PLANT_CROP =
      BlockEntityDefinitions.register("eating_plant_crop", CropPlot::new, AgricultureBlocks.EATING_PLANT_CROP);

  private AgricultureBlockEntities() {}

  public static void init() {}
}
