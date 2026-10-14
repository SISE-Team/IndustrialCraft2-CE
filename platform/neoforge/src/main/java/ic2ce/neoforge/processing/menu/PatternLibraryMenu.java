package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.processing.fabrication.PatternLibrary;
import ic2ce.neoforge.registry.MenuDefinitions;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Owned pattern disk input and immutable pattern selection field plan. */
public class PatternLibraryMenu extends PhysicalInventoryMenu<PatternLibrary> {
  public PatternLibraryMenu(int id, Inventory inventory, PatternLibrary machine) {
    super(MenuDefinitions.PATTERN_STORAGE, id, inventory, machine, 166);
    cell(machine.diskSlot, 0, 18, 20);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(
        super.getNetworkedFields(), "index", "maxIndex", "pattern", "patternUu", "patternEu");
  }
}
