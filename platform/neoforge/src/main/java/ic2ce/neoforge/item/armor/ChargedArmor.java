package ic2ce.neoforge.item.armor;


import ic2ce.api.item.*;
import ic2ce.neoforge.item.energy.ComponentChargeLedger;
import ic2ce.neoforge.item.tooltip.ElectricTooltip;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;

/** Finite charge protection, exact special-effect bills and native energy displays. */
public abstract class ChargedArmor extends EquipmentItem
    implements IElectricItem, ic2ce.api.item.CreativeVariants {
  public static final UUID[] MODIFIERS = {
    UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
        UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
    UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
        UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")
  };
  protected final double maxCharge, transferLimit;
  protected final int tier;

  public ChargedArmor(
      ArmorMaterial material,
      EquipmentSlot slot,
      Properties properties,
      double maximum,
      double rate,
      int tier) {
    super(material, properties, slot);
    if (!Double.isFinite(maximum) || maximum <= 0 || !Double.isFinite(rate) || rate < 0 || tier < 0)
      throw new IllegalArgumentException("Invalid armor charge descriptor");
    maxCharge = maximum;
    transferLimit = rate;
    this.tier = tier;
  }

  public abstract int getEnergyPerDamage();

  public static void damageArmor(Player owner, DamageSource source, float amount) {
    if (owner.level().isClientSide()
        || !Float.isFinite(amount)
        || amount <= 0
        || source.is(DamageTypeTags.BYPASSES_ARMOR)) return;
    double bill = Math.max(1, amount / 4.0);
    for (var slot : ic2ce.neoforge.menu.slot.EquipmentOrder.getAll()) {
      var stack = owner.getItemBySlot(slot);
      if (stack.getItem() instanceof ChargedArmor armor)
        armor.damageArmor(owner, stack, source, bill, slot);
    }
  }

  public void damageArmor(
      LivingEntity owner,
      ItemStack stack,
      DamageSource source,
      double damage,
      EquipmentSlot usedSlot) {
    if (usedSlot == slot
        && stack.is(this)
        && owner != null
        && !owner.level().isClientSide()
        && Double.isFinite(damage)
        && damage > 0)
      ElectricItem.manager.discharge(
          stack, damage * getEnergyPerDamage(), Integer.MAX_VALUE, true, false, false);
  }

  protected final boolean pay(ItemStack stack, double bill) {
    return stack.is(this) && ElectricItem.manager.use(stack, bill, null);
  }

  public boolean isEnchantable(ItemStack stack) {
    return false;
  }

  public void addCreativeVariants(java.util.function.Consumer<ItemStack> output) {
    output.accept(ComponentChargeLedger.getCharged(this, 0));
    output.accept(ComponentChargeLedger.getCharged(this, Double.POSITIVE_INFINITY));
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      Item.TooltipContext context,
      TooltipDisplay display,
      java.util.function.Consumer<Component> output,
      TooltipFlag flags) {
    ElectricTooltip.addTooltip(stack, output, flags);
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
  public int getTier(ItemStack stack) {
    return tier;
  }

  @Override
  public double getTransferLimit(ItemStack stack) {
    return transferLimit;
  }

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return ElectricItem.manager.getChargeLevel(stack) < 1;
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    return (int) Math.round(ElectricItem.manager.getChargeLevel(stack) * 13);
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return Mth.hsvToRgb((float) (ElectricItem.manager.getChargeLevel(stack) / 3), 1, 1);
  }
}
