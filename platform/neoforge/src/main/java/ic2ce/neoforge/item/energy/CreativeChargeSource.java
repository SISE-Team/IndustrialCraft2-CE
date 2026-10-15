package ic2ce.neoforge.item.energy;

import ic2ce.api.item.IElectricItemManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Explicit creative infinity is a separate protocol and cannot accept negative or NaN operations.
 */
public class CreativeChargeSource implements IElectricItemManager {
  private static double request(double amount) {
    return !Double.isNaN(amount) && amount >= 0 ? amount : 0;
  }

  @Override
  public double charge(
      ItemStack stack, double amount, int tier, boolean unlimited, boolean simulate) {
    return request(amount);
  }

  @Override
  public double discharge(
      ItemStack stack,
      double amount,
      int tier,
      boolean unlimited,
      boolean external,
      boolean simulate) {
    return request(amount);
  }

  @Override
  public double getCharge(ItemStack stack) {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double getStackCharge(ItemStack stack) {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double getMaxCharge(ItemStack stack) {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public boolean canUse(ItemStack stack, double amount) {
    return Double.isFinite(amount) && amount >= 0;
  }

  @Override
  public boolean use(ItemStack stack, double amount, LivingEntity owner) {
    return canUse(stack, amount);
  }

  @Override
  public void chargeFromArmor(ItemStack stack, LivingEntity owner) {}

  @Override
  public String getToolTip(ItemStack stack) {
    return "infinite EU";
  }

  @Override
  public int getTier(ItemStack stack) {
    return Integer.MAX_VALUE;
  }

  @Override
  public double getChargeLevel(ItemStack stack) {
    return 1;
  }

  @Override
  public double getStackChargeLevel(ItemStack stack) {
    return 1;
  }
}
