package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.processing.ProcessingMenus;
import ic2ce.neoforge.processing.fluid.FluidElectrolyzer;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Fluid electrolysis recipe state and electrical upgrade slots. */
public class ElectrolysisMenu extends PoweredMenu<FluidElectrolyzer> {
  public ElectrolysisMenu(int id, Inventory inventory, FluidElectrolyzer machine) {
    super(ProcessingMenus.ELECTROLYZER, id, inventory, machine, 166, 8, 62);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 152, 8, 0, 18);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(super.getNetworkedFields(), "progress", "recipe", "input");
  }
}
