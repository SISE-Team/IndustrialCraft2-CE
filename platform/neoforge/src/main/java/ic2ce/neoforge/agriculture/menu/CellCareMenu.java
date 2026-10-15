package ic2ce.neoforge.agriculture.menu;

import ic2ce.neoforge.agriculture.AgricultureMenus;
import ic2ce.neoforge.agriculture.CellCropCaretaker;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import net.minecraft.world.entity.player.Inventory;

/** Distinct finite fertilizer, water cells and herbicide columns. */
public class CellCareMenu extends PhysicalInventoryMenu<CellCropCaretaker> {
  public CellCareMenu(int id, Inventory inventory, CellCropCaretaker machine) {
    super(AgricultureMenus.CLASSIC_CROPMATRON, id, inventory, machine, 166);
    strip(machine.fertilizerSlot, 0, machine.fertilizerSlot.size(), 62, 20, 0, 18);
    strip(machine.hydrationSlot, 0, machine.hydrationSlot.size(), 98, 20, 0, 18);
    strip(machine.weedExSlot, 0, machine.weedExSlot.size(), 134, 20, 0, 18);
  }
}
