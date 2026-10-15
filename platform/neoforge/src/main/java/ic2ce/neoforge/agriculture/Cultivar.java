package ic2ce.neoforge.agriculture;

import ic2ce.api.crops.CropCard;
import ic2ce.api.crops.Crops;
import ic2ce.api.crops.ICropType;
import java.util.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

/** Native cultivar metadata with stable resource names and direct owner/name lookup. */
public enum Cultivar implements StringRepresentable, ICropType {
  NONE("none", null, 0),
  WEED("weed", AgricultureBlocks.WEED_CROP, 4),
  WHEAT("wheat", AgricultureBlocks.WHEAT_CROP, 7),
  CARROTS("carrots", AgricultureBlocks.CARROTS_CROP, 3),
  POTATO("potato", AgricultureBlocks.POTATO_CROP, 3),
  BEETROOTS("beetroots", AgricultureBlocks.BEETROOTS_CROP, 3),
  PUMPKIN("pumpkin", AgricultureBlocks.PUMPKIN_CROP, 3),
  MELON("melon", AgricultureBlocks.MELON_CROP, 3),
  DANDELION("dandelion", AgricultureBlocks.DANDELION_CROP, 3),
  POPPY("poppy", AgricultureBlocks.POPPY_CROP, 3),
  BLACKTHORN("blackthorn", AgricultureBlocks.BLACKTHORN_CROP, 3),
  TULIP("tulip", AgricultureBlocks.TULIP_CROP, 3),
  CYAZINT("cyazint", AgricultureBlocks.CYAZINT_CROP, 3),
  VENOMILIA("venomilia", AgricultureBlocks.VENOMILIA_CROP, 5),
  REED("reed", AgricultureBlocks.REED_CROP, 2),
  STICKY_REED("sticky_reed", AgricultureBlocks.STICKY_REED_CROP, 3),
  COCOA("cocoa", AgricultureBlocks.COCOA_CROP, 3),
  FLAX("flax", AgricultureBlocks.FLAX_CROP, 3),
  RED_MUSHROOM("red_mushroom", AgricultureBlocks.RED_MUSHROOM_CROP, 2),
  BROWN_MUSHROOM("brown_mushroom", AgricultureBlocks.BROWN_MUSHROOM_CROP, 2),
  NETHER_WART("nether_wart", AgricultureBlocks.NETHER_WART_CROP, 2),
  TERRA_WART("terra_wart", AgricultureBlocks.TERRA_WART_CROP, 2),
  OAK_SAPLING("oak_sapling", AgricultureBlocks.OAK_SAPLING_CROP, 4),
  SPRUCE_SAPLING("spruce_sapling", AgricultureBlocks.SPRUCE_SAPLING_CROP, 4),
  BIRCH_SAPLING("birch_sapling", AgricultureBlocks.BIRCH_SAPLING_CROP, 4),
  JUNGLE_SAPLING("jungle_sapling", AgricultureBlocks.JUNGLE_SAPLING_CROP, 4),
  ACACIA_SAPLING("acacia_sapling", AgricultureBlocks.ACACIA_SAPLING_CROP, 4),
  DARK_OAK_SAPLING("dark_oak_sapling", AgricultureBlocks.DARK_OAK_SAPLING_CROP, 4),
  FERRU("ferru", AgricultureBlocks.FERRU_CROP, 3),
  CYPRIUM("cyprium", AgricultureBlocks.CYPRIUM_CROP, 3),
  STAGNIUM("stagnium", AgricultureBlocks.STAGNIUM_CROP, 3),
  PLUMBISCUS("plumbiscus", AgricultureBlocks.PLUMBISCUS_CROP, 3),
  AURELIA("aurelia", AgricultureBlocks.AURELIA_CROP, 3),
  SHINING("shining", AgricultureBlocks.SHINING_CROP, 3),
  RED_WHEAT("red_wheat", AgricultureBlocks.RED_WHEAT_CROP, 6),
  COFFEE("coffee", AgricultureBlocks.COFFEE_CROP, 4),
  HOPS("hops", AgricultureBlocks.HOPS_CROP, 6),
  EATING_PLANT("eating_plant", AgricultureBlocks.EATING_PLANT_CROP, 5);
  private final String name;
  private final Block block;
  private final int maximumAge;
  private static final Map<String, Cultivar> BY_NAME;

  static {
    var names = new HashMap<String, Cultivar>();
    for (var value : values()) names.put(value.name, value);
    BY_NAME = Map.copyOf(names);
  }

  Cultivar(String name, Block block, int maximumAge) {
    this.name = name;
    this.block = block;
    this.maximumAge = maximumAge;
  }

  @Override
  public String getSerializedName() {
    return name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getOwner() {
    return "ic2";
  }

  @Override
  public Block getCropBlock() {
    return block;
  }

  @Override
  public int getMaxAge() {
    return maximumAge;
  }

  public static CropCard getCropCard(Cultivar type) {
    return type == null || Crops.instance == null
        ? null
        : Crops.instance.getCropCard("ic2", type.name);
  }

  public static Cultivar getCropType(CropCard species) {
    return species == null || !species.getOwner().equals("ic2")
        ? null
        : BY_NAME.get(species.getId());
  }
}
