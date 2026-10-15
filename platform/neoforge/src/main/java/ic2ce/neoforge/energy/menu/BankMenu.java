package ic2ce.neoforge.energy.menu;

import ic2ce.neoforge.energy.EnergyMenus;
import ic2ce.neoforge.energy.storage.EnergyBank;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import net.minecraft.world.entity.player.Inventory;

/** Physical charge/discharge and four native worn equipment slots. */
public class BankMenu extends PhysicalInventoryMenu<EnergyBank> {
  private static final java.util.List<String> FIELDS = java.util.List.of("redstoneMode");

  public BankMenu(int id, Inventory inventory, EnergyBank machine) {
    super(EnergyMenus.ENERGY_STORAGE, id, inventory, machine, 196);
    for (int slot = 0; slot < ic2ce.neoforge.menu.slot.EquipmentOrder.getCount(); slot++)
      addSlot(
          new ic2ce.neoforge.menu.slot.EquipmentItemSlot(
              inventory, ic2ce.neoforge.menu.slot.EquipmentOrder.get(slot), 8 + slot * 18, 84));
    cell(machine.chargeSlot, 0, 56, 17);
    cell(machine.dischargeSlot, 0, 56, 53);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
