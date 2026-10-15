package ic2ce.neoforge.item.armor;


import ic2ce.api.item.*;
import ic2ce.api.item.PropulsionEquipment;
import ic2ce.neoforge.item.StackTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterial;

/** Quantum protection has extended fall relief, dyed equipment and exact chest-thruster bills. */
public class QuantumArmor extends ChargedArmor
    implements PropulsionEquipment, IHazmatLike, IItemHudProvider {
  public static final int[] CHARGED_PROTECTION = {3, 6, 8, 3};

  public QuantumArmor(ArmorMaterial material, EquipmentSlot slot, Properties properties) {
    super(material, slot, properties.rarity(Rarity.RARE), 10_000_000, 12_000, 4);
  }

  @Override
  public int getEnergyPerDamage() {
    return 20_000;
  }

  public boolean hasCustomColor(ItemStack stack) {
    return getColor(stack) != -1;
  }

  public int getColor(ItemStack stack) {
    var nativeColor = stack.get(DataComponents.DYED_COLOR);
    if (nativeColor != null) return nativeColor.rgb();
    var data = StackTag.get(stack);
    return data == null ? -1 : data.getCompoundOrEmpty("display").getIntOr("color", -1);
  }

  public void setColor(ItemStack stack, int color) {
    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color & 0xFFFFFF));
    removeLegacyColor(stack);
  }

  public void clearColor(ItemStack stack) {
    stack.remove(DataComponents.DYED_COLOR);
    removeLegacyColor(stack);
  }

  private static void removeLegacyColor(ItemStack stack) {
    var data = StackTag.get(stack);
    if (data == null || !data.contains("display")) return;
    var display = data.getCompoundOrEmpty("display");
    boolean hadColor = display.contains("color");
    if (!hadColor && !display.isEmpty()) return;
    display.remove("color");
    if (display.isEmpty()) data.remove("display");
    else data.put("display", display);
    StackTag.set(stack, data.isEmpty() ? null : data);
  }

  @Override
  public boolean addsProtection(LivingEntity owner, EquipmentSlot usedSlot, ItemStack stack) {
    return usedSlot == slot
        && stack.is(this)
        && ElectricItem.manager.canUse(stack, getEnergyPerDamage());
  }

  public boolean absorbFall(ItemStack stack, LivingEntity owner, float distance) {
    if (slot != EquipmentSlot.FEET
        || owner == null
        || owner.level().isClientSide()
        || !Float.isFinite(distance)
        || distance < 0) return false;
    double damage = Math.max(0, Math.floor((double) distance) - 10);
    return pay(stack, damage * getEnergyPerDamage());
  }

  public Rarity getRarity(ItemStack stack) {
    return Rarity.RARE;
  }

  public int getEnchantmentValue() {
    return 0;
  }

  @Override
  public boolean drainEnergy(ItemStack stack, int amount) {
    return slot == EquipmentSlot.CHEST
        && amount >= 0
        && pay(stack, (double) amount + EU_ENERGY_INCREASE);
  }

  @Override
  public float getPower(ItemStack stack) {
    return 1;
  }

  @Override
  public float getDropPercentage(ItemStack stack) {
    return .05F;
  }

  @Override
  public double getChargeLevel(ItemStack stack) {
    return ElectricItem.manager.getChargeLevel(stack);
  }

  @Override
  public boolean isJetpackActive(ItemStack stack) {
    return slot == EquipmentSlot.CHEST && stack.is(this);
  }

  @Override
  public float getHoverMultiplier(ItemStack stack, boolean upwards) {
    return .1F;
  }

  @Override
  public float getWorldHeightDivisor(ItemStack stack) {
    return .9F;
  }

  @Override
  public boolean doesProvideHUD(ItemStack stack) {
    return slot == EquipmentSlot.HEAD && ElectricItem.manager.getCharge(stack) > 0;
  }

  @Override
  public HudMode getHudMode(ItemStack stack) {
    return HelmetControls.hud(stack);
  }

  @Override
  public void inventoryTick(
      ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot usedSlot) {
    if (slot == EquipmentSlot.HEAD
        && usedSlot == slot
        && stack.is(this)
        && entity instanceof Player owner
        && owner.getItemBySlot(slot) == stack)
      HelmetControls.tick(stack, owner, "Nightvision", true);
  }
}
