package ic2ce.neoforge.thermal.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.thermal.ElectricHeater;
import net.minecraft.world.entity.player.Inventory;

/** Electrical heater projects its ten real coils and electrical discharge. */
public class ElectricHeaterMenu extends PhysicalInventoryMenu<ElectricHeater> {
  private static final java.util.List<String> FIELDS =
      java.util.List.of("transmitHeat", "maxHeatEmitpeerTick");

  public ElectricHeaterMenu(int id, Inventory inventory, ElectricHeater machine) {
    super(MenuDefinitions.ELECTRIC_HEAT_GENERATOR, id, inventory, machine, 166);
    for (int slot = 0; slot < machine.coilSlot.size(); slot++)
      cell(machine.coilSlot, slot, 44 + (slot % 5) * 18, 27 + (slot / 5) * 18);
    cell(machine.dischargeSlot, 0, 8, 62);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
