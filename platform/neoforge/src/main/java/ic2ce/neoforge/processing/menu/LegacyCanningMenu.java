package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.processing.fluid.LegacyCellCanner;
import ic2ce.neoforge.registry.MenuDefinitions;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Distinct food/fuel canning resource, can and output slots. */
public class LegacyCanningMenu extends PoweredMenu<LegacyCellCanner> {
  public LegacyCanningMenu(int id, Inventory inventory, LegacyCellCanner machine) {
    super(MenuDefinitions.CLASSIC_CANNER, id, inventory, machine, 166, 30, 45);
    cell(machine.resInputSlot, 0, 69, 17);
    cell(machine.outputSlot, 0, 119, 35);
    cell(machine.inputSlot, 0, 69, 53);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(super.getNetworkedFields(), "progress", "mode");
  }
}
