package ic2ce.neoforge.item;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Bounded physical wear distinguishes native owner destruction from machine-slot break signals and
 * pseudo gauges.
 */
public final class PhysicalWear {
  private PhysicalWear() {}

  public static int getDamage(ItemStack stack) {
    return stack == null || stack.isEmpty() ? 0 : Math.max(0, stack.getDamageValue());
  }

  public static int getMaxDamage(ItemStack stack) {
    return stack == null || stack.isEmpty() ? 0 : Math.max(0, stack.getMaxDamage());
  }

  public static void setDamage(ItemStack stack, int damage, boolean displayOnly) {
    if (stack == null || stack.isEmpty()) return;
    int value = Math.clamp(damage, 0, getMaxDamage(stack));
    if (stack.getItem() instanceof IPseudoDamageItem gauge) {
      if (!displayOnly)
        throw new IllegalStateException("Pseudo gauge cannot receive physical wear");
      gauge.setStackDamage(stack, value);
    } else if (stack.isDamageableItem()) stack.setDamageValue(value);
  }

  public static boolean damage(
      ItemStack stack, int amount, LivingEntity owner, InteractionHand hand) {
    return damage(
        stack,
        amount,
        owner != null && owner.level() instanceof ServerLevel world ? world : null,
        owner,
        hand);
  }

  public static boolean damage(
      ItemStack stack, int amount, ServerLevel world, LivingEntity owner, InteractionHand hand) {
    if (stack == null
        || stack.isEmpty()
        || amount <= 0
        || !stack.isDamageableItem()
        || stack.getItem() instanceof IPseudoDamageItem) return false;
    if (owner != null) {
      if (!(owner.level() instanceof ServerLevel actual)) return false;
      stack.hurtAndBreak(
          amount,
          actual,
          owner,
          broken -> {
            if (hand != null)
              owner.onEquippedItemBroken(
                  broken,
                  hand == InteractionHand.MAIN_HAND
                      ? EquipmentSlot.MAINHAND
                      : EquipmentSlot.OFFHAND);
          });
      return true;
    }
    int wear =
        world == null
            ? amount
            : Math.max(0, EnchantmentHelper.processDurabilityChange(world, stack, amount));
    long total = (long) stack.getDamageValue() + wear;
    stack.setDamageValue((int) Math.clamp(total, 0, stack.getMaxDamage()));
    return total >= stack.getMaxDamage();
  }
}
