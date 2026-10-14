package ic2ce.neoforge.item.armor;


import ic2ce.api.item.IHazmatLike;
import ic2ce.neoforge.damage.DamageProfiles;
import ic2ce.neoforge.registry.ArmorProfiles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;

/** Sealed chemical clothing and short-fall rubber boots have separate protection budgets. */
public class SealedGarment extends UtilityGarment implements IHazmatLike {
  public SealedGarment(EquipmentSlot slot, Properties properties) {
    super(
        slot == EquipmentSlot.FEET ? ArmorProfiles.RUBBER_BOOTS : ArmorProfiles.HAZMAT,
        slot == EquipmentSlot.FEET ? properties.durability(64) : properties,
        slot);
  }

  @Override
  public boolean addsProtection(LivingEntity owner, EquipmentSlot usedSlot, ItemStack stack) {
    return usedSlot == slot && stack.is(this) && stack.getCount() == 1;
  }

  public static boolean hasCompleteHazmat(LivingEntity owner) {
    return IHazmatLike.hasCompleteHazmat(owner);
  }

  public static boolean hazmatAbsorbs(DamageSource source) {
    return source != null
        && (source.is(DamageTypes.IN_FIRE)
            || source.is(DamageTypes.ON_FIRE)
            || source.is(DamageTypes.LAVA)
            || source.is(DamageTypes.HOT_FLOOR)
            || source.is(DamageTypes.IN_WALL)
            || source.is(DamageProfiles.ELECTRICITY_KEY)
            || source.is(DamageProfiles.RADIATION_KEY));
  }

  public boolean absorbFall(ItemStack stack, LivingEntity owner, float distance) {
    if (slot != EquipmentSlot.FEET
        || !stack.is(this)
        || owner == null
        || !(owner.level() instanceof ServerLevel world)
        || !Float.isFinite(distance)
        || distance < 0) return false;
    int damage = Math.max(0, (int) distance - 3);
    if (damage >= 8) return false;
    if (damage == 0) return true;
    int wear = (damage + 1) / 2;
    if (wear > stack.getMaxDamage() - stack.getDamageValue()) return false;
    stack.hurtAndBreak(wear, world, owner, broken -> owner.onEquippedItemBroken(broken, slot));
    return true;
  }
}
