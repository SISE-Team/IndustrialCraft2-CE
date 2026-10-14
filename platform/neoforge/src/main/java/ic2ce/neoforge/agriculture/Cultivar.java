package ic2ce.neoforge.agriculture;

import ic2ce.api.crops.CropCard;
import ic2ce.api.crops.Crops;
import ic2ce.api.crops.ICropType;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

/** Native cultivar metadata with stable resource names and direct owner/name lookup. */
public enum Cultivar implements StringRepresentable, ICropType {
  NONE("none", null, 0),
  WEED("weed", BlockDefinitions.WEED_CROP, 4),
  WHEAT("wheat", BlockDefinitions.WHEAT_CROP, 7),
  CARROTS("carrots", BlockDefinitions.CARROTS_CROP, 3),
  POTATO("potato", BlockDefinitions.POTATO_CROP, 3),
  BEETROOTS("beetroots", BlockDefinitions.BEETROOTS_CROP, 3),
  PUMPKIN("pumpkin", BlockDefinitions.PUMPKIN_CROP, 3),
  MELON("melon", BlockDefinitions.MELON_CROP, 3),
  DANDELION("dandelion", BlockDefinitions.DANDELION_CROP, 3),
  POPPY("poppy", BlockDefinitions.POPPY_CROP, 3),
  BLACKTHORN("blackthorn", BlockDefinitions.BLACKTHORN_CROP, 3),
  TULIP("tulip", BlockDefinitions.TULIP_CROP, 3),
  CYAZINT("cyazint", BlockDefinitions.CYAZINT_CROP, 3),
  VENOMILIA("venomilia", BlockDefinitions.VENOMILIA_CROP, 5),
  REED("reed", BlockDefinitions.REED_CROP, 2),
  STICKY_REED("sticky_reed", BlockDefinitions.STICKY_REED_CROP, 3),
  COCOA("cocoa", BlockDefinitions.COCOA_CROP, 3),
  FLAX("flax", BlockDefinitions.FLAX_CROP, 3),
  RED_MUSHROOM("red_mushroom", BlockDefinitions.RED_MUSHROOM_CROP, 2),
  BROWN_MUSHROOM("brown_mushroom", BlockDefinitions.BROWN_MUSHROOM_CROP, 2),
  NETHER_WART("nether_wart", BlockDefinitions.NETHER_WART_CROP, 2),
  TERRA_WART("terra_wart", BlockDefinitions.TERRA_WART_CROP, 2),
  OAK_SAPLING("oak_sapling", BlockDefinitions.OAK_SAPLING_CROP, 4),
  SPRUCE_SAPLING("spruce_sapling", BlockDefinitions.SPRUCE_SAPLING_CROP, 4),
  BIRCH_SAPLING("birch_sapling", BlockDefinitions.BIRCH_SAPLING_CROP, 4),
  JUNGLE_SAPLING("jungle_sapling", BlockDefinitions.JUNGLE_SAPLING_CROP, 4),
  ACACIA_SAPLING("acacia_sapling", BlockDefinitions.ACACIA_SAPLING_CROP, 4),
  DARK_OAK_SAPLING("dark_oak_sapling", BlockDefinitions.DARK_OAK_SAPLING_CROP, 4),
  FERRU("ferru", BlockDefinitions.FERRU_CROP, 3),
  CYPRIUM("cyprium", BlockDefinitions.CYPRIUM_CROP, 3),
  STAGNIUM("stagnium", BlockDefinitions.STAGNIUM_CROP, 3),
  PLUMBISCUS("plumbiscus", BlockDefinitions.PLUMBISCUS_CROP, 3),
  AURELIA("aurelia", BlockDefinitions.AURELIA_CROP, 3),
  SHINING("shining", BlockDefinitions.SHINING_CROP, 3),
  RED_WHEAT("red_wheat", BlockDefinitions.RED_WHEAT_CROP, 6),
  COFFEE("coffee", BlockDefinitions.COFFEE_CROP, 4),
  HOPS("hops", BlockDefinitions.HOPS_CROP, 6),
  EATING_PLANT("eating_plant", BlockDefinitions.EATING_PLANT_CROP, 5);
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
