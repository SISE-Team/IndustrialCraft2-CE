package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.processing.ProcessingMenus;
import ic2ce.neoforge.processing.fabrication.PatternScanner;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Scanner physical resource and memory disk observation slots. */
public class ScanningMenu extends PoweredMenu<PatternScanner> {
  public ScanningMenu(int id, Inventory inventory, PatternScanner machine) {
    super(ProcessingMenus.UU_SCANNER, id, inventory, machine, 166, 8, 43);
    cell(machine.inputSlot, 0, 55, 35);
    cell(machine.diskSlot, 0, 152, 65);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(
        super.getNetworkedFields(), "state", "progress", "patternEu", "patternUu");
  }
}
