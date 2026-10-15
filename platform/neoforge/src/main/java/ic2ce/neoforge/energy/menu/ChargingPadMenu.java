package ic2ce.neoforge.energy.menu;

import ic2ce.neoforge.energy.EnergyMenus;
import ic2ce.neoforge.energy.storage.WirelessChargingPad;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import net.minecraft.world.entity.player.Inventory;

/** Wireless pad retains its own native circuit control layout. */
public class ChargingPadMenu extends PhysicalInventoryMenu<WirelessChargingPad> {
  private static final java.util.List<String> FIELDS = java.util.List.of("redstoneMode");

  public ChargingPadMenu(int id, Inventory inventory, WirelessChargingPad machine) {
    super(EnergyMenus.CHARGEPAD, id, inventory, machine, 161);
    cell(machine.chargeSlot, 0, 56, 17);
    cell(machine.dischargeSlot, 0, 56, 53);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
