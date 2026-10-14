package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.processing.fluid.SolarDistiller;
import ic2ce.neoforge.registry.MenuDefinitions;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Distinct solar-dependent source and distilled water container circuits. */
public class DistillationMenu extends PhysicalInventoryMenu<SolarDistiller> {
  public DistillationMenu(int id, Inventory inventory, SolarDistiller machine) {
    super(MenuDefinitions.SOLAR_DISTILLER, id, inventory, machine, 184);
    cell(machine.waterinputSlot, 0, 17, 27);
    cell(machine.destiwaterinputSlot, 0, 136, 64);
    cell(machine.wateroutputSlot, 0, 17, 45);
    cell(machine.destiwateroutputSlott, 0, 136, 82);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 152, 8, 0, 18);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(super.getNetworkedFields(), "skyLight", "inputTank", "outputTank");
  }
}
