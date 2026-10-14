package ic2ce.neoforge.item.armor;


import ic2ce.api.item.*;
import ic2ce.neoforge.registry.ArmorProfiles;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Tier-one optical utility equipment shares input controls but carries no charged combat armor. */
public class NightVisionGoggles extends ChargedArmor implements IItemHudInfo {
  public NightVisionGoggles(Properties properties) {
    super(ArmorProfiles.NIGHT_VISION_GOGGLES, EquipmentSlot.HEAD, properties, 200_000, 200, 1);
  }

  @Override
  public int getEnergyPerDamage() {
    return 0;
  }

  @Override
  public boolean isMetalArmor(ItemStack stack, Player owner) {
    return false;
  }

  @Override
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    return List.of(ElectricItem.manager.getToolTip(stack));
  }

  @Override
  public void inventoryTick(
      ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot usedSlot) {
    if (usedSlot == EquipmentSlot.HEAD
        && stack.is(this)
        && entity instanceof Player owner
        && owner.getItemBySlot(EquipmentSlot.HEAD) == stack)
      HelmetControls.tick(stack, owner, "active", false);
  }

  public boolean isValidRepairItem(ItemStack first, ItemStack second) {
    return false;
  }
}
