package ic2ce.neoforge.power.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.power.SolarCollector;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Solar collector presents native sunlight and battery charging. */
public class SolarCollectorMenu extends PhysicalInventoryMenu<SolarCollector> {
  private static final java.util.List<String> FIELDS = java.util.List.of("sunlight");

  public SolarCollectorMenu(int id, Inventory inventory, SolarCollector machine) {
    super(MenuDefinitions.SOLAR_GENERATOR, id, inventory, machine, 166);
    cell(machine.chargeSlot, 0, 80, 26);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
