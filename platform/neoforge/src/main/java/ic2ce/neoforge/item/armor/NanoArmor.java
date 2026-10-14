package ic2ce.neoforge.item.armor;


import ic2ce.api.item.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorMaterial;

/** Nano protection has a seven-damage short-fall limit and a configurable electrical helmet. */
public class NanoArmor extends ChargedArmor implements IItemHudProvider {
  public static final int[] CHARGED_PROTECTION = {3, 6, 8, 3};

  public NanoArmor(ArmorMaterial material, EquipmentSlot slot, Properties properties) {
    super(material, slot, properties.rarity(Rarity.UNCOMMON), 1_000_000, 1600, 3);
  }

  @Override
  public int getEnergyPerDamage() {
    return 5000;
  }

  public boolean absorbFall(ItemStack stack, LivingEntity owner, float distance) {
    if (slot != EquipmentSlot.FEET
        || owner == null
        || owner.level().isClientSide()
        || !Float.isFinite(distance)
        || distance < 0) return false;
    int damage = Math.max(0, (int) distance - 3);
    return damage < 8 && pay(stack, damage * (double) getEnergyPerDamage());
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

  public Rarity getRarity(ItemStack stack) {
    return Rarity.UNCOMMON;
  }

  @Override
  public boolean doesProvideHUD(ItemStack stack) {
    return slot == EquipmentSlot.HEAD && ElectricItem.manager.getCharge(stack) > 0;
  }

  @Override
  public HudMode getHudMode(ItemStack stack) {
    return HelmetControls.hud(stack);
  }
}
