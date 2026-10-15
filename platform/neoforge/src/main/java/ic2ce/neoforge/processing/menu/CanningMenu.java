package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.processing.ProcessingMenus;
import ic2ce.neoforge.processing.fluid.MultiPolicyCanner;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Separate solid packaging resources and bidirectional canning tank state. */
public class CanningMenu extends ProcessingMenu<MultiPolicyCanner> {
  public CanningMenu(int id, Inventory inventory, MultiPolicyCanner machine) {
    super(ProcessingMenus.CANNER, id, inventory, machine, 184, 8, 80, 80, 44, 119, 17, 152, 26);
    cell(machine.canInputSlot, 0, 41, 17);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(super.getNetworkedFields(), "mode", "inputTank", "outputTank");
  }
}
