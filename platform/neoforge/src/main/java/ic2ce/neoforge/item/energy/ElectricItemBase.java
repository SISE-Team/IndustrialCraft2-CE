package ic2ce.neoforge.item.energy;

import ic2ce.api.item.*;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;

/**
 * Immutable electrical item descriptor; native component charge remains separate from display and
 * creative enumeration.
 */
public abstract class ElectricItemBase extends Item
    implements IElectricItem, IItemHudInfo, ic2ce.api.item.CreativeVariants {
  protected final double maxCharge, transferLimit;
  protected final int tier;

  public ElectricItemBase(Properties properties, double capacity, double transfer, int tier) {
    super(properties);
    if (!Double.isFinite(capacity)
        || capacity <= 0
        || !Double.isFinite(transfer)
        || transfer < 0
        || tier < 0) throw new IllegalArgumentException("Invalid electrical item descriptor");
    maxCharge = capacity;
    transferLimit = transfer;
    this.tier = tier;
  }

  @Override
  public boolean canProvideEnergy(ItemStack stack) {
    return false;
  }

  @Override
  public double getMaxCharge(ItemStack stack) {
    return maxCharge;
  }

  @Override
  public double getTransferLimit(ItemStack stack) {
    return transferLimit;
  }

  @Override
  public int getTier(ItemStack stack) {
    return tier;
  }

  @Override
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    return List.of(java.util.Objects.toString(ElectricItem.manager.getToolTip(stack), ""));
  }

  public void addCreativeVariants(java.util.function.Consumer<ItemStack> output) {
    output.accept(ComponentChargeLedger.getCharged(this, 0));
    output.accept(ComponentChargeLedger.getCharged(this, maxCharge));
  }

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return true;
  }

  private double fraction(ItemStack stack) {
    double value = ElectricItem.manager.getStackChargeLevel(stack);
    return Double.isFinite(value) ? Math.clamp(value, 0, 1) : 0;
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    return (int) Math.round(fraction(stack) * 13);
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return Mth.hsvToRgb((float) (fraction(stack) / 3), 1, 1);
  }
}
