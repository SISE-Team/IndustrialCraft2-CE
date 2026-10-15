package ic2ce.neoforge.utility.menu;

import ic2ce.neoforge.processing.menu.PoweredMenu;
import ic2ce.neoforge.utility.PoleDrive;
import ic2ce.neoforge.utility.UtilityMenus;
import net.minecraft.world.entity.player.Inventory;

/** Pole drive binds the real worn boots rather than a loose armor template. */
public class PoleDriveMenu extends PoweredMenu<PoleDrive> {
  public PoleDriveMenu(int id, Inventory inventory, PoleDrive machine) {
    super(UtilityMenus.MAGNETIZER, id, inventory, machine, 166, 8, 44);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 152, 8, 0, 18);
    addSlot(
        new ic2ce.neoforge.menu.slot.EquipmentItemSlot(
            inventory, net.minecraft.world.entity.EquipmentSlot.FEET, 45, 26));
  }
}
