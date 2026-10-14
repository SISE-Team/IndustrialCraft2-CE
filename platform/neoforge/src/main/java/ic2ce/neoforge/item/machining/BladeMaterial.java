package ic2ce.neoforge.item.machining;

import ic2ce.api.block.IIdProvider;

/** Fixed blade material descriptors preserve saved ordinals and own their machining hardness. */
public enum BladeMaterial implements IIdProvider {
  iron(3, "ic2.IronBlockCuttingBlade.info"),
  steel(6, "ic2.AdvIronBlockCuttingBlade.info"),
  diamond(9, "ic2.DiamondBlockCuttingBlade.info");
  public final int hardness;
  public final String tooltip;

  BladeMaterial(int hardness, String tooltip) {
    this.hardness = hardness;
    this.tooltip = tooltip;
  }

  @Override
  public String getName() {
    return name();
  }

  @Override
  public int getId() {
    return ordinal();
  }
}
