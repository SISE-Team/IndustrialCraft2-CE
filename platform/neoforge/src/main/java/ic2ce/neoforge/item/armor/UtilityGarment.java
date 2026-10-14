package ic2ce.neoforge.item.armor;


import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;

/**
 * Fabric and utility equipment carries native equip components without claiming metal conductivity.
 */
public class UtilityGarment extends EquipmentItem {
  public UtilityGarment(ArmorMaterial material, Properties properties, EquipmentSlot slot) {
    super(material, properties, slot);
  }

  @Override
  public boolean isMetalArmor(ItemStack stack, Player owner) {
    return false;
  }
}
