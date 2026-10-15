package ic2ce.neoforge.item.energy;

import ic2ce.api.item.*;
import ic2ce.engine.energy.ChargeMath;
import ic2ce.engine.text.SiFormat;
import ic2ce.neoforge.item.PhysicalWear;
import ic2ce.neoforge.menu.slot.EquipmentOrder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;

/**
 * Component-backed physical charge ledger with primitive reads, bounded quantities and
 * debit-before-credit armor transfers.
 */
public class ComponentChargeLedger implements IElectricItemManager {
  private static final class ReadCache {
    CustomData data;
    double value;
  }

  private static final ThreadLocal<ReadCache> READS = ThreadLocal.withInitial(ReadCache::new);

  private static IElectricItem item(ItemStack stack) {
    return stack != null && !stack.isEmpty() && stack.getItem() instanceof IElectricItem electric
        ? electric
        : null;
  }

  private static double raw(ItemStack stack) {
    var data = stack.get(DataComponents.CUSTOM_DATA);
    if (data == null || !data.contains("charge")) return 0;
    var cache = READS.get();
    if (cache.data != data) {
      cache.data = data;
      cache.value = data.copyTag().getDoubleOr("charge", 0);
    }
    return cache.value;
  }

  private static double capacity(ItemStack stack, IElectricItem item) {
    return ChargeMath.capacity(item.getMaxCharge(stack));
  }

  private static void write(ItemStack stack, IElectricItem item, double value) {
    CustomData.update(
        DataComponents.CUSTOM_DATA,
        stack,
        data -> {
          if (value > 0) data.putDouble("charge", value);
          else data.remove("charge");
        });
    PhysicalWear.setDamage(
        stack,
        ChargeMath.displayDamage(value, capacity(stack, item), PhysicalWear.getMaxDamage(stack)),
        true);
  }

  @Override
  public double charge(
      ItemStack stack, double amount, int tier, boolean unlimited, boolean simulate) {
    var item = item(stack);
    if (item == null
        || stack.getCount() != 1
        || item.getTier(stack) > tier
        || Double.isNaN(amount)
        || amount < 0) return 0;
    double maximum = capacity(stack, item), current = ChargeMath.stored(raw(stack), maximum);
    double accepted =
        ChargeMath.charge(current, maximum, amount, item.getTransferLimit(stack), unlimited);
    if (!simulate && accepted > 0) write(stack, item, current + accepted);
    return accepted;
  }

  @Override
  public double discharge(
      ItemStack stack,
      double amount,
      int tier,
      boolean unlimited,
      boolean external,
      boolean simulate) {
    var item = item(stack);
    if (item == null
        || stack.getCount() != 1
        || item.getTier(stack) > tier
        || external && !item.canProvideEnergy(stack)
        || Double.isNaN(amount)
        || amount < 0) return 0;
    double maximum = capacity(stack, item), current = ChargeMath.stored(raw(stack), maximum);
    double accepted =
        ChargeMath.discharge(current, maximum, amount, item.getTransferLimit(stack), unlimited);
    if (!simulate && accepted > 0) write(stack, item, Math.max(0, current - accepted));
    return accepted;
  }

  @Override
  public double getCharge(ItemStack stack) {
    return stack == null || stack.getCount() != 1 ? 0 : getStackCharge(stack);
  }

  @Override
  public double getStackCharge(ItemStack stack) {
    var item = item(stack);
    return item == null ? 0 : ChargeMath.stored(raw(stack), capacity(stack, item));
  }

  @Override
  public double getMaxCharge(ItemStack stack) {
    var item = item(stack);
    return item == null ? 0 : capacity(stack, item);
  }

  @Override
  public int getTier(ItemStack stack) {
    var item = item(stack);
    return item == null ? 0 : Math.max(0, item.getTier(stack));
  }

  @Override
  public boolean canUse(ItemStack stack, double amount) {
    return item(stack) != null
        && stack.getCount() == 1
        && Double.isFinite(amount)
        && amount >= 0
        && getCharge(stack) >= amount;
  }

  @Override
  public boolean use(ItemStack stack, double amount, LivingEntity owner) {
    if (!Double.isFinite(amount) || amount < 0 || item(stack) == null || stack.getCount() != 1)
      return false;
    if (owner != null && owner.level().isClientSide()) return canUse(stack, amount);
    if (owner != null) chargeFromArmor(stack, owner);
    if (!canUse(stack, amount)) return false;
    double paid = discharge(stack, amount, Integer.MAX_VALUE, true, false, false);
    if (paid < amount) {
      if (paid > 0) charge(stack, paid, Integer.MAX_VALUE, true, false);
      return false;
    }
    if (owner != null) chargeFromArmor(stack, owner);
    return true;
  }

  @Override
  public void chargeFromArmor(ItemStack target, LivingEntity owner) {
    if (target == null || target.isEmpty() || owner == null || owner.level().isClientSide()) return;
    boolean changed = false;
    for (var slot : EquipmentOrder.getAll()) {
      var source = owner.getItemBySlot(slot);
      if (source == target || source.isEmpty()) continue;
      changed |=
          ItemChargeTransfers.move(
                  source, target, Double.POSITIVE_INFINITY, ElectricItem.manager.getTier(source))
              > 0;
    }
    if (changed && owner instanceof Player player) player.containerMenu.broadcastChanges();
  }

  @Override
  public String getToolTip(ItemStack stack) {
    return SiFormat.format(getCharge(stack), 3)
        + "/"
        + SiFormat.format(getMaxCharge(stack), 3)
        + " EU";
  }

  public static ItemStack getCharged(Item item, double amount) {
    if (!(item instanceof IElectricItem))
      throw new IllegalArgumentException("Item has no electric charge contract");
    var stack = new ItemStack(item);
    ElectricItem.manager.charge(stack, amount, Integer.MAX_VALUE, true, false);
    return stack;
  }

  public static void addChargeVariants(Item item, List<ItemStack> output) {
    output.add(getCharged(item, 0));
    output.add(getCharged(item, Double.POSITIVE_INFINITY));
  }
}
