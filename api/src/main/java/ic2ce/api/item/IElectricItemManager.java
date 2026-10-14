package ic2ce.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Physical charge services; simulations are observational and positive infinity may request
 * complete transfer.
 */
public interface IElectricItemManager {
  double charge(
      ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate);

  double discharge(
      ItemStack stack,
      double amount,
      int tier,
      boolean ignoreTransferLimit,
      boolean externally,
      boolean simulate);

  double getCharge(ItemStack stack);

  double getStackCharge(ItemStack stack);

  double getMaxCharge(ItemStack stack);

  private static double fraction(double charge, double capacity) {
    if (Double.isNaN(charge) || Double.isNaN(capacity) || charge <= 0 || capacity <= 0) return 0;
    if (charge == Double.POSITIVE_INFINITY) return 1;
    if (capacity == Double.POSITIVE_INFINITY) return 0;
    return Math.clamp(charge / capacity, 0, 1);
  }

  default double getChargeLevel(ItemStack stack) {
    return fraction(getCharge(stack), getMaxCharge(stack));
  }

  default double getStackChargeLevel(ItemStack stack) {
    return fraction(getStackCharge(stack), getMaxCharge(stack));
  }

  boolean canUse(ItemStack stack, double amount);

  boolean use(ItemStack stack, double amount, LivingEntity owner);

  void chargeFromArmor(ItemStack stack, LivingEntity owner);

  String getToolTip(ItemStack stack);

  int getTier(ItemStack stack);
}
