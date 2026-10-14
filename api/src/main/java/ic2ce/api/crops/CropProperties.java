package ic2ce.api.crops;

/** Immutable phenotype coordinates; accessor aliases preserve the public crop contract. */
public record CropProperties(
    int tier, int chemistry, int consumable, int defensive, int colorful, int weed) {
  public int getTier() {
    return tier;
  }

  public int getChemistry() {
    return chemistry;
  }

  public int getConsumable() {
    return consumable;
  }

  public int getDefensive() {
    return defensive;
  }

  public int getColorful() {
    return colorful;
  }

  public int getWeed() {
    return weed;
  }

  public int[] getAllProperties() {
    return new int[] {chemistry, consumable, defensive, colorful, weed};
  }
}
