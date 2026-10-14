package ic2ce.neoforge.kinetic.menu;

import ic2ce.neoforge.kinetic.WaterTurbine;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Water rotor menu observes actual flow and liquid identity. */
public class WaterTurbineMenu extends PhysicalInventoryMenu<WaterTurbine> {
  private static final java.util.List<String> FIELDS = java.util.List.of("waterFlow", "type");

  public WaterTurbineMenu(int id, Inventory inventory, WaterTurbine machine) {
    super(MenuDefinitions.WATER_KINETIC_GENERATOR, id, inventory, machine, 166);
    cell(machine.rotorSlot, 0, 80, 26);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
