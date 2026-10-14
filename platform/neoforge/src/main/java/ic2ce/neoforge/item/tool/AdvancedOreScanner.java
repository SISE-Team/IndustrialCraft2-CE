package ic2ce.neoforge.item.tool;

/** Twelve-block electrical scanner owns a distinct 250-EU layer/full scan policy. */
public class AdvancedOreScanner extends OreScanner {
  public AdvancedOreScanner(Properties properties) {
    super(properties, 1_000_000, 512, 2);
  }

  @Override
  protected int scanCost() {
    return 250;
  }

  @Override
  public int getScanRange() {
    return 12;
  }
}
