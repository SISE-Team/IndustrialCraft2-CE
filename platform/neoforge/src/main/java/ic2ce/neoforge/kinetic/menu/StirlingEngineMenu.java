package ic2ce.neoforge.kinetic.menu;

import ic2ce.neoforge.kinetic.HeatDrivenCogenerator;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Heat-driven cogenerator binds the four real cooling-circuit container ports. */
public class StirlingEngineMenu extends PhysicalInventoryMenu<HeatDrivenCogenerator> {
  private static final java.util.List<String> FIELDS = java.util.List.of("inputTank", "outputTank");

  public StirlingEngineMenu(int id, Inventory inventory, HeatDrivenCogenerator machine) {
    super(MenuDefinitions.STIRLING_KINETIC_GENERATOR, id, inventory, machine, 204);
    cell(machine.coolfluidinputSlot, 0, 8, 103);
    cell(machine.cooloutputSlot, 0, 26, 103);
    cell(machine.hotfluidinputSlot, 0, 134, 103);
    cell(machine.hotoutputSlot, 0, 152, 103);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 62, 103, 18, 0);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
