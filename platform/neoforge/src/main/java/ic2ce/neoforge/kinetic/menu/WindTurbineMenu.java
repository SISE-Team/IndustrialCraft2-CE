package ic2ce.neoforge.kinetic.menu;

import ic2ce.neoforge.kinetic.WindTurbine;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Wind rotor menu observes measured wind rather than liquid flow. */
public class WindTurbineMenu extends PhysicalInventoryMenu<WindTurbine> {
  private static final java.util.List<String> FIELDS = java.util.List.of("windStrength");

  public WindTurbineMenu(int id, Inventory inventory, WindTurbine machine) {
    super(MenuDefinitions.WIND_KINETIC_GENERATOR, id, inventory, machine, 166);
    cell(machine.rotorSlot, 0, 80, 26);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
