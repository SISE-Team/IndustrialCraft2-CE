package ic2ce.neoforge.item.energy;

import ic2ce.api.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Manager selection follows the actual stack; dynamic addon handlers never enter a stale per-item
 * cache.
 */
public class ElectricManagerRouter implements IElectricItemManager {
  private IElectricItemManager manager(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return null;
    var item = stack.getItem();
    var selected =
        item instanceof ISpecialElectricItem special
            ? special.getManager(stack)
            : item instanceof IElectricItem
                ? ElectricItem.rawManager
                : ElectricItem.getBackupManager(stack);
    return selected == this ? null : selected;
  }

  private static double value(double amount) {
    return Double.isNaN(amount) || amount < 0 ? 0 : amount;
  }

  private static boolean request(double amount) {
    return !Double.isNaN(amount) && amount >= 0;
  }

  @Override
  public double charge(
      ItemStack stack, double amount, int tier, boolean unlimited, boolean simulate) {
    var manager = manager(stack);
    return manager == null || !request(amount)
        ? 0
        : value(manager.charge(stack, amount, tier, unlimited, simulate));
  }

  @Override
  public double discharge(
      ItemStack stack,
      double amount,
      int tier,
      boolean unlimited,
      boolean external,
      boolean simulate) {
    var manager = manager(stack);
    return manager == null || !request(amount)
        ? 0
        : value(manager.discharge(stack, amount, tier, unlimited, external, simulate));
  }

  @Override
  public double getCharge(ItemStack stack) {
    var manager = manager(stack);
    return manager == null ? 0 : value(manager.getCharge(stack));
  }

  @Override
  public double getStackCharge(ItemStack stack) {
    var manager = manager(stack);
    return manager == null ? 0 : value(manager.getStackCharge(stack));
  }

  @Override
  public double getMaxCharge(ItemStack stack) {
    var manager = manager(stack);
    return manager == null ? 0 : value(manager.getMaxCharge(stack));
  }

  @Override
  public int getTier(ItemStack stack) {
    var manager = manager(stack);
    return manager == null ? 0 : Math.max(0, manager.getTier(stack));
  }

  @Override
  public boolean canUse(ItemStack stack, double amount) {
    var manager = manager(stack);
    return manager != null
        && Double.isFinite(amount)
        && amount >= 0
        && manager.canUse(stack, amount);
  }

  @Override
  public boolean use(ItemStack stack, double amount, LivingEntity owner) {
    var manager = manager(stack);
    if (manager == null || !Double.isFinite(amount) || amount < 0) return false;
    if (owner != null && owner.level().isClientSide()
        || owner instanceof Player player && player.getAbilities().instabuild)
      return manager.canUse(stack, amount);
    return manager.use(stack, amount, owner);
  }

  @Override
  public void chargeFromArmor(ItemStack stack, LivingEntity owner) {
    var manager = manager(stack);
    if (manager != null && owner != null && !owner.level().isClientSide())
      manager.chargeFromArmor(stack, owner);
  }

  @Override
  public String getToolTip(ItemStack stack) {
    var manager = manager(stack);
    return manager == null ? null : manager.getToolTip(stack);
  }
}
