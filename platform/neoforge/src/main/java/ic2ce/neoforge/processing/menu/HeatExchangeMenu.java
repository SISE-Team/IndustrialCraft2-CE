package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.processing.ProcessingMenus;
import ic2ce.neoforge.thermal.FluidHeatExchanger;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Hot/cold container circuits, ten exchange elements and actual heat output. */
public class HeatExchangeMenu extends PhysicalInventoryMenu<FluidHeatExchanger> {
  public HeatExchangeMenu(int id, Inventory inventory, FluidHeatExchanger machine) {
    super(ProcessingMenus.LIQUID_HEAT_EXCHANGER, id, inventory, machine, 204);
    cell(machine.hotfluidinputSlot, 0, 8, 103);
    cell(machine.cooloutputSlot, 0, 152, 103);
    cell(machine.coolfluidinputSlot, 0, 134, 103);
    cell(machine.hotoutputSlot, 0, 26, 103);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 62, 103, 18, 0);
    strip(machine.heatexchangerslots, 0, 5, 46, 50, 17, 0);
    strip(machine.heatexchangerslots, 5, 5, 46, 72, 17, 0);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(
        super.getNetworkedFields(),
        "inputTank",
        "outputTank",
        "transmitHeat",
        "maxHeatEmitpeerTick");
  }
}
