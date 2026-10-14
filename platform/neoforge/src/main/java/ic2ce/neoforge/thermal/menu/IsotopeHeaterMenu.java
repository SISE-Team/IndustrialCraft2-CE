package ic2ce.neoforge.thermal.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.thermal.RadioisotopeHeater;
import net.minecraft.world.entity.player.Inventory;

/** Radioisotope heater projects six heat pellets independently of electrical fuel. */
public class IsotopeHeaterMenu extends PhysicalInventoryMenu<RadioisotopeHeater> {
  private static final java.util.List<String> FIELDS =
      java.util.List.of("transmitHeat", "maxHeatEmitpeerTick");

  public IsotopeHeaterMenu(int id, Inventory inventory, RadioisotopeHeater machine) {
    super(MenuDefinitions.RT_HEAT_GENERATOR, id, inventory, machine, 166);
    for (int slot = 0; slot < machine.fuelSlot.size(); slot++)
      cell(machine.fuelSlot, slot, 62 + (slot % 3) * 18, 27 + (slot / 3) * 18);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
